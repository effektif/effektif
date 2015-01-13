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

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.effektif.workflow.api.WorkflowEngine;
import com.effektif.workflow.api.command.MessageCommand;
import com.effektif.workflow.api.command.RequestContext;
import com.effektif.workflow.api.command.StartCommand;
import com.effektif.workflow.api.query.WorkflowInstanceQuery;
import com.effektif.workflow.api.query.WorkflowQuery;
import com.effektif.workflow.api.workflow.Workflow;
import com.effektif.workflow.api.workflowinstance.WorkflowInstance;
import com.effektif.workflow.impl.activitytypes.CallerReference;
import com.effektif.workflow.impl.json.JsonService;
import com.effektif.workflow.impl.plugin.ServiceRegistry;
import com.effektif.workflow.impl.workflow.ActivityImpl;
import com.effektif.workflow.impl.workflow.WorkflowImpl;
import com.effektif.workflow.impl.workflow.WorkflowParser;
import com.effektif.workflow.impl.workflowinstance.ActivityInstanceImpl;
import com.effektif.workflow.impl.workflowinstance.LockImpl;
import com.effektif.workflow.impl.workflowinstance.WorkflowInstanceImpl;

public class WorkflowEngineImpl implements WorkflowEngine {

  public static final Logger log = LoggerFactory.getLogger(WorkflowEngine.class);

  public String id;
  public ExecutorService executorService;
  public WorkflowCache workflowCache;
  public WorkflowStore workflowStore;
  public WorkflowInstanceStore workflowInstanceStore;
  public JsonService jsonService;
  public ServiceRegistry serviceRegistry;
  public List<WorkflowInstanceEventListener> listeners;

  protected WorkflowEngineImpl() {
  }

  protected WorkflowEngineImpl(WorkflowEngineConfiguration configuration) {
    this.serviceRegistry = configuration.getServiceRegistry();
    this.serviceRegistry.registerService(this);
    initializeId(configuration);
    this.serviceRegistry.prepare(configuration);
    this.jsonService = serviceRegistry.getService(JsonService.class);
    this.executorService = serviceRegistry.getService(ExecutorService.class);
    this.workflowCache = serviceRegistry.getService(WorkflowCache.class);
    this.workflowStore = serviceRegistry.getService(WorkflowStore.class);
    this.workflowInstanceStore = serviceRegistry.getService(WorkflowInstanceStore.class);
    this.listeners = new ArrayList<>();
  }
  
  protected void initializeId(WorkflowEngineConfiguration configuration) {
    this.id = configuration.getId();
    if (id==null) {
      try {
        id = InetAddress.getLocalHost().getHostAddress();
        try {
          String processName = ManagementFactory.getRuntimeMXBean().getName();
          int atIndex = processName.indexOf('@');
          if (atIndex > 0) {
            id += ":" + processName.substring(0, atIndex);
          }
        } catch (Exception e) {
          id += ":?";
        }
      } catch (UnknownHostException e1) {
        id = UUID.randomUUID().toString();
      }
    }
  }

  public void startup() {
  }

  public void shutdown() {
    executorService.shutdown();
  }

  /// Workflow methods ////////////////////////////////////////////////////////////
  
  @Override
  public Workflow deployWorkflow(Workflow workflowApi) {
    return deployWorkflow(workflowApi, RequestContext.current());
  }

  @Override
  public Workflow deployWorkflow(Workflow workflowApi, RequestContext requestContext) {
    if (log.isDebugEnabled()) {
      log.debug("Deploying workflow");
    }
    if (requestContext!=null) {
      workflowApi.deployedBy(requestContext.getAuthenticatedUserId());
      workflowApi.organizationId(requestContext.getOrganizationId());
    }
    WorkflowParser parser = WorkflowParser.parse(this, workflowApi);
    if (!parser.hasErrors()) {
      WorkflowImpl workflowImpl = parser.getWorkflow();
      String workflowId = workflowStore.generateWorkflowId(); 
      workflowApi.setId(workflowId);
      workflowImpl.id = workflowId;
      workflowStore.insertWorkflow(workflowApi, workflowImpl, requestContext);
      workflowCache.put(workflowImpl);
    }
    return workflowApi;
  }

