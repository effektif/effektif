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

import static com.effektif.mongo.MongoHelper.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import com.mongodb.*;
import org.bson.types.ObjectId;
import org.slf4j.Logger;

import com.effektif.workflow.api.Configuration;
import com.effektif.workflow.api.model.WorkflowId;
import com.effektif.workflow.api.model.WorkflowInstanceId;
import com.effektif.workflow.api.query.WorkflowInstanceQuery;
import com.effektif.workflow.api.types.DataType;
import com.effektif.workflow.api.workflowinstance.VariableInstance;
import com.effektif.workflow.api.workflowinstance.WorkflowInstance;
import com.effektif.workflow.impl.WorkflowEngineImpl;
import com.effektif.workflow.impl.WorkflowInstanceStore;
import com.effektif.workflow.impl.configuration.Brewable;
import com.effektif.workflow.impl.configuration.Brewery;
import com.effektif.workflow.impl.data.DataTypeService;
import com.effektif.workflow.impl.job.Job;
import com.effektif.workflow.impl.util.Exceptions;
import com.effektif.workflow.impl.util.Time;
import com.effektif.workflow.impl.workflow.ActivityImpl;
import com.effektif.workflow.impl.workflow.ScopeImpl;
import com.effektif.workflow.impl.workflow.VariableImpl;
import com.effektif.workflow.impl.workflow.WorkflowImpl;
import com.effektif.workflow.impl.workflowinstance.ActivityInstanceImpl;
import com.effektif.workflow.impl.workflowinstance.LockImpl;
import com.effektif.workflow.impl.workflowinstance.ScopeInstanceImpl;
import com.effektif.workflow.impl.workflowinstance.VariableInstanceImpl;
import com.effektif.workflow.impl.workflowinstance.WorkflowInstanceImpl;
import com.effektif.workflow.impl.workflowinstance.WorkflowInstanceUpdates;


public class MongoWorkflowInstanceStore implements WorkflowInstanceStore, Brewable {
  
  public static final Logger log = MongoDb.log;

  protected Configuration configuration;
  protected WorkflowEngineImpl workflowEngine;
  protected MongoCollection workflowInstancesCollection;
  protected MongoJobStore mongoJobsStore;
  protected boolean storeWorkflowIdsAsStrings;
  protected DataTypeService dataTypeService;
  protected MongoObjectMapper mongoMapper;
  
  interface ScopeInstanceFields {
    String START = "start";
    String END = "end";
    String DURATION = "duration";
  }
  
  interface WorkflowInstanceFields extends ScopeInstanceFields {
    String _ID = "_id";
    String ORGANIZATION_ID = "organizationId";
    String WORKFLOW_ID = "workflowId";
    String ACTIVITY_INSTANCES = "activityInstances";
    String ARCHIVED_ACTIVITY_INSTANCES = "archivedActivities";
    String VARIABLE_INSTANCES = "variableInstances";
    String LOCK = "lock";
    String UPDATES = "updates";
    String WORK = "work";
    String WORK_ASYNC = "workAsync";
//    @Deprecated
//    String TASK_ID = "taskId";
    String CALLER_WORKFLOW_INSTANCE_ID = "callerWorkflowInstanceId";
    String CALLER_ACTIVITY_INSTANCE_ID = "callerActivityInstanceId";
    String NEXT_ACTIVITY_INSTANCE_ID = "nextActivityInstanceId";
    String NEXT_VARIABLE_INSTANCE_ID = "nextVariableInstanceId";
    String JOBS = "jobs";
    String PROPERTIES = "properties";
    String BUSINESS_KEY = "businessKey";
  }
  
  interface ActivityInstanceFields extends ScopeInstanceFields {
    String ID = "id";
    String PARENT = "parent";
    String CALLED_WORKFLOW_INSTANCE_ID = "calledWorkflowInstanceId";
    String ACTIVITY_ID = "activityId";
    String WORK_STATE = "workState";
    @Deprecated
    String TASK_ID = "taskId";
  }

  interface WorkflowInstanceLockFields {
    String TIME = "time";
    String OWNER = "owner";
  }

  interface VariableInstanceFields {
    String _ID = "_id";
    String VARIABLE_ID = "variableId";
    String VALUE = "value";
    String TYPE = "type";
  }

