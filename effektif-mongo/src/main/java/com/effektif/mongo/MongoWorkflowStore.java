/*
 * Copyright 2014 Effektif GmbH.
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
 * limitations under the License.
 */
package com.effektif.mongo;

import static com.effektif.mongo.MongoDb._ID;
import static com.effektif.mongo.WorkflowFields.*;

import com.effektif.workflow.api.Configuration;
import com.effektif.workflow.api.model.WorkflowId;
import com.effektif.workflow.api.query.OrderBy;
import com.effektif.workflow.api.query.OrderDirection;
import com.effektif.workflow.api.query.WorkflowQuery;
import com.effektif.workflow.api.workflow.AbstractWorkflow;
import com.effektif.workflow.api.workflow.ExecutableWorkflow;
import com.effektif.workflow.impl.WorkflowEngineImpl;
import com.effektif.workflow.impl.WorkflowStore;
import com.effektif.workflow.impl.activity.ActivityTypeService;
import com.effektif.workflow.impl.configuration.Brewable;
import com.effektif.workflow.impl.configuration.Brewery;
import com.effektif.workflow.impl.data.DataTypeService;
import com.effektif.workflow.impl.util.Exceptions;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import org.bson.types.ObjectId;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;


public class MongoWorkflowStore implements WorkflowStore, Brewable {
  
  public static final Logger log = MongoDb.log;
  
  protected WorkflowEngineImpl workflowEngine;
  protected DataTypeService dataTypeService;
  protected MongoCollection workflowsCollection;
  protected ActivityTypeService activityTypeService;
  protected Configuration configuration;
  protected MongoObjectMapper mongoMapper;

  @Override
  public void brew(Brewery brewery) {
    MongoDb mongoDb = brewery.get(MongoDb.class);
    MongoConfiguration mongoConfiguration = brewery.get(MongoConfiguration.class);
    this.workflowsCollection = mongoDb.createCollection(mongoConfiguration.getWorkflowsCollectionName());
    this.configuration = brewery.get(Configuration.class);
    this.workflowEngine = brewery.get(WorkflowEngineImpl.class);
    this.activityTypeService = brewery.get(ActivityTypeService.class);
    this.mongoMapper = brewery.get(MongoObjectMapper.class);
  }

  public BasicDBObject workflowApiToMongo(AbstractWorkflow workflow) {
    return (BasicDBObject) mongoMapper.write(workflow);
  }

  public <T extends AbstractWorkflow> T mongoToWorkflowApi(BasicDBObject dbWorkflow, Class<T> workflowClass) {
    return mongoMapper.read(dbWorkflow, workflowClass);
  }
  
  @Override
  public WorkflowId generateWorkflowId() {
    return new WorkflowId(new ObjectId().toString());
  }

  @Override
  public void insertWorkflow(ExecutableWorkflow workflow) {
    BasicDBObject dbWorkflow = workflowApiToMongo(workflow);
    workflowsCollection.insert("insert-workflow", dbWorkflow);
  }

  @Override
  public List<ExecutableWorkflow> findWorkflows(WorkflowQuery query) {
    if (query==null) {
      query = new WorkflowQuery();
    }
    List<ExecutableWorkflow> workflows = new ArrayList<>();
    DBCursor cursor = createWorkflowDbCursor(query);
    while (cursor.hasNext()) {
      BasicDBObject dbWorkflow = (BasicDBObject) cursor.next();
      ExecutableWorkflow workflow = mongoToWorkflowApi(dbWorkflow, ExecutableWorkflow.class);
      workflows.add(workflow);
    }
    return workflows;
  }

  @Override
  public ExecutableWorkflow loadWorkflowById(WorkflowId workflowId) {
    List<ExecutableWorkflow> workflows = findWorkflows(new WorkflowQuery()
      .workflowId(workflowId));
    return !workflows.isEmpty() ? workflows.get(0) : null;
  }

  @Override
  public void deleteWorkflows(WorkflowQuery query) {
    BasicDBObject dbQuery = createDbQuery(query);
    workflowsCollection.remove("delete-workflows", dbQuery);
  }
  
