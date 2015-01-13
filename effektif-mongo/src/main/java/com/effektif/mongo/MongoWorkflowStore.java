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
import java.util.List;
import java.util.Map;

import org.bson.types.ObjectId;
import org.slf4j.Logger;

import com.effektif.workflow.api.command.RequestContext;
import com.effektif.workflow.api.query.OrderBy;
import com.effektif.workflow.api.query.OrderDirection;
import com.effektif.workflow.api.query.WorkflowQuery;
import com.effektif.workflow.api.workflow.Workflow;
import com.effektif.workflow.impl.Retry;
import com.effektif.workflow.impl.WorkflowEngineImpl;
import com.effektif.workflow.impl.WorkflowStore;
import com.effektif.workflow.impl.json.JsonService;
import com.effektif.workflow.impl.plugin.Initializable;
import com.effektif.workflow.impl.plugin.ServiceRegistry;
import com.effektif.workflow.impl.util.Exceptions;
import com.effektif.workflow.impl.workflow.WorkflowImpl;
import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DB;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.WriteConcern;


public class MongoWorkflowStore extends MongoCollection implements WorkflowStore, Initializable<MongoWorkflowEngineConfiguration> {
  
  public static final Logger log = WorkflowEngineImpl.log;
  
  protected WorkflowEngineImpl workflowEngine;
  protected JsonService jsonService;
  protected WriteConcern writeConcernInsertWorkflow;
  protected MongoCollection workflowVersions;

  interface FieldsWorkflow {
    String _ID = "_id";
    String NAME = "name";
    String DEPLOYED_TIME = "deployedTime";
    String DEPLOYED_BY = "deployedBy";
    String ORGANIZATION_ID = "organizationId";
    String WORKFLOW_ID = "workflowId";
    String VERSION = "version";
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
  public void initialize(ServiceRegistry serviceRegistry, MongoWorkflowEngineConfiguration configuration) {
    DB db = serviceRegistry.getService(DB.class);
    this.dbCollection = db.getCollection(configuration.getWorkflowsCollectionName());
    this.isPretty = configuration.isPretty;
    this.jsonService = serviceRegistry.getService(JsonService.class);
    this.writeConcernInsertWorkflow = configuration.getWriteConcernInsertWorkflow(this.dbCollection);
    this.workflowEngine = serviceRegistry.getService(WorkflowEngineImpl.class);
    this.workflowVersions = new MongoCollection();
    this.workflowVersions.dbCollection = db.getCollection(configuration.getWorkflowsCollectionName());
    this.workflowVersions.isPretty =configuration.isPretty; 
  }

  @Override
  public void insertWorkflow(Workflow workflowApi, WorkflowImpl workflowImpl, RequestContext requestContext) {
    String workflowName = workflowApi.getName();
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
      workflowImpl.version = ((long)versionIds.size())+1;
      workflowApi.setVersion(workflowImpl.version);
      versionIds.add(workflowImpl.id);
      updateAndUnlockWorkflowVersions(workflowVersions);
    }

    Map<String,Object> jsonWorkflow = jsonService.objectToJsonMap(workflowApi);
    BasicDBObject dbWorkflow = new BasicDBObject();
    String workflowId = (String)jsonWorkflow.remove("id");
    dbWorkflow.put(FieldsWorkflow._ID, new ObjectId(workflowId));
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
  public List<Workflow> findWorkflows(WorkflowQuery query, RequestContext requestContext) {
    List<Workflow> workflows = new ArrayList<Workflow>();
    DBCursor cursor = createWorkflowDbCursor(query, requestContext);
    while (cursor.hasNext()) {
      BasicDBObject dbWorkflow = (BasicDBObject) cursor.next();
      dbWorkflow.put("id", dbWorkflow.remove(FieldsWorkflow._ID).toString());
      Workflow workflow = jsonService.jsonMapToObject(dbWorkflow, Workflow.class);
      workflows.add(workflow);
    }
    return workflows;
  }
  
  @Override
  public Workflow loadWorkflowById(String workflowId, RequestContext requestContext) {
    List<Workflow> workflows = findWorkflows(new WorkflowQuery()
      .workflowId(workflowId), requestContext);
    return !workflows.isEmpty() ? workflows.get(0) : null;
  }

  @Override
  public void deleteWorkflows(WorkflowQuery query, RequestContext requestContext) {
    BasicDBObject dbQuery = createWorkflowDbQuery(query, requestContext);
    remove(dbQuery);
  }

