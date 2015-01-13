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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.bson.types.ObjectId;
import org.slf4j.Logger;

import com.effektif.workflow.api.command.RequestContext;
import com.effektif.workflow.api.query.WorkflowInstanceQuery;
import com.effektif.workflow.api.workflowinstance.WorkflowInstance;
import com.effektif.workflow.impl.Time;
import com.effektif.workflow.impl.WorkflowEngineImpl;
import com.effektif.workflow.impl.WorkflowInstanceStore;
import com.effektif.workflow.impl.plugin.Initializable;
import com.effektif.workflow.impl.plugin.ServiceRegistry;
import com.effektif.workflow.impl.workflow.WorkflowImpl;
import com.effektif.workflow.impl.workflowinstance.ActivityInstanceImpl;
import com.effektif.workflow.impl.workflowinstance.LockImpl;
import com.effektif.workflow.impl.workflowinstance.ScopeInstanceImpl;
import com.effektif.workflow.impl.workflowinstance.VariableInstanceImpl;
import com.effektif.workflow.impl.workflowinstance.WorkflowInstanceImpl;
import com.effektif.workflow.impl.workflowinstance.WorkflowInstanceUpdates;
import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DB;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.WriteConcern;


public class MongoWorkflowInstanceStore extends MongoCollection implements WorkflowInstanceStore, Initializable<MongoWorkflowEngineConfiguration> {
  
  public static final Logger log = WorkflowEngineImpl.log;

  protected WorkflowEngineImpl workflowEngine;
  protected WriteConcern writeConcernInsertWorkflowInstance;
  protected WriteConcern writeConcernFlushUpdates;
  
  interface WorkflowInstanceFields {
    String _ID = "_id";
    String ORGANIZATION_ID = "organizationId";
    String WORKFLOW_ID = "workflowId";
    String START = "start";
    String END = "end";
    String DURATION = "duration";
    String ACTIVITY_INSTANCES = "activities";
    String ARCHIVED_ACTIVITY_INSTANCES = "archivedActivities";
    String VARIABLE_INSTANCES = "variables";
    String PARENT = "parent";
    String VARIABLE_ID = "variableId";
    String VALUE = "value";
    String ACTIVITY_ID = "activityId";
    String LOCK = "lock";
    String TIME = "time";
    String OWNER = "owner";
    String UPDATES = "updates";
    String WORK_STATE = "workState";
    String WORK = "work";
    String WORK_ASYNC = "workAsync";
    String CALLER_WORKFLOW_INSTANCE_ID = "callerWorkflowInstanceId";
    String CALLER_ACTIVITY_INSTANCE_ID = "callerActivityInstanceId";
    String CALLED_WORKFLOW_INSTANCE_ID = "calledWorkflowInstanceId";
  }

  
  public MongoWorkflowInstanceStore() {
  }

  public MongoWorkflowInstanceStore(ServiceRegistry serviceRegistry) {
  }
  
  @Override
  public void initialize(ServiceRegistry serviceRegistry, MongoWorkflowEngineConfiguration configuration) {
    DB db = serviceRegistry.getService(DB.class);
    this.dbCollection = db.getCollection(configuration.workflowInstancesCollectionName);
    this.isPretty = configuration.isPretty;
    this.workflowEngine = serviceRegistry.getService(WorkflowEngineImpl.class);
    this.writeConcernInsertWorkflowInstance = configuration.getWriteConcernInsertWorkflowInstance(this.dbCollection);
    this.writeConcernFlushUpdates = configuration.getWriteConcernFlushUpdates(this.dbCollection);
  }

  @Override
  public void insertWorkflowInstance(WorkflowInstanceImpl workflowInstance) {
    BasicDBObject dbProcessInstance = writeProcessInstance(workflowInstance);
    insert(dbProcessInstance, writeConcernInsertWorkflowInstance);
    workflowInstance.trackUpdates(false);
  }

