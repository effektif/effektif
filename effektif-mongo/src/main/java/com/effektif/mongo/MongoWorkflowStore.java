/* Copyright (c) 2014, Effektif GmbH.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. */
package com.effektif.mongo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.bson.types.ObjectId;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;

import com.effektif.workflow.api.Configuration;
import com.effektif.workflow.api.command.RequestContext;
import com.effektif.workflow.api.query.OrderBy;
import com.effektif.workflow.api.query.OrderDirection;
import com.effektif.workflow.api.query.WorkflowQuery;
import com.effektif.workflow.api.workflow.Workflow;
import com.effektif.workflow.impl.Retry;
import com.effektif.workflow.impl.WorkflowEngineImpl;
import com.effektif.workflow.impl.WorkflowStore;
import com.effektif.workflow.impl.activity.ActivityTypeService;
import com.effektif.workflow.impl.configuration.Brewable;
import com.effektif.workflow.impl.configuration.Brewery;
import com.effektif.workflow.impl.data.DataTypeService;
import com.effektif.workflow.impl.json.JsonService;
import com.effektif.workflow.impl.script.ScriptService;
import com.effektif.workflow.impl.util.Exceptions;
import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DB;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.WriteConcern;


public class MongoWorkflowStore extends MongoCollection implements WorkflowStore, Brewable {
  
  public static final Logger log = WorkflowEngineImpl.log;
  
  protected WorkflowEngineImpl workflowEngine;
  protected JsonService jsonService;
  protected DataTypeService dataTypeService;
  protected WriteConcern writeConcernInsertWorkflow;
  protected MongoCollection workflowVersions;
  protected ActivityTypeService activityTypeService;
  protected Configuration configuration;
  protected ScriptService scriptService;
  
  interface FieldsWorkflow {
    String _ID = "_id";
    String NAME = "name";
    String ORGANIZATION_ID = "organizationId";
    String DEPLOYED_TIME = "deployedTime";
  }

  interface FieldsWorkflowVersions {
    String _ID = "_id";
    String WORKFLOW_NAME = "workflowName";
    String VERSION_IDS = "versionIds";
    String LOCK = "lock";
  }
  interface FieldsWorkflowVersionsLock {
    String OWNER = "owner";
    String TIME = "time";
  }

  @Override
  public void brew(Brewery brewery) {
    DB db = brewery.get(DB.class);
    MongoConfiguration mongoConfiguration = brewery.get(MongoConfiguration.class);
    this.dbCollection = db.getCollection(mongoConfiguration.getWorkflowsCollectionName());
    this.workflowVersions = new MongoCollection();
    this.workflowVersions.dbCollection = db.getCollection(mongoConfiguration.getWorkflowsCollectionName());
    this.workflowVersions.isPretty =mongoConfiguration.isPretty; 
    this.configuration = brewery.get(Configuration.class);
    this.workflowEngine = brewery.get(WorkflowEngineImpl.class);
    this.jsonService = brewery.get(JsonService.class);
    this.scriptService = brewery.get(ScriptService.class);
    this.activityTypeService = brewery.get(ActivityTypeService.class);
    this.writeConcernInsertWorkflow = mongoConfiguration.getWriteConcernInsertWorkflow(this.dbCollection);
    this.isPretty = mongoConfiguration.isPretty;
  }

