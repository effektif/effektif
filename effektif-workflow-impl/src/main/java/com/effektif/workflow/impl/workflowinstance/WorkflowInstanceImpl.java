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
package com.effektif.workflow.impl.workflowinstance;

import static com.effektif.workflow.impl.workflowinstance.ActivityInstanceImpl.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.effektif.workflow.api.Configuration;
import com.effektif.workflow.api.WorkflowEngine;
import com.effektif.workflow.api.model.TriggerInstance;
import com.effektif.workflow.api.model.WorkflowInstanceId;
import com.effektif.workflow.api.query.WorkflowInstanceQuery;
import com.effektif.workflow.api.workflowinstance.WorkflowInstance;
import com.effektif.workflow.impl.ExecutorService;
import com.effektif.workflow.impl.WorkflowEngineImpl;
import com.effektif.workflow.impl.WorkflowInstanceStore;
import com.effektif.workflow.impl.activity.ActivityType;
import com.effektif.workflow.impl.activity.types.CallImpl;
import com.effektif.workflow.impl.job.Job;
import com.effektif.workflow.impl.util.Lists;
import com.effektif.workflow.impl.util.Time;
import com.effektif.workflow.impl.workflow.ActivityImpl;
import com.effektif.workflow.impl.workflow.MultiInstanceImpl;
import com.effektif.workflow.impl.workflow.WorkflowImpl;


/**
 * @author Tom Baeyens
 */
public class WorkflowInstanceImpl extends ScopeInstanceImpl {
  
  public static final Logger log = LoggerFactory.getLogger(WorkflowEngine.class);

  public WorkflowInstanceId id;
  public String businessKey;
  public LockImpl lock;
  public Queue<ActivityInstanceImpl> work;
  public Queue<ActivityInstanceImpl> workAsync;
  public WorkflowInstanceId callerWorkflowInstanceId;
  public String callerActivityInstanceId;
  public List<String> startActivityIds;
  public Boolean isAsync;
  public Long nextActivityInstanceId;
  public Long nextVariableInstanceId;
  public List<Job> jobs;
  
  /** local cache of the locked workflow instance for the purpose of the 
   * call activity.  in case the subprocess is fully synchronous and it 
   * finishes and wants to continue the parent, that parent is already 
   * locked in the db.  the call activity will first check this cache to see 
   * if the workflow instance is already locked and use this one instead of 
   * going to the db. */
  public Map<WorkflowInstanceId,WorkflowInstanceImpl> lockedWorkflowInstances;

  public WorkflowInstanceImpl() {
  }

  public WorkflowInstanceImpl(
          Configuration configuration, 
          WorkflowImpl workflow, 
          WorkflowInstanceId workflowInstanceId, 
          TriggerInstance triggerInstance,
          LockImpl lock) {
    this.id = workflowInstanceId;
    this.configuration = configuration;
    this.workflow = workflow;
    this.scope = workflow;
    this.workflowInstance = this;
    this.start = Time.now();
    this.nextActivityInstanceId = 1l;
    this.nextVariableInstanceId = 1l;
    this.businessKey = triggerInstance.getBusinessKey();
    this.callerWorkflowInstanceId = triggerInstance.getCallerWorkflowInstanceId();
    this.callerActivityInstanceId = triggerInstance.getCallerActivityInstanceId();
    this.startActivityIds = triggerInstance.getStartActivityIds();
    this.lock = lock;
    this.initializeVariableInstances();
  }

  public WorkflowInstance toWorkflowInstance() {
    return toWorkflowInstance(false);
  }
  
  public WorkflowInstance toWorkflowInstance(boolean includeWorkState) {
    WorkflowInstance workflowInstance = new WorkflowInstance();
    workflowInstance.setId(id);
    workflowInstance.setBusinessKey(businessKey);
    workflowInstance.setWorkflowId(workflow.id);
    workflowInstance.setCallerWorkflowInstanceId(callerWorkflowInstanceId);
    workflowInstance.setCallerActivityInstanceId(callerActivityInstanceId);
    toScopeInstance(workflowInstance, includeWorkState);
    return workflowInstance;
  }
  
  public static List<WorkflowInstance> toWorkflowInstances(List<WorkflowInstanceImpl> workflowInstanceImpls) {
    if (workflowInstanceImpls==null) {
      return null;
    }
    List<WorkflowInstance> workflowInstances = new ArrayList<>();
    for (WorkflowInstanceImpl workflowInstance: workflowInstanceImpls) {
      workflowInstances.add(workflowInstance.toWorkflowInstance());
    }
    return workflowInstances;
  }
  