  @Override
  public void flush(WorkflowInstanceImpl workflowInstance) {
    if (log.isDebugEnabled())
      log.debug("Flushing...");
    
    WorkflowInstanceUpdates updates = workflowInstance.getUpdates();
    
    DBObject query = BasicDBObjectBuilder.start()
            .add(WorkflowInstanceFields._ID,  new ObjectId(workflowInstance.id))
            .add(WorkflowInstanceFields.LOCK,  writeLock(workflowInstance.lock))
            .get();
    
    BasicDBObject sets = new BasicDBObject();
    BasicDBObject unsets = new BasicDBObject();
    BasicDBObject update = new BasicDBObject();

    if (updates.isEndChanged) {
      if (log.isDebugEnabled())
        log.debug("  Workflow instance ended");
      sets.append(WorkflowInstanceFields.END, workflowInstance.end);
      sets.append(WorkflowInstanceFields.DURATION, workflowInstance.duration);
    }
    // MongoDB can't combine updates of array elements together with 
    // adding elements to that array.  That's why we overwrite the whole
    // activity instance array when an update happened in there.
    // We do archive the ended (and joined) activity instances into a separate collection 
    // that doesn't have to be loaded.
    if (updates.isActivityInstancesChanged) {
      if (log.isDebugEnabled())
        log.debug("  Activity instances changed");
      List<BasicDBObject> activityInstances = new ArrayList<>();
      List<BasicDBObject> archivedActivityInstances = new ArrayList<>();
      collectActivities(workflowInstance, activityInstances, archivedActivityInstances);
      sets.append(WorkflowInstanceFields.ACTIVITY_INSTANCES, activityInstances);
      if (!archivedActivityInstances.isEmpty()) {
        update.append("$push", new BasicDBObject(WorkflowInstanceFields.ARCHIVED_ACTIVITY_INSTANCES, archivedActivityInstances));
      }
    } else {
      if (log.isDebugEnabled())
        log.debug("  No activity instances changed");
    }
    
    if (updates.isVariableInstancesChanged) {
      if (log.isDebugEnabled())
        log.debug("  Variable instances changed");
      writeVariables(sets, workflowInstance);
    } else {
      if (log.isDebugEnabled())
        log.debug("  No variable instances changed");
    }

    if (updates.isWorkChanged) {
      if (log.isDebugEnabled())
        log.debug("  Work changed");
      List<ObjectId> work = writeWork(workflowInstance.work);
      if (work!=null) {
        sets.put(WorkflowInstanceFields.WORK, work);
      } else {
        unsets.put(WorkflowInstanceFields.WORK, 1);
      }
    } else {
      if (log.isDebugEnabled())
        log.debug("  No work changed");
    }

    if (updates.isAsyncWorkChanged) {
      if (log.isDebugEnabled())
        log.debug("  Aync work changed");
      List<ObjectId> workAsync = writeWork(workflowInstance.workAsync);
      if (workAsync!=null) {
        sets.put(WorkflowInstanceFields.WORK_ASYNC, workAsync);
      } else {
        unsets.put(WorkflowInstanceFields.WORK_ASYNC, 1);
      }
    } else {
      if (log.isDebugEnabled())
        log.debug("  No async work changed");
    }

    if (!sets.isEmpty()) {
      update.append("$set", sets);
    } else {
      if (log.isDebugEnabled())
        log.debug("  No sets");
    }
    if (!unsets.isEmpty()) {
      update.append("$unset", unsets);
    } else {
      if (log.isDebugEnabled())
        log.debug("  No unsets");
    }
    
    if (!update.isEmpty()) {
      update(query, update, false, false, writeConcernFlushUpdates);
    } else {
      if (log.isDebugEnabled())
        log.debug("  Nothing to flush");
    }
    
    // reset the update tracking as all changes have been saved
    workflowInstance.trackUpdates(false);
  }

  @Override
  public void flushAndUnlock(WorkflowInstanceImpl processInstance) {
    processInstance.lock = null;
    BasicDBObject dbProcessInstance = writeProcessInstance(processInstance);
    saveProcessInstance(dbProcessInstance);
    processInstance.trackUpdates(false);
  }

  @Override
  public List<WorkflowInstance> findWorkflowInstances(WorkflowInstanceQuery workflowInstanceQuery, RequestContext requestContext) {
    BasicDBObject query = buildQuery(workflowInstanceQuery, requestContext);
    DBCursor workflowInstanceCursor = find(query);
    List<WorkflowInstanceImpl> workflowInstances = new ArrayList<>();
    while (workflowInstanceCursor.hasNext()) {
      BasicDBObject dbWorkflowInstance = (BasicDBObject) workflowInstanceCursor.next();
      WorkflowInstanceImpl workflowInstance = readWorkflowInstance(dbWorkflowInstance, requestContext);
      workflowInstances.add(workflowInstance);
    }
    return workflowInstances;
  }
  