  @Override
  public void insertWorkflow(Workflow workflow) {
    String workflowName = workflow.getName();
    if (workflowName!=null) {
      // try if we can acquire the lock right away (without retry)
      BasicDBObject workflowVersions = lockWorkflowVersions(workflowName);
      if (workflowVersions==null) {
        // if the lock couldn't be obtained, it could have 2 reasons:
        // 1) the workflow versions document doesn't yet exist (most likely)
        // 2) the lock could not be acquired
        // this sequence also ensures proper locking even if 2 separate engines
        // try to deploy an unexisting workflow with the same name
        ensureWorkflowVersions(workflowName);
        workflowVersions = lockWorkflowVersionsWithRetry(workflowName);
      }
      List<String> versionIds = (List<String>) workflowVersions.get(FieldsWorkflowVersions.VERSION_IDS);
      workflow.setVersion(((long)versionIds.size())+1);
      versionIds.add(workflow.getId());
      updateAndUnlockWorkflowVersions(workflowVersions);
    }

    Map<String,Object> jsonWorkflow = jsonService.objectToJsonMap(workflow);
    BasicDBObject dbWorkflow = new BasicDBObject();    

    // We use jackson to serialize the Workflow into workflow json
    // But there are 2 exceptions that jackson doesn't convert as it should, so 
    // convert those 2 properties first

    // convert id
    jsonWorkflow.remove("id");
    writeId(jsonWorkflow, FieldsWorkflow._ID, workflow.getId());
    // convert deployedTime
    writeTimeOpt(jsonWorkflow, FieldsWorkflow.DEPLOYED_TIME, workflow.getDeployedTime());
    
    dbWorkflow.putAll(jsonWorkflow);   
    insert(dbWorkflow, writeConcernInsertWorkflow);
  }

  protected void updateAndUnlockWorkflowVersions(BasicDBObject workflowVersionsDocument) {
    workflowVersionsDocument.remove(FieldsWorkflowVersions.LOCK);
    workflowVersions.save(workflowVersionsDocument, writeConcernInsertWorkflow);
  }

  protected void ensureWorkflowVersions(String workflowName) {
    DBObject query = new BasicDBObject(FieldsWorkflowVersions.WORKFLOW_NAME, workflowName);
    DBObject update = BasicDBObjectBuilder.start()
      .push("$set")
        .add(FieldsWorkflowVersions.WORKFLOW_NAME, workflowName)
      .pop()
      .get();
    workflowVersions.update(query, update, true, false, writeConcernInsertWorkflow);
  }

  protected BasicDBObject lockWorkflowVersionsWithRetry(final String workflowName) {
    Retry<BasicDBObject> retry = new Retry<BasicDBObject>() {
      @Override
      public BasicDBObject tryOnce() {
        return lockWorkflowVersions(workflowName);
      }
    };
    return retry.tryManyTimes();
  }

  protected BasicDBObject lockWorkflowVersions(final String workflowName) {
    DBObject query = BasicDBObjectBuilder.start()
      .add(FieldsWorkflowVersions.WORKFLOW_NAME, workflowName)
      .push("$exists")
        .add(FieldsWorkflowVersions.LOCK, false)
      .pop()
      .get();
    BasicDBObject lock = new BasicDBObject()
      .append(FieldsWorkflowVersionsLock.OWNER, workflowEngine.getId())
      .append(FieldsWorkflowVersionsLock.TIME, System.currentTimeMillis());
    DBObject update = BasicDBObjectBuilder.start()
      .push("$set")
        .add(FieldsWorkflowVersions.LOCK, lock)
      .pop()
      .get();
    return workflowVersions.findAndModify(query, update);
  }

  @Override
  public List<Workflow> findWorkflows(WorkflowQuery query) {
    List<Workflow> workflows = new ArrayList<>();
    DBCursor cursor = createWorkflowDbCursor(query);
    while (cursor.hasNext()) {
      BasicDBObject dbWorkflow = (BasicDBObject) cursor.next();

      // We use jackson to parse the workflow json into a Workflow
      // But there are 2 exceptions that jackson doesn't convert as it should, so 
      // convert those 2 properties first
      
      // convert id
      String workflowId = (String)dbWorkflow.remove(FieldsWorkflow._ID);
      writeId(dbWorkflow, "id", workflowId);
      // convert deployed time from date to LocalDateTime
      LocalDateTime deployedTime = readTime(dbWorkflow, FieldsWorkflow.DEPLOYED_TIME);
      writeTimeOpt(dbWorkflow, FieldsWorkflow.DEPLOYED_TIME, deployedTime);
      
      Workflow workflow = jsonService.jsonMapToObject(dbWorkflow, Workflow.class);
      workflows.add(workflow);
    }
    return workflows;
  }
  
  @Override
  public Workflow loadWorkflowById(String workflowId) {
    List<Workflow> workflows = findWorkflows(new WorkflowQuery()
      .workflowId(workflowId));
    return !workflows.isEmpty() ? workflows.get(0) : null;
  }