  @Override
  public void brew(Brewery brewery) {
    MongoConfiguration mongoConfiguration = brewery.get(MongoConfiguration.class);
    MongoDb mongoDb = brewery.get(MongoDb.class);
    this.configuration = brewery.get(MongoConfiguration.class);
    this.workflowEngine = brewery.get(WorkflowEngineImpl.class);
    this.workflowInstancesCollection = mongoDb.createCollection(mongoConfiguration.workflowInstancesCollectionName);
    this.storeWorkflowIdsAsStrings = mongoConfiguration.getStoreWorkflowIdsAsString();
    this.mongoJobsStore = brewery.get(MongoJobStore.class);
    this.dataTypeService = brewery.get(DataTypeService.class);
    this.mongoMapper = brewery.get(MongoObjectMapper.class);
  }
  
  @Override
  public WorkflowInstanceId generateWorkflowInstanceId() {
    return new WorkflowInstanceId(new ObjectId().toString());
  }

  @Override
  public void insertWorkflowInstance(WorkflowInstanceImpl workflowInstance) {
    BasicDBObject dbWorkflowInstance = writeWorkflowInstance(workflowInstance);
    workflowInstancesCollection.insert("insert-workflow-instance", dbWorkflowInstance);
    workflowInstance.trackUpdates(false);
  }

  @Override
  public void flush(WorkflowInstanceImpl workflowInstance) {
    if (log.isDebugEnabled()) log.debug("Flushing workflow instance...");
    
    WorkflowInstanceUpdates updates = workflowInstance.getUpdates();
    
    DBObject query = BasicDBObjectBuilder.start()
            .add(WorkflowInstanceFields._ID,  new ObjectId(workflowInstance.id.getInternal()))
            // I don't recall what this line was for... if you re-add it, please add a comment to explain
            // .add(WorkflowInstanceFields.LOCK,  writeLock(workflowInstance.lock))
            .get();
    
    BasicDBObject sets = new BasicDBObject();
    BasicDBObject unsets = new BasicDBObject();
    BasicDBObject update = new BasicDBObject();

    if (updates.isEndChanged) {
      // if (log.isDebugEnabled()) log.debug("  Workflow instance ended");
      sets.append(WorkflowInstanceFields.END, workflowInstance.end.toDate());
      sets.append(WorkflowInstanceFields.DURATION, workflowInstance.duration);
    }
    // MongoDB can't combine updates of array elements together with 
    // adding elements to that array.  That's why we overwrite the whole
    // activity instance array when an update happened in there.
    // We do archive the ended (and joined) activity instances into a separate collection 
    // that doesn't have to be loaded.
    if (updates.isActivityInstancesChanged) {
      // if (log.isDebugEnabled()) log.debug("  Activity instances changed");
//      BasicDBList dbArchivedActivityInstances = new BasicDBList();
//      collectArchivedActivities(workflowInstance, dbArchivedActivityInstances);
      BasicDBList dbActivityInstances = writeActiveActivityInstances(workflowInstance.activityInstances);
      sets.append(WorkflowInstanceFields.ACTIVITY_INSTANCES, dbActivityInstances);
//      if (!dbArchivedActivityInstances.isEmpty()) {
//        update.append("$push", new BasicDBObject(WorkflowInstanceFields.ARCHIVED_ACTIVITY_INSTANCES, dbArchivedActivityInstances));
//      }
    } else {
      // if (log.isDebugEnabled()) log.debug("  No activity instances changed");
    }
    
    if (updates.isVariableInstancesChanged) {
      // if (log.isDebugEnabled()) log.debug("  Variable instances changed");
      writeVariableInstances(sets, workflowInstance);
    } else {
      // if (log.isDebugEnabled()) log.debug("  No variable instances changed");
    }

    if (updates.isWorkChanged) {
      // if (log.isDebugEnabled()) log.debug("  Work changed");
      List<String> work = writeWork(workflowInstance.work);
      if (work!=null) {
        sets.put(WorkflowInstanceFields.WORK, work);
      } else {
        unsets.put(WorkflowInstanceFields.WORK, 1);
      }
    } else {
      // if (log.isDebugEnabled()) log.debug("  No work changed");
    }

    if (updates.isAsyncWorkChanged) {
      // if (log.isDebugEnabled()) log.debug("  Aync work changed");
      List<String> workAsync = writeWork(workflowInstance.workAsync);
      if (workAsync!=null) {
        sets.put(WorkflowInstanceFields.WORK_ASYNC, workAsync);
      } else {
        unsets.put(WorkflowInstanceFields.WORK_ASYNC, 1);
      }
    } else {
      // if (log.isDebugEnabled()) log.debug("  No async work changed");
    }

    if (updates.isNextActivityInstanceIdChanged) {
      // if (log.isDebugEnabled()) log.debug("  Next activity instance changed");
      sets.put(WorkflowInstanceFields.NEXT_ACTIVITY_INSTANCE_ID, workflowInstance.nextActivityInstanceId);
    }

    if (updates.isNextVariableInstanceIdChanged) {
      // if (log.isDebugEnabled()) log.debug("  Next variable instance changed");
      sets.put(WorkflowInstanceFields.NEXT_VARIABLE_INSTANCE_ID, workflowInstance.nextVariableInstanceId);
    }

    if (updates.isLockChanged) {
      // if (log.isDebugEnabled()) log.debug("  Lock changed");
      // a lock is only removed 
      unsets.put(WorkflowInstanceFields.LOCK, 1);
    }

    if (updates.isWorkflowChanged) {
      sets.put(WorkflowInstanceFields.WORKFLOW_ID, new ObjectId(workflowInstance.getWorkflow().getId().getInternal()));
    }

    if (updates.isJobsChanged) {
      // if (log.isDebugEnabled()) log.debug("  Jobs changed");
      List<BasicDBObject> dbJobs = writeJobs(workflowInstance.jobs);
      if (dbJobs!=null) {
        sets.put(WorkflowInstanceFields.JOBS, dbJobs);
      } else {
        unsets.put(WorkflowInstanceFields.JOBS, 1);
      }
    } else {
      // if (log.isDebugEnabled()) log.debug("  No jobs changed");
    }

    if (!sets.isEmpty()) {
      update.append("$set", sets);
    } else {
      // if (log.isDebugEnabled()) log.debug("  No sets");
    }
    if (!unsets.isEmpty()) {
      update.append("$unset", unsets);
    } else {
      // if (log.isDebugEnabled()) log.debug("  No unsets");
    }
    
    if (!update.isEmpty()) {
      workflowInstancesCollection.update("flush-workflow-instance", query, update, false, false);
    } else {
      // if (log.isDebugEnabled()) log.debug("  Nothing to flush");
    }
    
    // reset the update tracking as all changes have been saved
    workflowInstance.trackUpdates(false);
  }

