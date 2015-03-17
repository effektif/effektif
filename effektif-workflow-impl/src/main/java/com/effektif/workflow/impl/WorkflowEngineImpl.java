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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.effektif.workflow.api.Configuration;
import com.effektif.workflow.api.WorkflowEngine;
import com.effektif.workflow.api.model.Deployment;
import com.effektif.workflow.api.model.Message;
import com.effektif.workflow.api.model.TriggerInstance;
import com.effektif.workflow.api.model.WorkflowId;
import com.effektif.workflow.api.model.WorkflowInstanceId;
import com.effektif.workflow.api.query.WorkflowInstanceQuery;
import com.effektif.workflow.api.query.WorkflowQuery;
import com.effektif.workflow.api.workflow.Workflow;
import com.effektif.workflow.api.workflowinstance.WorkflowInstance;
import com.effektif.workflow.impl.activity.ActivityTypeService;
import com.effektif.workflow.impl.configuration.Brewable;
import com.effektif.workflow.impl.configuration.Brewery;
import com.effektif.workflow.impl.data.DataTypeService;
import com.effektif.workflow.impl.json.JsonService;
import com.effektif.workflow.impl.json.SerializedMessage;
import com.effektif.workflow.impl.json.SerializedTriggerInstance;
import com.effektif.workflow.impl.util.Time;
import com.effektif.workflow.impl.workflow.ActivityImpl;
import com.effektif.workflow.impl.workflow.TransitionImpl;
import com.effektif.workflow.impl.workflow.VariableImpl;
import com.effektif.workflow.impl.workflow.WorkflowImpl;
import com.effektif.workflow.impl.workflowinstance.ActivityInstanceImpl;
import com.effektif.workflow.impl.workflowinstance.LockImpl;
import com.effektif.workflow.impl.workflowinstance.WorkflowInstanceImpl;

/**
 * @author Tom Baeyens
 */
public class WorkflowEngineImpl implements WorkflowEngine, Brewable {

  public static final Logger log = LoggerFactory.getLogger(WorkflowEngine.class);

  public String id;
  public ExecutorService executorService;
  public WorkflowCache workflowCache;
  public WorkflowStore workflowStore;
  public WorkflowInstanceStore workflowInstanceStore;
  public JsonService jsonService;
  public Brewery brewery;
  public Configuration configuration;
  public List<WorkflowExecutionListener> workflowExecutionListeners;
  
  @Override
  public void brew(Brewery brewery) {
    this.id = brewery.get(WorkflowEngineConfiguration.class).getWorkflowEngineId();
    this.configuration = brewery.get(Configuration.class);
    this.jsonService = brewery.get(JsonService.class);
    this.executorService = brewery.get(ExecutorService.class);
    this.workflowCache = brewery.get(WorkflowCache.class);
    this.workflowStore = brewery.get(WorkflowStore.class);
    this.workflowInstanceStore = brewery.get(WorkflowInstanceStore.class);
    this.brewery = brewery;
    
    // ensuring the default activity types are registered
    brewery.get(ActivityTypeService.class);
    // ensuring the default data types are registered
    brewery.get(DataTypeService.class);
  }
  
  public void startup() {
  }

  public void shutdown() {
    executorService.shutdown();
  }

  /// Workflow methods ////////////////////////////////////////////////////////////
  
  @Override
  public Deployment deployWorkflow(Workflow workflowApi) {
    if (log.isDebugEnabled()) {
      log.debug("Deploying workflow");
    }
    
    WorkflowParser parser = WorkflowParser.parse(configuration, workflowApi);

    if (!parser.hasErrors()) {
      WorkflowImpl workflowImpl = parser.getWorkflow();
      WorkflowId workflowId; 
      if (workflowApi.getId()==null) {
        workflowId = workflowStore.generateWorkflowId();
        workflowApi.setId(workflowId);
      }
      workflowApi.setCreateTime(Time.now());
      workflowImpl.id = workflowApi.getId();
      workflowStore.insertWorkflow(workflowApi);
      if (workflowImpl.trigger!=null) {
        workflowImpl.trigger.published(workflowImpl);
      }
      workflowCache.put(workflowImpl);
    }
    
    return new Deployment(workflowApi.getId(), parser.getIssues());
  }
  
  @Override
  public List<Workflow> findWorkflows(WorkflowQuery workflowQuery) {
    return workflowStore.findWorkflows(workflowQuery);
  }
  
