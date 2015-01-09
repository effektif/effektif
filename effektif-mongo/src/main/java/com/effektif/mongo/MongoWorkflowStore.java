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
import java.util.Stack;

import org.bson.types.ObjectId;
import org.slf4j.Logger;

import com.effektif.workflow.api.query.OrderBy;
import com.effektif.workflow.api.query.OrderDirection;
import com.effektif.workflow.api.query.WorkflowQuery;
import com.effektif.workflow.api.validate.DeployResult;
import com.effektif.workflow.api.workflow.Activity;
import com.effektif.workflow.api.workflow.ParseIssues;
import com.effektif.workflow.api.workflow.Scope;
import com.effektif.workflow.api.workflow.Workflow;
import com.effektif.workflow.impl.WorkflowEngineImpl;
import com.effektif.workflow.impl.WorkflowStore;
import com.effektif.workflow.impl.definition.ActivityImpl;
import com.effektif.workflow.impl.definition.ScopeImpl;
import com.effektif.workflow.impl.definition.TransitionImpl;
import com.effektif.workflow.impl.definition.VariableImpl;
import com.effektif.workflow.impl.definition.WorkflowImpl;
import com.effektif.workflow.impl.definition.WorkflowValidator;
import com.effektif.workflow.impl.json.JsonService;
import com.effektif.workflow.impl.plugin.ActivityType;
import com.effektif.workflow.impl.plugin.Initializable;
import com.effektif.workflow.impl.plugin.ServiceRegistry;
import com.effektif.workflow.impl.type.DataType;
import com.effektif.workflow.impl.util.Exceptions;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.WriteConcern;


public class MongoWorkflowStore extends MongoCollection implements WorkflowStore, Initializable<MongoWorkflowEngineConfiguration> {
  
  public static final Logger log = WorkflowEngineImpl.log;
  
  protected JsonService jsonService;
  protected MongoWorkflowEngineConfiguration.WorkflowFields fields;
  protected WriteConcern writeConcernInsertWorkflow;
  protected MongoCollection workflowVersionsCollection;
  
  @Override
  public void initialize(ServiceRegistry serviceRegistry, MongoWorkflowEngineConfiguration configuration) {
    this.jsonService = serviceRegistry.getService(JsonService.class);
    DB db = serviceRegistry.getService(DB.class);
    this.dbCollection = db.getCollection(configuration.getWorkflowsCollectionName());
    this.isPretty = configuration.isPretty;
    this.fields = configuration.getProcessDefinitionFields();
    this.writeConcernInsertWorkflow = configuration.getWriteConcernInsertWorkflow(this.dbCollection);
  }

