/* Copyright 2014 Effektif GmbH.
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
package com.effektif.workflow.impl.instance;

import static com.effektif.workflow.impl.instance.ActivityInstanceImpl.STATE_NOTIFYING;
import static com.effektif.workflow.impl.instance.ActivityInstanceImpl.STATE_STARTING;
import static com.effektif.workflow.impl.instance.ActivityInstanceImpl.STATE_STARTING_MULTI_CONTAINER;
import static com.effektif.workflow.impl.instance.ActivityInstanceImpl.STATE_STARTING_MULTI_INSTANCE;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.joda.time.Duration;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.effektif.workflow.api.WorkflowEngine;
import com.effektif.workflow.api.workflowinstance.WorkflowInstance;
import com.effektif.workflow.impl.ExecutorService;
import com.effektif.workflow.impl.Time;
import com.effektif.workflow.impl.WorkflowEngineImpl;
import com.effektif.workflow.impl.WorkflowInstanceStore;
import com.effektif.workflow.impl.activitytypes.CallImpl;
import com.effektif.workflow.impl.definition.ActivityImpl;
import com.effektif.workflow.impl.definition.WorkflowImpl;


public class WorkflowInstanceImpl extends ScopeInstanceImpl {
  
  public static final Logger log = LoggerFactory.getLogger(WorkflowEngine.class);

  public LockImpl lock;
  public Queue<ActivityInstanceImpl> work;
  public Queue<ActivityInstanceImpl> workAsync;
  public String organizationId;
  public String callerWorkflowInstanceId;
  public String callerActivityInstanceId;
  public Boolean isAsync;

  public WorkflowInstanceImpl() {
  }
  
  public WorkflowInstanceImpl(WorkflowEngineImpl processEngine, WorkflowImpl workflow, String processInstanceId) {
    this.id = processInstanceId;
    this.workflowEngine = processEngine;
    this.organizationId = workflow.organizationId;
    this.workflow = workflow;
    this.scope = workflow;
    this.workflowInstance = this;
    this.start = Time.now();
    initializeVariableInstances();
    if (log.isDebugEnabled()) log.debug("Created "+workflowInstance);
  }
  
  public WorkflowInstance toWorkflowInstance() {
    WorkflowInstance w = new WorkflowInstance();
    w.setWorkflowId(workflow.id);
    w.setCallerWorkflowInstanceId(callerWorkflowInstanceId);
    w.setCallerActivityInstanceId(callerActivityInstanceId);
    toScopeInstance(w);
    return w;
  }
  
  public void executeWork() {
    WorkflowInstanceStore workflowInstanceStore = workflowEngine.getWorkflowInstanceStore();
    boolean isFirst = true;
    while (hasWork()) {
      // in the first iteration, the updates will be empty and hence no updates will be flushed
      if (isFirst) {
        isFirst = false;
      } else {
        workflowInstanceStore.flush(this); 
      }
      ActivityInstanceImpl activityInstance = getNextWork();
      ActivityImpl activity = activityInstance.getActivity();
      
      if (STATE_STARTING.equals(activityInstance.workState)) {
        if (log.isDebugEnabled())
          log.debug("Starting "+activityInstance);
        activityInstance.execute();
        
      } else if (STATE_STARTING_MULTI_INSTANCE.equals(activityInstance.workState)) {
        if (log.isDebugEnabled())
          log.debug("Starting multi instance "+activityInstance);
        activityInstance.execute();
        
      } else if (STATE_STARTING_MULTI_CONTAINER.equals(activityInstance.workState)) {
        List<Object> values = null;
        if (activity.multiInstance!=null) {
          values = activityInstance.getValue(activity.multiInstance.collection);
        }
        if (values!=null && !values.isEmpty()) {
          if (log.isDebugEnabled())
            log.debug("Starting multi container "+activityInstance);
          for (Object value: values) {
            ActivityInstanceImpl elementActivityInstance = activityInstance.createActivityInstance(activity);
            elementActivityInstance.setWorkState(STATE_STARTING_MULTI_INSTANCE); 
            elementActivityInstance.initializeForEachElement(activity.multiInstance.elementVariable, value);
          }
        } else {
          if (log.isDebugEnabled())
            log.debug("Skipping empty multi container "+activityInstance);
          activityInstance.onwards();
        }
  
      } else if (STATE_NOTIFYING.equals(activityInstance.workState)) {
        if (log.isDebugEnabled())
          log.debug("Notifying parent of "+activityInstance);
        activityInstance.parent.ended(activityInstance);
        activityInstance.workState = null;
      }
    }
    if (hasAsyncWork()) {
      if (log.isDebugEnabled())
        log.debug("Going asynchronous "+workflowInstance);
      workflowInstanceStore.flush(workflowInstance);
      ExecutorService executor = workflowEngine.getExecutorService();
      executor.execute(new Runnable(){
        public void run() {
          try {
            workflowInstance.work = workflowInstance.workAsync;
            workflowInstance.workAsync = null;
            workflowInstance.workflowInstance.isAsync = true;
            if (workflowInstance.updates!=null) {
              workflowInstance.getUpdates().isWorkChanged = true;
              workflowInstance.getUpdates().isAsyncWorkChanged = true;
            }
            workflowInstance.executeWork();
          } catch (Throwable e) {
            e.printStackTrace();
          }
        }});
    } else {
      workflowInstanceStore.flushAndUnlock(workflowInstance.workflowInstance);
    }
  }
  
  public void workflowInstanceEnded() {
    if (callerWorkflowInstanceId!=null) {
      WorkflowInstanceImpl callerProcessInstance = workflowEngine.lockProcessInstanceWithRetry(
              workflowInstance.callerWorkflowInstanceId,
              workflowInstance.callerActivityInstanceId);
      ActivityInstanceImpl callerActivityInstance = callerProcessInstance.findActivityInstance(callerActivityInstanceId);
      if (callerActivityInstance.isEnded()) {
        throw new RuntimeException("Call activity instance "+callerActivityInstance+" is already ended");
      }
      if (log.isDebugEnabled()) log.debug("Notifying caller "+callerActivityInstance);
      ActivityImpl activityDefinition = callerActivityInstance.getActivity();
      CallImpl callActivity = (CallImpl) activityDefinition.activityType;
      callActivity.calledProcessInstanceEnded(callerActivityInstance, workflowInstance);
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

  @Override
  public void ended(ActivityInstanceImpl activityInstance) {
    if (!hasOpenActivityInstances()) {
      end();
    }
  }
  
  public void end() {
    if (this.end==null) {
      if (hasOpenActivityInstances()) {
        throw new RuntimeException("Can't end this process instance. There are open activity instances: "+this);
      }
      setEnd(Time.now());
      if (log.isDebugEnabled())
        log.debug("Ends "+this);
      workflowInstanceEnded();
    }
  }

  public String toString() {
    return "("+(id!=null ? id.toString() : Integer.toString(System.identityHashCode(this)))+"|wi)";
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
      this.duration = new Duration(start.toDateTime(), end.toDateTime()).getMillis();
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
  public boolean isProcessInstance() {
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
}
