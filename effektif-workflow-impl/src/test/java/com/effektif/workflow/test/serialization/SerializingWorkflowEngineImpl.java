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

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.effektif.workflow.api.command.MessageCommand;
import com.effektif.workflow.api.command.RequestContext;
import com.effektif.workflow.api.command.StartCommand;
import com.effektif.workflow.api.query.WorkflowInstanceQuery;
import com.effektif.workflow.api.query.WorkflowQuery;
import com.effektif.workflow.api.workflow.Workflow;
import com.effektif.workflow.api.workflowinstance.WorkflowInstance;
import com.effektif.workflow.impl.ExecutorService;
import com.effektif.workflow.impl.WorkflowCache;
import com.effektif.workflow.impl.WorkflowEngineImpl;
import com.effektif.workflow.impl.WorkflowInstanceEventListener;
import com.effektif.workflow.impl.WorkflowInstanceStore;
import com.effektif.workflow.impl.WorkflowStore;
import com.effektif.workflow.impl.activitytypes.CallerReference;
import com.effektif.workflow.impl.json.JsonService;
import com.effektif.workflow.impl.plugin.ServiceRegistry;
import com.effektif.workflow.impl.workflow.WorkflowImpl;
import com.effektif.workflow.impl.workflowinstance.WorkflowInstanceImpl;


public class SerializingWorkflowEngineImpl extends WorkflowEngineImpl {
  
  private static final Logger log = LoggerFactory.getLogger(SerializingWorkflowEngineImpl.class);
  
  WorkflowEngineImpl workflowEngine;
  JsonService jsonService;

  public SerializingWorkflowEngineImpl(WorkflowEngineImpl workflowEngine) {
    this.workflowEngine = workflowEngine;
    this.jsonService = workflowEngine.getJsonService();
  }

  protected Workflow wireizeWorkflow(Workflow workflow) {
    workflow = jsonService.serializeWorkflow(workflow);
    workflow = wireize(workflow, Workflow.class);
    return jsonService.deserializeWorkflow(workflow);
  }
  
  protected StartCommand wireizeStartCommand(StartCommand startCommand) {
    return wireize(startCommand, StartCommand.class);
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
    return null;
  }

  @Override
  public List<Workflow> findWorkflows(WorkflowQuery workflowQuery, RequestContext requestContext) {
    return null;
  }

  @Override
  public void deleteWorkflows(WorkflowQuery workflowQuery) {
  }

  @Override
  public void deleteWorkflows(WorkflowQuery workflowQuery, RequestContext requestContext) {
  }

  @Override
  public WorkflowInstance startWorkflowInstance(StartCommand startCommand) {
    return startWorkflowInstance(startCommand, null);
  }

  @Override
  public WorkflowInstance startWorkflowInstance(StartCommand startCommand, RequestContext requestContext) {
    startCommand = wireizeStartCommand(startCommand);
    requestContext = wireize(requestContext, RequestContext.class);
    WorkflowInstance workflowInstance = workflowEngine.startWorkflowInstance(startCommand, requestContext);
    return wireize(workflowInstance, WorkflowInstance.class);
  }

  @Override
  public WorkflowInstance sendMessage(MessageCommand messageCommand) {
    return null;
  }

  @Override
  public WorkflowInstance sendMessage(MessageCommand messageCommand, RequestContext requestContext) {
    return null;
  }

  @Override
  public List<WorkflowInstance> findWorkflowInstances(WorkflowInstanceQuery query) {
    return null;
  }

  @Override
  public List<WorkflowInstance> findWorkflowInstances(WorkflowInstanceQuery query, RequestContext requestContext) {
    return null;
  }

  @Override
  public void deleteWorkflowInstances(WorkflowInstanceQuery query) {
  }

  @Override
  public void deleteWorkflowInstances(WorkflowInstanceQuery query, RequestContext requestContext) {
  }

  @Override
  public void startup() {
    workflowEngine.startup();
  }

  @Override
  public void shutdown() {
    workflowEngine.shutdown();
  }

  @Override
  public WorkflowInstance startWorkflowInstance(StartCommand startCommand, CallerReference callerReference, RequestContext requestContext) {
    return workflowEngine.startWorkflowInstance(startCommand, callerReference, requestContext);
  }

  @Override
  public WorkflowImpl getWorkflowImpl(String workflowId, RequestContext requestContext) {
    return workflowEngine.getWorkflowImpl(workflowId, requestContext);
  }

  @Override
  public WorkflowInstanceImpl lockProcessInstanceWithRetry(String workflowInstanceId, String activityInstanceId, RequestContext requestContext) {
    return workflowEngine.lockProcessInstanceWithRetry(workflowInstanceId, activityInstanceId, requestContext);
  }

  @Override
  public String getId() {
    return workflowEngine.getId();
  }

  @Override
  public ServiceRegistry getServiceRegistry() {
    return workflowEngine.getServiceRegistry();
  }

  @Override
  public JsonService getJsonService() {
    return workflowEngine.getJsonService();
  }

  @Override
  public ExecutorService getExecutorService() {
    return workflowEngine.getExecutorService();
  }

  @Override
  public WorkflowCache getProcessDefinitionCache() {
    return workflowEngine.getProcessDefinitionCache();
  }

  @Override
  public WorkflowStore getWorkflowStore() {
    return workflowEngine.getWorkflowStore();
  }

  @Override
  public WorkflowInstanceStore getWorkflowInstanceStore() {
    return workflowEngine.getWorkflowInstanceStore();
  }

  @Override
  public void addListener(WorkflowInstanceEventListener listener) {
    workflowEngine.addListener(listener);
  }

  @Override
  public void removeListener(WorkflowInstanceEventListener listener) {
    workflowEngine.removeListener(listener);
  }

  @Override
  public List<WorkflowInstanceEventListener> getListeners() {
    return workflowEngine.getListeners();
  }
}