  @Override
  public void deleteWorkflows(WorkflowQuery workflowQuery) {
    workflowStore.deleteWorkflows(workflowQuery);
  }

  /** caller has to ensure that start.variableValues is not serialized @see VariableRequestImpl#serialize & VariableRequestImpl#deserialize */
  public WorkflowInstance start(TriggerInstance triggerInstance) {
    WorkflowInstanceImpl workflowInstance = startInitialize(triggerInstance);
    return startExecute(workflowInstance);
  }

  /** first part of starting a new workflow instance: creating the workflow instance and applying the trigger data */
  public WorkflowInstanceImpl startInitialize(TriggerInstance triggerInstance) {
    WorkflowId workflowId = getLatestWorkflowId(triggerInstance);
    WorkflowImpl workflow = getWorkflowImpl(workflowId);

    LockImpl lock = new LockImpl();
    lock.setTime(Time.now());
    lock.setOwner(getId());

    WorkflowInstanceId workflowInstanceId = workflowInstanceStore.generateWorkflowInstanceId();
    
    WorkflowInstanceImpl workflowInstance = new WorkflowInstanceImpl(configuration, workflow, workflowInstanceId);
    
    workflowInstance.businessKey = triggerInstance.getBusinessKey();
    workflowInstance.caseId = triggerInstance.getCaseId();
    workflowInstance.callerWorkflowInstanceId = triggerInstance.getCallerWorkflowInstanceId();
    workflowInstance.callerActivityInstanceId = triggerInstance.getCallerActivityInstanceId();
    workflowInstance.start = Time.now();
    workflowInstance.lock = lock;

    if (triggerInstance instanceof SerializedTriggerInstance) {
      if (workflow.trigger!=null) {
        workflow.trigger.deserializeTriggerInstance(triggerInstance, workflow);
      } else {
        deserializeTriggerInstanceData(triggerInstance, workflow);
      }
    }

    if (workflow.trigger!=null) {
      workflow.trigger.applyTriggerData(workflowInstance, triggerInstance);
    } else {
      workflowInstance.setVariableValues(triggerInstance.getData());
    }

    return workflowInstance;
  }

  /** second part of starting a new workflow instance: executing the start actvities */
  public WorkflowInstance startExecute(WorkflowInstanceImpl workflowInstance) {
    WorkflowImpl workflow = workflowInstance.workflow;
    if (log.isDebugEnabled()) log.debug("Starting "+workflowInstance);
    
    if (workflow.startActivities!=null) {
      for (ActivityImpl startActivityDefinition: workflow.startActivities) {
        workflowInstance.execute(startActivityDefinition);
      }
    } else {
      workflowInstance.end();
    }
    
    workflowInstanceStore.insertWorkflowInstance(workflowInstance);
    workflowInstance.executeWork();
    return workflowInstance.toWorkflowInstance();
  }

  public WorkflowId getLatestWorkflowId(TriggerInstance triggerInstance) {
    WorkflowId workflowId = triggerInstance.getWorkflowId();
    if (workflowId==null) {
      if (triggerInstance.getSourceWorkflowId()!=null) {
        workflowId = workflowStore.findLatestWorkflowIdBySource(triggerInstance.getSourceWorkflowId());
        if (workflowId==null) throw new RuntimeException("No workflow found for source '"+triggerInstance.getSourceWorkflowId()+"'");
      } else {
        throw new RuntimeException("No workflow specified");
      }
    }
    return workflowId;
  }
  
  @Override
  public WorkflowInstance send(Message message) {
    WorkflowInstanceImpl workflowInstance = lockWorkflowInstanceWithRetry(message.getWorkflowInstanceId(), message.getActivityInstanceId());
    
    if (message instanceof SerializedMessage) {
      deserializeMessage(message, workflowInstance.workflow);
    }
    
    workflowInstance.setVariableValues(message.getData());
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
    workflowInstanceStore.deleteWorkflowInstances(query);
  }

  @Override
  public List<WorkflowInstance> findWorkflowInstances(WorkflowInstanceQuery query) {
    List<WorkflowInstanceImpl> workflowInstances = workflowInstanceStore.findWorkflowInstances(query);
    return WorkflowInstanceImpl.toWorkflowInstances(workflowInstances);
  }
  
