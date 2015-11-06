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
package com.effektif.workflow.test.serialization;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.effektif.workflow.api.WorkflowEngine;
import com.effektif.workflow.api.model.Deployment;
import com.effektif.workflow.api.model.Message;
import com.effektif.workflow.api.model.TriggerInstance;
import com.effektif.workflow.api.model.VariableValues;
import com.effektif.workflow.api.model.WorkflowInstanceId;
import com.effektif.workflow.api.query.WorkflowInstanceQuery;
import com.effektif.workflow.api.query.WorkflowQuery;
import com.effektif.workflow.api.workflow.ExecutableWorkflow;
import com.effektif.workflow.api.workflowinstance.WorkflowInstance;
import com.effektif.workflow.impl.WorkflowEngineImpl;
import com.effektif.workflow.impl.json.JsonStreamMapper;


/**
 * @author Tom Baeyens
 */
public class SerializingWorkflowEngineImpl implements WorkflowEngine {

  protected static final Logger log = LoggerFactory.getLogger(SerializingWorkflowEngineImpl.class+".JSON");

  protected WorkflowEngineImpl workflowEngine;
  protected JsonStreamMapper jsonMapper;
  
  public SerializingWorkflowEngineImpl(WorkflowEngineImpl workflowEngine, JsonStreamMapper jsonStreamMapper) {
    this.workflowEngine = workflowEngine;
    this.jsonMapper = jsonStreamMapper;
  }

  protected <T> T wireize(String name, T o) {
    if (o==null) return null;
    Class<T> clazz = (Class<T>) o.getClass();
    String jsonString = jsonMapper.write(o);
    log.debug(name+jsonString);
    return jsonMapper.readString(jsonString, clazz);
  }

  @Override
  public Deployment deployWorkflow(ExecutableWorkflow workflow) {
    log.debug("deployWorkflow");
    workflow = wireize(" >>workflow>> ", workflow);
    Deployment deployment = workflowEngine.deployWorkflow(workflow);
    return wireize("  <<deployment<< ", deployment);
  }

  @Override
  public List<ExecutableWorkflow> findWorkflows(WorkflowQuery query) {
    log.debug("findWorkflow");
    query = wireize(" >>query>> ", query);
    List<ExecutableWorkflow> workflows = workflowEngine.findWorkflows(query);
    if (workflows==null) {
      return null;
    }
    List<ExecutableWorkflow> wirizedWorkflows = new ArrayList<>(workflows.size());
    for (ExecutableWorkflow workflow: workflows) {
      wirizedWorkflows.add(wireize("  <<workflow<< ", workflow));
    }
    return wirizedWorkflows;
  }

  @Override
  public void deleteWorkflows(WorkflowQuery query) {
    log.debug("deleteWorkflow");
    query = wireize(" >>query>> ", query);
    workflowEngine.deleteWorkflows(query);
  }

  @Override
  public WorkflowInstance start(TriggerInstance triggerInstance) {
    log.debug("startWorkflow");
    triggerInstance = wireize(" >>start>> ", triggerInstance);
    WorkflowInstance workflowInstance = workflowEngine.start(triggerInstance);
    workflowInstance = wireize("  <<workflowInstance<< ", workflowInstance);
    return workflowInstance;
  }

  @Override
  public WorkflowInstance send(Message message) {
    log.debug("sendMessage");
    message = wireize(" >>message>> ", message);
    WorkflowInstance workflowInstance = workflowEngine.send(message);
    workflowInstance = wireize("  <<workflowInstance<< ", workflowInstance);
    return workflowInstance;
  }

  @Override
  public WorkflowInstance move(WorkflowInstanceId workflowInstanceId, String activityInstanceId, String newActivityId) {
    log.debug("moveWorkflowInstance");
    return workflowEngine.move(workflowInstanceId, newActivityId);
  }

  @Override
  public WorkflowInstance move(WorkflowInstanceId workflowInstanceId, String newActivityId) {
    return move(workflowInstanceId, null, newActivityId);
  }

  @Override
  public VariableValues getVariableValues(WorkflowInstanceId workflowInstanceId) {
    log.debug("getVariableValues");
    VariableValues variableValues = workflowEngine.getVariableValues(workflowInstanceId);
    variableValues = wireize("  <<variableValues<< ", variableValues);
    return variableValues;
  }

  @Override
  public VariableValues getVariableValues(WorkflowInstanceId workflowInstanceId, String activityInstanceId) {
    log.debug("getVariableValues");
    VariableValues variableValues = workflowEngine.getVariableValues(workflowInstanceId, activityInstanceId);
    variableValues = wireize("  <<variableValues<< ", variableValues);
    return variableValues;
  }

  @Override
  public void setVariableValues(WorkflowInstanceId workflowInstanceId, VariableValues variableValues) {
    log.debug("setVariableValues");
    variableValues = wireize(" >>variableValues>> ", variableValues);
    workflowEngine.setVariableValues(workflowInstanceId, null, variableValues);
  }

  @Override
  public void setVariableValues(WorkflowInstanceId workflowInstanceId, String activityInstanceId, VariableValues variableValues) {
    log.debug("setVariableValues");
    variableValues = wireize(" >>variableValues>> ", variableValues);
    workflowEngine.setVariableValues(workflowInstanceId, activityInstanceId, variableValues);
  }

  @Override
  public List<WorkflowInstance> findWorkflowInstances(WorkflowInstanceQuery query) {
    log.debug("findWorkflowInstances");
    query = wireize(" >>query>>", query);
    List<WorkflowInstance> workflowInstances = workflowEngine.findWorkflowInstances(query);
    if (workflowInstances==null) {
      return null;
    }
    List<WorkflowInstance> wirizedWorkflowInstances = new ArrayList<>(workflowInstances.size());
    for (WorkflowInstance workflowInstance: workflowInstances) {
      workflowInstance = wireize("  <-workflowInstance-", workflowInstance);
      wirizedWorkflowInstances.add(workflowInstance);
    }
    return wirizedWorkflowInstances;
  }

  @Override
  public void deleteWorkflowInstances(WorkflowInstanceQuery query) {
    log.debug("deleteWorkflowInstances");
    query = wireize(" >>query>>", query);
    workflowEngine.deleteWorkflowInstances(query);
  }

  @Override
  public WorkflowInstance cancel(WorkflowInstanceId workflowInstanceId) {
    log.debug("cancel");
    WorkflowInstance workflowInstance = workflowEngine.cancel(workflowInstanceId);
    workflowInstance = wireize("  <<workflowInstance<< ", workflowInstance);
    return workflowInstance;
  }
}