  @Override
  public void deleteWorkflows(WorkflowQuery query) {
    BasicDBObject dbQuery = createWorkflowDbQuery(query);
    remove(dbQuery);
  }

  @Override
  public String findLatestWorkflowIdByName(String workflowName) {
    Exceptions.checkNotNullParameter(workflowName, "workflowName");
    BasicDBObject dbQuery = new BasicDBObject();
    dbQuery.append(FieldsWorkflow.NAME, workflowName);
    RequestContext requestContext = RequestContext.current();
    if (hasOrganizationId(requestContext)) {
      dbQuery.append(FieldsWorkflow.ORGANIZATION_ID, requestContext.getOrganizationId());
    }
    BasicDBObject dbFields = new BasicDBObject(FieldsWorkflow._ID, 1);
    BasicDBObject dbWorkflow = findOne(dbQuery, dbFields);
    return dbWorkflow!=null ? dbWorkflow.get("_id").toString() : null;
  }

  public DBCursor createWorkflowDbCursor(WorkflowQuery query) {
    BasicDBObject dbQuery = createWorkflowDbQuery(query);
    DBCursor dbCursor = find(dbQuery);
    if (query.getLimit()!=null) {
      dbCursor.limit(query.getLimit());
    }
    if (query.getOrderBy()!=null) {
      dbCursor.sort(writeOrderBy(query.getOrderBy()));
    }
    return dbCursor;
  }

  protected BasicDBObject createWorkflowDbQuery(WorkflowQuery query) {
    BasicDBObject dbQuery = new BasicDBObject();
    RequestContext requestContext = RequestContext.current();
    if (query.getWorkflowId()!=null) {
      dbQuery.append(FieldsWorkflow._ID, new ObjectId(query.getWorkflowId()));
    }
    if (hasOrganizationId(requestContext)) {
      dbQuery.append(FieldsWorkflow.ORGANIZATION_ID, requestContext.getOrganizationId());
    }
    if (query.getWorkflowName()!=null) {
      dbQuery.append(FieldsWorkflow.NAME, query.getWorkflowName());
    }
    return dbQuery;
  }
  
  public DBObject writeOrderBy(List<OrderBy> orderBy) {
    BasicDBObject dbOrderBy = new BasicDBObject();
    for (OrderBy element: orderBy) {
      String dbField = getDbField(element.getField());
      int dbDirection = (element.getDirection()==OrderDirection.asc ? 1 : -1);
      dbOrderBy.append(dbField, dbDirection);
    }
    return dbOrderBy;
  }
  