  @Override
  public long countWorkflowInstances(WorkflowInstanceQuery workflowInstanceQueryImpl) {
    return 0;
  }

  @Override
  public void deleteWorkflowInstances(WorkflowInstanceQuery workflowInstanceQuery) {
    BasicDBObject query = buildQuery(workflowInstanceQuery);
    remove(query);
  }

  protected BasicDBObject buildQuery(WorkflowInstanceQuery workflowInstanceQuery, RequestContext requestContext) {
    BasicDBObject query = new BasicDBObject();
    if (RequestContext.hasOrganizationId(requestContext)) {
      query.append(WorkflowInstanceFields.ORGANIZATION_ID, requestContext.getOrganizationId());
    }
    if (workflowInstanceQuery.getWorkflowInstanceId()!=null) {
      query.append(WorkflowInstanceFields._ID, workflowInstanceQuery.getWorkflowInstanceId());
    }
    if (workflowInstanceQuery.getActivityInstanceId()!=null) {
      query.append(WorkflowInstanceFields.ACTIVITY_INSTANCES+"."+WorkflowInstanceFields._ID, workflowInstanceQuery.getActivityInstanceId());
    }
    return query;
  }
  
  public void saveProcessInstance(BasicDBObject dbProcessInstance) {
    save(dbProcessInstance, writeConcernInsertWorkflowInstance);
  }
  
  public WorkflowInstanceImpl lockWorkflowInstance(WorkflowInstanceQuery processInstanceQuery) {
    BasicDBObjectBuilder builder = BasicDBObjectBuilder.start();
    if (processInstanceQuery.getWorkflowInstanceId()!=null) {
      builder.add(WorkflowInstanceFields._ID, processInstanceQuery.getWorkflowInstanceId());
    }
    if (processInstanceQuery.getActivityInstanceId()!=null) {
      builder.add(WorkflowInstanceFields.ACTIVITY_INSTANCES+"."+WorkflowInstanceFields._ID, processInstanceQuery.getActivityInstanceId());
    }

    DBObject query = builder 
            .push(WorkflowInstanceFields.LOCK)
              .add("$exists", false)
            .pop()
            .get(); 
    DBObject update = BasicDBObjectBuilder.start()
            .push("$set")
            .push(WorkflowInstanceFields.LOCK)
              .add(WorkflowInstanceFields.TIME, Time.now().toDate())
              .add(WorkflowInstanceFields.OWNER, workflowEngine.getId())
            .pop()
          .pop()
          .get();
    DBObject retrieveFields = new BasicDBObject()
          .append(WorkflowInstanceFields.ARCHIVED_ACTIVITY_INSTANCES, false);
    
    BasicDBObject dbProcessInstance = findAndModify(query, update, retrieveFields);
    if (dbProcessInstance==null) {
      return null;
    }

    WorkflowInstanceImpl workflowInstance = readWorkflowInstance(dbProcessInstance);
    workflowInstance.trackUpdates(false);
    return workflowInstance;
  }
  
  public BasicDBObject writeProcessInstance(WorkflowInstanceImpl workflowInstance) {
    BasicDBObject dbProcess = new BasicDBObject();
    writeId(dbProcess, WorkflowInstanceFields._ID, workflowInstance.id);
    writeStringOpt(dbProcess, WorkflowInstanceFields.ORGANIZATION_ID, workflowInstance.organizationId);
    writeId(dbProcess, WorkflowInstanceFields.WORKFLOW_ID, workflowInstance.workflow.id);
    writeIdOpt(dbProcess, WorkflowInstanceFields.CALLER_WORKFLOW_INSTANCE_ID, workflowInstance.callerWorkflowInstanceId);
    writeIdOpt(dbProcess, WorkflowInstanceFields.CALLER_ACTIVITY_INSTANCE_ID, workflowInstance.callerActivityInstanceId);
    writeTimeOpt(dbProcess, WorkflowInstanceFields.START, workflowInstance.start);
    writeTimeOpt(dbProcess, WorkflowInstanceFields.END, workflowInstance.end);
    writeLongOpt(dbProcess, WorkflowInstanceFields.DURATION, workflowInstance.duration);
    writeObjectOpt(dbProcess, WorkflowInstanceFields.LOCK, writeLock(workflowInstance.lock));
    List<BasicDBObject> activityInstances = new ArrayList<>();
    List<BasicDBObject> archivedActivityInstances = new ArrayList<>();
    collectActivities(workflowInstance, activityInstances, archivedActivityInstances);
    writeObjectOpt(dbProcess, WorkflowInstanceFields.ACTIVITY_INSTANCES, activityInstances);
    if (!archivedActivityInstances.isEmpty()) {
      writeObjectOpt(dbProcess, WorkflowInstanceFields.ARCHIVED_ACTIVITY_INSTANCES, archivedActivityInstances);
    }
    writeObjectOpt(dbProcess, WorkflowInstanceFields.WORK, writeWork(workflowInstance.work));
    writeObjectOpt(dbProcess, WorkflowInstanceFields.WORK_ASYNC, writeWork(workflowInstance.workAsync));
    return dbProcess;
  }
  
