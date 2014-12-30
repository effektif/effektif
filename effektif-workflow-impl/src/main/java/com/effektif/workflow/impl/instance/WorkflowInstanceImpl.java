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

import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import org.joda.time.Duration;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.effektif.workflow.api.WorkflowEngine;
import com.effektif.workflow.api.workflowinstance.WorkflowInstance;
import com.effektif.workflow.impl.Time;
import com.effektif.workflow.impl.WorkflowEngineImpl;
import com.effektif.workflow.impl.definition.WorkflowImpl;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;




/**
 * @author Walter White
 */
@JsonPropertyOrder({"id", "workflowId", "start", "end", "duration", "activityInstances", "variableInstances"})
public class WorkflowInstanceImpl extends ScopeInstanceImpl implements WorkflowInstance {
  
  public static final Logger log = LoggerFactory.getLogger(WorkflowEngine.class);

  public String workflowId;
  public LockImpl lock;
  public Queue<ActivityInstanceImpl> work;
  public Queue<ActivityInstanceImpl> workAsync;
  public String organizationId;
  public String callerWorkflowInstanceId;
  public String callerActivityInstanceId;
  
  @JsonIgnore
  public Boolean isAsync;
  
  @JsonIgnore
  public Map<String, Object> transientContext;

  public WorkflowInstanceImpl() {
  }
  
  public WorkflowInstanceImpl(WorkflowEngineImpl processEngine, WorkflowImpl workflow, String processInstanceId) {
    this.id = processInstanceId;
    this.workflowEngine = processEngine;
    this.organizationId = workflow.organizationId;
    this.workflow = workflow;
    this.workflowId = workflow.id;
    this.scopeDefinition = workflow;
    this.workflowInstance = this;
    this.start = Time.now();
    initializeVariableInstances();
    if (log.isDebugEnabled())
      log.debug("Created "+workflowInstance);
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
      workflowEngine.executeWorkflowInstanceEnded(this);
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

  public LockImpl getLock() {
    return lock;
  }

  public void setLock(LockImpl lock) {
    this.lock = lock;
    if (updates!=null) {
      getUpdates().isLockChanged = true;
    }
  }
  
  public void setWorkflowId(String processDefinitionId) {
    this.workflowId = processDefinitionId;
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
  
  public Object getTransientContextObject(String key) {
    return transientContext!=null ? transientContext.get(key) : null;
  }
  
  /** getter for casting convenience */ 
  @Override
  public WorkflowInstanceUpdates getUpdates() {
    return (WorkflowInstanceUpdates) updates;
  }


  @Override
  public String getWorkflowId() {
    return workflowId;
  }

  @Override
  public boolean isProcessInstance() {
    return true;
  }

  public String getOrganizationId() {
    return organizationId;
  }
  
  public void setOrganizationId(String organizationId) {
    this.organizationId = organizationId;
  }

  public void trackUpdates(boolean isNew) {
    if (updates==null) {
      updates = new WorkflowInstanceUpdates(isNew);
    } else {
      updates.reset(isNew);
    }
    super.trackUpdates(isNew);
  }

  @Override
  public Map<String, Object> getTransientContext() {
    return transientContext;
  }
}
