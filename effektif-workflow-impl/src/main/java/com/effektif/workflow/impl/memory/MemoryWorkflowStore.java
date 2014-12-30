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
import com.effektif.workflow.impl.WorkflowStore;
import com.effektif.workflow.impl.definition.WorkflowImpl;
import com.effektif.workflow.impl.plugin.ServiceRegistry;


/**
 * @author Walter White
 */
public class MemoryWorkflowStore implements WorkflowStore {

  protected Map<String, WorkflowImpl> workflows;

  public MemoryWorkflowStore() {
  }

  public MemoryWorkflowStore(ServiceRegistry serviceRegistry) {
    this.workflows = new ConcurrentHashMap<String, WorkflowImpl>();
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
    if (query.id!=null) {
      result = new ArrayList<WorkflowImpl>();
      WorkflowImpl processDefinition = workflows.get(query.id);
      if (processDefinition!=null) {
        result.add(processDefinition);
      }
    } else if (result==null) {
      result = new ArrayList<WorkflowImpl>(workflows.values());
    }
    if (query.name!=null && !result.isEmpty()) {
      filterByName(result, query.name);
    }
    if (query.limit!=null) {
      while (result.size()>query.limit) {
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
    if (query.name!=null && !query.name.equals(process.name)) {
      return false;
    }
    return true;
  }

  @Override
  public void deleteWorkflow(String workflowId) {
    workflows.remove(workflowId);
  }
}