  protected List<ObjectId> writeWork(Queue<ActivityInstanceImpl> workQueue) {
    List<ObjectId> workActivityInstanceIds = null;
    if (workQueue!=null && !workQueue.isEmpty()) {
      workActivityInstanceIds = new ArrayList<ObjectId>();
      for (ActivityInstanceImpl workActivityInstance: workQueue) {
        workActivityInstanceIds.add(new ObjectId(workActivityInstance.id));
      }
    }
    return workActivityInstanceIds;
  }

  public WorkflowInstanceImpl readWorkflowInstance(BasicDBObject dbWorkflowInstance, RequestContext requestContext) {
    WorkflowInstanceImpl workflowInstance = new WorkflowInstanceImpl();
    workflowInstance.workflowEngine = workflowEngine;
    workflowInstance.requestContext = requestContext;
    workflowInstance.workflowInstance = workflowInstance;
    workflowInstance.id = readId(dbWorkflowInstance, WorkflowInstanceFields._ID);
    workflowInstance.organizationId = readString(dbWorkflowInstance, WorkflowInstanceFields.ORGANIZATION_ID);
    workflowInstance.callerWorkflowInstanceId = readId(dbWorkflowInstance, WorkflowInstanceFields.CALLER_WORKFLOW_INSTANCE_ID);
    workflowInstance.callerActivityInstanceId = readId(dbWorkflowInstance, WorkflowInstanceFields.CALLER_ACTIVITY_INSTANCE_ID);
    workflowInstance.start = readTime(dbWorkflowInstance, WorkflowInstanceFields.START);
    workflowInstance.end = readTime(dbWorkflowInstance, WorkflowInstanceFields.END);
    workflowInstance.duration = readLong(dbWorkflowInstance, WorkflowInstanceFields.DURATION);
    workflowInstance.lock = readLock((BasicDBObject) dbWorkflowInstance.get(WorkflowInstanceFields.LOCK));
    WorkflowImpl workflow = workflowEngine.newWorkflowQuery()
            .representation(Representation.EXECUTABLE)
            .id(workflowInstance.workflowId)
            .current();
    if (workflow!=null) {
      workflowInstance.workflow = workflow;
      workflowInstance.workflowId = workflow!=null ? workflow.id : null;
      workflowInstance.scopeDefinition = workflowInstance.workflow;
    } else {
      throw new RuntimeException("No workflow for instance "+workflowInstance.id);
    }
    
    Map<Object, ActivityInstanceImpl> allActivityInstances = new LinkedHashMap<>();
    Map<Object, Object> parentIds = new HashMap<>();
    List<BasicDBObject> dbActivityInstances = readList(dbWorkflowInstance, WorkflowInstanceFields.ACTIVITY_INSTANCES);
    if (dbActivityInstances!=null) {
      for (BasicDBObject dbActivityInstance: dbActivityInstances) {
        ActivityInstanceImpl activityInstance = readActivityInstance(workflowInstance, dbActivityInstance, requestContext);
        allActivityInstances.put(activityInstance.id, activityInstance);
        parentIds.put(activityInstance.id, dbActivityInstance.get(WorkflowInstanceFields.PARENT));
      }
    }
    
    for (ActivityInstanceImpl activityInstance: allActivityInstances.values()) {
      Object parentId = parentIds.get(activityInstance.id);
      activityInstance.parent = (parentId!=null ? allActivityInstances.get(parentId.toString()) : workflowInstance);
      activityInstance.parent.addActivityInstance(activityInstance);
    }
    
    workflowInstance.variableInstances = readVariableInstances(dbWorkflowInstance, workflowInstance);
    workflowInstance.work = readWork(dbWorkflowInstance, WorkflowInstanceFields.WORK, workflowInstance);
    workflowInstance.workAsync = readWork(dbWorkflowInstance, WorkflowInstanceFields.WORK_ASYNC, workflowInstance);
    
    return workflowInstance;
  }