  @Override
  public void flushAndUnlock(WorkflowInstanceImpl workflowInstance) {
    workflowInstance.removeLock();
    flush(workflowInstance);
  }

  @Override
  public List<WorkflowInstanceImpl> findWorkflowInstances(WorkflowInstanceQuery query) {
    BasicDBObject dbQuery = createDbQuery(query);
    return findWorkflowInstances(dbQuery);
  }

  public List<String> findWorkflowInstanceIds(WorkflowInstanceQuery query) {
    BasicDBObject dbQuery = createDbQuery(query);
    BasicDBObject dbFields = new BasicDBObject(WorkflowInstanceFields._ID, true);

    DBCursor workflowInstanceCursor = workflowInstancesCollection.dbCollection.find(dbQuery, dbFields);
    List<String> workflowInstances = new ArrayList<>();
    while (workflowInstanceCursor.hasNext()) {
      BasicDBObject dbWorkflowInstance = (BasicDBObject) workflowInstanceCursor.next();
      workflowInstances.add(dbWorkflowInstance.getString(WorkflowInstanceFields._ID));
    }

    return workflowInstances;

  }

  public List<WorkflowInstanceImpl> findWorkflowInstances(BasicDBObject dbQuery) {
    DBCursor workflowInstanceCursor = workflowInstancesCollection.find("find-workflow-instance-impls", dbQuery);
    List<WorkflowInstanceImpl> workflowInstances = new ArrayList<>();
    while (workflowInstanceCursor.hasNext()) {
      BasicDBObject dbWorkflowInstance = (BasicDBObject) workflowInstanceCursor.next();
      WorkflowInstanceImpl workflowInstance = readWorkflowInstanceImpl(dbWorkflowInstance);
      workflowInstances.add(workflowInstance);
    }
    return workflowInstances;
  }

  @Override
  public void deleteWorkflowInstances(WorkflowInstanceQuery workflowInstanceQuery) {
    BasicDBObject query = createDbQuery(workflowInstanceQuery);
    workflowInstancesCollection.remove("delete-workflow-instances", query);
  }
  
  @Override
  public void deleteAllWorkflowInstances() {
    workflowInstancesCollection.remove("delete-workflow-instances-unchecked", new BasicDBObject(), false);
  }

