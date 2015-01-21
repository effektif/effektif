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

import com.effektif.workflow.api.WorkflowEngine;
import com.effektif.workflow.api.command.AbstractCommand;
import com.effektif.workflow.api.command.Message;
import com.effektif.workflow.api.command.RequestContext;
import com.effektif.workflow.api.command.Start;
import com.effektif.workflow.api.query.WorkflowInstanceQuery;
import com.effektif.workflow.api.query.WorkflowQuery;
import com.effektif.workflow.api.workflow.Workflow;
import com.effektif.workflow.api.workflowinstance.WorkflowInstance;
import com.effektif.workflow.impl.json.JsonService;


public class SerializingWorkflowEngineImpl extends AbstractSerializingService implements WorkflowEngine {
  
  WorkflowEngine workflowEngine;

  public SerializingWorkflowEngineImpl(WorkflowEngine workflowEngine, JsonService jsonService) {
    super(jsonService);
    this.workflowEngine = workflowEngine;
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

  @Override
  public Workflow deployWorkflow(Workflow workflow) {
    workflow = wireizeWorkflow(workflow);
    workflow = workflowEngine.deployWorkflow(workflow);
    return wireizeWorkflow(workflow);
  }

  @Override
  public Workflow validateWorkflow(Workflow workflow) {
    workflow = wireizeWorkflow(workflow);
    workflow = workflowEngine.validateWorkflow(workflow);
    return wireizeWorkflow(workflow);
  }

  @Override
  public List<Workflow> findWorkflows(WorkflowQuery query) {
    query = wireize(query, WorkflowQuery.class);
    List<Workflow> workflows = workflowEngine.findWorkflows(query);
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
    query = wireize(query, WorkflowQuery.class);
    workflowEngine.deleteWorkflows(query);
  }

  @Override
  public WorkflowInstance startWorkflowInstance(Workflow workflow) {
    String workflowId = workflow.getId();
    return startWorkflowInstance(new Start().workflowId(workflowId));
  }

  @Override
  public WorkflowInstance startWorkflowInstance(Start start) {
    start = wireizeCommand(start);
    WorkflowInstance workflowInstance = workflowEngine.startWorkflowInstance(start);
    return wireize(workflowInstance, WorkflowInstance.class);
  }

  @Override
  public WorkflowInstance sendMessage(Message message) {
    message = wireizeCommand(message);
    WorkflowInstance workflowInstance = workflowEngine.sendMessage(message);
    return wireizeWorkflowInstance(workflowInstance);
  }

  @Override
  public List<WorkflowInstance> findWorkflowInstances(WorkflowInstanceQuery query) {
    query = wireize(query, WorkflowInstanceQuery.class);
    List<WorkflowInstance> workflowInstances = workflowEngine.findWorkflowInstances(query);
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
    query = wireize(query, WorkflowInstanceQuery.class);
    workflowEngine.deleteWorkflowInstances(query);
  }

  @Override
  public WorkflowEngine createWorkflowEngine(RequestContext requestContext) {
    throw new RuntimeException("please implement");
  }
}
