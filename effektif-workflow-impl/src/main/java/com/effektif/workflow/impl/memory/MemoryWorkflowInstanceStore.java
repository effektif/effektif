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
package com.effektif.workflow.impl.memory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;

import com.effektif.workflow.api.model.WorkflowInstanceId;
import com.effektif.workflow.api.query.WorkflowInstanceQuery;
import com.effektif.workflow.impl.WorkflowEngineImpl;
import com.effektif.workflow.impl.WorkflowInstanceStore;
import com.effektif.workflow.impl.configuration.Brewable;
import com.effektif.workflow.impl.configuration.Brewery;
import com.effektif.workflow.impl.job.Job;
import com.effektif.workflow.impl.util.Lists;
import com.effektif.workflow.impl.util.Time;
import com.effektif.workflow.impl.workflowinstance.LockImpl;
import com.effektif.workflow.impl.workflowinstance.WorkflowInstanceImpl;


/**
 * @author Tom Baeyens
 */
public class MemoryWorkflowInstanceStore implements WorkflowInstanceStore, Brewable {
  
  private static final Logger log = WorkflowEngineImpl.log;

  protected String workflowEngineId;
  protected Map<WorkflowInstanceId, WorkflowInstanceImpl> workflowInstances;
  protected Set<WorkflowInstanceId> lockedWorkflowInstanceIds;
  
  public MemoryWorkflowInstanceStore() {
  }

  @Override
  public void brew(Brewery brewery) {
    initializeWorkflowInstances();
    this.workflowEngineId = brewery.get(WorkflowEngineImpl.class).id;
  }

  protected void initializeWorkflowInstances() {
    this.workflowInstances = new ConcurrentHashMap<>();
    this.lockedWorkflowInstanceIds = Collections.newSetFromMap(new ConcurrentHashMap<WorkflowInstanceId, Boolean>());
  }
  
  @Override
  public WorkflowInstanceId generateWorkflowInstanceId() {
    return new WorkflowInstanceId(UUID.randomUUID().toString());
  }

  @Override
  public void insertWorkflowInstance(WorkflowInstanceImpl workflowInstance) {
    workflowInstances.put(workflowInstance.id, workflowInstance);
  }

  @Override
  public void flush(WorkflowInstanceImpl workflowInstance) {
  }

  @Override
  public void flushAndUnlock(WorkflowInstanceImpl workflowInstance) {
    lockedWorkflowInstanceIds.remove(workflowInstance.id);
    workflowInstance.removeLock();
    workflowInstance.notifyUnlockListeners();
  }
  
  @Override
  public void unlockWorkflowInstance(WorkflowInstanceImpl workflowInstance) {
    if (workflowInstance!=null) {
      workflowInstance.removeLock();
      lockedWorkflowInstanceIds.remove(workflowInstance.id);
      workflowInstance.notifyUnlockListeners();
    }
  }

  @Override
  public List<WorkflowInstanceImpl> findWorkflowInstances(WorkflowInstanceQuery query) {
    if (query.getWorkflowInstanceId()!=null) {
      WorkflowInstanceImpl workflowInstance = workflowInstances.get(query.getWorkflowInstanceId());
      if (workflowInstance!=null && workflowInstance.isIncluded(query)) {
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
    for (WorkflowInstanceImpl workflowInstance: findWorkflowInstances(workflowInstanceQuery)) {
      workflowInstances.remove(workflowInstance.id);
    }
  }

  @Override
  public WorkflowInstanceImpl getWorkflowInstanceImplById(WorkflowInstanceId workflowInstanceId) {
    if (workflowInstanceId==null) {
      return null;
    }
    return workflowInstances.get(workflowInstanceId);
  }

  @Override
  public WorkflowInstanceImpl lockWorkflowInstance(WorkflowInstanceId workflowInstanceId) {
    WorkflowInstanceQuery query = new WorkflowInstanceQuery()
      .workflowInstanceId(workflowInstanceId);
    query.setLimit(1);
    List<WorkflowInstanceImpl> workflowInstances = findWorkflowInstances(query);
    if (workflowInstances==null || workflowInstances.isEmpty()) { 
      throw new RuntimeException("Process instance doesn't exist");
    }
    WorkflowInstanceImpl workflowInstance = workflowInstances.get(0);
    workflowInstanceId = workflowInstance.id;
    lockWorkflowInstance(workflowInstance);
    return workflowInstance;
  }

  @Override
  public WorkflowInstanceImpl lockWorkflowInstanceWithJobsDue() {
    Iterator<WorkflowInstanceImpl> iterator = this.workflowInstances.values().iterator();
    while (iterator.hasNext()) {
      WorkflowInstanceImpl workflowInstance = iterator.next();
      if (workflowInstance.jobs!=null) {
        for (Job job: workflowInstance.jobs) {
          if (job.isDue()) {
            lockWorkflowInstance(workflowInstance);
            return workflowInstance;
          }
        }
      }
    }
    return null;
  }

  public synchronized void lockWorkflowInstance(WorkflowInstanceImpl workflowInstance) {
    WorkflowInstanceId workflowInstanceId = workflowInstance.getId();
    if (lockedWorkflowInstanceIds.contains(workflowInstanceId)) {
      throw new RuntimeException("Process instance "+workflowInstanceId+" is already locked");
    }
    lockedWorkflowInstanceIds.add(workflowInstanceId);
    LockImpl lock = new LockImpl();
    lock.setTime(Time.now());
    lock.setOwner(workflowEngineId);
    workflowInstance.setLock(lock);
    if (log.isDebugEnabled()) { 
      log.debug("Locked process instance "+workflowInstanceId);
    }
  }


  @Override
  public void deleteAllWorkflowInstances() {
    initializeWorkflowInstances();
  }
}