  @SuppressWarnings("unchecked")
  protected Queue<ActivityInstanceImpl> readWork(BasicDBObject dbWorkflowInstance, String fieldName, WorkflowInstanceImpl workflowInstance) {
    Queue<ActivityInstanceImpl> workQueue = null;
    List<ObjectId> workActivityInstanceIds = (List<ObjectId>) dbWorkflowInstance.get(fieldName);
    if (workActivityInstanceIds!=null) {
      workQueue = new LinkedList<>();
      for (ObjectId workActivityInstanceId: workActivityInstanceIds) {
        ActivityInstanceImpl workActivityInstance = workflowInstance.findActivityInstance(workActivityInstanceId.toString());
        workQueue.add(workActivityInstance);
      }
    }
    return workQueue;
  }

  private List<VariableInstanceImpl> readVariableInstances(BasicDBObject dbWorkflowInstance, ScopeInstanceImpl parent) {
    List<BasicDBObject> dbVariableInstances = readList(dbWorkflowInstance, WorkflowInstanceFields.VARIABLE_INSTANCES);
    if (dbVariableInstances!=null) {
      for (BasicDBObject dbVariableInstance: dbVariableInstances) {
        VariableInstanceImpl variableInstance = new VariableInstanceImpl();
        variableInstance.processEngine = workflowEngine;
        variableInstance.processInstance = parent.workflowInstance;
        variableInstance.id = readId(dbVariableInstance, WorkflowInstanceFields._ID);
        variableInstance.variableDefinitionId = readString(dbVariableInstance, WorkflowInstanceFields.VARIABLE_ID);
        WorkflowImpl workflow = parent.workflowInstance.workflow;
        if (workflow!=null) {
          variableInstance.variableDefinition = workflow.findVariable(variableInstance.variableDefinitionId);
          variableInstance.variableDefinitionId = variableInstance.variableDefinition.id;
          variableInstance.dataType = variableInstance.variableDefinition.dataType;
          variableInstance.value = variableInstance.dataType.convertJsonToInternalValue(dbVariableInstance.get(WorkflowInstanceFields.VALUE));
        }
        parent.addVariableInstance(variableInstance);
      }
    }

    return null;
  }

  protected BasicDBObject writeLock(LockImpl lock) {
    if (lock==null) {
      return null;
    }
    BasicDBObject dbLock = new BasicDBObject();
    writeTimeOpt(dbLock, WorkflowInstanceFields.TIME, lock.time);
    writeObjectOpt(dbLock, WorkflowInstanceFields.OWNER, lock.owner);
    return dbLock;
  }
  
  protected LockImpl readLock(BasicDBObject dbLock) {
    if (dbLock==null) {
      return null;
    }
    LockImpl lock = new LockImpl();
    lock.owner = readString(dbLock, WorkflowInstanceFields.OWNER);
    lock.time = readTime(dbLock, WorkflowInstanceFields.TIME);
    return lock;
  }
  
  protected void collectActivities(ScopeInstanceImpl scopeInstance, List<BasicDBObject> dbActivityInstances, List<BasicDBObject> dbArchivedActivityInstances) {
    if (scopeInstance.activityInstances!=null) {
      List<ActivityInstanceImpl> activeActivityInstances = new ArrayList<>(); 
      for (ActivityInstanceImpl activity: scopeInstance.activityInstances) {
        BasicDBObject dbActivity = writeActivityInstance(activity);
        if (activity.workState!=null) { // null means ready to be archived
          dbActivityInstances.add(dbActivity);
          activeActivityInstances.add(activity);
        } else {
          dbArchivedActivityInstances.add(dbActivity);
        }
        collectActivities(activity, dbActivityInstances, dbArchivedActivityInstances);
      }
      scopeInstance.activityInstances = activeActivityInstances;
    }
  }

