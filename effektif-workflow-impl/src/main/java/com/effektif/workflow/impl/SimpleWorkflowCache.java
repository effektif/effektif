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
package com.effektif.workflow.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.effektif.workflow.api.command.RequestContext;
import com.effektif.workflow.impl.util.Exceptions;
import com.effektif.workflow.impl.workflow.WorkflowImpl;


/** caches executable workflows */
public class SimpleWorkflowCache implements WorkflowCache {
  
  protected Map<Object, WorkflowImpl> workflows = new ConcurrentHashMap<Object, WorkflowImpl>();

  @Override
  public WorkflowImpl get(String workflowId) {
    RequestContext requestContext = RequestContext.current();
    String organizationId = requestContext!=null ? requestContext.getOrganizationId() : null;
    return workflows.get(getKey(workflowId, organizationId));
  }

  protected Object getKey(String workflowId, String organizationId) {
    Exceptions.checkNotNullParameter(workflowId, "workflowId");
    List<String> key = new ArrayList<>();
    key.add(workflowId);
    if (organizationId!=null) {
      key.add(organizationId);
    }
    return key;
  }

  @Override
  public void put(WorkflowImpl workflow) {
    workflows.put(getKey(workflow.id, workflow.organizationId), workflow);
  }
  
  public Map<Object, WorkflowImpl> getWorkflows() {
    return workflows;
  }
  
  public void setWorkflows(Map<Object, WorkflowImpl> workflows) {
    this.workflows = workflows;
  }
}
