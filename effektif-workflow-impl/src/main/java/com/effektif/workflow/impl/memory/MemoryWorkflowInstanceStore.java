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
package com.effektif.workflow.impl.memory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;

import com.effektif.workflow.api.query.WorkflowInstanceQuery;
import com.effektif.workflow.api.workflowinstance.WorkflowInstance;
import com.effektif.workflow.impl.Time;
import com.effektif.workflow.impl.WorkflowEngineConfiguration;
import com.effektif.workflow.impl.WorkflowEngineImpl;
import com.effektif.workflow.impl.WorkflowInstanceStore;
import com.effektif.workflow.impl.plugin.Initializable;
import com.effektif.workflow.impl.plugin.ServiceRegistry;
import com.effektif.workflow.impl.util.Lists;
import com.effektif.workflow.impl.workflow.ActivityImpl;
import com.effektif.workflow.impl.workflow.VariableImpl;
import com.effektif.workflow.impl.workflow.WorkflowImpl;
import com.effektif.workflow.impl.workflowinstance.ActivityInstanceImpl;
import com.effektif.workflow.impl.workflowinstance.LockImpl;
import com.effektif.workflow.impl.workflowinstance.ScopeInstanceImpl;
import com.effektif.workflow.impl.workflowinstance.VariableInstanceImpl;
import com.effektif.workflow.impl.workflowinstance.WorkflowInstanceImpl;


public class MemoryWorkflowInstanceStore implements WorkflowInstanceStore, Initializable<WorkflowEngineConfiguration> {
  
  private static final Logger log = WorkflowEngineImpl.log;

  protected String workflowEngineId;
  protected Map<String, WorkflowInstanceImpl> workflowInstances;
  protected Set<String> lockedWorkflowInstances;
  
  public MemoryWorkflowInstanceStore() {
  }

  @Override
  public void initialize(ServiceRegistry serviceRegistry, WorkflowEngineConfiguration configuration) {
    this.workflowInstances = new ConcurrentHashMap<>();
    this.lockedWorkflowInstances = Collections.synchronizedSet(new HashSet<String>());
    this.workflowEngineId = serviceRegistry.getService(WorkflowEngineImpl.class).getId();
  }

  @Override
  public void insertWorkflowInstance(WorkflowInstanceImpl processInstance) {
    workflowInstances.put(processInstance.id, processInstance);
  }

  @Override
  public void flush(WorkflowInstanceImpl processInstance) {
  }

  @Override
  public void flushAndUnlock(WorkflowInstanceImpl processInstance) {
    lockedWorkflowInstances.remove(processInstance.id);
    processInstance.removeLock();
  }
  
  @Override
  public List<WorkflowInstance> findWorkflowInstances(WorkflowInstanceQuery query) {
    List<WorkflowInstanceImpl> workflowInstances = findWorkflowInstanceImpls(query);
    return WorkflowInstanceImpl.toWorkflowInstances(workflowInstances);
  }
  
  public List<WorkflowInstanceImpl> findWorkflowInstanceImpls(WorkflowInstanceQuery query) {
    if (query.getWorkflowInstanceId()!=null) {
      WorkflowInstanceImpl workflowInstance = workflowInstances.get(query.getWorkflowInstanceId());
      if (workflowInstance.isIncluded(query)) {
        return Lists.of(workflowInstance);
      } else {
        return Collections.EMPTY_LIST;
      }
    }
    List<WorkflowInstanceImpl> workflowInstances = new ArrayList<>();
    Iterator<WorkflowInstanceImpl> iterator = this.workflowInstances.values().iterator();
    int limit = query.getLimit()!=null ? query.getLimit() : Integer.MAX_VALUE;
    while (iterator.hasNext() && workflowInstances.size()<limit) {
      WorkflowInstanceImpl workflowInstance = iterator.next();
      if (workflowInstance.isIncluded(query)) {
        workflowInstances.add(workflowInstance);
      }
    }
    return workflowInstances;
  }

  @Override
  public void deleteWorkflowInstances(WorkflowInstanceQuery workflowInstanceQuery) {
    for (WorkflowInstanceImpl workflowInstance: findWorkflowInstanceImpls(workflowInstanceQuery)) {
      workflowInstances.remove(workflowInstance.id);
    }
  }

  @Override
  public WorkflowInstanceImpl lockWorkflowInstance(String workflowInstanceId, String activityInstanceId) {
    WorkflowInstanceQuery query = new WorkflowInstanceQuery()
      .workflowInstanceId(workflowInstanceId)
      .activityInstanceId(activityInstanceId);
    query.setLimit(1);
    List<WorkflowInstanceImpl> workflowInstances = findWorkflowInstanceImpls(query);
    if (workflowInstances==null || workflowInstances.isEmpty()) { 
      throw new RuntimeException("Process instance doesn't exist");
    }
    WorkflowInstanceImpl workflowInstance = workflowInstances.get(0);
    workflowInstanceId = workflowInstance.id;
    if (lockedWorkflowInstances.contains(workflowInstanceId)) {
      throw new RuntimeException("Process instance "+workflowInstanceId+" is already locked");
    }
    lockedWorkflowInstances.add(workflowInstanceId);
    LockImpl lock = new LockImpl();
    lock.setTime(Time.now());
    lock.setOwner(workflowEngineId);
    workflowInstance.setLock(lock);
    if (log.isDebugEnabled()) { 
      log.debug("Locked process instance "+workflowInstanceId);
    }
    return workflowInstance;
  }
  
  public WorkflowInstanceImpl createWorkflowInstance(WorkflowImpl workflow) {
    return new WorkflowInstanceImpl(workflow.workflowEngine, workflow, createId());
  }

  /** instantiates and assign an id.
   * parent and activityDefinition are only passed for reference.  
   * Apart from choosing the activity instance class to instantiate and assigning the id,
   * this method does not need to link the parent or the activityDefinition. */
  public ActivityInstanceImpl createActivityInstance(ScopeInstanceImpl parent, ActivityImpl activity) {
    return new ActivityInstanceImpl(parent, activity, createId());
  }

  public VariableInstanceImpl createVariableInstance(ScopeInstanceImpl parent, VariableImpl variable) {
    return new VariableInstanceImpl(parent, variable, createId());
  }

  protected String createId() {
    return UUID.randomUUID().toString();
  }
}
