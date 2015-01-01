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

import static com.effektif.workflow.impl.instance.ActivityInstanceImpl.STATE_NOTIFYING;
import static com.effektif.workflow.impl.instance.ActivityInstanceImpl.STATE_STARTING;
import static com.effektif.workflow.impl.instance.ActivityInstanceImpl.STATE_STARTING_MULTI_CONTAINER;
import static com.effektif.workflow.impl.instance.ActivityInstanceImpl.STATE_STARTING_MULTI_INSTANCE;

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
import com.effektif.workflow.api.query.WorkflowInstanceQuery;
import com.effektif.workflow.api.query.WorkflowQuery;
import com.effektif.workflow.api.validate.DeployResult;
import com.effektif.workflow.api.validate.ParseIssues;
import com.effektif.workflow.api.workflow.Activity;
import com.effektif.workflow.api.workflow.Workflow;
import com.effektif.workflow.api.workflowinstance.WorkflowInstance;
import com.effektif.workflow.impl.activitytypes.CallImpl;
import com.effektif.workflow.impl.definition.ActivityImpl;
import com.effektif.workflow.impl.definition.TransitionImpl;
import com.effektif.workflow.impl.definition.VariableImpl;
import com.effektif.workflow.impl.definition.WorkflowImpl;
import com.effektif.workflow.impl.definition.WorkflowValidator;
import com.effektif.workflow.impl.instance.ActivityInstanceImpl;
import com.effektif.workflow.impl.instance.LockImpl;
import com.effektif.workflow.impl.instance.ScopeInstanceImpl;
import com.effektif.workflow.impl.instance.VariableInstanceImpl;
import com.effektif.workflow.impl.instance.WorkflowInstanceImpl;
import com.effektif.workflow.impl.json.JsonService;
import com.effektif.workflow.impl.plugin.ServiceRegistry;
import com.effektif.workflow.impl.type.AnyDataType;
import com.effektif.workflow.impl.util.Exceptions;
import com.effektif.workflow.impl.util.Lists;

public abstract class WorkflowEngineImpl implements WorkflowEngine {

  public static final Logger log = LoggerFactory.getLogger(WorkflowEngine.class);

  protected String id;
  protected ExecutorService executorService;
  protected WorkflowCache workflowCache;
  protected WorkflowStore workflowStore;
  protected WorkflowInstanceStore workflowInstanceStore;
  protected JsonService jsonService;
  protected ServiceRegistry serviceRegistry;
  protected List<WorkflowInstanceEventListener> listeners;

  protected WorkflowEngineImpl() {
  }