  protected BasicDBObject createDbQuery(WorkflowInstanceQuery query) {
    if (query == null) {
      query = new WorkflowInstanceQuery();
    }
    BasicDBObject dbQuery = new BasicDBObject();
    if (query.getWorkflowInstanceId() != null) {
      dbQuery.append(WorkflowInstanceFields._ID, new ObjectId(query.getWorkflowInstanceId().getInternal()));
    }

    if (query.getActivityId() != null) {
      dbQuery.append(WorkflowInstanceFields.ACTIVITY_INSTANCES
              , new BasicDBObject("$elemMatch", new BasicDBObject(ActivityInstanceFields.ACTIVITY_ID, query.getActivityId())
              .append(ActivityInstanceFields.WORK_STATE, new BasicDBObject("$exists", true))));
    }

    if (query.getWorkflowId() != null) {
      dbQuery.append(WorkflowInstanceFields.WORKFLOW_ID, new ObjectId(query.getWorkflowId()));
    }

    return dbQuery;
  }
  
  public void saveWorkflowInstance(BasicDBObject dbWorkflowInstance) {
    workflowInstancesCollection.save("save-workfow-instance", dbWorkflowInstance);
  }
  
  @Override
  public WorkflowInstanceImpl getWorkflowInstanceImplById(WorkflowInstanceId workflowInstanceId) {
    if (workflowInstanceId==null) {
      return null;
    }
    DBObject query = createLockQuery();
    query.put(WorkflowInstanceFields._ID, new ObjectId(workflowInstanceId.getInternal()));
    
    BasicDBObject dbWorkflowInstance = workflowInstancesCollection.findOne("get-workflow-instance", query);
    if (dbWorkflowInstance==null) {
      return null;
    }

    return readWorkflowInstanceImpl(dbWorkflowInstance);
  }

  @Override
  public WorkflowInstanceImpl lockWorkflowInstance(WorkflowInstanceId workflowInstanceId, String owner) {
    Exceptions.checkNotNullParameter(workflowInstanceId, "workflowInstanceId");

    DBObject query = createLockQuery();
    query.put(WorkflowInstanceFields._ID, new ObjectId(workflowInstanceId.getInternal()));
    
    DBObject update = createLockUpdate(owner);
    
    DBObject retrieveFields = new BasicDBObject()
          .append(WorkflowInstanceFields.ARCHIVED_ACTIVITY_INSTANCES, false);
    
    BasicDBObject dbWorkflowInstance = workflowInstancesCollection.findAndModify("lock-workflow-instance", query, update, retrieveFields);
    if (dbWorkflowInstance==null) {
      return null;
    }

    WorkflowInstanceImpl workflowInstance = readWorkflowInstanceImpl(dbWorkflowInstance);
    workflowInstance.trackUpdates(false);
    return workflowInstance;
  }

  @Override
  public void unlockWorkflowInstance(WorkflowInstanceId workflowInstanceId) {
    workflowInstancesCollection.update("unlock-workflow-instance", 
      new Query()
        ._id(new ObjectId(workflowInstanceId.getInternal()))
        .get(), 
      new Update()
        .unset(WorkflowInstanceFields.LOCK)
        .get());
  }

  public DBObject createLockQuery() {
    return BasicDBObjectBuilder.start()
      .push(WorkflowInstanceFields.LOCK)
        .add("$exists", false)
      .pop()
      .get();
  }

  public DBObject createLockUpdate(String owner) {
    return BasicDBObjectBuilder.start()
      .push("$set")
        .push(WorkflowInstanceFields.LOCK)
          .add(WorkflowInstanceLockFields.TIME, Time.now().toDate())
          .add(WorkflowInstanceLockFields.OWNER, owner)
        .pop()
      .pop()
      .get();
  }

  @Override
  public WorkflowInstanceImpl lockWorkflowInstanceWithJobsDue() {
    return null;
  }
  