  /** retrieves the executable form of the workflow using the workflow cache */
  public WorkflowImpl getWorkflowImpl(WorkflowId workflowId) {
    WorkflowImpl workflowImpl = workflowCache.get(workflowId);
    if (workflowImpl==null) {
      Workflow workflow = workflowStore.loadWorkflowById(workflowId);
      WorkflowParser parser = WorkflowParser.parse(configuration, workflow);
      workflowImpl = parser.getWorkflow();
      workflowCache.put(workflowImpl);
    }
    return workflowImpl;
  }
  
  public WorkflowInstanceImpl lockWorkflowInstanceWithRetry(
          final WorkflowInstanceId workflowInstanceId, 
          final String activityInstanceId) {
    Retry<WorkflowInstanceImpl> retry = new Retry<WorkflowInstanceImpl>() {
      @Override
      public WorkflowInstanceImpl tryOnce() {
        return workflowInstanceStore.lockWorkflowInstance(workflowInstanceId, activityInstanceId);
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

  public void addWorkflowExecutionListener(WorkflowExecutionListener workflowExecutionListener) {
    if (workflowExecutionListeners==null) {
      workflowExecutionListeners = new ArrayList<>();
    }
    workflowExecutionListeners.add(workflowExecutionListener);
  }
  
  public void removeWorkflowExecutionListener(WorkflowExecutionListener workflowExecutionListener) {
    if (workflowExecutionListeners!=null) {
      workflowExecutionListeners.remove(workflowExecutionListener);
      if (workflowExecutionListeners.isEmpty()) {
        workflowExecutionListeners = null;
      }
    }
  }

  public List<WorkflowExecutionListener> getWorkflowExecutionListeners() {
    return workflowExecutionListeners;
  }
  
  public void setWorkflowExecutionListeners(List<WorkflowExecutionListener> workflowExecutionListeners) {
    this.workflowExecutionListeners = workflowExecutionListeners;
  }
  
  public void notifyActivityInstanceStarted(ActivityInstanceImpl activityInstance) {
    if (workflowExecutionListeners!=null) {
      for (WorkflowExecutionListener workflowExecutionListener: workflowExecutionListeners) {
        workflowExecutionListener.started(activityInstance);
      }
    }
  }

  public void notifyActivityInstanceEnded(ActivityInstanceImpl activityInstance) {
    if (workflowExecutionListeners!=null) {
      for (WorkflowExecutionListener workflowExecutionListener: workflowExecutionListeners) {
        workflowExecutionListener.ended(activityInstance);
      }
    }
  }

  public void notifyTransitionTaken(ActivityInstanceImpl activityInstanceFrom, TransitionImpl transition, ActivityInstanceImpl activityInstanceTo) {
    if (workflowExecutionListeners!=null) {
      for (WorkflowExecutionListener workflowExecutionListener: workflowExecutionListeners) {
        workflowExecutionListener.transition(activityInstanceFrom, transition, activityInstanceTo);
      }
    }
  }

  /**
   * Default deserialization of trigger instance data.
   * This is called if there is no trigger defined and it will 
   * assume the trigger instance data maps variable ids to values. */
  public void deserializeTriggerInstanceData(TriggerInstance triggerInstance, WorkflowImpl workflow) {
    if (triggerInstance!=null && triggerInstance.getData()!=null) {
      for (String variableId: triggerInstance.getData().keySet()) {
        VariableImpl variable = workflow.findVariableByIdLocal(variableId);
        if (variable!=null) {
          Object dataValue = triggerInstance.getData(variableId);
          Object deserializedValue = variable.type.convertJsonToInternalValue(dataValue);
          triggerInstance.data(variableId, deserializedValue);
        } else {
          log.debug("Can't deserialize undeclared variableId '"+variableId+"' in trigger instance data");
        }
      }
    }
  }
  
  public void deserializeMessage(Message message, WorkflowImpl workflow) {
    deserializeVariableValues(message.getData(), workflow);
  }

  protected void deserializeVariableValues(Map<String,Object> variableValues, WorkflowImpl workflow) {
    if (variableValues!=null) {
      for (String variableId: variableValues.keySet()) {
        Object jsonValue = variableValues.get(variableId);
        VariableImpl variable = workflow.findVariableByIdLocal(variableId);
        if (variable!=null && variable.type!=null) {
          Object value = variable.type.convertJsonToInternalValue(jsonValue);
          variableValues.put(variableId, value);
        }
      }
    }
  }
}