  @Override
  public void deleteAllWorkflows() {
    workflowsCollection.remove("delete-workflows-unchecked", new BasicDBObject(), false);
  }

  @Override
  public WorkflowId findLatestWorkflowIdBySource(String sourceWorkflowId) {
    Exceptions.checkNotNullParameter(sourceWorkflowId, "sourceWorkflowId");
    Query query = new Query()
      .equal(SOURCE_WORKFLOW_ID, sourceWorkflowId)
      .orderDesc(CREATE_TIME)
      .page(0,  1);
    Fields fields = new Fields()
      .include(_ID);
    BasicDBObject dbWorkflow = workflowsCollection.findOne("find-latest-workflow", query.get(), fields.get(), query.orderBy);
    return dbWorkflow!=null ? new WorkflowId(dbWorkflow.get(_ID).toString()) : null;
  }

  public DBCursor createWorkflowDbCursor(WorkflowQuery query) {
    BasicDBObject dbQuery = createDbQuery(query);
    DBCursor dbCursor = workflowsCollection.find("find-workflows", dbQuery);
    if (query.getLimit()!=null) {
      dbCursor.limit(query.getLimit());
    }
    if (query.getOrderBy()!=null) {
      dbCursor.sort(writeOrderBy(query.getOrderBy()));
    }
    return dbCursor;
  }