  @Override
  public String findLatestWorkflowIdByName(String workflowName, RequestContext requestContext) {
    Exceptions.checkNotNullParameter(workflowName, "workflowName");
    BasicDBObject dbQuery = new BasicDBObject();
    dbQuery.append(FieldsWorkflow.NAME, workflowName);
    if (RequestContext.hasOrganizationId(requestContext)) {
      dbQuery.append(FieldsWorkflow.ORGANIZATION_ID, requestContext.getOrganizationId());
    }
    BasicDBObject dbFields = new BasicDBObject(FieldsWorkflow._ID, 1);
    BasicDBObject dbWorkflow = findOne(dbQuery, dbFields);
    return dbWorkflow!=null ? dbWorkflow.get("_id").toString() : null;
  }

  public DBCursor createWorkflowDbCursor(WorkflowQuery query, RequestContext requestContext) {
    BasicDBObject dbQuery = createWorkflowDbQuery(query, requestContext);
    DBCursor dbCursor = find(dbQuery);
    if (query.getLimit()!=null) {
      dbCursor.limit(query.getLimit());
    }
    if (query.getOrderBy()!=null) {
      dbCursor.sort(writeOrderBy(query.getOrderBy()));
    }
    return dbCursor;
  }

  protected BasicDBObject createWorkflowDbQuery(WorkflowQuery query, RequestContext requestContext) {
    BasicDBObject dbQuery = new BasicDBObject();
    if (RequestContext.hasOrganizationId(requestContext)) {
      dbQuery.append(FieldsWorkflow.ORGANIZATION_ID, query.getWorkflowId());
    }
    if (query.getWorkflowId()!=null) {
      dbQuery.append(FieldsWorkflow._ID, new ObjectId(query.getWorkflowId()));
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

//  @Override
//  public void deleteWorkflow(String workflowId) {
//    BasicDBObject query = new BasicDBObject(fields._id, new ObjectId(workflowId));
//    remove(query);
//  }
//
//  public Workflow readWorkflow(BasicDBObject dbWorkflow) {
//    Workflow workflow = new WorkflowImpl();
//    workflow.setId(readId(dbWorkflow, FLD_WF._id));
//    workflow.setName(readString(dbWorkflow, FLD_WF.name));
//    workflow.setDeployedTime(readTime(dbWorkflow, FLD_WF.deployedTime));
//    workflow.setDeployedBy(readId(dbWorkflow, FLD_WF.deployedBy));
//    workflow.setOrganizationId(readId(dbWorkflow, FLD_WF.organizationId));
//    workflow.setVersion(readLong(dbWorkflow, FLD_WF.version));
//    readActivities(workflow, dbWorkflow);
//    readVariables(workflow, dbWorkflow);
//    readTransitions(workflow, dbWorkflow);
//    return workflow;
//  }
//  
//  public BasicDBObject writeWorkflow(Workflow workflow) {
//    BasicDBObject dbWorkflow = new BasicDBObject();
//    Stack<BasicDBObject> dbObjectStack = new Stack<>();
//    dbObjectStack.push(dbWorkflow);
//    writeId(dbWorkflow, FLD_WF._id, workflow.getId());
//    writeString(dbWorkflow, FLD_WF.name, workflow.getName());
//    writeTimeOpt(dbWorkflow, FLD_WF.deployedTime, workflow.getDeployedTime());
//    writeIdOpt(dbWorkflow, FLD_WF.deployedBy, workflow.getDeployedBy());
//    writeIdOpt(dbWorkflow, FLD_WF.organizationId, workflow.getOrganizationId());
//    writeObjectOpt(dbWorkflow, FLD_WF.version, workflow.getVersion());
//    writeActivities(workflow, dbObjectStack);
//    writeTransitions(workflow, dbObjectStack);
//    writeVariables(workflow, dbObjectStack);
//    return dbWorkflow;
//  }
//  
//  protected void readActivities(Scope scope, BasicDBObject dbScope) {
//    List<BasicDBObject> dbActivities = readList(dbScope, FLD_WF.activitys);
//    if (dbActivities!=null) {
//      for (BasicDBObject dbActivity: dbActivities) {
//        ActivityImpl activity = new ActivityImpl();
//        activity.id = readString(dbActivity, FLD_WF._id);
//        Map<String,Object> activityTypeJson = readObjectMap(dbActivity, FLD_WF.activityType);
//        activity.activityType = jsonService.jsonMapToObject(activityTypeJson, ActivityType.class);
//        readActivities(activity, dbActivity);
//        readVariables(activity, dbActivity);
//        readTransitions(activity, dbActivity);
//        scope.addActivity(activity);
//      }
//    }
//  }
//
//  protected void writeActivities(Scope scope, Stack<Map<String,Object>> dbObjectStack) {
//    if (scope.getActivities()!=null) {
//      for (Activity activity: scope.getActivities()) {
//        
//        Map<String,Object> dbActivity = jsonService.objectToJsonMap(activity);
//        Map<String,Object> dbParentScope = dbObjectStack.peek(); 
//        dbObjectStack.push(dbActivity);
//        // writeString(dbActivity, fields._id, activity.getId());
//        // writeObjectOpt(dbActivity, fields.activityType, activityTypeJson);
//        
//        writeActivities(activity, dbObjectStack);
//        writeTransitions(activity, dbObjectStack);
//        writeVariables(activity, dbObjectStack);
//        dbObjectStack.pop();
//      }
//    }
//  }
//
//  protected void readTransitions(ScopeImpl scope, BasicDBObject dbScope) {
//    List<BasicDBObject> dbTransitions = readList(dbScope, FLD_WF.transitions);
//    if (dbTransitions!=null) {
//      for (BasicDBObject dbTransition: dbTransitions) {
//        TransitionImpl transition = new TransitionImpl();
//        transition.id = readString(dbTransition, FLD_WF._id);
//        transition.fromId = readString(dbTransition, FLD_WF.from);
//        transition.toId = readString(dbTransition, FLD_WF.to);
//        scope.addTransition(transition);
//      }
//    }
//  }
//  
//  protected void writeTransitions(ScopeImpl scope, Stack<BasicDBObject> dbObjectStack) {
//    if (scope.transitionDefinitions!=null) {
//      for (TransitionImpl transition: scope.transitionDefinitions) {
//        BasicDBObject dbParentScope = dbObjectStack.peek(); 
//        BasicDBObject dbTransition = new BasicDBObject();
//        writeStringOpt(dbTransition, FLD_WF._id, transition.id);
//        writeObjectOpt(dbTransition, FLD_WF.from, transition.fromId!=null ? transition.fromId : (transition.from!=null ? transition.from.id : null));
//        writeObjectOpt(dbTransition, FLD_WF.to, transition.toId!=null ? transition.toId : (transition.to!=null ? transition.to.id : null));
//        writeListElementOpt(dbParentScope, FLD_WF.transitions, dbTransition);
//      }
//    }
//  }
//
//  protected void readVariables(ScopeImpl scope, BasicDBObject dbScope) {
//    List<BasicDBObject> dbVariables = readList(dbScope, FLD_WF.variables);
//    if (dbVariables!=null) {
//      scope.variableDefinitions = new ArrayList<>();
//      for (BasicDBObject dbVariable: dbVariables) {
//        VariableImpl variable = new VariableImpl();
//        variable.id = readString(dbVariable, FLD_WF._id);
//        
//        Map<String,Object> dataTypeJson = readObjectMap(dbVariable, FLD_WF.dataType);
//        variable.dataType = jsonService.jsonMapToObject(dataTypeJson, DataType.class);
//
//        Object dbInitialValue = dbVariable.get(FLD_WF.initialValue);
//        variable.initialValue = variable.dataType
//                .convertJsonToInternalValue(dbInitialValue);
//        
//        scope.variableDefinitions.add(variable);
//      }
//    }
//  }
//
//  protected void writeVariables(ScopeImpl scope, Stack<BasicDBObject> dbObjectStack) {
//    if (scope.variableDefinitions!=null) {
//      for (VariableImpl variable: scope.variableDefinitions) {
//        BasicDBObject dbParentScope = dbObjectStack.peek(); 
//        BasicDBObject dbVariable = new BasicDBObject();
//        writeString(dbVariable, FLD_WF._id, variable.id);
//        
//        Map<String,Object> dataTypeJson = jsonService.objectToJsonMap(variable.dataType);
//        writeObjectOpt(dbVariable, FLD_WF.dataType, dataTypeJson);
//
//        if (variable.initialValue!=null) {
//          Object jsonValue = variable.dataType
//                  .convertInternalToJsonValue(variable.initialValue);
//          writeObjectOpt(dbVariable, FLD_WF.initialValue, jsonValue);
//        }
//
//        writeListElementOpt(dbParentScope, FLD_WF.variables, dbVariable);
//      }
//    }
//  }

//
//@Override
//public void addError(String message, Object... messageArgs) {
//  throw new RuntimeException("Should not happen when reading process definitions from db: "+String.format(message, messageArgs));
//}
//
//@Override
//public void addWarning(String message, Object... messageArgs) {
//  // warnings should be ignored during reading of process definition from db.
//}
//
//
//  @Override
//  public ServiceRegistry getServiceRegistry() {
//    return serviceRegistry;
//  }
}
