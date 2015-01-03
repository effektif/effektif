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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.effektif.workflow.api.query.WorkflowInstanceQuery;
import com.effektif.workflow.impl.Time;
import com.effektif.workflow.impl.WorkflowEngineImpl;
import com.effektif.workflow.impl.WorkflowInstanceStore;
import com.effektif.workflow.impl.definition.ActivityImpl;
import com.effektif.workflow.impl.definition.VariableImpl;
import com.effektif.workflow.impl.definition.WorkflowImpl;
import com.effektif.workflow.impl.instance.ActivityInstanceImpl;
import com.effektif.workflow.impl.instance.LockImpl;
import com.effektif.workflow.impl.instance.ScopeInstanceImpl;
import com.effektif.workflow.impl.instance.VariableInstanceImpl;
import com.effektif.workflow.impl.instance.WorkflowInstanceImpl;
import com.effektif.workflow.impl.plugin.Initializable;
import com.effektif.workflow.impl.plugin.ServiceRegistry;
import com.effektif.workflow.impl.util.Lists;


public class MemoryWorkflowInstanceStore implements WorkflowInstanceStore, Initializable {

  protected String workflowEngineId;
  protected Map<String, WorkflowInstanceImpl> workflowInstances;
  protected Set<String> lockedWorkflowInstances;
  
  public MemoryWorkflowInstanceStore() {
  }

  @Override
  public void initialize(ServiceRegistry serviceRegistry) {
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
  public List<WorkflowInstanceImpl> findWorkflowInstances(WorkflowInstanceQuery workflowInstanceQuery) {
    if (workflowInstanceQuery.workflowInstanceId!=null) {
      return Lists.of(workflowInstances.get(workflowInstanceQuery.workflowInstanceId));
    }
    List<WorkflowInstanceImpl> workflowInstances = new ArrayList<>();
    for (WorkflowInstanceImpl processInstance: this.workflowInstances.values()) {
      if (meetsConditions(processInstance, workflowInstanceQuery)) {
        workflowInstances.add(processInstance);
      }
    }
    return workflowInstances;
  }

  @Override
  public long countWorkflowInstances(WorkflowInstanceQuery workflowInstanceQuery) {
    return 0;
  }

  @Override
  public void deleteWorkflowInstances(WorkflowInstanceQuery workflowInstanceQuery) {
    for (WorkflowInstanceImpl workflowInstance: findWorkflowInstances(workflowInstanceQuery)) {
      workflowInstances.remove(workflowInstance.id);
    }
  }

  @Override
  public WorkflowInstanceImpl lockWorkflowInstance(String workflowInstanceId, String activityInstanceId) {
    WorkflowInstanceQuery query = new WorkflowInstanceQuery()
    .workflowInstanceId(workflowInstanceId)
    .activityInstanceId(activityInstanceId);
    query.setLimit(1);
    List<WorkflowInstanceImpl> workflowInstances = findWorkflowInstances(query);
    if (workflowInstances==null || workflowInstances.isEmpty()) { 
      throw new RuntimeException("Process instance doesn't exist");
    }
    WorkflowInstanceImpl workflowInstance = workflowInstances.get(0);
    String processInstanceId = workflowInstance.id;
    if (lockedWorkflowInstances.contains(processInstanceId)) {
      throw new RuntimeException("Process instance "+processInstanceId+" is already locked");
    }
    lockedWorkflowInstances.add(processInstanceId);
    LockImpl lock = new LockImpl();
    lock.setTime(Time.now());
    lock.setOwner(workflowEngineId);
    workflowInstance.setLock(lock);
    //if (log.isDebugEnabled())
    //  log.debug("Locked process instance: "+jsonService.objectToJsonStringPretty(processInstance));
    return workflowInstance;
  }
  
  public boolean meetsConditions(WorkflowInstanceImpl processInstance, WorkflowInstanceQuery processInstanceQuery) {
    if (processInstanceQuery.activityInstanceId!=null && !containsCompositeInstance(processInstance, processInstanceQuery.activityInstanceId)) {
      return false;
    }
    return true;
  }

  boolean containsCompositeInstance(ScopeInstanceImpl scopeInstance, Object activityInstanceId) {
    if (scopeInstance.hasActivityInstances()) {
      for (ActivityInstanceImpl activityInstance : scopeInstance.activityInstances) {
        if (containsActivityInstance(activityInstance, activityInstanceId)) {
          return true;
        }
      }
    }
    return false;
  }

  boolean containsActivityInstance(ActivityInstanceImpl activityInstance, Object activityInstanceId) {
    if (activityInstanceId.equals(activityInstance.id)) {
      return true;
    }
    return containsCompositeInstance(activityInstance, activityInstanceId);
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