  @Override
  public void insertWorkflow(Workflow workflow) {
    workflow.setId(new ObjectId().toString());
    BasicDBObject dbWorkflow = writeWorkflow(workflow);
    
    dbWorkflow.putAll(jsonService.objectToJsonMap(workflow));
    insert(dbWorkflow, writeConcernInsertWorkflow);
    String workflowName = workflow.getName();
    if (workflowName!=null && workflow.getVersion()==null) {
      long maxVersion = 0;
      BasicDBObject dbQuery = new BasicDBObject()
        .append(fields.name, workflowName);
      find()
    }
      List<Workflow> workflows = findWorkflows(new WorkflowQuery().workflowName(workflowName));
      for (Workflow workflow: workflows) {
        
      }
    }
  }

  @Override
  public List<Workflow> findWorkflows(WorkflowQuery query) {
    List<Workflow> workflows = new ArrayList<Workflow>();
    DBCursor cursor = createWorkflowDbCursor(query);
    while (cursor.hasNext()) {
      BasicDBObject dbWorkflow = (BasicDBObject) cursor.next();
      Workflow workflow = readWorkflow(dbWorkflow);
      workflows.add(workflow);
    }
    return workflows;
  }

  @Override
  public void deleteWorkflows(WorkflowQuery query) {
    BasicDBObject dbQuery = createWorkflowDbQuery(query);
    remove(dbQuery);
  }

  @Override
  public String findLatestWorkflowIdByName(String workflowName, String organizationId) {
    Exceptions.checkNotNullParameter(workflowName, "workflowName");
    BasicDBObject dbQuery = new BasicDBObject();
    dbQuery.append(fields.name, new ObjectId(workflowName));
    if (organizationId!=null) {
      dbQuery.append(fields.organizationId, new ObjectId(organizationId));
    }
    BasicDBObject dbFields = new BasicDBObject(fields._id, 1);
    BasicDBObject dbWorkflow = findOne(dbQuery, dbFields);
    return dbWorkflow!=null ? dbWorkflow.get("_id").toString() : null;
  }

  @Override
  public WorkflowImpl loadWorkflowImplById(String workflowId, String organizationId) {
    Exceptions.checkNotNullParameter(workflowId, "workflowId");
    BasicDBObject dbQuery = new BasicDBObject(fields._id, new ObjectId(workflowId));
    BasicDBObject dbWorkflow = findOne(dbQuery, dbFields);
    Workflow workflow = readWorkflow(dbWorkflow);
    WorkflowValidator validator = validateWorkflowInternal(workflow);
    if (log.isDebugEnabled()) {
      log.debug("Deploying workflow");
    }
    DeployResult deployResult = new DeployResult();
    ParseIssues issues = validator.getIssues();
    deployResult.setIssues(issues);
    
    if (!issues.hasErrors()) {
      WorkflowImpl workflowImpl = validator.workflow;

    return null;
  }

  @Override
  public WorkflowImpl createWorkflow() {
    return null;
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
    if (query.getWorkflowId()!=null) {
      dbQuery.append(fields._id, new ObjectId(query.getWorkflowId()));
    }
    if (query.getWorkflowName()!=null) {
      dbQuery.append(fields.name, query.getWorkflowName());
    }
    return dbQuery;
  }

//  @Override
//  public void deleteWorkflow(String workflowId) {
//    BasicDBObject query = new BasicDBObject(fields._id, new ObjectId(workflowId));
//    remove(query);
//  }
//
  public Workflow readWorkflow(BasicDBObject dbWorkflow) {
    Workflow workflow = new WorkflowImpl();
    workflow.setId(readId(dbWorkflow, fields._id));
    workflow.setName(readString(dbWorkflow, fields.name));
    workflow.setDeployedTime(readTime(dbWorkflow, fields.deployedTime));
    workflow.setDeployedBy(readId(dbWorkflow, fields.deployedBy));
    workflow.setOrganizationId(readId(dbWorkflow, fields.organizationId));
    workflow.setVersion(readLong(dbWorkflow, fields.version));
    readActivities(workflow, dbWorkflow);
    readVariables(workflow, dbWorkflow);
    readTransitions(workflow, dbWorkflow);
    return workflow;
  }
  
  public BasicDBObject writeWorkflow(Workflow workflow) {
    BasicDBObject dbWorkflow = new BasicDBObject();
    Stack<BasicDBObject> dbObjectStack = new Stack<>();
    dbObjectStack.push(dbWorkflow);
    writeId(dbWorkflow, fields._id, workflow.getId());
    writeString(dbWorkflow, fields.name, workflow.getName());
    writeTimeOpt(dbWorkflow, fields.deployedTime, workflow.getDeployedTime());
    writeIdOpt(dbWorkflow, fields.deployedBy, workflow.getDeployedBy());
    writeIdOpt(dbWorkflow, fields.organizationId, workflow.getOrganizationId());
    writeObjectOpt(dbWorkflow, fields.version, workflow.getVersion());
    writeActivities(workflow, dbObjectStack);
    writeTransitions(workflow, dbObjectStack);
    writeVariables(workflow, dbObjectStack);
    return dbWorkflow;
  }
  
  protected void readActivities(Scope scope, BasicDBObject dbScope) {
    List<BasicDBObject> dbActivities = readList(dbScope, fields.activitys);
    if (dbActivities!=null) {
      for (BasicDBObject dbActivity: dbActivities) {
        ActivityImpl activity = new ActivityImpl();
        activity.id = readString(dbActivity, fields._id);
        Map<String,Object> activityTypeJson = readObjectMap(dbActivity, fields.activityType);
        activity.activityType = jsonService.jsonMapToObject(activityTypeJson, ActivityType.class);
        readActivities(activity, dbActivity);
        readVariables(activity, dbActivity);
        readTransitions(activity, dbActivity);
        scope.addActivity(activity);
      }
    }
  }

  protected void writeActivities(Scope scope, Stack<Map<String,Object>> dbObjectStack) {
    if (scope.getActivities()!=null) {
      for (Activity activity: scope.getActivities()) {
        
        Map<String,Object> dbActivity = jsonService.objectToJsonMap(activity);
        Map<String,Object> dbParentScope = dbObjectStack.peek(); 
        dbObjectStack.push(dbActivity);
        // writeString(dbActivity, fields._id, activity.getId());
        // writeObjectOpt(dbActivity, fields.activityType, activityTypeJson);
        
        writeActivities(activity, dbObjectStack);
        writeTransitions(activity, dbObjectStack);
        writeVariables(activity, dbObjectStack);
        dbObjectStack.pop();
      }
    }
  }

  protected void readTransitions(ScopeImpl scope, BasicDBObject dbScope) {
    List<BasicDBObject> dbTransitions = readList(dbScope, fields.transitions);
    if (dbTransitions!=null) {
      for (BasicDBObject dbTransition: dbTransitions) {
        TransitionImpl transition = new TransitionImpl();
        transition.id = readString(dbTransition, fields._id);
        transition.fromId = readString(dbTransition, fields.from);
        transition.toId = readString(dbTransition, fields.to);
        scope.addTransition(transition);
      }
    }
  }
  
  protected void writeTransitions(ScopeImpl scope, Stack<BasicDBObject> dbObjectStack) {
    if (scope.transitionDefinitions!=null) {
      for (TransitionImpl transition: scope.transitionDefinitions) {
        BasicDBObject dbParentScope = dbObjectStack.peek(); 
        BasicDBObject dbTransition = new BasicDBObject();
        writeStringOpt(dbTransition, fields._id, transition.id);
        writeObjectOpt(dbTransition, fields.from, transition.fromId!=null ? transition.fromId : (transition.from!=null ? transition.from.id : null));
        writeObjectOpt(dbTransition, fields.to, transition.toId!=null ? transition.toId : (transition.to!=null ? transition.to.id : null));
        writeListElementOpt(dbParentScope, fields.transitions, dbTransition);
      }
    }
  }

  protected void readVariables(ScopeImpl scope, BasicDBObject dbScope) {
    List<BasicDBObject> dbVariables = readList(dbScope, fields.variables);
    if (dbVariables!=null) {
      scope.variableDefinitions = new ArrayList<>();
      for (BasicDBObject dbVariable: dbVariables) {
        VariableImpl variable = new VariableImpl();
        variable.id = readString(dbVariable, fields._id);
        
        Map<String,Object> dataTypeJson = readObjectMap(dbVariable, fields.dataType);
        variable.dataType = jsonService.jsonMapToObject(dataTypeJson, DataType.class);

        Object dbInitialValue = dbVariable.get(fields.initialValue);
        variable.initialValue = variable.dataType
                .convertJsonToInternalValue(dbInitialValue);
        
        scope.variableDefinitions.add(variable);
      }
    }
  }

  protected void writeVariables(ScopeImpl scope, Stack<BasicDBObject> dbObjectStack) {
    if (scope.variableDefinitions!=null) {
      for (VariableImpl variable: scope.variableDefinitions) {
        BasicDBObject dbParentScope = dbObjectStack.peek(); 
        BasicDBObject dbVariable = new BasicDBObject();
        writeString(dbVariable, fields._id, variable.id);
        
        Map<String,Object> dataTypeJson = jsonService.objectToJsonMap(variable.dataType);
        writeObjectOpt(dbVariable, fields.dataType, dataTypeJson);

        if (variable.initialValue!=null) {
          Object jsonValue = variable.dataType
                  .convertInternalToJsonValue(variable.initialValue);
          writeObjectOpt(dbVariable, fields.initialValue, jsonValue);
        }

        writeListElementOpt(dbParentScope, fields.variables, dbVariable);
      }
    }
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
      return fields.deployedTime;
    }
    throw new RuntimeException("Unknown field "+field);
  }

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
