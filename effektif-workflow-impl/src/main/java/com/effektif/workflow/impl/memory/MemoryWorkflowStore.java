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
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.joda.time.LocalDateTime;

import com.effektif.workflow.api.query.WorkflowQuery;
import com.effektif.workflow.impl.WorkflowStore;
import com.effektif.workflow.impl.configuration.Brewable;
import com.effektif.workflow.impl.configuration.Brewery;
import com.effektif.workflow.impl.workflow.WorkflowImpl;


public class MemoryWorkflowStore implements WorkflowStore, Brewable {

  protected Map<String, Long> nextVersionByName;
  protected Map<String, WorkflowImpl> workflows;

  public MemoryWorkflowStore() {
  }

  @Override
  public void brew(Brewery brewery) {
    this.workflows = new ConcurrentHashMap<>();
    this.nextVersionByName = new ConcurrentHashMap<>();
  }
  
  @Override
  public String generateWorkflowId() {
    return UUID.randomUUID().toString();
  }

  @Override
  public void insertWorkflow(WorkflowImpl workflowImpl) {
    workflows.put(workflowImpl.id, workflowImpl);

    String workflowName = workflowImpl.name;
    if (workflowName!=null) {
      Long nextVersion = nextVersionByName.get(workflowName);
      if (nextVersion==null) {
        nextVersion = 1l;
      }
      workflowImpl.version = nextVersion;
      nextVersion++;
      nextVersionByName.put(workflowName, nextVersion);
    }
  }

  @Override
  public List<WorkflowImpl> findWorkflows(WorkflowQuery query) {
    List<WorkflowImpl> result = new ArrayList<>();
    if (query.getWorkflowId()!=null) {
      WorkflowImpl workflow = workflows.get(query.getWorkflowId());
      if (workflow!=null) {
        result.add(workflow);
      }
    } else {
      result = new ArrayList<>(workflows.values());
    }
    if (query.getWorkflowName()!=null && !result.isEmpty()) {
      filterByName(result, query.getWorkflowName());
    }
    if (query.getLimit()!=null) {
      while (result.size()>query.getLimit()) {
        result.remove(result.size()-1);
      }
    }
    return result;
  }
  
  protected void filterByName(List<WorkflowImpl> result, String name) {
    for (int i=result.size()-1; i>=0; i--) {
      if (!name.equals(result.get(i).name)) {
        result.remove(i);
      }
    }
  }

  protected boolean matchesProcessDefinitionCriteria(WorkflowImpl process, WorkflowQuery query) {
    if (query.getWorkflowName()!=null && !query.getWorkflowName().equals(process.name)) {
      return false;
    }
    return true;
  }

  @Override
  public String findLatestWorkflowIdByName(String workflowName) {
    if (workflowName==null) {
      return null;
    }
    WorkflowImpl latestWorkflow = null;
    LocalDateTime latestDeployTime = null;
    for (WorkflowImpl workflow: workflows.values()) {
      if ( workflowName.equals(workflow.name)
           && (latestDeployTime==null || latestDeployTime.isAfter(workflow.deployedTime)) ) {
        latestWorkflow = workflow;
        latestDeployTime = workflow.deployedTime;
      }
    }
    return latestWorkflow!=null ? latestWorkflow.id : null;
  }

  @Override
  public void deleteWorkflows(WorkflowQuery query) {
    for (WorkflowImpl workflow: findWorkflows(query)) {
      workflows.remove(workflow.id);
    }
  }

  @Override
  public WorkflowImpl loadWorkflowById(String workflowId) {
    return workflows.get(workflowId);
  }
}
