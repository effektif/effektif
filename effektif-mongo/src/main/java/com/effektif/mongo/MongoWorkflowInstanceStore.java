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

import com.effektif.impl.Time;
import com.effektif.impl.WorkflowEngineImpl;
import com.effektif.impl.WorkflowInstanceStore;
import com.effektif.impl.definition.WorkflowImpl;
import com.effektif.impl.instance.ActivityInstanceImpl;
import com.effektif.impl.instance.LockImpl;
import com.effektif.impl.instance.ScopeInstanceImpl;
import com.effektif.impl.instance.VariableInstanceImpl;
import com.effektif.impl.instance.WorkflowInstanceImpl;
import com.effektif.impl.instance.WorkflowInstanceUpdates;
import com.effektif.impl.plugin.ServiceRegistry;
import com.effektif.workflow.api.query.WorkflowInstanceQuery;
import com.effektif.workflow.api.query.WorkflowQuery.Representation;
import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.WriteConcern;


public class MongoWorkflowInstanceStore extends MongoCollection implements WorkflowInstanceStore {
  
  public static final Logger log = MongoWorkflowEngine.log;

  protected WorkflowEngineImpl processEngine;
  protected MongoWorkflowEngineConfiguration.WorkflowInstanceFields fields;
  protected WriteConcern writeConcernStoreProcessInstance;
  protected WriteConcern writeConcernFlushUpdates;
  
  public MongoWorkflowInstanceStore() {
  }

  public MongoWorkflowInstanceStore(ServiceRegistry serviceRegistry) {
  }

  @Override
  public String createWorkflowInstanceId(WorkflowImpl processDefinition) {
    return new ObjectId().toString();
  }

  @Override
  public String createActivityInstanceId() {
    return new ObjectId().toString();
  }

  @Override
  public String createVariableInstanceId() {
    return new ObjectId().toString();
  }

  @Override
  public void insertWorkflowInstance(WorkflowInstanceImpl workflowInstance) {
    BasicDBObject dbProcessInstance = writeProcessInstance(workflowInstance);
    insert(dbProcessInstance, writeConcernStoreProcessInstance);
    workflowInstance.trackUpdates(false);
  }