  private String getDbField(String field) {
    if (WorkflowQuery.FIELD_DEPLOY_TIME.equals(field)) {
      return FieldsWorkflow.DEPLOYED_TIME;
    }
    throw new RuntimeException("Unknown field "+field);
  }

  
//  interface FieldsScope {
//    String _ID = "_id";
//    String ACTIVITIES = "activities";
//    String VARIABLES = "variables";
//    String TRANSITIONS = "transitions";
//    String TIMERS = "timers";
//  }
//
//  interface FieldsWorkflow extends FieldsScope {
//    String NAME = "name";
//    String DEPLOYED_TIME = "deployedTime";
//    String DEPLOYED_BY = "deployedBy";
//    String ORGANIZATION_ID = "organizationId";
//    String WORKFLOW_ID = "workflowId";
//    String VERSION = "version";
//  }
//  
//  interface FieldsActivity extends FieldsScope {
//    String DEFAULT_TRANSITION_ID = "defaultTransitionId";
//    String MULTI_INSTANCE = "multiInstance";
//    String ACTIVITY_TYPE = "type";
//  }
//
//  interface FieldsBinding {
//    String EXPRESSION = "expression";
//    String VARIABLE_ID = "variableId";
//    String TYPED_VALUE = "value";
//  }
//
//  interface FieldsTypedValue {
//    String TYPE = "type";
//    String VALUE = "value";
//  }
//
//  interface FieldsMultiInstance {
//    String ELEMENT_VARIABLE = "elementVariable";
//    String VALUE_BINDINGS = "valueBindings";
//  }
//  
//  interface FieldsTransition {
//    String _ID = "_id";
//    String FROM = "from";
//    String TO = "to";
//    String CONDITION = null;
//  }
//
//  interface FieldsVariable {
//    String _ID = "_id";
//    String TYPE = "type";
//    String INITIAL_VALUE = "initialValue";
//  }
//  
//  public WorkflowImpl readWorkflow(BasicDBObject dbWorkflow) {
//    WorkflowParser parser = new WorkflowParser(configuration);
//    
//    WorkflowImpl workflow = new WorkflowImpl();
//    workflow.id = readId(dbWorkflow, FieldsWorkflow._ID);
//    workflow.name = readString(dbWorkflow, FieldsWorkflow.NAME);
//    workflow.deployedTime = readTime(dbWorkflow, FieldsWorkflow.DEPLOYED_TIME);
//    workflow.deployedBy = readId(dbWorkflow, FieldsWorkflow.DEPLOYED_BY);
//    workflow.organizationId = readId(dbWorkflow, FieldsWorkflow.ORGANIZATION_ID);
//    workflow.version = readLong(dbWorkflow, FieldsWorkflow.VERSION);
//    workflow.workflow = workflow;
//    workflow.configuration = configuration;
//    
//    readScope(workflow, dbWorkflow, parser);
//    workflow.startActivities = parser.getStartActivities(workflow);
//    
//    if (parser.hasErrors()) {
//      log.error("Couldn't read workflow "+workflow.id+" from database: \n"+parser.getIssues().getIssueReport());
//      return null;
//    }
//    
//    return workflow;
//  }
//
//  public BasicDBObject writeWorkflow(WorkflowImpl workflow) {
//    BasicDBObject dbWorkflow = new BasicDBObject();
//    Stack<Map<String,Object>> dbObjectStack = new Stack<>();
//    dbObjectStack.push(dbWorkflow);
//    writeId(dbWorkflow, FieldsWorkflow._ID, workflow.id);
//    writeString(dbWorkflow, FieldsWorkflow.NAME, workflow.name);
//    writeTimeOpt(dbWorkflow, FieldsWorkflow.DEPLOYED_TIME, workflow.deployedTime);
//    writeIdOpt(dbWorkflow, FieldsWorkflow.DEPLOYED_BY, workflow.deployedBy);
//    writeIdOpt(dbWorkflow, FieldsWorkflow.ORGANIZATION_ID, workflow.organizationId);
//    writeObjectOpt(dbWorkflow, FieldsWorkflow.VERSION, workflow.version);
//    
//    writeActivities(workflow, dbObjectStack);
//    writeTransitions(workflow, dbObjectStack);
//    writeVariables(workflow, dbObjectStack);
//    writeTimers(workflow, dbObjectStack);
//    return dbWorkflow;
//  }
//  
//  protected void readActivities(ScopeImpl scope, BasicDBObject dbScope, WorkflowParser parser, Map<String,String> defaultTransitionIds) {
//    List<BasicDBObject> dbActivities = readList(dbScope, FieldsWorkflow.ACTIVITIES);
//    if (dbActivities!=null) {
//      for (BasicDBObject dbActivity: dbActivities) {
//        ActivityImpl activity = new ActivityImpl();
//        activity.id = readString(dbActivity, FieldsWorkflow._ID);
//        activity.workflow = scope.workflow;
//        activity.configuration = configuration;
//        activity.parent = scope;
//        
//        activity.multiInstance = readMultiInstance(readBasicDBObject(dbActivity, FieldsActivity.MULTI_INSTANCE), parser);
//        String defaultTransitionId = (String) dbActivity.get(FieldsActivity.DEFAULT_TRANSITION_ID);
//        if (defaultTransitionId!=null) {
//          defaultTransitionIds.put(activity.id, defaultTransitionId);
//        }
//        
//        readScope(activity, dbActivity, parser);
//
//        Map<String,Object> dbActivityType = readObjectMap(dbActivity, FieldsActivity.ACTIVITY_TYPE);
//        log.debug("ACTIVITY JSON: "+PrettyPrinter.toJsonPrettyPrint(dbActivityType));
//        Activity activityApi = null;
//        try {
//          activityApi = jsonService.jsonMapToObject(dbActivityType, Activity.class);
//          activity.activityType = activityTypeService.instantiateActivityType(activityApi);
//          activity.activityType.parse(activity, activityApi, parser);
//          scope.addActivity(activity);
//        } catch (Exception e) {
//          parser.addError("Couldn't parse activity %s", dbActivityType);
//        }
//      }
//    }
//  }
//
//  protected void readScope(ScopeImpl scope, BasicDBObject dbScope, WorkflowParser parser) {
//    Map<String,String> defaultTransitionIds = new HashMap<>();
//    readActivities(scope, dbScope, parser, defaultTransitionIds);
//    readVariables(scope, dbScope, parser);
//    readTransitions(scope, dbScope);
//    if (!defaultTransitionIds.isEmpty()) {
//      for (ActivityImpl activity: scope.activities.values()) {
//        String defaultTransitionId = defaultTransitionIds.get(activity.id);
//        activity.defaultTransition = activity.findTransitionByIdLocal(defaultTransitionId);
//      }
//    }
//  }
//  
//  protected void writeActivities(ScopeImpl scope, Stack<Map<String,Object>> dbObjectStack) {
//    if (scope.activities!=null) {
//      for (ActivityImpl activity: scope.activities.values()) {
//        Map<String,Object> dbActivity = new BasicDBObject();
//        Map<String,Object> dbParentScope = dbObjectStack.peek(); 
//        dbObjectStack.push(dbActivity);
//        
//        Map<String, Object> dbActivityType = jsonService.objectToJsonMap(activity.activityType.serialize());
//        writeObjectOpt(dbActivity, FieldsActivity.ACTIVITY_TYPE, dbActivityType);
//        
//        writeString(dbActivity, FieldsActivity._ID, activity.id);
//        writeString(dbActivity, FieldsActivity.DEFAULT_TRANSITION_ID, activity.id);
//        writeObjectOpt(dbActivity, FieldsActivity.MULTI_INSTANCE, writeMultiInstance(activity.multiInstance));
//        writeActivities(activity, dbObjectStack);
//        writeTransitions(activity, dbObjectStack);
//        writeVariables(activity, dbObjectStack);
//        writeTimers(activity, dbObjectStack);
//        dbObjectStack.pop();
//        writeListElementOpt(dbParentScope, FieldsWorkflow.ACTIVITIES, dbActivity);
//      }
//    }
//  }
//
//  protected MultiInstanceImpl readMultiInstance(BasicDBObject dbMultiInstance, WorkflowParser parser) {
//    if (dbMultiInstance==null) {
//      return null;
//    }
//    MultiInstanceImpl multiInstance = new MultiInstanceImpl();
//    BasicDBObject dbVariable = readBasicDBObject(dbMultiInstance, FieldsMultiInstance.ELEMENT_VARIABLE);
//    multiInstance.elementVariable = readVariable(null, dbVariable, parser);
//    List<BasicDBObject> dbValueBindings = readList(dbVariable, FieldsMultiInstance.VALUE_BINDINGS);
//    if (dbValueBindings!=null) {
//      multiInstance.valueBindings = new ArrayList<>(dbValueBindings.size());
//      for (BasicDBObject dbValueBinding: dbValueBindings) {
//        multiInstance.addValueBinding(readBinding(dbValueBinding));
//      }
//    }
//    return multiInstance;
//  }
//
//  protected Map<String, Object> writeMultiInstance(MultiInstanceImpl multiInstance) {
//    if (multiInstance==null) {
//      return null;
//    }
//    Map<String, Object> dbMultiInstance = new BasicDBObject();
//    writeObjectOpt(dbMultiInstance, FieldsMultiInstance.ELEMENT_VARIABLE, writeVariable(multiInstance.elementVariable));
//    if (multiInstance.valueBindings!=null) {
//      for (BindingImpl binding: multiInstance.valueBindings) {
//        writeListElementOpt(dbMultiInstance, FieldsMultiInstance.VALUE_BINDINGS, writeBinding(binding));
//      }
//    }
//    return dbMultiInstance;
//  }
//
//  protected BindingImpl readBinding(BasicDBObject dbBinding) {
//    if (dbBinding==null) {
//      return null;
//    }
//    BindingImpl binding = new BindingImpl<>(null);
//    // i hope a null value for binding.expectedValueType is ok 
//    binding.typedValue = readTypedValue(readBasicDBObject(dbBinding, FieldsBinding.TYPED_VALUE));
//    binding.variableId = readString(dbBinding, FieldsBinding.VARIABLE_ID);
//    binding.expressionText = readString(dbBinding, FieldsBinding.EXPRESSION);
//    binding.expression = scriptService.compile(binding.expressionText);
//    return binding;
//  }
//
//  protected BasicDBObject writeBinding(BindingImpl binding) {
//    BasicDBObject dbBinding = new BasicDBObject();
//    writeStringOpt(dbBinding, FieldsBinding.EXPRESSION, binding.expressionText);
//    writeStringOpt(dbBinding, FieldsBinding.VARIABLE_ID, binding.variableId);
//    writeObjectOpt(dbBinding, FieldsBinding.TYPED_VALUE, writeTypedValue(binding.typedValue));
//    return dbBinding;
//  }
//
//  protected TypedValueImpl readTypedValue(BasicDBObject dbTypedValue) {
//    if (dbTypedValue==null) {
//      return null;
//    }
//    DataType dataType = null;
//    Object value = null;
//    Map<String,Object> dbType = readObjectMap(dbTypedValue, FieldsTypedValue.TYPE);
//    if (dbType!=null) {
//      Type type = jsonService.jsonMapToObject(dbType, Type.class);
//      dataType = dataTypeService.createDataType(type);
//      
//      Object dbValue = readObject(dbTypedValue, FieldsTypedValue.VALUE);
//      value = dataType.convertJsonToInternalValue(dbValue);
//    }
//    return new TypedValueImpl(dataType, value);
//  }
//
//  protected BasicDBObject writeTypedValue(TypedValueImpl typedValue) {
//    if (typedValue==null) {
//      return null;
//    }
//    BasicDBObject dbTypedValue = new BasicDBObject();
//    if (typedValue.type!=null) {
//      Map<String,Object> dbType = jsonService.objectToJsonMap(typedValue.type.serialize());
//      writeObjectOpt(dbTypedValue, FieldsTypedValue.TYPE, dbType);
//      if (typedValue.value!=null) {
//        Object dbValue = typedValue.type.convertInternalToJsonValue(typedValue.value);
//        writeObjectOpt(dbTypedValue, FieldsTypedValue.VALUE, dbValue);
//      }
//    }
//    return dbTypedValue;
//  }
//
//  protected void readTransitions(ScopeImpl scope, BasicDBObject dbScope) {
//    List<BasicDBObject> dbTransitions = readList(dbScope, FieldsWorkflow.TRANSITIONS);
//    if (dbTransitions!=null) {
//      for (BasicDBObject dbTransition: dbTransitions) {
//        TransitionImpl transition = new TransitionImpl();
//        transition.id = readString(dbTransition, FieldsTransition._ID);
//        transition.configuration = configuration;
//        transition.parent = scope;
//        transition.workflow = scope.workflow;
//        
//        String fromId = readString(dbTransition, FieldsTransition.FROM);
//        transition.from = scope.findActivityByIdLocal(fromId);
//        transition.from.addOutgoingTransition(transition);
//        
//        String toId = readString(dbTransition, FieldsTransition.TO);
//        transition.to = scope.findActivityByIdLocal(toId);
//        transition.to.addIncomingTransition(transition);
//        
//        String script = readString(dbTransition, FieldsTransition.CONDITION);
//        if (script!=null) {
//          transition.conditionScriptText = script; 
//          transition.conditionScript = scriptService.compile(script);
//        }
//
//        scope.addTransition(transition);
//      }
//    }
//  }
//  
//  protected void writeTransitions(ScopeImpl scope, Stack<Map<String,Object>> dbObjectStack) {
//    if (scope.transitions!=null) {
//      for (TransitionImpl transition: scope.transitions) {
//        Map<String,Object> dbParentScope = dbObjectStack.peek(); 
//        BasicDBObject dbTransition = new BasicDBObject();
//        writeStringOpt(dbTransition, FieldsTransition._ID, transition.id);
//        writeObjectOpt(dbTransition, FieldsTransition.FROM, transition.from!=null ? transition.from.id : null);
//        writeObjectOpt(dbTransition, FieldsTransition.TO, transition.to!=null ? transition.to.id : null);
//        writeObjectOpt(dbTransition, FieldsTransition.CONDITION, transition.conditionScriptText);
//        writeListElementOpt(dbParentScope, FieldsScope.TRANSITIONS, dbTransition);
//      }
//    }
//  }
//
//  protected void readVariables(ScopeImpl scope, BasicDBObject dbScope, WorkflowParser parser) {
//    List<BasicDBObject> dbVariables = readList(dbScope, FieldsWorkflow.VARIABLES);
//    if (dbVariables!=null) {
//      for (BasicDBObject dbVariable: dbVariables) {
//        VariableImpl variable = readVariable(scope, dbVariable, parser);
//        scope.addVariable(variable);
//      }
//    }
//  }
//
//  protected void writeVariables(ScopeImpl scope, Stack<Map<String,Object>> dbObjectStack) {
//    if (scope.variables!=null) {
//      for (VariableImpl variable: scope.variables.values()) {
//        Map<String,Object> dbParentScope = dbObjectStack.peek(); 
//        BasicDBObject dbVariable = writeVariable(variable);
//        writeListElementOpt(dbParentScope, FieldsScope.VARIABLES, dbVariable);
//      }
//    }
//  }
//
//  protected VariableImpl readVariable(ScopeImpl scope, BasicDBObject dbVariable, WorkflowParser parser) {
//    VariableImpl variable = new VariableImpl();
//    variable.id = readString(dbVariable, FieldsWorkflow._ID);
//    if (scope!=null) {
//      variable.parent = scope;
//      variable.workflow = scope.workflow;
//    }
//    Map<String,Object> dbType = readObjectMap(dbVariable, FieldsVariable.TYPE);
//    if (dbType!=null) {
//      try {
//        Type type = jsonService.jsonMapToObject(dbType, Type.class);
//        variable.type = dataTypeService.createDataType(type);
//        Object dbInitialValue = dbVariable.get(FieldsVariable.INITIAL_VALUE);
//        if (dbInitialValue!=null) {
//          try {
//            variable.initialValue = variable.type.convertJsonToInternalValue(dbInitialValue);
//          } catch (Exception e) {
//            parser.addError("Couldn't parse initial value from db: %s", dbInitialValue);
//          }
//        }
//      } catch (Exception e) {
//        parser.addError("Couldn't parse type from db: %s", dbType);
//      }
//    }
//    return variable;
//  }
//
//  public BasicDBObject writeVariable(VariableImpl variable) {
//    BasicDBObject dbVariable = new BasicDBObject();
//    writeString(dbVariable, FieldsWorkflow._ID, variable.id);
//    
//    if (variable.type!=null) {
//      Map<String,Object> dbType = jsonService.objectToJsonMap(variable.type);
//      writeObjectOpt(dbVariable, FieldsVariable.TYPE, dbType);
//      if (variable.initialValue!=null) {
//        Object dbValue = variable.type.convertInternalToJsonValue(variable.initialValue);
//        writeObjectOpt(dbVariable, FieldsVariable.INITIAL_VALUE, dbValue);
//      }
//    }
//
//    return dbVariable;
//  }
//
//  protected void writeTimers(ScopeImpl scope, Stack<Map<String,Object>> dbObjectStack) {
//    if (scope.timers!=null) {
//      for (TimerImpl timer: scope.timers) {
//        throw new RuntimeException("TODO");
//      }
//    }
//  }
}
