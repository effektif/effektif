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
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.joda.time.LocalDateTime;

import com.effektif.workflow.api.model.WorkflowId;
import com.effektif.workflow.api.query.WorkflowQuery;
import com.effektif.workflow.api.workflow.ExecutableWorkflow;
import com.effektif.workflow.impl.WorkflowStore;
import com.effektif.workflow.impl.configuration.Brewable;
import com.effektif.workflow.impl.configuration.Brewery;


/**
 * @author Tom Baeyens
 */
public class MemoryWorkflowStore implements WorkflowStore, Brewable {

  protected Map<WorkflowId, ExecutableWorkflow> workflows;

  public MemoryWorkflowStore() {
  }

  @Override
  public void brew(Brewery brewery) {
    initializeWorkflows();
  }

  protected void initializeWorkflows() {
    this.workflows = new ConcurrentHashMap<>();
  }
  
  @Override
  public WorkflowId generateWorkflowId() {
    return new WorkflowId(UUID.randomUUID().toString());
  }

  @Override
  public void insertWorkflow(ExecutableWorkflow workflow) {
    workflows.put(workflow.getId(), workflow);
  }

  @Override
  public List<ExecutableWorkflow> findWorkflows(WorkflowQuery query) {
    if (query==null) {
      query = new WorkflowQuery();
    }
    List<ExecutableWorkflow> result = new ArrayList<>();
    if (query.getWorkflowId()!=null) {
      ExecutableWorkflow workflow = workflows.get(query.getWorkflowId());
      if (workflow!=null) {
        result.add(workflow);
      }
    } else {
      result = new ArrayList<>(workflows.values());
    }
    if (query.getWorkflowSource()!=null && !result.isEmpty()) {
      filterByName(result, query.getWorkflowSource());
    }
    if (query.getLimit()!=null) {
      while (result.size()>query.getLimit()) {
        result.remove(result.size()-1);
      }
    }
    return result;
  }
  
  protected void filterByName(List<ExecutableWorkflow> result, String name) {
    for (int i=result.size()-1; i>=0; i--) {
      if (!name.equals(result.get(i).getSourceWorkflowId())) {
        result.remove(i);
      }
    }
  }

  @Override
  public WorkflowId findLatestWorkflowIdBySource(String workflowName) {
    if (workflowName==null) {
      return null;
    }
    ExecutableWorkflow latestWorkflow = null;
    LocalDateTime latestDeployTime = null;
    for (ExecutableWorkflow workflow: workflows.values()) {
      if ( workflowName.equals(workflow.getSourceWorkflowId())
           && (latestDeployTime==null || latestDeployTime.isAfter(workflow.getCreateTime())) ) {
        latestWorkflow = workflow;
        latestDeployTime = workflow.getCreateTime();
      }
    }
    return latestWorkflow!=null ? latestWorkflow.getId() : null;
  }

  @Override
  public void deleteWorkflows(WorkflowQuery query) {
    for (ExecutableWorkflow workflow: findWorkflows(query)) {
      workflows.remove(workflow.getId());
    }
  }
  
  @Override
  public void deleteAllWorkflows() {
    initializeWorkflows();
  }


  @Override
  public ExecutableWorkflow loadWorkflowById(WorkflowId workflowId) {
    return workflows.get(workflowId);
  }
}