  @Override
  public void flush(WorkflowInstanceImpl workflowInstance) {
    if (log.isDebugEnabled())
      log.debug("Flushing...");
    
    WorkflowInstanceUpdates updates = workflowInstance.getUpdates();
    
    DBObject query = BasicDBObjectBuilder.start()
            .add(fields._id,  new ObjectId(workflowInstance.id))
            .add(fields.lock,  writeLock(workflowInstance.lock))
            .get();
    
    BasicDBObject sets = new BasicDBObject();
    BasicDBObject unsets = new BasicDBObject();
    BasicDBObject update = new BasicDBObject();

    if (updates.isEndChanged) {
      if (log.isDebugEnabled())
        log.debug("  Workflow instance ended");
      sets.append(fields.end, workflowInstance.end);
      sets.append(fields.duration, workflowInstance.duration);
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
      sets.append(fields.activityInstances, activityInstances);
      if (!archivedActivityInstances.isEmpty()) {
        update.append("$push", new BasicDBObject(fields.archivedActivityInstances, archivedActivityInstances));
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
        sets.put(fields.work, work);
      } else {
        unsets.put(fields.work, 1);
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
        sets.put(fields.workAsync, workAsync);
      } else {
        unsets.put(fields.workAsync, 1);
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
  public List<WorkflowInstanceImpl> findWorkflowInstances(WorkflowInstanceQuery workflowInstanceQuery) {
    BasicDBObject query = buildQuery(workflowInstanceQuery);
    DBCursor workflowInstanceCursor = find(query);
    List<WorkflowInstanceImpl> workflowInstances = new ArrayList<>();
    while (workflowInstanceCursor.hasNext()) {
      BasicDBObject dbWorkflowInstance = (BasicDBObject) workflowInstanceCursor.next();
      WorkflowInstanceImpl workflowInstance = readProcessInstance(dbWorkflowInstance);
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

  protected BasicDBObject buildQuery(WorkflowInstanceQuery workflowInstanceQuery) {
    BasicDBObject query = new BasicDBObject();
    if (workflowInstanceQuery.workflowInstanceId!=null) {
      query.append(fields._id, new ObjectId(workflowInstanceQuery.workflowInstanceId));
    }
    if (workflowInstanceQuery.activityInstanceId!=null) {
      query.append(fields.activityInstances+"."+fields._id, new ObjectId(workflowInstanceQuery.workflowInstanceId));
    }
    return query;
  }
  
  public void saveProcessInstance(BasicDBObject dbProcessInstance) {
    save(dbProcessInstance, writeConcernStoreProcessInstance);
  }
  
  public WorkflowInstanceImpl lockWorkflowInstance(WorkflowInstanceQuery processInstanceQuery) {
    BasicDBObjectBuilder builder = BasicDBObjectBuilder.start();
    if (processInstanceQuery.workflowInstanceId!=null) {
      builder.add(fields._id, new ObjectId(processInstanceQuery.workflowInstanceId));
    }
    if (processInstanceQuery.activityInstanceId!=null) {
      builder.add(fields.activityInstances+"."+fields._id, new ObjectId(processInstanceQuery.activityInstanceId));
    }

    DBObject query = builder 
            .push(fields.lock)
              .add("$exists", false)
            .pop()
            .get(); 
    DBObject update = BasicDBObjectBuilder.start()
            .push("$set")
            .push(fields.lock)
              .add(fields.time, Time.now().toDate())
              .add(fields.owner, processEngine.getId())
            .pop()
          .pop()
          .get();
    DBObject retrieveFields = new BasicDBObject()
          .append(fields.archivedActivityInstances, false);
    
    BasicDBObject dbProcessInstance = findAndModify(query, update, retrieveFields);
    if (dbProcessInstance==null) {
      return null;
    }

    WorkflowInstanceImpl workflowInstance = readProcessInstance(dbProcessInstance);
    workflowInstance.trackUpdates(false);
    return workflowInstance;
  }
  
  public BasicDBObject writeProcessInstance(WorkflowInstanceImpl workflowInstance) {
    BasicDBObject dbProcess = new BasicDBObject();
    writeId(dbProcess, fields._id, workflowInstance.id);
    writeStringOpt(dbProcess, fields.organizationId, workflowInstance.organizationId);
    writeId(dbProcess, fields.workflowId, workflowInstance.workflow.id);
    writeIdOpt(dbProcess, fields.callerWorkflowInstanceId, workflowInstance.callerWorkflowInstanceId);
    writeIdOpt(dbProcess, fields.callerActivityInstanceId, workflowInstance.callerActivityInstanceId);
    writeTimeOpt(dbProcess, fields.start, workflowInstance.start);
    writeTimeOpt(dbProcess, fields.end, workflowInstance.end);
    writeLongOpt(dbProcess, fields.duration, workflowInstance.duration);
    writeObjectOpt(dbProcess, fields.lock, writeLock(workflowInstance.lock));
    List<BasicDBObject> activityInstances = new ArrayList<>();
    List<BasicDBObject> archivedActivityInstances = new ArrayList<>();
    collectActivities(workflowInstance, activityInstances, archivedActivityInstances);
    writeObjectOpt(dbProcess, fields.activityInstances, activityInstances);
    if (!archivedActivityInstances.isEmpty()) {
      writeObjectOpt(dbProcess, fields.archivedActivityInstances, archivedActivityInstances);
    }
    writeObjectOpt(dbProcess, fields.work, writeWork(workflowInstance.work));
    writeObjectOpt(dbProcess, fields.workAsync, writeWork(workflowInstance.workAsync));
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

  public WorkflowInstanceImpl readProcessInstance(BasicDBObject dbWorkflowInstance) {
    WorkflowInstanceImpl workflowInstance = new WorkflowInstanceImpl();
    workflowInstance.workflowEngine = processEngine;
    workflowInstance.workflowInstance = workflowInstance;
    workflowInstance.id = readId(dbWorkflowInstance, fields._id);
    workflowInstance.organizationId = readString(dbWorkflowInstance, fields.organizationId);
    workflowInstance.callerWorkflowInstanceId = readId(dbWorkflowInstance, fields.callerWorkflowInstanceId);
    workflowInstance.callerActivityInstanceId = readId(dbWorkflowInstance, fields.callerActivityInstanceId);
    workflowInstance.start = readTime(dbWorkflowInstance, fields.start);
    workflowInstance.end = readTime(dbWorkflowInstance, fields.end);
    workflowInstance.duration = readLong(dbWorkflowInstance, fields.duration);
    workflowInstance.lock = readLock((BasicDBObject) dbWorkflowInstance.get(fields.lock));
    workflowInstance.workflowId = readId(dbWorkflowInstance, fields.workflowId);
    WorkflowImpl workflow = processEngine.newWorkflowQuery()
            .representation(Representation.EXECUTABLE)
            .id(workflowInstance.workflowId)
            .get();
    if (workflow!=null) {
      workflowInstance.workflow = workflow;
      workflowInstance.workflowId = workflow!=null ? workflow.id : null;
      workflowInstance.scopeDefinition = workflowInstance.workflow;
    } else {
      throw new RuntimeException("No workflow for instance "+workflowInstance.id);
    }
    
    Map<Object, ActivityInstanceImpl> allActivityInstances = new LinkedHashMap<>();
    Map<Object, Object> parentIds = new HashMap<>();
    List<BasicDBObject> dbActivityInstances = readList(dbWorkflowInstance, fields.activityInstances);
    if (dbActivityInstances!=null) {
      for (BasicDBObject dbActivityInstance: dbActivityInstances) {
        ActivityInstanceImpl activityInstance = readActivityInstance(workflowInstance, dbActivityInstance);
        allActivityInstances.put(activityInstance.id, activityInstance);
        parentIds.put(activityInstance.id, dbActivityInstance.get(fields.parent));
      }
    }
    
    for (ActivityInstanceImpl activityInstance: allActivityInstances.values()) {
      Object parentId = parentIds.get(activityInstance.id);
      activityInstance.parent = (parentId!=null ? allActivityInstances.get(parentId.toString()) : workflowInstance);
      activityInstance.parent.addActivityInstance(activityInstance);
    }
    
    workflowInstance.variableInstances = readVariableInstances(dbWorkflowInstance, workflowInstance);
    workflowInstance.work = readWork(dbWorkflowInstance, fields.work, workflowInstance);
    workflowInstance.workAsync = readWork(dbWorkflowInstance, fields.workAsync, workflowInstance);
    
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
    List<BasicDBObject> dbVariableInstances = readList(dbWorkflowInstance, fields.variableInstances);
    if (dbVariableInstances!=null) {
      for (BasicDBObject dbVariableInstance: dbVariableInstances) {
        VariableInstanceImpl variableInstance = new VariableInstanceImpl();
        variableInstance.processEngine = processEngine;
        variableInstance.processInstance = parent.workflowInstance;
        variableInstance.id = readId(dbVariableInstance, fields._id);
        variableInstance.variableDefinitionId = readString(dbVariableInstance, fields.variableId);
        WorkflowImpl workflow = parent.workflowInstance.workflow;
        if (workflow!=null) {
          variableInstance.variableDefinition = workflow.findVariable(variableInstance.variableDefinitionId);
          variableInstance.variableDefinitionId = variableInstance.variableDefinition.id;
          variableInstance.dataType = variableInstance.variableDefinition.dataType;
          variableInstance.value = variableInstance.dataType.convertJsonToInternalValue(dbVariableInstance.get(fields.value));
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
    writeTimeOpt(dbLock, fields.time, lock.time);
    writeObjectOpt(dbLock, fields.owner, lock.owner);
    return dbLock;
  }
  
  protected LockImpl readLock(BasicDBObject dbLock) {
    if (dbLock==null) {
      return null;
    }
    LockImpl lock = new LockImpl();
    lock.owner = readString(dbLock, fields.owner);
    lock.time = readTime(dbLock, fields.time);
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
    writeId(dbActivity, fields._id, activityInstance.id);
    writeStringOpt(dbActivity, fields.activityId, activityInstance.activityId);
    writeStringOpt(dbActivity, fields.workState, activityInstance.workState);
    writeIdOpt(dbActivity, fields.parent, parentId);
    writeIdOpt(dbActivity, fields.calledWorkflowInstanceId, activityInstance.calledWorkflowInstanceId);
    writeTimeOpt(dbActivity, fields.start, activityInstance.start);
    writeTimeOpt(dbActivity, fields.end, activityInstance.end);
    writeLongOpt(dbActivity, fields.duration, activityInstance.duration);
    return dbActivity;
  }
  
  protected ActivityInstanceImpl readActivityInstance(WorkflowInstanceImpl processInstance, BasicDBObject dbActivityInstance) {
    ActivityInstanceImpl activityInstance = new ActivityInstanceImpl();
    activityInstance.id = readId(dbActivityInstance, fields._id);
    activityInstance.start = readTime(dbActivityInstance, fields.start);
    activityInstance.end = readTime(dbActivityInstance, fields.end);
    activityInstance.calledWorkflowInstanceId = readId(dbActivityInstance, fields.calledWorkflowInstanceId);
    activityInstance.duration = readLong(dbActivityInstance, fields.duration);
    activityInstance.workState = readString(dbActivityInstance, fields.workState);
    activityInstance.activityId = readString(dbActivityInstance, fields.activityId);
    activityInstance.workflowEngine = processEngine;
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
        writeId(dbVariable, fields._id, variable.id);
        writeString(dbVariable, fields.variableId, variable.variableDefinitionId);
        writeIdOpt(dbVariable, fields.parent, parentId);
        Object jsonValue = variable.dataType.convertInternalToJsonValue(variable.value);
        writeObjectOpt(dbVariable, fields.value, jsonValue);
        writeListElementOpt(dbScope, fields.variableInstances, dbVariable);
      }
    }
  }
  
  public WorkflowEngineImpl getProcessEngine() {
    return processEngine;
  }
  
  public MongoWorkflowEngineConfiguration.WorkflowInstanceFields getFields() {
    return fields;
  }

  public WriteConcern getWriteConcernStoreProcessInstance() {
    return writeConcernStoreProcessInstance;
  }
  
  public WriteConcern getWriteConcernFlushUpdates() {
    return writeConcernFlushUpdates;
  }
}