  @Override
  public Workflow validateWorkflow(Workflow workflowApi) {
    return validateWorkflow(workflowApi, RequestContext.current());
  }
  
  @Override
  public Workflow validateWorkflow(Workflow workflowApi, RequestContext requestContext) {
    if (log.isDebugEnabled()) {
      log.debug("Validating workflow");
    }
    if (requestContext!=null) {
      workflowApi.deployedBy(requestContext.getAuthenticatedUserId());
      workflowApi.organizationId(requestContext.getOrganizationId());
    }
    WorkflowParser.parse(this, workflowApi);
    return workflowApi;
  }

  @Override
  public List<Workflow> findWorkflows(WorkflowQuery workflowQuery) {
    return findWorkflows(workflowQuery, RequestContext.current()); 
  }

  @Override
  public List<Workflow> findWorkflows(WorkflowQuery workflowQuery, RequestContext requestContext) {
    return workflowStore.findWorkflows(workflowQuery, requestContext);
  }
  
  @Override
  public void deleteWorkflows(WorkflowQuery workflowQuery) {
    deleteWorkflows(workflowQuery, RequestContext.current());
  }
    
  @Override
  public void deleteWorkflows(WorkflowQuery workflowQuery, RequestContext requestContext) {
    workflowStore.deleteWorkflows(workflowQuery, requestContext);
  }

  public WorkflowInstance startWorkflowInstance(StartCommand startCommand) {
    return startWorkflowInstance(startCommand, RequestContext.current());
  }
  
  @Override
  public WorkflowInstance startWorkflowInstance(StartCommand startCommand, RequestContext requestContext) {
    return startWorkflowInstance(startCommand, null, requestContext);
  }

  /** caller has to ensure that start.variableValues is not serialized @see VariableRequestImpl#serialize & VariableRequestImpl#deserialize */
  public WorkflowInstance startWorkflowInstance(StartCommand startCommand, CallerReference callerReference, RequestContext requestContext) {
    String workflowId = startCommand.getWorkflowId();
    if (workflowId==null) {
      if (startCommand.getWorkflowName()!=null) {
        workflowId = workflowStore.findLatestWorkflowIdByName(startCommand.getWorkflowName(), requestContext);
        if (workflowId==null) throw new RuntimeException("No workflow found for name '"+startCommand.getWorkflowName()+"'");
      } else {
        throw new RuntimeException("No workflow specified");
      }
    }

    WorkflowImpl workflow = getWorkflowImpl(workflowId, requestContext);
    String workflowInstanceId = workflowInstanceStore.generateWorkflowInstanceId();
    WorkflowInstanceImpl workflowInstance = new WorkflowInstanceImpl(requestContext, this, workflow, workflowInstanceId);
    if (callerReference!=null) {
      workflowInstance.callerWorkflowInstanceId = callerReference.callerWorkflowInstanceId;
      workflowInstance.callerActivityInstanceId = callerReference.callerActivityInstanceId;
    }
    workflowInstance.setVariableValues(startCommand.getVariableValues());
    if (log.isDebugEnabled()) log.debug("Starting "+workflowInstance);
    workflowInstance.start = Time.now();
    if (workflow.startActivities!=null) {
      for (ActivityImpl startActivityDefinition: workflow.startActivities) {
        workflowInstance.execute(startActivityDefinition);
      }
    }
    LockImpl lock = new LockImpl();
    lock.setTime(Time.now());
    lock.setOwner(getId());
    workflowInstance.setLock(lock);
    workflowInstanceStore.insertWorkflowInstance(workflowInstance);
    workflowInstance.executeWork();
    return workflowInstance.toWorkflowInstance();
  }
  
  @Override
  public WorkflowInstance sendMessage(MessageCommand message) {
    return sendMessage(message, RequestContext.current());
  }
  
