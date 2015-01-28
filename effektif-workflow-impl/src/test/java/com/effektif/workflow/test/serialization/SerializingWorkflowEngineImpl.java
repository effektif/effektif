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

  @Override
  public Workflow deployWorkflow(Workflow workflow) {
    log.debug("deployWorkflow");
    workflow = wireize("  -workflow->", workflow, Workflow.class);
    workflow = workflowEngine.deployWorkflow(workflow);
    return wireize("  <-workflow-", workflow, Workflow.class);
  }

  @Override
  public Workflow validateWorkflow(Workflow workflow) {
    log.debug("validateWorkflow");
    workflow = wireize("  -workflow->", workflow, Workflow.class);
    workflow = workflowEngine.validateWorkflow(workflow);
    return wireize("  <-workflow-", workflow, Workflow.class);
  }

  @Override
  public List<Workflow> findWorkflows(WorkflowQuery query) {
    log.debug("findWorkflow");
    query = wireize("  -query->", query, WorkflowQuery.class);
    List<Workflow> workflows = workflowEngine.findWorkflows(query);
    if (workflows==null) {
      return null;
    }
    List<Workflow> wirizedWorkflows = new ArrayList<>(workflows.size());
    for (Workflow workflow: workflows) {
      wirizedWorkflows.add(wireize("  <-workflow-", workflow, Workflow.class));
    }
    return wirizedWorkflows;
  }

  @Override
  public void deleteWorkflows(WorkflowQuery query) {
    log.debug("deleteWorkflow");
    query = wireize("  -query->", query, WorkflowQuery.class);
    workflowEngine.deleteWorkflows(query);
  }

  @Override
  public WorkflowInstance startWorkflowInstance(Workflow workflow) {
    String workflowId = workflow.getId();
    return startWorkflowInstance(new Start().workflowId(workflowId));
  }

  @Override
  public WorkflowInstance startWorkflowInstance(Start start) {
    log.debug("startWorkflow");
    start = wireize("  -start->", start, Start.class);
    WorkflowInstance workflowInstance = workflowEngine.startWorkflowInstance(start);
    return wireize("  <-workflowInstance-", workflowInstance, WorkflowInstance.class);
  }

  @Override
  public WorkflowInstance sendMessage(Message message) {
    log.debug("sendMessage");
    message = wireize("  -message->", message, Message.class);
    WorkflowInstance workflowInstance = workflowEngine.sendMessage(message);
    return wireize("  <-workflowInstance-", workflowInstance, WorkflowInstance.class);
  }

  @Override
  public List<WorkflowInstance> findWorkflowInstances(WorkflowInstanceQuery query) {
    log.debug("findWorkflowInstances");
    query = wireize("  -query->", query, WorkflowInstanceQuery.class);
    List<WorkflowInstance> workflowInstances = workflowEngine.findWorkflowInstances(query);
    if (workflowInstances==null) {
      return null;
    }
    List<WorkflowInstance> wirizedWorkflowInstances = new ArrayList<>(workflowInstances.size());
    for (WorkflowInstance workflowInstance: workflowInstances) {
      wirizedWorkflowInstances.add(wireize("  <-workflowInstance-", workflowInstance, WorkflowInstance.class));
    }
    return wirizedWorkflowInstances;
  }

  @Override
  public void deleteWorkflowInstances(WorkflowInstanceQuery query) {
    log.debug("deleteWorkflowInstances");
    query = wireize("  -query->", query, WorkflowInstanceQuery.class);
    workflowEngine.deleteWorkflowInstances(query);
  }

  @Override
  public WorkflowEngine createWorkflowEngine(RequestContext requestContext) {
    throw new RuntimeException("please implement");
  }
}