  public BasicDBObject writeWorkflowInstance(WorkflowInstanceImpl workflowInstance) {
//    BasicDBList dbArchivedActivityInstances = new BasicDBList();
//    collectArchivedActivities(workflowInstance, dbArchivedActivityInstances);
    BasicDBObject dbWorkflowInstance = mongoMapper.write(workflowInstance.toWorkflowInstance(true));
//    if (!dbArchivedActivityInstances.isEmpty()) {
//      writeObjectOpt(dbWorkflowInstance, WorkflowInstanceFields.ARCHIVED_ACTIVITY_INSTANCES, dbArchivedActivityInstances);
//    }
    if (storeWorkflowIdsAsStrings) {
      writeString(dbWorkflowInstance, WorkflowInstanceFields.WORKFLOW_ID, workflowInstance.workflow.id.getInternal());
    }

    writeLongOpt(dbWorkflowInstance, WorkflowInstanceFields.NEXT_ACTIVITY_INSTANCE_ID, workflowInstance.nextActivityInstanceId);
    writeLongOpt(dbWorkflowInstance, WorkflowInstanceFields.NEXT_VARIABLE_INSTANCE_ID, workflowInstance.nextVariableInstanceId);
    writeObjectOpt(dbWorkflowInstance, WorkflowInstanceFields.WORK, writeWork(workflowInstance.work));
    writeObjectOpt(dbWorkflowInstance, WorkflowInstanceFields.WORK_ASYNC, writeWork(workflowInstance.workAsync));
    writeObjectOpt(dbWorkflowInstance, WorkflowInstanceFields.JOBS, writeJobs(workflowInstance.jobs));
    writeObjectOpt(dbWorkflowInstance, WorkflowInstanceFields.LOCK, writeLock(workflowInstance.lock));
    
//    BasicDBObject dbWorkflowInstance = new BasicDBObject();
//    writeIdOptNew(dbWorkflowInstance, WorkflowInstanceFields._ID, workflowInstance.id);
//    if (storeWorkflowIdsAsStrings) {
//      writeString(dbWorkflowInstance, WorkflowInstanceFields.WORKFLOW_ID, workflowInstance.workflow.id.getInternal());
//    } else {
//      writeIdOptNew(dbWorkflowInstance, WorkflowInstanceFields.WORKFLOW_ID, workflowInstance.workflow.id);
//    }
//    writeStringOpt(dbWorkflowInstance, WorkflowInstanceFields.BUSINESS_KEY, workflowInstance.businessKey);
//    writeIdOptNew(dbWorkflowInstance, WorkflowInstanceFields.TASK_ID, workflowInstance.caseId);
//    writeIdOptNew(dbWorkflowInstance, WorkflowInstanceFields.CALLER_WORKFLOW_INSTANCE_ID, workflowInstance.callerWorkflowInstanceId);
//    writeStringOpt(dbWorkflowInstance, WorkflowInstanceFields.CALLER_ACTIVITY_INSTANCE_ID, workflowInstance.callerActivityInstanceId);
//    writeLongOpt(dbWorkflowInstance, WorkflowInstanceFields.NEXT_ACTIVITY_INSTANCE_ID, workflowInstance.nextActivityInstanceId);
//    writeLongOpt(dbWorkflowInstance, WorkflowInstanceFields.NEXT_VARIABLE_INSTANCE_ID, workflowInstance.nextVariableInstanceId);
//    writeTimeOpt(dbWorkflowInstance, WorkflowInstanceFields.START, workflowInstance.start);
//    writeTimeOpt(dbWorkflowInstance, WorkflowInstanceFields.END, workflowInstance.end);
//    writeLongOpt(dbWorkflowInstance, WorkflowInstanceFields.DURATION, workflowInstance.duration);
//    writeObjectOpt(dbWorkflowInstance, WorkflowInstanceFields.LOCK, writeLock(workflowInstance.lock));
//    
    // writeVariableInstances(dbWorkflowInstance, workflowInstance);
//    writeObjectOpt(dbWorkflowInstance, WorkflowInstanceFields.WORK, writeWork(workflowInstance.work));
//    writeObjectOpt(dbWorkflowInstance, WorkflowInstanceFields.WORK_ASYNC, writeWork(workflowInstance.workAsync));
//    writeObjectOpt(dbWorkflowInstance, WorkflowInstanceFields.JOBS, writeJobs(workflowInstance.jobs));
//    writeObjectOpt(dbWorkflowInstance, WorkflowInstanceFields.PROPERTIES, workflowInstance.properties);
    
    return dbWorkflowInstance;
  }
  
  protected List<String> writeWork(Queue<ActivityInstanceImpl> workQueue) {
    List<String> workActivityInstanceIds = null;
    if (workQueue!=null && !workQueue.isEmpty()) {
      workActivityInstanceIds = new ArrayList<String>();
      for (ActivityInstanceImpl workActivityInstance: workQueue) {
        workActivityInstanceIds.add(workActivityInstance.id);
      }
    }
    return workActivityInstanceIds;
  }

  public WorkflowInstance readWorkflowInstance(BasicDBObject dbWorkflowInstance) {
    return mongoMapper.read(dbWorkflowInstance, WorkflowInstance.class);
  }

