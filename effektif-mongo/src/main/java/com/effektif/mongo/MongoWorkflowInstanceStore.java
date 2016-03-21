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

import com.effektif.mongo.WorkflowInstanceFields.*;
import com.effektif.workflow.api.Configuration;
import com.effektif.workflow.api.model.WorkflowId;
import com.effektif.workflow.api.model.WorkflowInstanceId;
import com.effektif.workflow.api.query.WorkflowInstanceQuery;
import com.effektif.workflow.api.types.DataType;
import com.effektif.workflow.api.workflow.Extensible;
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
import com.effektif.workflow.impl.workflowinstance.*;
import com.mongodb.*;
import org.bson.types.ObjectId;
import org.slf4j.Logger;

import java.util.*;

import static com.effektif.mongo.ActivityInstanceFields.*;
import static com.effektif.mongo.ActivityInstanceFields.DURATION;
import static com.effektif.mongo.ActivityInstanceFields.END;
import static com.effektif.mongo.ActivityInstanceFields.END_STATE;
import static com.effektif.mongo.ActivityInstanceFields.START;
import static com.effektif.mongo.MongoDb._ID;
import static com.effektif.mongo.MongoHelper.*;
import static com.effektif.mongo.WorkflowInstanceFields.*;


public class MongoWorkflowInstanceStore implements WorkflowInstanceStore, Brewable {
  
  public static final Logger log = MongoDb.log;

  protected Configuration configuration;
  protected WorkflowEngineImpl workflowEngine;
  protected MongoCollection workflowInstancesCollection;
  protected MongoJobStore mongoJobsStore;
  protected boolean storeWorkflowIdsAsStrings;
  protected DataTypeService dataTypeService;
  protected MongoObjectMapper mongoMapper;
  
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
            .add(_ID,  new ObjectId(workflowInstance.id.getInternal()))
            // I don't recall what this line was for... if you re-add it, please add a comment to explain
            // .add(LOCK,  writeLock(workflowInstance.lock))
            .get();
    
    BasicDBObject sets = new BasicDBObject();
    BasicDBObject unsets = new BasicDBObject();
    BasicDBObject update = new BasicDBObject();

    if (updates.isEndChanged) {
      // if (log.isDebugEnabled()) log.debug("  Workflow instance ended");
      if (workflowInstance.end != null) {
        sets.append(END, workflowInstance.end.toDate());
        sets.append(DURATION, workflowInstance.duration);
      }
      else {
        unsets.append(END, 1);
        unsets.append(DURATION, 1);
      }
    }
    if (updates.isEndStateChanged) {
      sets.append(END_STATE, workflowInstance.getEndState());
    }


    // MongoDB can't combine updates of array elements together with 
    // adding elements to that array.  That's why we overwrite the whole
    // activity instance array when an update happened in there.
    // We do archive the ended (and joined) activity instances into a separate collection 
    // that doesn't have to be loaded.
    if (updates.isActivityInstancesChanged) {
      BasicDBList dbActivityInstances = writeActiveActivityInstances(workflowInstance.activityInstances);
      sets.append(ACTIVITY_INSTANCES, dbActivityInstances);
    }
    
    if (updates.isVariableInstancesChanged) {
      writeVariableInstances(sets, workflowInstance);
    }

    if (updates.isWorkChanged) {
      List<String> work = writeWork(workflowInstance.work);
      if (work!=null) {
        sets.put(WORK, work);
      } else {
        unsets.put(WORK, 1);
      }
    }

    if (updates.isAsyncWorkChanged) {
      List<String> workAsync = writeWork(workflowInstance.workAsync);
      if (workAsync!=null) {
        sets.put(WORK_ASYNC, workAsync);
      } else {
        unsets.put(WORK_ASYNC, 1);
      }
    }

    if (updates.isNextActivityInstanceIdChanged) {
      sets.put(NEXT_ACTIVITY_INSTANCE_ID, workflowInstance.nextActivityInstanceId);
    }

    if (updates.isNextVariableInstanceIdChanged) {
      sets.put(NEXT_VARIABLE_INSTANCE_ID, workflowInstance.nextVariableInstanceId);
    }

    if (updates.isLockChanged) {
      // a lock is only removed 
      unsets.put(LOCK, 1);
    }
    
    if (updates.isJobsChanged) {
      List<BasicDBObject> dbJobs = writeJobs(workflowInstance.jobs);
      if (dbJobs!=null) {
        sets.put(JOBS, dbJobs);
      } else {
        unsets.put(JOBS, 1);
      }
    }