  public void executeWork() {
    WorkflowInstanceStore workflowInstanceStore = configuration.get(WorkflowInstanceStore.class);
    boolean isFirst = true;
    while (hasWork()) {
      ActivityInstanceImpl activityInstance = getNextWork();
      ActivityImpl activity = activityInstance.getActivity();
      ActivityType activityType = activity.activityType;

      // in the first iteration, the updates will be empty and hence no updates will be flushed
      if (isFirst || activityType.isFlushSkippable()) {
        isFirst = false;
      } else {
        workflowInstanceStore.flush(this); 
      }
      
      if (STATE_STARTING.equals(activityInstance.workState)) {
        if (log.isDebugEnabled())
          log.debug("Starting "+activityInstance);
        activityInstance.execute();
        
      } else if (STATE_STARTING_MULTI_INSTANCE.equals(activityInstance.workState)) {
        if (log.isDebugEnabled())
          log.debug("Starting multi instance "+activityInstance);
        activityInstance.execute();
        
      } else if (STATE_STARTING_MULTI_CONTAINER.equals(activityInstance.workState)) {
        Collection<Object> values = null;
        MultiInstanceImpl multiInstance = activityType.getMultiInstance();
        if (multiInstance!=null && multiInstance.valuesBindings!=null) {
          Object value = activityInstance.getValues(multiInstance.valuesBindings);
          if (value!=null) {
            if (value instanceof Collection) {
              values = (Collection<Object>) value;
            } else {
              values = Lists.of(value);
            }
          }
        }
        if (values!=null) {
          if (log.isDebugEnabled()) {
            log.debug("Starting multi instance container "+activityInstance);
          }
          for (Object element: values) {
            ActivityInstanceImpl elementActivityInstance = activityInstance.createActivityInstance(activity);
            elementActivityInstance.setWorkState(STATE_STARTING_MULTI_INSTANCE);
            elementActivityInstance.initializeForEachElement(multiInstance.elementVariable, element);
          }
        } else {
          if (log.isDebugEnabled()) {
            log.debug("Skipping empty multi instance container "+activityInstance);
          }
          activityInstance.onwards();
        }
  
      } else if (STATE_PROPAGATE_TO_PARENT.equals(activityInstance.workState)) {
        if (log.isDebugEnabled()) {
          log.debug("Propagating end of "+activityInstance+" to parent "+activityInstance.parent);
        }
        activityInstance.parent.activityInstanceEnded(activityInstance);
        activityInstance.workState = null;
      } else if (activityInstance.workState==null) {
        if (log.isDebugEnabled()) {
          log.debug("Activity instance "+activityInstance+" is completely done");
        }
      }
    }
    if (hasAsyncWork()) {
      if (log.isDebugEnabled())
        log.debug("Going asynchronous "+this);
      workflowInstanceStore.flush(this);
      Runnable asyncContinuation = new Runnable(){
        public void run() {
          try {
            work = workAsync;
            workAsync = null;
            isAsync = true;
            if (updates!=null) {
              getUpdates().isWorkChanged = true;
              getUpdates().isAsyncWorkChanged = true;
            }
            executeWork();
          } catch (Throwable e) {
            e.printStackTrace();
          }
        }};
        WorkflowEngineImpl workflowEngine = configuration.get(WorkflowEngineImpl.class);
        workflowEngine.continueAsync(asyncContinuation);
    } else {
      workflowInstanceStore.flushAndUnlock(this);
    }
  }
  
  public void addLockedWorkflowInstance(WorkflowInstanceImpl lockedWorkflowInstance) {
    if (lockedWorkflowInstances==null) {
      lockedWorkflowInstances = new HashMap<>();
    }
    lockedWorkflowInstances.put(lockedWorkflowInstance.getId(), lockedWorkflowInstance);
  }

  public void workflowInstanceEnded() {
    if (callerWorkflowInstanceId!=null) {
      WorkflowInstanceImpl callerProcessInstance = null;
      if (lockedWorkflowInstances!=null) {
        // the lockedWorkflowInstances is a local cache of the locked workflow instances 
        // which is passed down to the sub workflow instance in the call activity.  
        // in case the subprocess is fully synchronous and it 
        // finishes and wants to continue the parent, that parent is already 
        // locked in the db.  the call activity will first check this cache to see 
        // if the workflow instance is already locked and use this one instead of 
        // going to the db.
        callerProcessInstance = lockedWorkflowInstances.get(workflowInstance.callerWorkflowInstanceId);
      }
      if (callerProcessInstance==null) {
        WorkflowEngineImpl workflowEngine = configuration.get(WorkflowEngineImpl.class);
        callerProcessInstance = workflowEngine.lockWorkflowInstanceWithRetry(
                workflowInstance.callerWorkflowInstanceId);
        if (callerProcessInstance==null) {
          log.error("Couldn't continue calling activity instance after workflow instance completion");
        }
      }
      ActivityInstanceImpl callerActivityInstance = callerProcessInstance.findActivityInstance(callerActivityInstanceId);
      if (log.isDebugEnabled()) log.debug("Notifying caller "+callerActivityInstance);
      ActivityImpl activityDefinition = callerActivityInstance.getActivity();
      CallImpl callActivity = (CallImpl) activityDefinition.activityType;
      callActivity.calledWorkflowInstanceEnded(callerActivityInstance, workflowInstance);
      callerActivityInstance.onwards();
      callerProcessInstance.executeWork();
    }
  }