  public WorkflowInstanceImpl readWorkflowInstanceImpl(BasicDBObject dbWorkflowInstance) {
    if (dbWorkflowInstance==null) {
      return null;
    }
    WorkflowInstanceImpl workflowInstance = new WorkflowInstanceImpl();
    workflowInstance.id = readWorkflowInstanceId(dbWorkflowInstance, WorkflowInstanceFields._ID);
    workflowInstance.businessKey = readString(dbWorkflowInstance, WorkflowInstanceFields.BUSINESS_KEY);
    
    Object workflowIdObject = readObject(dbWorkflowInstance, WorkflowInstanceFields.WORKFLOW_ID);

    // workflowId is ObjectId in the MongoConfiguration
    // workflowId is String in the MongoMemoryConfiguration
    // The code is written to work dynamically (and not according to the 
    // configuration field storeWorkflowIdsAsStrings) because the test 
    // suite cleanup might encounter workflow instances created by the other engine
    WorkflowId workflowId = new WorkflowId(workflowIdObject.toString());
    WorkflowImpl workflow = workflowEngine.getWorkflowImpl(workflowId);
    if (workflow==null) {
      throw new RuntimeException("No workflow for instance "+workflowInstance.id);
    }
    
    workflowInstance.workflow = workflow;
    workflowInstance.workflowInstance = workflowInstance;
    workflowInstance.scope = workflow;
    workflowInstance.configuration = configuration;
    workflowInstance.callerWorkflowInstanceId = readWorkflowInstanceId(dbWorkflowInstance, WorkflowInstanceFields.CALLER_WORKFLOW_INSTANCE_ID);
    workflowInstance.callerActivityInstanceId = readString(dbWorkflowInstance, WorkflowInstanceFields.CALLER_ACTIVITY_INSTANCE_ID);
    workflowInstance.nextActivityInstanceId = readLong(dbWorkflowInstance, WorkflowInstanceFields.NEXT_ACTIVITY_INSTANCE_ID);
    workflowInstance.nextVariableInstanceId = readLong(dbWorkflowInstance, WorkflowInstanceFields.NEXT_VARIABLE_INSTANCE_ID);
    workflowInstance.start = readTime(dbWorkflowInstance, WorkflowInstanceFields.START);
    workflowInstance.end = readTime(dbWorkflowInstance, WorkflowInstanceFields.END);
    workflowInstance.duration = readLong(dbWorkflowInstance, WorkflowInstanceFields.DURATION);
    workflowInstance.lock = readLock((BasicDBObject) dbWorkflowInstance.get(WorkflowInstanceFields.LOCK));
    workflowInstance.jobs = readJobs(readList(dbWorkflowInstance, WorkflowInstanceFields.JOBS));
    
    Map<ActivityInstanceImpl, String> allActivityIds = new HashMap<>();

    readScopeImpl(workflowInstance, dbWorkflowInstance, allActivityIds);

    resolveActivityReferences(workflowInstance, workflow, allActivityIds);
    
    workflowInstance.work = readWork(dbWorkflowInstance, WorkflowInstanceFields.WORK, workflowInstance);
    workflowInstance.workAsync = readWork(dbWorkflowInstance, WorkflowInstanceFields.WORK_ASYNC, workflowInstance);
    workflowInstance.properties = readObjectMap(dbWorkflowInstance, WorkflowInstanceFields.PROPERTIES);
    return workflowInstance;
  }

  protected void readScopeImpl(ScopeInstanceImpl scopeInstance, BasicDBObject dbScopeInstance, Map<ActivityInstanceImpl, String> allActivityIds) {
    readActivityInstances(scopeInstance, dbScopeInstance, allActivityIds);
    readVariableInstances(scopeInstance, dbScopeInstance);
  }

  protected void readActivityInstances(ScopeInstanceImpl scopeInstance, BasicDBObject dbScopeInstance, Map<ActivityInstanceImpl, String> allActivityIds) {
    Map<Object, ActivityInstanceImpl> allActivityInstances = new LinkedHashMap<>();
    List<BasicDBObject> dbActivityInstances = readList(dbScopeInstance, WorkflowInstanceFields.ACTIVITY_INSTANCES);
    if (dbActivityInstances!=null) {
      for (BasicDBObject dbActivityInstance: dbActivityInstances) {
        ActivityInstanceImpl activityInstance = readActivityInstance(scopeInstance, dbActivityInstance, allActivityIds);
        allActivityInstances.put(activityInstance.id, activityInstance);
        String activityId = readString(dbActivityInstance, ActivityInstanceFields.ACTIVITY_ID);
        allActivityIds.put(activityInstance, activityId);
        scopeInstance.addActivityInstance(activityInstance);
      }
    }
  }
  
