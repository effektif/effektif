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
package com.effektif.workflow.test.serialization;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.effektif.workflow.api.WorkflowEngine;
import com.effektif.workflow.api.command.AbstractCommand;
import com.effektif.workflow.api.command.MessageCommand;
import com.effektif.workflow.api.command.RequestContext;
import com.effektif.workflow.api.command.StartCommand;
import com.effektif.workflow.api.query.WorkflowInstanceQuery;
import com.effektif.workflow.api.query.WorkflowQuery;
import com.effektif.workflow.api.workflow.Workflow;
import com.effektif.workflow.api.workflowinstance.WorkflowInstance;
import com.effektif.workflow.impl.WorkflowEngineImpl;
import com.effektif.workflow.impl.json.JsonService;


public class SerializingWorkflowEngineImpl extends WorkflowEngineImpl {
  
  private static final Logger log = LoggerFactory.getLogger(SerializingWorkflowEngineImpl.class);
  
  WorkflowEngine workflowEngine;
  JsonService jsonService;

  public SerializingWorkflowEngineImpl(WorkflowEngine workflowEngine, JsonService jsonService) {
    this.workflowEngine = workflowEngine;
    this.jsonService = jsonService;
  }

  protected Workflow wireizeWorkflow(Workflow workflow) {
    workflow = jsonService.serializeWorkflow(workflow);
    workflow = wireize(workflow, Workflow.class);
    return jsonService.deserializeWorkflow(workflow);
  }
  
  protected WorkflowInstance wireizeWorkflowInstance(WorkflowInstance workflowInstance) {
    // serializing is not necessary as the engine ensures that the type is always added
    // workflowInstance = jsonService.serializeWorkflowInstance(workflowInstance);
    workflowInstance = wireize(workflowInstance, WorkflowInstance.class);
    return jsonService.deserializeWorkflowInstance(workflowInstance);
  }
  
  protected <T extends AbstractCommand> T wireizeCommand(T command) {
    command = jsonService.serializeCommand(command);
    command = wireize(command, (Class<T>) command.getClass());
    return jsonService.deserializeCommand(command);
  }

  protected <T> T wireize(Object o, Class<T> type) {
    if (o==null) return null;
    String jsonString = jsonService.objectToJsonStringPretty(o);
    log.debug("wirized: "+jsonString);
    return jsonService.jsonToObject(jsonString, type);
  }
  
  @Override
  public Workflow deployWorkflow(Workflow workflow) {
    return deployWorkflow(workflow, null);
  }

  @Override
  public Workflow deployWorkflow(Workflow workflow, RequestContext requestContext) {
    workflow = wireizeWorkflow(workflow);
    workflow = workflowEngine.deployWorkflow(workflow, requestContext);
    return wireizeWorkflow(workflow);
  }

  @Override
  public Workflow validateWorkflow(Workflow workflow) {
    return validateWorkflow(workflow, null);
  }

  @Override
  public Workflow validateWorkflow(Workflow workflow, RequestContext requestContext) {
    workflow = wireizeWorkflow(workflow);
    requestContext = wireize(requestContext, RequestContext.class);
    workflow = workflowEngine.validateWorkflow(workflow, requestContext);
    return wireizeWorkflow(workflow);
  }

  @Override
  public List<Workflow> findWorkflows(WorkflowQuery workflowQuery) {
    return findWorkflows(workflowQuery, null);
  }

  @Override
  public List<Workflow> findWorkflows(WorkflowQuery query, RequestContext requestContext) {
    query = wireize(query, WorkflowQuery.class);
    requestContext = wireize(requestContext, RequestContext.class);
    List<Workflow> workflows = workflowEngine.findWorkflows(query, requestContext);
    if (workflows==null) {
      return null;
    }
    List<Workflow> wirizedWorkflows = new ArrayList<>(workflows.size());
    for (Workflow workflow: workflows) {
      wirizedWorkflows.add(wireizeWorkflow(workflow));
    }
    return wirizedWorkflows;
  }

  @Override
  public void deleteWorkflows(WorkflowQuery query) {
    deleteWorkflows(query, null);
  }

  @Override
  public void deleteWorkflows(WorkflowQuery query, RequestContext requestContext) {
    query = wireize(query, WorkflowQuery.class);
    requestContext = wireize(requestContext, RequestContext.class);
    workflowEngine.deleteWorkflows(query, requestContext);
  }

  @Override
  public WorkflowInstance startWorkflowInstance(StartCommand startCommand) {
    return startWorkflowInstance(startCommand, null);
  }

  @Override
  public WorkflowInstance startWorkflowInstance(StartCommand startCommand, RequestContext requestContext) {
    startCommand = wireizeCommand(startCommand);
    requestContext = wireize(requestContext, RequestContext.class);
    WorkflowInstance workflowInstance = workflowEngine.startWorkflowInstance(startCommand, requestContext);
    return wireize(workflowInstance, WorkflowInstance.class);
  }

  @Override
  public WorkflowInstance sendMessage(MessageCommand messageCommand) {
    return sendMessage(messageCommand, null);
  }

  @Override
  public WorkflowInstance sendMessage(MessageCommand messageCommand, RequestContext requestContext) {
    messageCommand = wireizeCommand(messageCommand);
    requestContext = wireize(requestContext, RequestContext.class);
    WorkflowInstance workflowInstance = workflowEngine.sendMessage(messageCommand, requestContext);
    return wireizeWorkflowInstance(workflowInstance);
  }

  @Override
  public List<WorkflowInstance> findWorkflowInstances(WorkflowInstanceQuery query) {
    return findWorkflowInstances(query, null);
  }

  @Override
  public List<WorkflowInstance> findWorkflowInstances(WorkflowInstanceQuery query, RequestContext requestContext) {
    query = wireize(query, WorkflowInstanceQuery.class);
    requestContext = wireize(requestContext, RequestContext.class);
    List<WorkflowInstance> workflowInstances = workflowEngine.findWorkflowInstances(query, requestContext);
    if (workflowInstances==null) {
      return null;
    }
    List<WorkflowInstance> wirizedWorkflowInstances = new ArrayList<>(workflowInstances.size());
    for (WorkflowInstance workflowInstance: workflowInstances) {
      wirizedWorkflowInstances.add(wireizeWorkflowInstance(workflowInstance));
    }
    return wirizedWorkflowInstances;
  }

  @Override
  public void deleteWorkflowInstances(WorkflowInstanceQuery query) {
    deleteWorkflowInstances(query, null);
  }

  @Override
  public void deleteWorkflowInstances(WorkflowInstanceQuery query, RequestContext requestContext) {
    query = wireize(query, WorkflowInstanceQuery.class);
    requestContext = wireize(requestContext, RequestContext.class);
    workflowEngine.deleteWorkflowInstances(query, requestContext);
  }
}
