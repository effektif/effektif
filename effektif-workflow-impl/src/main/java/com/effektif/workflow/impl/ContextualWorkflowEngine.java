/* Copyright (c) 2014, Effektif GmbH.
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

import java.util.List;

import com.effektif.workflow.api.WorkflowEngine;
import com.effektif.workflow.api.command.Message;
import com.effektif.workflow.api.command.RequestContext;
import com.effektif.workflow.api.command.Start;
import com.effektif.workflow.api.query.WorkflowInstanceQuery;
import com.effektif.workflow.api.query.WorkflowQuery;
import com.effektif.workflow.api.workflow.Workflow;
import com.effektif.workflow.api.workflowinstance.WorkflowInstance;


public class ContextualWorkflowEngine implements WorkflowEngine {

  WorkflowEngineImpl workflowEngine;
  RequestContext requestContext;
  
  public ContextualWorkflowEngine(WorkflowEngineImpl workflowEngine, RequestContext requestContext) {
    this.workflowEngine = workflowEngine;
    this.requestContext = requestContext;
  }

  @Override
  public Workflow deployWorkflow(Workflow workflow) {
    try {
      RequestContext.set(requestContext);
      return workflowEngine.deployWorkflow(workflow);
    } finally {
      RequestContext.unset();
    }
  }

  @Override
  public Workflow validateWorkflow(Workflow workflow) {
    try {
      RequestContext.set(requestContext);
    } finally {
      RequestContext.unset();
    }
    return workflowEngine.validateWorkflow(workflow);
  }

  @Override
  public List<Workflow> findWorkflows(WorkflowQuery query) {
    try {
      RequestContext.set(requestContext);
      return workflowEngine.findWorkflows(query);
    } finally {
      RequestContext.unset();
    }
  }

  @Override
  public void deleteWorkflows(WorkflowQuery query) {
    try {
      RequestContext.set(requestContext);
      workflowEngine.deleteWorkflows(query);
    } finally {
      RequestContext.unset();
    }
  }

  @Override
  public WorkflowInstance startWorkflowInstance(Start start) {
    try {
      RequestContext.set(requestContext);
      return workflowEngine.startWorkflowInstance(start);
    } finally {
      RequestContext.unset();
    }
  }

  @Override
  public WorkflowInstance sendMessage(Message message) {
    try {
      RequestContext.set(requestContext);
      return workflowEngine.sendMessage(message);
    } finally {
      RequestContext.unset();
    }
  }

  @Override
  public List<WorkflowInstance> findWorkflowInstances(WorkflowInstanceQuery query) {
    try {
      RequestContext.set(requestContext);
      return workflowEngine.findWorkflowInstances(query);
    } finally {
      RequestContext.unset();
    }
  }

  @Override
  public void deleteWorkflowInstances(WorkflowInstanceQuery query) {
    try {
      RequestContext.set(requestContext);
      workflowEngine.deleteWorkflowInstances(query);
    } finally {
      RequestContext.unset();
    }
  }

  @Override
  public WorkflowEngine createWorkflowEngine(RequestContext requestContext) {
    return new ContextualWorkflowEngine(workflowEngine, requestContext);
  }

  @Override
  public WorkflowInstance startWorkflowInstance(Workflow workflow) {
    String workflowId = workflow.getId();
    return startWorkflowInstance(new Start().workflowId(workflowId));
  }
}