  protected BasicDBObject writeActivityInstance(ActivityInstanceImpl activityInstance) {
    String parentId = (activityInstance.parent.isProcessInstance() ? null : activityInstance.parent.getId());
    BasicDBObject dbActivity = new BasicDBObject();
    writeId(dbActivity, WorkflowInstanceFields._ID, activityInstance.id);
    writeStringOpt(dbActivity, WorkflowInstanceFields.ACTIVITY_ID, activityInstance.activityId);
    writeStringOpt(dbActivity, WorkflowInstanceFields.WORK_STATE, activityInstance.workState);
    writeIdOpt(dbActivity, WorkflowInstanceFields.PARENT, parentId);
    writeIdOpt(dbActivity, WorkflowInstanceFields.CALLED_WORKFLOW_INSTANCE_ID, activityInstance.calledWorkflowInstanceId);
    writeTimeOpt(dbActivity, WorkflowInstanceFields.START, activityInstance.start);
    writeTimeOpt(dbActivity, WorkflowInstanceFields.END, activityInstance.end);
    writeLongOpt(dbActivity, WorkflowInstanceFields.DURATION, activityInstance.duration);
    return dbActivity;
  }
  
  protected ActivityInstanceImpl readActivityInstance(WorkflowInstanceImpl processInstance, BasicDBObject dbActivityInstance, RequestContext requestContext) {
    ActivityInstanceImpl activityInstance = new ActivityInstanceImpl();
    activityInstance.id = readId(dbActivityInstance, WorkflowInstanceFields._ID);
    activityInstance.start = readTime(dbActivityInstance, WorkflowInstanceFields.START);
    activityInstance.end = readTime(dbActivityInstance, WorkflowInstanceFields.END);
    activityInstance.calledWorkflowInstanceId = readId(dbActivityInstance, WorkflowInstanceFields.CALLED_WORKFLOW_INSTANCE_ID);
    activityInstance.duration = readLong(dbActivityInstance, WorkflowInstanceFields.DURATION);
    activityInstance.workState = readString(dbActivityInstance, WorkflowInstanceFields.WORK_STATE);
    activityInstance.activityId = readString(dbActivityInstance, WorkflowInstanceFields.ACTIVITY_ID);
    activityInstance.workflowEngine = workflowEngine;
    activityInstance.requestContext = requestContext;
    WorkflowImpl workflow = processInstance.workflow;
    if (workflow!=null) {
      activityInstance.workflow = workflow;
      activityInstance.activityDefinition = workflow.findActivity(activityInstance.activityId);
      activityInstance.activityId = activityInstance.activityDefinition.id;
      activityInstance.scopeDefinition = activityInstance.activityDefinition;
    }
    activityInstance.workflowInstance = processInstance;
    activityInstance.variableInstances = readVariableInstances(dbActivityInstance, activityInstance);
    return activityInstance;
  }

  protected void writeVariables(BasicDBObject dbScope, ScopeInstanceImpl scope) {
    if (scope.variableInstances!=null) {
      ScopeInstanceImpl parent = scope.getParent();
      String parentId = (parent!=null ? parent.getId() : null);
      for (VariableInstanceImpl variable: scope.variableInstances) {
        BasicDBObject dbVariable = new BasicDBObject();
        writeId(dbVariable, WorkflowInstanceFields._ID, variable.id);
        writeString(dbVariable, WorkflowInstanceFields.VARIABLE_ID, variable.variableDefinitionId);
        writeIdOpt(dbVariable, WorkflowInstanceFields.PARENT, parentId);
        Object jsonValue = variable.dataType.convertInternalToJsonValue(variable.value);
        writeObjectOpt(dbVariable, WorkflowInstanceFields.VALUE, jsonValue);
        writeListElementOpt(dbScope, WorkflowInstanceFields.VARIABLE_INSTANCES, dbVariable);
      }
    }
  }
  
  public WorkflowEngineImpl getProcessEngine() {
    return workflowEngine;
  }
  
  public MongoWorkflowEngineConfiguration.WorkflowInstanceFields getFields() {
    return fields;
  }

  public WriteConcern getWriteConcernStoreProcessInstance() {
    return writeConcernInsertWorkflowInstance;
  }
  
  public WriteConcern getWriteConcernFlushUpdates() {
    return writeConcernFlushUpdates;
  }
}
