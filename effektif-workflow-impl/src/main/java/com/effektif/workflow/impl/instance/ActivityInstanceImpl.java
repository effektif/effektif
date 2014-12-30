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
package com.effektif.workflow.impl.instance;

import java.util.HashSet;
import java.util.Set;

import org.joda.time.Duration;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.effektif.workflow.api.WorkflowEngine;
import com.effektif.workflow.api.command.StartCommand;
import com.effektif.workflow.api.workflow.Transition;
import com.effektif.workflow.api.workflowinstance.ActivityInstance;
import com.effektif.workflow.impl.StartImpl;
import com.effektif.workflow.impl.Time;
import com.effektif.workflow.impl.WorkflowInstanceEventListener;
import com.effektif.workflow.impl.definition.ActivityImpl;
import com.effektif.workflow.impl.definition.TransitionImpl;
import com.effektif.workflow.impl.json.JsonService;
import com.effektif.workflow.impl.plugin.ControllableActivityInstance;
import com.effektif.workflow.impl.plugin.ServiceRegistry;
import com.effektif.workflow.impl.util.Lists;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


/**
 * @author Walter White
 */
@JsonPropertyOrder({"id", "activityId", "start", "end", "duration", "activityInstances", "variableInstances"})
public class ActivityInstanceImpl extends ScopeInstanceImpl implements ActivityInstance, ControllableActivityInstance {
  
  public static final String STATE_STARTING = "starting"; 
  public static final String STATE_STARTING_MULTI_CONTAINER = "startingMultiParent"; 
  public static final String STATE_STARTING_MULTI_INSTANCE = "startingMultiInstance"; 
  public static final String STATE_NOTIFYING = "notifying"; 
  public static final String STATE_JOINING = "joining"; 
  public static final String STATE_WAITING = "waiting"; 

  /** @see WorkflowInstanceImpl#isWorkAsync(ActivityInstanceImpl) */
  public static final Set<String> START_WORKSTATES = new HashSet<>(Lists.of(
    STATE_STARTING,
    STATE_STARTING_MULTI_CONTAINER,
    STATE_STARTING_MULTI_INSTANCE));

  public static final Logger log = LoggerFactory.getLogger(WorkflowEngine.class);

  @JsonIgnore
  public ActivityImpl activityDefinition;
  
  public String activityId;
  public String workState;
  public String calledWorkflowInstanceId;

  public void onwards() {
    workflowEngine.executeOnwards(this);
  }

  public void end() {
    workflowEngine.executeEnd(this, true);
  }

  public void end(boolean notifyParent) {
    workflowEngine.executeEnd(this, notifyParent);
  }

  public void setWorkState(String workState) {
    this.workState = workState;
    if (updates!=null) {
      getUpdates().isWorkStateChanged = true;
      propagateActivityInstanceChange(parent);
    }
  }

  @Override
  public void setJoining() {
    setWorkState(STATE_JOINING);
  }
  
  @Override
  public boolean isJoining(ActivityInstance activityInstance) {
    ActivityInstanceImpl activityInstanceImpl = (ActivityInstanceImpl) activityInstance;
    return STATE_JOINING.equals(activityInstanceImpl.workState);
  }
  
  @Override
  public void removeJoining(ActivityInstance activityInstance) {
    ActivityInstanceImpl activityInstanceImpl = (ActivityInstanceImpl) activityInstance;
    activityInstanceImpl.setWorkState(null);
  }

  /** Starts the to (destination) activity in the current (parent) scope.
   * This methods will also end the current activity instance.
   * This method can be called multiple times in one start() */
  public void takeTransition(Transition transition) {
    ActivityImpl to = (ActivityImpl) transition.getTo();
    end(to==null);
    for (WorkflowInstanceEventListener listener : getWorkflowEngine().getListeners()) {
      listener.transition(this, transition);
    }
    if (to!=null) {
      if (log.isDebugEnabled())
        log.debug("Taking transition to "+to);
      parent.createActivityInstance(to);
    }
  }
  
  @Override
  public void ended(ActivityInstanceImpl nestedEndedActivityInstance) {
    activityDefinition.activityType.ended(this, nestedEndedActivityInstance);
  }
  
  @Override
  public ActivityInstanceImpl findActivityInstance(String activityInstanceId) {
    if (activityInstanceId.equals(this.id)) {
      return this;
    }
    return super.findActivityInstance(activityInstanceId);
  }

  public ActivityImpl getActivity() {
    return activityDefinition;
  }
  
  public void setActivityDefinition(ActivityImpl activityDefinition) {
    this.activityDefinition = activityDefinition;
  }
  
  public String toString() {
    String activityDefinitionType = activityDefinition.activityType.getClass().getSimpleName();
    return "("+activityDefinition.id+"|"+activityDefinitionType+"|"+id+"|ai)";
  }
  
  public void setEnd(LocalDateTime end) {
    this.end = end;
    if (start!=null && end!=null) {
      this.duration = new Duration(start.toDateTime(), end.toDateTime()).getMillis();
    }
    if (updates!=null) {
      updates.isEndChanged = true;
      propagateActivityInstanceChange(parent);
    }
  }

  public void setActivityId(String activityDefinitionId) {
    this.activityId = activityDefinitionId;
  }

  @Override
  public String getActivityId() {
    return activityId;
  }
  
  @Override
  public ActivityInstanceImpl findActivityInstanceByActivityId(String activityDefinitionId) {
    if (activityDefinitionId==null) {
      return null;
    }
    if (activityDefinitionId.equals(this.activityId)) {
      return this;
    }
    return super.findActivityInstanceByActivityId(activityDefinitionId);
  }
  
  public Object getTransientContextObject(String key) {
    return workflowInstance.getTransientContextObject(key);
  }

  @Override
  public boolean isProcessInstance() {
    return false;
  }

  public StartCommand newSubWorkflowStart(String subSubWorkflowId) {
    JsonService jsonService = workflowEngine.getServiceRegistry().getService(JsonService.class);
    StartImpl start = new StartImpl(workflowEngine, jsonService);
    start.processDefinitionId = subSubWorkflowId;
    start.callerWorkflowInstanceId = workflowInstance.id;
    start.callerActivityInstanceId = id;
    return start;
  }

  public void setCalledWorkflowInstanceId(String calledWorkflowInstanceId) {
    this.calledWorkflowInstanceId = calledWorkflowInstanceId;
  }
  
  public String getCalledWorkflowInstanceId() {
    return calledWorkflowInstanceId;
  }

  @Override
  public ActivityInstanceUpdates getUpdates() {
    return (ActivityInstanceUpdates) updates;
  }

  @Override
  public ServiceRegistry getServiceRegistry() {
    return workflowEngine.getServiceRegistry();
  }
  
  public void trackUpdates(boolean isNew) {
    if (updates==null) {
      updates = new ActivityInstanceUpdates(isNew);
    } else {
      updates.reset(isNew);
    }
    super.trackUpdates(isNew);
  }
}
