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

import com.effektif.workflow.api.query.WorkflowQuery;
import com.effektif.workflow.api.workflow.Workflow;
import com.effektif.workflow.impl.WorkflowStore;
import com.effektif.workflow.impl.definition.WorkflowImpl;
import com.effektif.workflow.impl.plugin.Initializable;
import com.effektif.workflow.impl.plugin.ServiceRegistry;


public class MemoryWorkflowStore implements WorkflowStore, Initializable {

  protected Map<String, WorkflowImpl> workflows;

  public MemoryWorkflowStore() {
  }

  @Override
  public void initialize(ServiceRegistry serviceRegistry) {
    this.workflows = new ConcurrentHashMap<String, WorkflowImpl>();
  }

  @Override
  public WorkflowImpl createWorkflow() {
    return new WorkflowImpl();
  }

  /** ensures that every element in this process definition has an id */
  @Override
  public String createWorkflowId(WorkflowImpl processDefinition) {
    return UUID.randomUUID().toString();
  }

  @Override
  public void insertWorkflow(WorkflowImpl processDefinition) {
    workflows.put(processDefinition.id, processDefinition);
  }

  @Override
  public List<WorkflowImpl> loadWorkflows(WorkflowQuery query) {
    List<WorkflowImpl> result = null;
    if (query.getWorkflowId()!=null) {
      result = new ArrayList<WorkflowImpl>();
      WorkflowImpl processDefinition = workflows.get(query.getWorkflowId());
      if (processDefinition!=null) {
        result.add(processDefinition);
      }
    } else if (result==null) {
      result = new ArrayList<WorkflowImpl>(workflows.values());
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
  public String findLatestWorkflowIdByName(String workflowName, String organizationId) {
    if (workflowName==null) {
      return null;
    }
    WorkflowImpl latest = null;
    for (WorkflowImpl workflow: workflows.values()) {
      if ( workflowName.equals(workflow.name)
           && (latest==null || workflow.deployTime.isBefore(latest.deployTime))
         ) {
        latest = workflow;
      }
    }
    return latest!=null ? latest.id : null;
  }

  @Override
  public List<Workflow> findWorkflows(WorkflowQuery query) {
    throw new RuntimeException("TODO");
  }

  @Override
  public void deleteWorkflows(WorkflowQuery query) {
    for (Workflow workflow: findWorkflows(query)) {
      workflows.remove(workflow.getId());
    }
  }

  @Override
  public WorkflowImpl findWorkflowImplById(String workflowId, String organizationId) {
    return workflows.get(workflowId);
  }
}