  protected ActivityInstanceImpl readActivityInstance(ScopeInstanceImpl parent, BasicDBObject dbActivityInstance, Map<ActivityInstanceImpl, String> allActivityIds) {
    ActivityInstanceImpl activityInstanceImpl = new ActivityInstanceImpl();
    activityInstanceImpl.id = readId(dbActivityInstance, ActivityInstanceFields.ID);
    activityInstanceImpl.start = readTime(dbActivityInstance, ActivityInstanceFields.START);
    activityInstanceImpl.end = readTime(dbActivityInstance, ActivityInstanceFields.END);
    activityInstanceImpl.calledWorkflowInstanceId = readWorkflowInstanceId(dbActivityInstance, ActivityInstanceFields.CALLED_WORKFLOW_INSTANCE_ID);
    activityInstanceImpl.duration = readLong(dbActivityInstance, ActivityInstanceFields.DURATION);
    
    activityInstanceImpl.workState = readString(dbActivityInstance, ActivityInstanceFields.WORK_STATE);

    activityInstanceImpl.configuration = configuration;
    activityInstanceImpl.parent = parent;
    activityInstanceImpl.workflow = parent.workflow;
    activityInstanceImpl.workflowInstance = parent.workflowInstance;
    
    readScopeImpl(activityInstanceImpl, dbActivityInstance, allActivityIds);
    return activityInstanceImpl;
  }


  protected void resolveActivityReferences(ScopeInstanceImpl scopeInstance, ScopeImpl scope, Map<ActivityInstanceImpl, String> allActivityIds) {
    if (scopeInstance.activityInstances!=null) {
      for (ActivityInstanceImpl activityInstance : scopeInstance.activityInstances) {
        String activityId = allActivityIds.get(activityInstance);
        ActivityImpl activity = scope.findActivityByIdLocal(activityId);
        activityInstance.activity = activity;
        activityInstance.scope = activity;
        ScopeImpl nestedScope = activity.isMultiInstance() ? activity.parent : activity;
        resolveActivityReferences(activityInstance, nestedScope, allActivityIds);
      }
    }
  }

  @SuppressWarnings("unchecked")
  protected Queue<ActivityInstanceImpl> readWork(BasicDBObject dbWorkflowInstance, String fieldName, WorkflowInstanceImpl workflowInstance) {
    Queue<ActivityInstanceImpl> workQueue = null;
    List<String> workActivityInstanceIds = (List<String>) dbWorkflowInstance.get(fieldName);
    if (workActivityInstanceIds!=null) {
      workQueue = new LinkedList<>();
      for (String workActivityInstanceId: workActivityInstanceIds) {
        ActivityInstanceImpl workActivityInstance = workflowInstance.findActivityInstance(workActivityInstanceId);
        workQueue.add(workActivityInstance);
      }
    }
    return workQueue;
  }

  private void readVariableInstances(ScopeInstanceImpl parent, BasicDBObject dbWorkflowInstance) {
    List<BasicDBObject> dbVariableInstances = readList(dbWorkflowInstance, WorkflowInstanceFields.VARIABLE_INSTANCES);
    if (dbVariableInstances!=null && !dbVariableInstances.isEmpty()) {
      for (BasicDBObject dbVariableInstance: dbVariableInstances) {
        VariableInstance variableInstance = mongoMapper.read(dbVariableInstance, VariableInstance.class);
        
        VariableInstanceImpl variableInstanceImpl = new VariableInstanceImpl();
        variableInstanceImpl.id = variableInstance.getId();
        String variableId = variableInstance.getVariableId();
        variableInstanceImpl.variable = findVariableByIdRecurseParents(parent.scope, variableId);
        if (variableInstanceImpl.variable!=null) {
          variableInstanceImpl.type = variableInstanceImpl.variable.type;
        } else {
          variableInstanceImpl.variable = new VariableImpl();
          DataType type = variableInstance.getType();
          if (type!=null) {
            variableInstanceImpl.type = dataTypeService.createDataType(type);
          }
        }
        variableInstanceImpl.value = variableInstance.getValue();

        variableInstanceImpl.configuration = configuration;
        variableInstanceImpl.workflowInstance = parent.workflowInstance;
        variableInstanceImpl.parent = parent;
        variableInstanceImpl.workflow = parent.workflow;

        parent.addVariableInstance(variableInstanceImpl);
      }
    }
  }

  protected VariableImpl findVariableByIdRecurseParents(ScopeImpl scope, String variableId) {
    if (scope==null) {
      return null;
    }
    VariableImpl variable = scope.findVariableByIdLocal(variableId);
    if (variable!=null) {
      return variable;
    }
    return findVariableByIdRecurseParents(scope.parent, variableId);
  }

  protected BasicDBObject writeLock(LockImpl lock) {
    if (lock==null) {
      return null;
    }
    BasicDBObject dbLock = new BasicDBObject();
    writeTimeOpt(dbLock, WorkflowInstanceLockFields.TIME, lock.time);
    writeObjectOpt(dbLock, WorkflowInstanceLockFields.OWNER, lock.owner);
    return dbLock;
  }
  