  @Override
  public WorkflowInstance sendMessage(MessageCommand message, RequestContext requestContext) {
    WorkflowInstanceImpl workflowInstance = lockProcessInstanceWithRetry(message.getWorkflowInstanceId(), message.getActivityInstanceId(), requestContext);
    workflowInstance.setVariableValues(message.getVariableValues());
    ActivityInstanceImpl activityInstance = workflowInstance.findActivityInstance(message.getActivityInstanceId());
    if (activityInstance.isEnded()) {
      throw new RuntimeException("Activity instance "+activityInstance+" is already ended");
    }
    if (log.isDebugEnabled())
      log.debug("Signalling "+activityInstance);
    ActivityImpl activity = activityInstance.getActivity();
    activity.activityType.message(activityInstance);
    workflowInstance.executeWork();
    return workflowInstance.toWorkflowInstance();
  }

  @Override
  public void deleteWorkflowInstances(WorkflowInstanceQuery query) {
    deleteWorkflowInstances(query, RequestContext.current());
  }


  @Override
  public void deleteWorkflowInstances(WorkflowInstanceQuery query, RequestContext requestContext) {
    workflowInstanceStore.deleteWorkflowInstances(query, requestContext);
  }

  @Override
  public List<WorkflowInstance> findWorkflowInstances(WorkflowInstanceQuery query) {
    return findWorkflowInstances(query, RequestContext.current());
  }
  
  @Override
  public List<WorkflowInstance> findWorkflowInstances(WorkflowInstanceQuery query, RequestContext requestContext) {
    List<WorkflowInstanceImpl> workflowInstances = workflowInstanceStore.findWorkflowInstances(query, requestContext);
    return WorkflowInstanceImpl.toWorkflowInstances(workflowInstances);
  }
  
  /** retrieves the executable form of the workflow using the workflow cache */
  public WorkflowImpl getWorkflowImpl(String workflowId, RequestContext requestContext) {
    WorkflowImpl workflowImpl = workflowCache.get(workflowId, requestContext);
    if (workflowImpl==null) {
      Workflow workflow = workflowStore.loadWorkflowById(workflowId, requestContext);
      workflowImpl = WorkflowParser.parse(this, workflow)
        .checkNoErrors() // throws runtime exception if there are errors
        .getWorkflow();
      workflowCache.put(workflowImpl);
    }
    return workflowImpl;
  }
  
  public WorkflowInstanceImpl lockProcessInstanceWithRetry(
          final String workflowInstanceId, 
          final String activityInstanceId,
          final RequestContext requestContext) {
    Retry<WorkflowInstanceImpl> retry = new Retry<WorkflowInstanceImpl>() {
      @Override
      public WorkflowInstanceImpl tryOnce() {
        return workflowInstanceStore.lockWorkflowInstance(workflowInstanceId, activityInstanceId, requestContext);
      }
      @Override
      protected void failedWaitingForRetry() {
        if (log.isDebugEnabled()) {
          log.debug("Locking workflow instance "+workflowInstanceId+" failed... retrying in "+wait+" millis");
        }
      }
      @Override
      protected void interrupted() {
        if (log.isDebugEnabled()) {
          log.debug("Waiting for workflow instance lock was interrupted");
        }
      }
      @Override
      protected void failedPermanent() {
        throw new RuntimeException("Couldn't lock process instance with workflowInstanceId="+workflowInstanceId+" and activityInstanceId="+activityInstanceId);
      }
    };
    return retry.tryManyTimes();
  }

  public String getId() {
    return id;
  }

  public ServiceRegistry getServiceRegistry() {
    return serviceRegistry;
  }
  
  public JsonService getJsonService() {
    return jsonService;
  }
  
  public ExecutorService getExecutorService() {
    return executorService;
  }

  public WorkflowCache getProcessDefinitionCache() {
    return workflowCache;
  }

  public WorkflowStore getWorkflowStore() {
    return workflowStore;
  }
  
  public WorkflowInstanceStore getWorkflowInstanceStore() {
    return workflowInstanceStore;
  }

  public void addListener(WorkflowInstanceEventListener listener) {
    synchronized (listener) {
      listeners.add(listener);
    }
  }

  public void removeListener(WorkflowInstanceEventListener listener) {
    synchronized (listener) {
      listeners.remove(listener);
    }
  }

  public List<WorkflowInstanceEventListener> getListeners() {
    return Collections.unmodifiableList(listeners);
  }
}