  protected BasicDBObject createDbQuery(WorkflowQuery query) {
    BasicDBObject dbQuery = new BasicDBObject();
    if (query.getWorkflowId()!=null) {
      dbQuery.append(_ID, new ObjectId(query.getWorkflowId().getInternal()));
    }
// TODO change to MongoQuery
//  if (MongoHelper.hasOrganizationId(authorization)) {
//    dbQuery.append(ORGANIZATION_ID, authorization.getOrganizationId());
//  }
    if (query.getWorkflowSource()!=null) {
      dbQuery.append(SOURCE_WORKFLOW_ID, query.getWorkflowSource());
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
    if (WorkflowQuery.FIELD_CREATE_TIME.equals(field)) {
      return CREATE_TIME;
    }
    throw new RuntimeException("Unknown field "+field);
  }

  public MongoCollection getWorkflowsCollection() {
    return workflowsCollection;
  }
  
//  public WorkflowImpl readWorkflow(BasicDBObject dbWorkflow) {
//    WorkflowParser parser = new WorkflowParser(configuration);
//    
//    WorkflowImpl workflow = new WorkflowImpl();
//    workflow.id = readId(dbWorkflow, _ID);
//    workflow.name = readString(dbWorkflow, NAME);
//    workflow.deployedTime = readTime(dbWorkflow, DEPLOYED_TIME);
//    workflow.deployedBy = readId(dbWorkflow, DEPLOYED_BY);
//    workflow.organizationId = readId(dbWorkflow, ORGANIZATION_ID);
//    workflow.version = readLong(dbWorkflow, VERSION);
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
//    writeId(dbWorkflow, _ID, workflow.id);
//    writeString(dbWorkflow, NAME, workflow.name);
//    writeTimeOpt(dbWorkflow, DEPLOYED_TIME, workflow.deployedTime);
//    writeIdOpt(dbWorkflow, DEPLOYED_BY, workflow.deployedBy);
//    writeIdOpt(dbWorkflow, ORGANIZATION_ID, workflow.organizationId);
//    writeObjectOpt(dbWorkflow, VERSION, workflow.version);
//    
//    writeActivities(workflow, dbObjectStack);
//    writeTransitions(workflow, dbObjectStack);
//    writeVariables(workflow, dbObjectStack);
//    writeTimers(workflow, dbObjectStack);
//    return dbWorkflow;
//  }
//  
//  protected void readActivities(ScopeImpl scope, BasicDBObject dbScope, WorkflowParser parser, Map<String,String> defaultTransitionIds) {
//    List<BasicDBObject> dbActivities = readList(dbScope, ACTIVITIES);
//    if (dbActivities!=null) {
//      for (BasicDBObject dbActivity: dbActivities) {
//        ActivityImpl activity = new ActivityImpl();
//        activity.id = readString(dbActivity, _ID);
//        activity.workflow = scope.workflow;
//        activity.configuration = configuration;
//        activity.parent = scope;
//        
//        activity.multiInstance = readMultiInstance(readBasicDBObject(dbActivity, WorkflowFields.Activity.MULTI_INSTANCE), parser);
//        String defaultTransitionId = (String) dbActivity.get(WorkflowFields.Activity.DEFAULT_TRANSITION_ID);
//        if (defaultTransitionId!=null) {
//          defaultTransitionIds.put(activity.id, defaultTransitionId);
//        }
//        
//        readScope(activity, dbActivity, parser);
//
//        Map<String,Object> dbActivityType = readObjectMap(dbActivity, WorkflowFields.Activity.ACTIVITY_TYPE);
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
//        writeObjectOpt(dbActivity, WorkflowFields.Activity.ACTIVITY_TYPE, dbActivityType);
//        
//        writeString(dbActivity, WorkflowFields.Activity._ID, activity.id);
//        writeString(dbActivity, WorkflowFields.Activity.DEFAULT_TRANSITION_ID, activity.id);
//        writeObjectOpt(dbActivity, WorkflowFields.Activity.MULTI_INSTANCE, writeMultiInstance(activity.multiInstance));
//        writeActivities(activity, dbObjectStack);
//        writeTransitions(activity, dbObjectStack);
//        writeVariables(activity, dbObjectStack);
//        writeTimers(activity, dbObjectStack);
//        dbObjectStack.pop();
//        writeListElementOpt(dbParentScope, ACTIVITIES, dbActivity);
//      }
//    }
//  }
//
//  protected MultiInstanceImpl readMultiInstance(BasicDBObject dbMultiInstance, WorkflowParser parser) {
//    if (dbMultiInstance==null) {
//      return null;
//    }
//    MultiInstanceImpl multiInstance = new MultiInstanceImpl();
//    BasicDBObject dbVariable = readBasicDBObject(dbMultiInstance, WorkflowFields.MultiInstance.ELEMENT_VARIABLE);
//    multiInstance.elementVariable = readVariable(null, dbVariable, parser);
//    List<BasicDBObject> dbValueBindings = readList(dbVariable, WorkflowFields.MultiInstance.VALUE_BINDINGS);
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
//    writeObjectOpt(dbMultiInstance, WorkflowFields.MultiInstance.ELEMENT_VARIABLE, writeVariable(multiInstance.elementVariable));
//    if (multiInstance.valueBindings!=null) {
//      for (BindingImpl binding: multiInstance.valueBindings) {
//        writeListElementOpt(dbMultiInstance, WorkflowFields.MultiInstance.VALUE_BINDINGS, writeBinding(binding));
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
//    binding.typedValue = readTypedValue(readBasicDBObject(dbBinding, WorkflowFields.Binding.TYPED_VALUE));
//    binding.variableId = readString(dbBinding, WorkflowFields.Binding.VARIABLE_ID);
//    binding.expressionText = readString(dbBinding, WorkflowFields.Binding.EXPRESSION);
//    binding.expression = scriptService.compile(binding.expressionText);
//    return binding;
//  }
//
//  protected BasicDBObject writeBinding(BindingImpl binding) {
//    BasicDBObject dbBinding = new BasicDBObject();
//    writeStringOpt(dbBinding, WorkflowFields.Binding.EXPRESSION, binding.expressionText);
//    writeStringOpt(dbBinding, WorkflowFields.Binding.VARIABLE_ID, binding.variableId);
//    writeObjectOpt(dbBinding, WorkflowFields.Binding.TYPED_VALUE, writeTypedValue(binding.typedValue));
//    return dbBinding;
//  }
//
//  protected TypedValueImpl readTypedValue(BasicDBObject dbTypedValue) {
//    if (dbTypedValue==null) {
//      return null;
//    }
//    DataType dataType = null;
//    Object value = null;
//    Map<String,Object> dbType = readObjectMap(dbTypedValue, WorkflowFields.TypedValue.TYPE);
//    if (dbType!=null) {
//      Type type = jsonService.jsonMapToObject(dbType, Type.class);
//      dataType = dataTypeService.createDataType(type);
//      
//      Object dbValue = readObject(dbTypedValue, WorkflowFields.TypedValue.VALUE);
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
//      writeObjectOpt(dbTypedValue, WorkflowFields.TypedValue.TYPE, dbType);
//      if (typedValue.value!=null) {
//        Object dbValue = typedValue.type.convertInternalToJsonValue(typedValue.value);
//        writeObjectOpt(dbTypedValue, WorkflowFields.TypedValue.VALUE, dbValue);
//      }
//    }
//    return dbTypedValue;
//  }
//
//  protected void readTransitions(ScopeImpl scope, BasicDBObject dbScope) {
//    List<BasicDBObject> dbTransitions = readList(dbScope, TRANSITIONS);
//    if (dbTransitions!=null) {
//      for (BasicDBObject dbTransition: dbTransitions) {
//        TransitionImpl transition = new TransitionImpl();
//        transition.id = readString(dbTransition, WorkflowFields.Transition._ID);
//        transition.configuration = configuration;
//        transition.parent = scope;
//        transition.workflow = scope.workflow;
//        
//        String fromId = readString(dbTransition, WorkflowFields.Transition.FROM);
//        transition.from = scope.findActivityByIdLocal(fromId);
//        transition.from.addOutgoingTransition(transition);
//        
//        String toId = readString(dbTransition, WorkflowFields.Transition.TO);
//        transition.to = scope.findActivityByIdLocal(toId);
//        transition.to.addIncomingTransition(transition);
//        
//        String script = readString(dbTransition, WorkflowFields.Transition.CONDITION);
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
//        writeStringOpt(dbTransition, WorkflowFields.Transition._ID, transition.id);
//        writeObjectOpt(dbTransition, WorkflowFields.Transition.FROM, transition.from!=null ? transition.from.id : null);
//        writeObjectOpt(dbTransition, WorkflowFields.Transition.TO, transition.to!=null ? transition.to.id : null);
//        writeObjectOpt(dbTransition, WorkflowFields.Transition.CONDITION, transition.conditionScriptText);
//        writeListElementOpt(dbParentScope, WorkflowFields.Scope.TRANSITIONS, dbTransition);
//      }
//    }
//  }
//
//  protected void readVariables(ScopeImpl scope, BasicDBObject dbScope, WorkflowParser parser) {
//    List<BasicDBObject> dbVariables = readList(dbScope, VARIABLES);
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
//        writeListElementOpt(dbParentScope, WorkflowFields.Scope.VARIABLES, dbVariable);
//      }
//    }
//  }
//
//  protected VariableImpl readVariable(ScopeImpl scope, BasicDBObject dbVariable, WorkflowParser parser) {
//    VariableImpl variable = new VariableImpl();
//    variable.id = readString(dbVariable, _ID);
//    if (scope!=null) {
//      variable.parent = scope;
//      variable.workflow = scope.workflow;
//    }
//    Map<String,Object> dbType = readObjectMap(dbVariable, WorkflowFields.Variable.TYPE);
//    if (dbType!=null) {
//      try {
//        Type type = jsonService.jsonMapToObject(dbType, Type.class);
//        variable.type = dataTypeService.createDataType(type);
//        Object dbInitialValue = dbVariable.get(WorkflowFields.Variable.INITIAL_VALUE);
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
//    writeString(dbVariable, _ID, variable.id);
//    
//    if (variable.type!=null) {
//      Map<String,Object> dbType = jsonService.objectToJsonMap(variable.type);
//      writeObjectOpt(dbVariable, WorkflowFields.Variable.TYPE, dbType);
//      if (variable.initialValue!=null) {
//        Object dbValue = variable.type.convertInternalToJsonValue(variable.initialValue);
//        writeObjectOpt(dbVariable, WorkflowFields.Variable.INITIAL_VALUE, dbValue);
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