  protected LockImpl readLock(BasicDBObject dbLock) {
    if (dbLock==null) {
      return null;
    }
    LockImpl lock = new LockImpl();
    lock.owner = readString(dbLock, WorkflowInstanceLockFields.OWNER);
    lock.time = readTime(dbLock, WorkflowInstanceLockFields.TIME);
    return lock;
  }

  /** writes the given activityInstances to db format, preserving the hierarchy and including the workState. */
  protected BasicDBList writeActiveActivityInstances(List<ActivityInstanceImpl> activityInstances) {
    if (activityInstances==null || activityInstances.isEmpty()) {
      return null;
    }
    BasicDBList dbActivityInstances = new BasicDBList();
    for (ActivityInstanceImpl activityInstance: activityInstances) {
      BasicDBObject dbActivityInstance = mongoMapper.write(activityInstance.toActivityInstance(true));
      dbActivityInstances.add(dbActivityInstance);
    }
    return dbActivityInstances;
  }

  /** recursively removes the archivable activities from the scopeInstance, serializes them to DB format and adds them to the dbArchivedActivityInstances as a flat list */
  protected void collectArchivedActivities(ScopeInstanceImpl scopeInstance, BasicDBList dbArchivedActivityInstances) {
    if (scopeInstance.activityInstances!=null) {
      List<ActivityInstanceImpl> activeActivityInstances = new ArrayList<>(); 
      for (ActivityInstanceImpl activityInstance: scopeInstance.activityInstances) {
        if (activityInstance.workState!=null) { // null means ready to be archived
          activeActivityInstances.add(activityInstance);
        } else {
          activityInstance.activityInstances = null;
          BasicDBObject dbActivity = mongoMapper.write(activityInstance.toActivityInstance());
          String parentId = (activityInstance.parent.isWorkflowInstance() ? null : ((ActivityInstanceImpl) activityInstance.parent).id);
          writeString(dbActivity, ActivityInstanceFields.PARENT, parentId);
          dbArchivedActivityInstances.add(dbActivity);
        }
        collectArchivedActivities(activityInstance, dbArchivedActivityInstances);
      }
      scopeInstance.activityInstances = activeActivityInstances;
    }
  }

  protected void writeVariableInstances(BasicDBObject dbScope, ScopeInstanceImpl scope) {
    if (scope.variableInstances!=null) {
      for (VariableInstanceImpl variableInstanceImpl: scope.variableInstances) {
        VariableInstance variableInstance = variableInstanceImpl.toVariableInstance();
        BasicDBObject dbVariable = mongoMapper.write(variableInstance);
        writeListElementOpt(dbScope, WorkflowInstanceFields.VARIABLE_INSTANCES, dbVariable);
      }
    }
  }

  protected List<BasicDBObject> writeJobs(List<Job> jobs) {
    if (jobs==null || jobs.isEmpty()) {
      return null;
    }
    List<BasicDBObject> dbJobs = new ArrayList<BasicDBObject>();
    for (Job job: jobs) {
      BasicDBObject dbJob = mongoJobsStore.writeJob(job);
      dbJobs.add(dbJob);
    }
    return dbJobs;
  }

  protected List<Job> readJobs(List<BasicDBObject> dbJobs) {
    if (dbJobs==null || dbJobs.isEmpty()) {
      return null;
    }
    List<Job> jobs = new ArrayList<>();
    for (BasicDBObject dbJob: dbJobs) {
      Job job = mongoJobsStore.readJob(dbJob);
      jobs.add(job);
    }
    return jobs;
  }

  public LinkedHashMap<WorkflowInstanceId, WorkflowInstanceImpl> findWorkflowInstanceMap(Collection<ObjectId> workflowInstanceIds) {
    LinkedHashMap<WorkflowInstanceId, WorkflowInstanceImpl> workflowInstanceMap = new LinkedHashMap<>();
    if (workflowInstanceIds!=null && !workflowInstanceIds.isEmpty()) {
      Query query = new Query()
        .in(WorkflowInstanceFields._ID, workflowInstanceIds);
      DBCursor workflowInstanceCursor = workflowInstancesCollection.find("find-workflow-instance", query.get());
      while (workflowInstanceCursor.hasNext()) {
        BasicDBObject dbWorkflowInstance = (BasicDBObject) workflowInstanceCursor.next();
        WorkflowInstanceImpl workflowInstance = readWorkflowInstanceImpl(dbWorkflowInstance);
        workflowInstanceMap.put(workflowInstance.getId(), workflowInstance);
      }
    }
    return workflowInstanceMap;
  }
}