  protected WorkflowEngineImpl(WorkflowEngineConfiguration configuration) {
    this.serviceRegistry = configuration.getServiceRegistry();
    this.serviceRegistry.registerService(this);
    initializeId(configuration);
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
  public DeployResult deployWorkflow(Workflow workflow) {
    DeployResult result = validateAndDeploy(workflow);
    result.checkNoErrors();
    return result.getWorkflowId();
  }

  @Override
  public ParseIssues validateWorkflow(Workflow workflow) {
    Exceptions.checkNotNull(workflow, "workflow");
    if (log.isDebugEnabled()) {
      log.debug("Deploying workflow");
    }

    DeployResult deployResult = new DeployResult();

    // throws an exception if there are errors 
    WorkflowValidator validator = new WorkflowValidator(this);
    workflow.visit(validator);
    ParseIssues issues = validator.getIssues();
    deployResult.setIssues(issues);
    
    if (!issues.hasErrors()) {
      workflow.id = workflowStore.createWorkflowId(workflow);
      deployResult.setWorkflowId(workflow.id);

      workflowStore.insertWorkflow(workflow);
      workflowCache.put(workflow);
    }
    
    return deployResult;
  }

  public String deployWorkflow(WorkflowImpl workflow) {
  }

  public WorkflowQuery newWorkflowQuery() {
    return new WorkflowQuery(this);
  }

  public WorkflowImpl findWorkflow(WorkflowQuery query) {
    List<Workflow> workflows = findWorkflows(query);
    if (workflows!=null && !workflows.isEmpty()) {
      return workflows.get(0);
    }
    return null;
  }

  public List<WorkflowImpl> findWorkflows(WorkflowQuery query) {
    if (query.onlyIdSpecified()) {
      WorkflowImpl cachedProcessDefinition = workflowCache.get(query.id);
      if (cachedProcessDefinition!=null) {
        return Lists.of(cachedProcessDefinition);
      }
    }
    List<WorkflowImpl> result = workflowStore.loadWorkflows(query);
    if (Representation.EXECUTABLE==query.representation) {
      for (WorkflowImpl processDefinition: result) {
        WorkflowValidator validator = new WorkflowValidator(this);
        processDefinition.visit(validator);
        workflowCache.put(processDefinition);
      }
    }
    return result;
  }

  /// Workflow instance methods //////////////////////////////////////////////////////////// 
  
  @Override
  public StartImpl newStart() {
    return new StartImpl(this, jsonService);
  }

  @Override
  public MessageCommand newMessage() {
    return new MessageImpl(this, jsonService);
  }

  @Override
  public WorkflowInstanceQuery newWorkflowInstanceQuery() {
    return new WorkflowInstanceQuery(workflowInstanceStore);
  }

  /** caller has to ensure that start.variableValues is not serialized @see VariableRequestImpl#serialize & VariableRequestImpl#deserialize */
  public WorkflowInstance startWorkflowInstance(StartImpl start) {
    WorkflowImpl workflow = newWorkflowQuery()
      .representation(Representation.EXECUTABLE)
      .id(start.processDefinitionId)
      .name(start.processDefinitionName)
      .orderByDeployTimeDescending()
      .get();
    
    if (workflow==null) {
      throw new RuntimeException("Could not find process definition "+start.processDefinitionId+" "+start.processDefinitionName);
    }
    WorkflowInstanceImpl workflowInstance = createWorkflowInstance(workflow);
    workflowInstance.callerWorkflowInstanceId = start.callerWorkflowInstanceId;
    workflowInstance.callerActivityInstanceId = start.callerActivityInstanceId;
    workflowInstance.transientContext = start.transientContext;
    workflowInstance.setVariableValues(start.variableValues);
    if (log.isDebugEnabled()) log.debug("Starting "+workflowInstance);
    workflowInstance.setStart(Time.now());
    List<ActivityImpl> startActivityDefinitions = workflow.getStartActivities();
    if (startActivityDefinitions!=null) {
      for (ActivityImpl startActivityDefinition: startActivityDefinitions) {
        workflowInstance.execute(startActivityDefinition);
      }
    }
    LockImpl lock = new LockImpl();
    lock.setTime(Time.now());
    lock.setOwner(getId());
    workflowInstance.setLock(lock);
    workflowInstanceStore.insertWorkflowInstance(workflowInstance);
    workflowInstance.workflowEngine.executeWork(workflowInstance);
    return workflowInstance;
  }
  
  public WorkflowInstanceImpl sendActivityInstanceMessage(MessageImpl message) {
    WorkflowInstanceQuery query = newWorkflowInstanceQuery()
      .workflowInstanceId(message.processInstanceId)
      .activityInstanceId(message.activityInstanceId);
    WorkflowInstanceImpl processInstance = lockProcessInstanceWithRetry(query);
    // TODO set variables and context
    ActivityInstanceImpl activityInstance = processInstance.findActivityInstance(message.activityInstanceId);
    if (activityInstance.isEnded()) {
      throw new RuntimeException("Activity instance "+activityInstance+" is already ended");
    }
    if (log.isDebugEnabled())
      log.debug("Signalling "+activityInstance);
    ActivityImpl activityDefinition = activityInstance.getActivity();
    activityDefinition.activityType.message(activityInstance);
    processInstance.workflowEngine.executeWork(processInstance);
    return processInstance;
  }
  
  public WorkflowInstanceImpl lockProcessInstanceWithRetry(WorkflowInstanceQuery query) {
    long wait = 50l;
    long attempts = 0;
    long maxAttempts = 4;
    long backoffFactor = 5;
    WorkflowInstanceImpl processInstance = workflowInstanceStore.lockWorkflowInstance(query);
    while ( processInstance==null 
            && attempts <= maxAttempts ) {
      try {
        if (log.isDebugEnabled())
          log.debug("Locking failed... retrying");
        Thread.sleep(wait);
      } catch (InterruptedException e) {
        if (log.isDebugEnabled())
          log.debug("Waiting for lock to be released was interrupted");
      }
      wait = wait * backoffFactor;
      attempts++;
      processInstance = workflowInstanceStore.lockWorkflowInstance(query);
    }
    if (processInstance==null) {
      throw new RuntimeException("Couldn't lock process instance with "+query);
    }
    return processInstance;
  }

  protected WorkflowInstanceImpl createWorkflowInstance(WorkflowImpl workflow) {
    String processInstanceId = workflowInstanceStore.createWorkflowInstanceId(workflow);
    return new WorkflowInstanceImpl(this, workflow, processInstanceId);
  }

  /** instantiates and assign an id.
   * parent and activityDefinition are only passed for reference.  
   * Apart from choosing the activity instance class to instantiate and assigning the id,
   * this method does not need to link the parent or the activityDefinition. */
  public ActivityInstanceImpl createActivityInstance(ScopeInstanceImpl parent, ActivityImpl activityDefinition) {
    ActivityInstanceImpl activityInstance = new ActivityInstanceImpl();
    activityInstance.id = workflowInstanceStore.createActivityInstanceId();
    return activityInstance;
  }

  /** instantiates and assign an id.
   * parent and variableDefinition are only passed for reference.  
   * Apart from choosing the variable instance class to instantiate and assigning the id,
   * this method does not need to link the parent or variableDefinition. */
  public VariableInstanceImpl createVariableInstance(ScopeInstanceImpl parent, VariableImpl variableDefinition) {
    VariableInstanceImpl variableInstance = new VariableInstanceImpl();
    variableInstance.id = workflowInstanceStore.createVariableInstanceId();
    return variableInstance;
  }
  
  // process execution methods ////////////////////////////////////////////////////////
  
  
  

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

  public void executeWork(final WorkflowInstanceImpl workflowInstance) {
    WorkflowInstanceStore workflowInstanceStore = getWorkflowInstanceStore();
    boolean isFirst = true;
    while (workflowInstance.hasWork()) {
      // in the first iteration, the updates will be empty and hence no updates will be flushed
      if (isFirst) {
        isFirst = false;
      } else {
        workflowInstanceStore.flush(workflowInstance); 
      }
      ActivityInstanceImpl activityInstance = workflowInstance.getNextWork();
      ActivityImpl activity = activityInstance.getActivity();
      
      if (STATE_STARTING.equals(activityInstance.workState)) {
        if (log.isDebugEnabled())
          log.debug("Starting "+activityInstance);
        executeStart(activityInstance);
        
      } else if (STATE_STARTING_MULTI_INSTANCE.equals(activityInstance.workState)) {
        if (log.isDebugEnabled())
          log.debug("Starting multi instance "+activityInstance);
        executeStart(activityInstance);
        
      } else if (STATE_STARTING_MULTI_CONTAINER.equals(activityInstance.workState)) {
        List<Object> values = activityInstance.getValue(activity.multiInstance);
        if (values!=null && !values.isEmpty()) {
          if (log.isDebugEnabled())
            log.debug("Starting multi container "+activityInstance);
          for (Object value: values) {
            ActivityInstanceImpl elementActivityInstance = activityInstance.createActivityInstance(activity);
            elementActivityInstance.setWorkState(STATE_STARTING_MULTI_INSTANCE); 
            elementActivityInstance.initializeForEachElement(activity.multiInstanceElement, value);
          }
        } else {
          if (log.isDebugEnabled())
            log.debug("Skipping empty multi container "+activityInstance);
          activityInstance.onwards();
        }
  
      } else if (STATE_NOTIFYING.equals(activityInstance.workState)) {
        if (log.isDebugEnabled())
          log.debug("Notifying parent of "+activityInstance);
        activityInstance.parent.ended(activityInstance);
        activityInstance.workState = null;
      }
    }
    if (workflowInstance.hasAsyncWork()) {
      if (log.isDebugEnabled())
        log.debug("Going asynchronous "+workflowInstance.workflowInstance);
      workflowInstanceStore.flush(workflowInstance.workflowInstance);
      ExecutorService executor = getExecutorService();
      executor.execute(new Runnable(){
        public void run() {
          try {
            workflowInstance.work = workflowInstance.workAsync;
            workflowInstance.workAsync = null;
            workflowInstance.workflowInstance.isAsync = true;
            if (workflowInstance.updates!=null) {
              workflowInstance.getUpdates().isWorkChanged = true;
              workflowInstance.getUpdates().isAsyncWorkChanged = true;
            }
            executeWork(workflowInstance);
          } catch (Throwable e) {
            e.printStackTrace();
          }
        }});
    } else {
      workflowInstanceStore.flushAndUnlock(workflowInstance.workflowInstance);
    }
  }
  
  public void executeStart(ActivityInstanceImpl activityInstance) {
    for (WorkflowInstanceEventListener listener : listeners) {
      listener.started(activityInstance);
    }
    ActivityImpl activity = activityInstance.getActivity();
    activity.activityType.start(activityInstance);
    if (ActivityInstanceImpl.START_WORKSTATES.contains(activityInstance.workState)) {
      activityInstance.setWorkState(ActivityInstanceImpl.STATE_WAITING);
    }
  }

  public void executeWorkflowInstanceEnded(WorkflowInstanceImpl workflowInstance) {
    if (workflowInstance.callerWorkflowInstanceId!=null) {
      WorkflowInstanceQuery processInstanceQuery = newWorkflowInstanceQuery()
       .workflowInstanceId(workflowInstance.callerWorkflowInstanceId)
       .activityInstanceId(workflowInstance.callerActivityInstanceId);
      WorkflowInstanceImpl callerProcessInstance = lockProcessInstanceWithRetry(processInstanceQuery);
      ActivityInstanceImpl callerActivityInstance = callerProcessInstance.findActivityInstance(workflowInstance.callerActivityInstanceId);
      if (callerActivityInstance.isEnded()) {
        throw new RuntimeException("Call activity instance "+callerActivityInstance+" is already ended");
      }
      if (log.isDebugEnabled())
        log.debug("Notifying caller "+callerActivityInstance);
      ActivityImpl activityDefinition = callerActivityInstance.getActivity();
      CallImpl callActivity = (CallImpl) activityDefinition.activityType;
      callActivity.calledProcessInstanceEnded(callerActivityInstance, workflowInstance);
      callerActivityInstance.onwards();
      executeWork(callerProcessInstance);
    }
  }

  public void executeOnwards(ActivityInstanceImpl activityInstance) {
    if (log.isDebugEnabled())
      log.debug("Onwards "+this);
    ActivityImpl activity = activityInstance.activityDefinition;
    // Default BPMN logic when an activity ends
    // If there are outgoing transitions (in bpmn they are called sequence flows)
    if (activity.hasOutgoingTransitionDefinitions()) {
      // Ensure that each transition is taken
      // Note that process concurrency does not require java concurrency
      activityInstance.end(false);
      for (TransitionImpl transitionDefinition: activity.outgoingDefinitions) {
        activityInstance.takeTransition(transitionDefinition);
      }
    } else {
      // Propagate completion upwards
      activityInstance.end(true);
    }
  }
  
  public void executeEnd(ActivityInstanceImpl activityInstance, boolean notifyParent) {
    if (activityInstance.end==null) {
      if (activityInstance.hasOpenActivityInstances()) {
        throw new RuntimeException("Can't end this activity instance. There are open activity instances: " +activityInstance);
      }
      activityInstance.setEnd(Time.now());
      for (WorkflowInstanceEventListener listener : listeners) {
        listener.ended(activityInstance);
      }
      if (notifyParent) {
        activityInstance.setWorkState(STATE_NOTIFYING);
        activityInstance.workflowInstance.addWork(activityInstance);

      } else {
        activityInstance.setWorkState(null); // means please archive me.
      }
    }
  }

  public void createVariableInstanceByValue(ScopeInstanceImpl scopeInstance, Object value) {
    VariableImpl variable = new VariableImpl();
    if (value instanceof String) {
      variable.dataType = DataTypes.TEXT;
    } else if (value instanceof Number) {
      variable.dataType = DataTypes.NUMBER;
    } else {
      variable.dataType = new AnyDataType();
    }
    VariableInstanceImpl variableInstance = scopeInstance.createVariableInstance(variable);
    variableInstance.setValue(value);
  }
}
