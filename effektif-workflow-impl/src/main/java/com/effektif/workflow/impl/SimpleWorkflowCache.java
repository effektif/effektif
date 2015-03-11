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
package com.effektif.workflow.impl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.effektif.workflow.api.model.WorkflowId;
import com.effektif.workflow.impl.workflow.WorkflowImpl;


/** caches executable workflows */
public class SimpleWorkflowCache implements WorkflowCache {
  
  protected Map<WorkflowId, WorkflowImpl> workflows = new ConcurrentHashMap<WorkflowId, WorkflowImpl>();

  @Override
  public WorkflowImpl get(WorkflowId workflowId) {
    return workflows.get(workflowId);
  }

  @Override
  public void put(WorkflowImpl workflow) {
    workflows.put(workflow.id, workflow);
  }
  
  public Map<WorkflowId, WorkflowImpl> getWorkflows() {
    return workflows;
  }
  
  public void setWorkflows(Map<WorkflowId, WorkflowImpl> workflows) {
    this.workflows = workflows;
  }
}