  public void addWork(ActivityInstanceImpl activityInstance) {
    if (isWorkAsync(activityInstance)) {
      addAsyncWork(activityInstance);
    } else {
      addSyncWork(activityInstance);
    }
  }
  
  protected boolean isWorkAsync(ActivityInstanceImpl activityInstance) {
    // if this workflow instance is already running in an async thread, 
    // the new work should be done sync in this thread.
    if (Boolean.TRUE.equals(isAsync)) {
      return false;
    }
    if (!ActivityInstanceImpl.START_WORKSTATES.contains(activityInstance.workState)) {
      return false;
    }
    return activityInstance.getActivity().activityType.isAsync(activityInstance);
  }

  protected void addSyncWork(ActivityInstanceImpl activityInstance) {
    if (work==null) {
      work = new LinkedList<>();
    }
    work.add(activityInstance);
    if (updates!=null) {
      getUpdates().isWorkChanged = true;
    }
  }

  protected void addAsyncWork(ActivityInstanceImpl activityInstance) {
    if (workAsync==null) {
      workAsync = new LinkedList<>();
    }
    workAsync.add(activityInstance);
    if (updates!=null) {
      getUpdates().isAsyncWorkChanged = true;
    }
  }

  public ActivityInstanceImpl getNextWork() {
    ActivityInstanceImpl nextWork = work!=null ? work.poll() : null;
    if (nextWork!=null && updates!=null) {
      getUpdates().isWorkChanged = true;
    }
    return nextWork;
  }

  public boolean hasAsyncWork() {
    return workAsync!=null && !workAsync.isEmpty();
  }

  public boolean hasWork() {
    return work!=null && !work.isEmpty();
  }

  public void endAndPropagateToParent() {
    if (this.end==null) {
      if (hasOpenActivityInstances()) {
        throw new RuntimeException("Can't end this process instance. There are open activity instances: "+this);
      }
      setEnd(Time.now());
      if (log.isDebugEnabled()) {
        log.debug("Ends "+this);
      }
      workflowInstanceEnded();
    }
  }

  public String toString() {
    return "("+(workflow.sourceWorkflowId!=null?workflow.sourceWorkflowId+"|":"")+(id!=null ? id.toString() : Integer.toString(System.identityHashCode(this)))+")";
  }

  public void removeLock() {
    setLock(null);
    if (updates!=null) {
      getUpdates().isLockChanged = true;
    }
  }

  public void setLock(LockImpl lock) {
    this.lock = lock;
    if (updates!=null) {
      getUpdates().isLockChanged = true;
    }
  }
  
  public void setEnd(LocalDateTime end) {
    this.end = end;
    if (start!=null && end!=null) {
      this.duration = end.toDate().getTime()-start.toDate().getTime();
    }
    if (updates!=null) {
      getUpdates().isEndChanged = true;
    }
  }
  
  /** getter for casting convenience */ 
  @Override
  public WorkflowInstanceUpdates getUpdates() {
    return (WorkflowInstanceUpdates) updates;
  }

  @Override
  public boolean isWorkflowInstance() {
    return true;
  }

  public void trackUpdates(boolean isNew) {
    if (updates==null) {
      updates = new WorkflowInstanceUpdates(isNew);
    } else {
      updates.reset(isNew);
    }
    super.trackUpdates(isNew);
  }

  /***
   * isIncluded
   * @param query, with any combination of ActivityId and WorkflowInstanceId set or not set.
   *               When set, the value is taken into account, otherwise it is ignored.
   *               If both ActivityId and WorkflowInstanceId are null (empty query), true is returned
   */
  public boolean isIncluded(WorkflowInstanceQuery query) {

    if (query.getActivityId() == null && query.getWorkflowInstanceId() == null) return true;

    if (query.getWorkflowInstanceId()!=null
            && query.getWorkflowInstanceId().equals(id)) {
      return true;
    }

    if (query.getActivityId()!=null && hasActivityInstances()) {
      for (ActivityInstanceImpl activityInstance : activityInstances) {
        if (activityInstance.activity.getId().equals(query.getActivityId())
                && !activityInstance.isEnded()) {
          return true;
        }
      }
    }

    return false;
  }

  public String generateNextActivityInstanceId() {
    if (updates!=null) {
      getUpdates().isNextActivityInstanceIdChanged = true;
    }
    return Long.toString(nextActivityInstanceId++);
  }

  public String generateNextVariableInstanceId() {
    if (updates!=null) {
      getUpdates().isNextVariableInstanceIdChanged = true;
    }
    return Long.toString(nextVariableInstanceId++);
  }
  
  public void addJob(Job job) {
    if (jobs==null) {
      jobs = new ArrayList<>();
    }
    jobs.add(job);
    if (updates!=null) {
      getUpdates().isJobsChanged = true;
    }
  }
  public void removeJob(Job job) {
    if (jobs!=null) {
      jobs.remove(job);
    }
    if (updates!=null) {
      getUpdates().isJobsChanged = true;
    }
  }

  public WorkflowInstanceId getId() {
    return this.id;
  }
}
