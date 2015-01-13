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
package com.effektif.workflow.impl.workflowinstance;

import java.util.HashSet;
import java.util.Set;

import org.joda.time.Duration;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.effektif.workflow.api.WorkflowEngine;
import com.effektif.workflow.api.workflowinstance.ActivityInstance;
import com.effektif.workflow.impl.Time;
import com.effektif.workflow.impl.WorkflowInstanceEventListener;
import com.effektif.workflow.impl.plugin.ServiceRegistry;
import com.effektif.workflow.impl.util.Lists;
import com.effektif.workflow.impl.workflow.ActivityImpl;
import com.effektif.workflow.impl.workflow.TransitionImpl;


public class ActivityInstanceImpl extends ScopeInstanceImpl {
  
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

  public ActivityImpl activity;
  public String workState;
  public String calledWorkflowInstanceId;
  
  public ActivityInstanceImpl() {
  }

  public ActivityInstanceImpl(ScopeInstanceImpl parent, ActivityImpl activity, String id) {
    super(parent, activity, id);
    this.activity = activity;
  }

  public ActivityInstance toActivityInstance() {
    ActivityInstance activityInstance = new ActivityInstance();
    activityInstance.setActivityId(activity.id);
    activityInstance.setCalledWorkflowInstanceId(calledWorkflowInstanceId);
    toScopeInstance(activityInstance);
    return activityInstance;
  }
  
  public void execute() {
    for (WorkflowInstanceEventListener listener : workflowEngine.listeners) {
      listener.started(this);
    }
    activity.activityType.execute(this);
    if (START_WORKSTATES.contains(workState)) {
      setWorkState(ActivityInstanceImpl.STATE_WAITING);
    }
  }

  public void onwards() {
    if (log.isDebugEnabled())
      log.debug("Onwards "+this);
    // Default BPMN logic when an activity ends
    // If there are outgoing transitions (in bpmn they are called sequence flows)
    if (activity.hasOutgoingTransitionDefinitions()) {
      // Ensure that each transition is taken
      // Note that process concurrency does not require java concurrency
      end(false);
      for (TransitionImpl transitionDefinition: activity.outgoingTransitions) {
        takeTransition(transitionDefinition);
      }
    } else {
      // Propagate completion upwards
      end(true);
    }
  }

  public void end() {
    end(true);
  }

  public void end(boolean notifyParent) {
    if (end==null) {
      if (hasOpenActivityInstances()) {
        throw new RuntimeException("Can't end this activity instance. There are open activity instances: " +this);
      }
      setEnd(Time.now());
      for (WorkflowInstanceEventListener listener : workflowEngine.listeners) {
        listener.ended(this);
      }
      if (notifyParent) {
        setWorkState(STATE_NOTIFYING);
        workflowInstance.addWork(this);

      } else {
        setWorkState(null); // means please archive me.
      }
    }
  }

  public void setWorkState(String workState) {
    this.workState = workState;
    if (updates!=null) {
      getUpdates().isWorkStateChanged = true;
      propagateActivityInstanceChange(parent);
    }
  }

  public void setJoining() {
    setWorkState(STATE_JOINING);
  }
  
  public boolean isJoining(ActivityInstanceImpl activityInstance) {
    return STATE_JOINING.equals(activityInstance.workState);
  }
  
  public void removeJoining(ActivityInstanceImpl activityInstance) {
    activityInstance.setWorkState(null);
  }

  /** Starts the to (destination) activity in the current (parent) scope.
   * This methods will also end the current activity instance.
   * This method can be called multiple times in one start() */
  public void takeTransition(TransitionImpl transition) {
    ActivityImpl to = transition.to;
    end(to==null);
    for (WorkflowInstanceEventListener listener : workflowEngine.listeners) {
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
    activity.activityType.ended(this, nestedEndedActivityInstance);
  }
  
  @Override
  public ActivityInstanceImpl findActivityInstance(String activityInstanceId) {
    if (activityInstanceId.equals(this.id)) {
      return this;
    }
    return super.findActivityInstance(activityInstanceId);
  }

  public ActivityImpl getActivity() {
    return activity;
  }
  
  public void setActivity(ActivityImpl activityDefinition) {
    this.activity = activityDefinition;
  }
  
  public String toString() {
    String activityDefinitionType = activity.activityType.getApiClass().getSimpleName();
    return "("+(activity.id!=null?activity.id+"|":"")+id+"|"+activityDefinitionType+")";
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

  @Override
  public ActivityInstanceImpl findActivityInstanceByActivityId(String activityDefinitionId) {
    if (activityDefinitionId==null) {
      return null;
    }
    if (activityDefinitionId.equals(activity.id)) {
      return this;
    }
    return super.findActivityInstanceByActivityId(activityDefinitionId);
  }
  
  @Override
  public boolean isProcessInstance() {
    return false;
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

  public boolean hasActivityInstance(String activityInstanceId) {
    if (id!=null && id.equals(activityInstanceId)) {
      return true;
    }
    return super.hasActivityInstance(activityInstanceId);
  }
}