    if (updates.isPropertiesChanged) {
      if (workflowInstance.properties != null && workflowInstance.properties.size() > 0)
        sets.append(PROPERTIES, new BasicDBObject(workflowInstance.getProperties()));
      else
        unsets.append(PROPERTIES, 1);
    }
    
    if (!sets.isEmpty()) {
      update.append("$set", sets);
    }
    if (!unsets.isEmpty()) {
      update.append("$unset", unsets);
    }
    if (!update.isEmpty()) {
      workflowInstancesCollection.update("flush-workflow-instance", query, update, false, false);
    }
    
    // reset the update tracking as all changes have been saved
    workflowInstance.trackUpdates(false);
  }

  @Override
  public void flushAndUnlock(WorkflowInstanceImpl workflowInstance) {
    workflowInstance.removeLock();
    flush(workflowInstance);
    workflowInstance.notifyUnlockListeners();
  }

  @Override
  public List<WorkflowInstanceImpl> findWorkflowInstances(WorkflowInstanceQuery query) {
    BasicDBObject dbQuery = createDbQuery(query);
    return findWorkflowInstances(dbQuery);
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
      dbQuery.append(_ID, new ObjectId(query.getWorkflowInstanceId().getInternal()));
    }

    if (query.getActivityId() != null) {
      dbQuery.append(ACTIVITY_INSTANCES
              , new BasicDBObject("$elemMatch", new BasicDBObject(ACTIVITY_ID, query.getActivityId())
              .append(WORK_STATE, new BasicDBObject("$exists", true))));
    }

    if (query.getLockedBefore() != null) {
      dbQuery.append(LOCK + "." + Lock.TIME, new BasicDBObject("$lt", query.getLockedBefore().toDate()));
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
    query.put(_ID, new ObjectId(workflowInstanceId.getInternal()));
    
    BasicDBObject dbWorkflowInstance = workflowInstancesCollection.findOne("get-workflow-instance", query);
    if (dbWorkflowInstance==null) {
      return null;
    }

    return readWorkflowInstanceImpl(dbWorkflowInstance);
  }

  @Override
  public WorkflowInstanceImpl lockWorkflowInstance(WorkflowInstanceId workflowInstanceId) {
    Exceptions.checkNotNullParameter(workflowInstanceId, "workflowInstanceId");

    DBObject query = createLockQuery();
    query.put(_ID, new ObjectId(workflowInstanceId.getInternal()));
    
    DBObject update = createLockUpdate();
    
    DBObject retrieveFields = new BasicDBObject()
          .append(ARCHIVED_ACTIVITY_INSTANCES, false);
    
    BasicDBObject dbWorkflowInstance = workflowInstancesCollection.findAndModify("lock-workflow-instance", query, update, retrieveFields);
    if (dbWorkflowInstance==null) {
      return null;
    }

    WorkflowInstanceImpl workflowInstance = readWorkflowInstanceImpl(dbWorkflowInstance);
    workflowInstance.trackUpdates(false);
    return workflowInstance;
  }
  
  @Override
  public void unlockWorkflowInstance(WorkflowInstanceImpl workflowInstance) {
    if (workflowInstance!=null) {
      ObjectId workflowInstanceId = new ObjectId(workflowInstance.id.getInternal());
      // @formatter:off
      workflowInstancesCollection.update("unlock-workflow-instance", 
        new Query()
          ._id(workflowInstanceId)
          .get(), 
        new Update()
          .unset(LOCK)
          .get());
      // @formatter:off
      
      workflowInstance.notifyUnlockListeners();
    }
  }

  public DBObject createLockQuery() {
    return BasicDBObjectBuilder.start()
      .push(LOCK)
        .add("$exists", false)
      .pop()
      .get();
  }

  public DBObject createLockUpdate() {
    return BasicDBObjectBuilder.start()
      .push("$set")
        .push(LOCK)
          .add(Lock.TIME, Time.now().toDate())
          .add(Lock.OWNER, workflowEngine.getId())
        .pop()
      .pop()
      .get();
  }

  @Override
  public WorkflowInstanceImpl lockWorkflowInstanceWithJobsDue() {

    DBObject query = createLockQuery();
    query.put(JobFields.DONE, new BasicDBObject("$exists", false));
    query.put(JOBS + "." + JobFields.DUE_DATE, new BasicDBObject("$lte", Time.now().toDate()));

    DBObject update = createLockUpdate();

    DBObject retrieveFields = new BasicDBObject()
        .append(ARCHIVED_ACTIVITY_INSTANCES, false);

    BasicDBObject dbWorkflowInstance = workflowInstancesCollection.findAndModify("lock-workflow-instance", query, update, retrieveFields, new BasicDBObject(START, 1), false, true, false);
    if (dbWorkflowInstance==null) {
      return null;
    }

    WorkflowInstanceImpl workflowInstance = readWorkflowInstanceImpl(dbWorkflowInstance);
    workflowInstance.trackUpdates(false);
    return workflowInstance;
  }

  public BasicDBObject writeWorkflowInstance(WorkflowInstanceImpl workflowInstance) {
    BasicDBObject dbWorkflowInstance = mongoMapper.write(workflowInstance.toWorkflowInstance(true));
    if (storeWorkflowIdsAsStrings) {
      writeString(dbWorkflowInstance, WORKFLOW_ID, workflowInstance.workflow.id.getInternal());
    }

    writeLongOpt(dbWorkflowInstance, NEXT_ACTIVITY_INSTANCE_ID, workflowInstance.nextActivityInstanceId);
    writeLongOpt(dbWorkflowInstance, NEXT_VARIABLE_INSTANCE_ID, workflowInstance.nextVariableInstanceId);
    writeObjectOpt(dbWorkflowInstance, WORK, writeWork(workflowInstance.work));
    writeObjectOpt(dbWorkflowInstance, WORK_ASYNC, writeWork(workflowInstance.workAsync));
    writeObjectOpt(dbWorkflowInstance, JOBS, writeJobs(workflowInstance.jobs));
    writeObjectOpt(dbWorkflowInstance, LOCK, writeLock(workflowInstance.lock));
    
    return dbWorkflowInstance;
  }
  
  protected List<String> writeWork(Queue<ActivityInstanceImpl> workQueue) {
    List<String> workActivityInstanceIds = null;
    if (workQueue!=null && !workQueue.isEmpty()) {
      workActivityInstanceIds = new ArrayList<>();
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
    workflowInstance.id = readWorkflowInstanceId(dbWorkflowInstance, _ID);
    workflowInstance.businessKey = readString(dbWorkflowInstance, BUSINESS_KEY);
    
    Object workflowIdObject = readObject(dbWorkflowInstance, WORKFLOW_ID);

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
    workflowInstance.callingWorkflowInstanceId = readWorkflowInstanceId(dbWorkflowInstance, CALLING_WORKFLOW_INSTANCE_ID);
    workflowInstance.callingActivityInstanceId = readString(dbWorkflowInstance, CALLING_ACTIVITY_INSTANCE_ID);
    workflowInstance.nextActivityInstanceId = readLong(dbWorkflowInstance, NEXT_ACTIVITY_INSTANCE_ID);
    workflowInstance.nextVariableInstanceId = readLong(dbWorkflowInstance, NEXT_VARIABLE_INSTANCE_ID);
    workflowInstance.lock = readLock((BasicDBObject) dbWorkflowInstance.get(LOCK));
    workflowInstance.jobs = readJobs(readList(dbWorkflowInstance, JOBS));
    
    Map<ActivityInstanceImpl, String> allActivityIds = new HashMap<>();
    readScopeImpl(workflowInstance, dbWorkflowInstance, allActivityIds);
    resolveActivityReferences(workflowInstance, workflow, allActivityIds);
    
    workflowInstance.work = readWork(dbWorkflowInstance, WORK, workflowInstance);
    workflowInstance.workAsync = readWork(dbWorkflowInstance, WORK_ASYNC, workflowInstance);
    workflowInstance.properties = readObjectMap(dbWorkflowInstance, PROPERTIES);
    workflowInstance.setProperty(ORGANIZATION_ID, readObject(dbWorkflowInstance, ORGANIZATION_ID));

    copyProperties(dbWorkflowInstance, workflowInstance);

    return workflowInstance;
  }

  /**
   * Reads database fields (that do not have Java fields) and copies them to workflow instance properties.
   * This makes it possible to write non-standard fields to the database and read them from properties.
   */
  private void copyProperties(BasicDBObject dbWorkflowInstance, WorkflowInstanceImpl workflowInstance) {
    if (dbWorkflowInstance == null || workflowInstance == null) {
      return;
    }
    Set<String> invalidPropertyKeys = Extensible.getInvalidPropertyKeys(WorkflowInstance.class);
    // Map<String,?> mappedBeanFields = mongoMapper.write(workflowInstance.toWorkflowInstance());
    for (String fieldName : dbWorkflowInstance.keySet()) {
      boolean property = !invalidPropertyKeys.contains(fieldName);
      if (property) {
        workflowInstance.setProperty(fieldName, dbWorkflowInstance.get(fieldName));
      }
    }
  }

  protected void readScopeImpl(ScopeInstanceImpl scopeInstance, BasicDBObject dbScopeInstance, Map<ActivityInstanceImpl, String> allActivityIds) {
    scopeInstance.start = readTime(dbScopeInstance, START);
    scopeInstance.end = readTime(dbScopeInstance, END);
    scopeInstance.endState = readString(dbScopeInstance, END_STATE);
    scopeInstance.duration = readLong(dbScopeInstance, DURATION);
    readActivityInstances(scopeInstance, dbScopeInstance, allActivityIds);
    readVariableInstances(scopeInstance, dbScopeInstance);
  }

  protected void readActivityInstances(ScopeInstanceImpl scopeInstance, BasicDBObject dbScopeInstance, Map<ActivityInstanceImpl, String> allActivityIds) {
    Map<Object, ActivityInstanceImpl> allActivityInstances = new LinkedHashMap<>();
    List<BasicDBObject> dbActivityInstances = readList(dbScopeInstance, ACTIVITY_INSTANCES);
    if (dbActivityInstances!=null) {
      for (BasicDBObject dbActivityInstance: dbActivityInstances) {
        ActivityInstanceImpl activityInstance = readActivityInstance(scopeInstance, dbActivityInstance, allActivityIds);
        allActivityInstances.put(activityInstance.id, activityInstance);
        String activityId = readString(dbActivityInstance, ACTIVITY_ID);
        allActivityIds.put(activityInstance, activityId);
        scopeInstance.addActivityInstance(activityInstance);
      }
    }
  }
  
  protected ActivityInstanceImpl readActivityInstance(ScopeInstanceImpl parent, BasicDBObject dbActivityInstance, Map<ActivityInstanceImpl, String> allActivityIds) {
    ActivityInstanceImpl activityInstance = new ActivityInstanceImpl();
    activityInstance.id = readId(dbActivityInstance, ID);
    activityInstance.calledWorkflowInstanceId = readWorkflowInstanceId(dbActivityInstance, CALLED_WORKFLOW_INSTANCE_ID);
    activityInstance.workState = readString(dbActivityInstance, WORK_STATE);
    activityInstance.configuration = configuration;
    activityInstance.parent = parent;
    activityInstance.workflow = parent.workflow;
    activityInstance.workflowInstance = parent.workflowInstance;
    
    readScopeImpl(activityInstance, dbActivityInstance, allActivityIds);
    return activityInstance;
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
    List<BasicDBObject> dbVariableInstances = readList(dbWorkflowInstance, VARIABLE_INSTANCES);
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
    writeTimeOpt(dbLock, Lock.TIME, lock.time);
    writeObjectOpt(dbLock, Lock.OWNER, lock.owner);
    return dbLock;
  }
  
  protected LockImpl readLock(BasicDBObject dbLock) {
    if (dbLock==null) {
      return null;
    }
    LockImpl lock = new LockImpl();
    lock.owner = readString(dbLock, Lock.OWNER);
    lock.time = readTime(dbLock, Lock.TIME);
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
          writeString(dbActivity, PARENT, parentId);
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
        writeListElementOpt(dbScope, VARIABLE_INSTANCES, dbVariable);
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
      Query query = new Query()._ids(workflowInstanceIds);
      DBCursor workflowInstanceCursor = workflowInstancesCollection.find("find-workflow-instance", query.get());
      while (workflowInstanceCursor.hasNext()) {
        BasicDBObject dbWorkflowInstance = (BasicDBObject) workflowInstanceCursor.next();
        WorkflowInstanceImpl workflowInstance = readWorkflowInstanceImpl(dbWorkflowInstance);
        workflowInstanceMap.put(workflowInstance.getId(), workflowInstance);
      }
    }
    return workflowInstanceMap;
  }

  public MongoCollection getWorkflowInstancesCollection() {
    return workflowInstancesCollection;
  }
}
