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
package com.effektif.workflow.impl.workflowinstance;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.effektif.workflow.api.WorkflowEngine;
import com.effektif.workflow.api.model.WorkflowInstanceId;
import com.effektif.workflow.api.workflowinstance.ActivityInstance;
import com.effektif.workflow.impl.util.Lists;
import com.effektif.workflow.impl.util.Time;
import com.effektif.workflow.impl.workflow.ActivityImpl;
import com.effektif.workflow.impl.workflow.BindingImpl;
import com.effektif.workflow.impl.workflow.InputParameterImpl;
import com.effektif.workflow.impl.workflow.TransitionImpl;


/**
 * @author Tom Baeyens
 */
public class ActivityInstanceImpl extends ScopeInstanceImpl {
  
  public static final String STATE_STARTING = "starting"; 
  public static final String STATE_STARTING_MULTI_CONTAINER = "startingMultiParent"; 
  public static final String STATE_STARTING_MULTI_INSTANCE = "startingMultiInstance"; 
  public static final String STATE_NOTIFY_PARENT = "notifying"; 
  public static final String STATE_JOINING = "joining"; 
  public static final String STATE_WAITING = "waiting"; 

  /** @see WorkflowInstanceImpl#isWorkAsync(ActivityInstanceImpl) */
  public static final Set<String> START_WORKSTATES = new HashSet<>(Lists.of(
    STATE_STARTING,
    STATE_STARTING_MULTI_CONTAINER,
    STATE_STARTING_MULTI_INSTANCE));

  public static final Logger log = LoggerFactory.getLogger(WorkflowEngine.class);

  public String id;
  public ActivityImpl activity;
  public String workState;
  public WorkflowInstanceId calledWorkflowInstanceId;
  public List<String> transitionsTaken;

  public ActivityInstanceImpl() {
  }

  public ActivityInstanceImpl(ScopeInstanceImpl parent, ActivityImpl activity, String id) {
    super(parent, activity);
    this.id = id;
    this.activity = activity;
  }

  public ActivityInstance toActivityInstance() {
    return toActivityInstance(false);
  }
  
  public ActivityInstance toActivityInstance(boolean includeWorkState) {
    ActivityInstance activityInstance = new ActivityInstance();
    activityInstance.setId(id);
    activityInstance.setActivityId(activity.id);
    activityInstance.setCalledWorkflowInstanceId(calledWorkflowInstanceId);
    toScopeInstance(activityInstance, includeWorkState);
    if (includeWorkState) {
      activityInstance.setPropertyOpt("workState", workState);
    }
    return activityInstance;
  }
  
  public void execute() {
    workflow.workflowEngine.notifyActivityInstanceStarted(this);
    activity.activityType.execute(this);
    if (START_WORKSTATES.contains(workState)) {
      setWorkState(ActivityInstanceImpl.STATE_WAITING);
    }
  }

  @Override
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
        // TODO evaluate conditions if there are any
        takeTransition(transitionDefinition);
      }
    } else {
      // Propagate completion upwards
      if (end==null) {
        end(true);
      } else {
        notifyParent();
      }
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
      workflow.workflowEngine.notifyActivityInstanceEnded(this);
      if (notifyParent) {
        notifyParent();
      } else {
        setWorkState(null); // means please archive me.
      }
    }
  }

  protected void notifyParent() {
    setWorkState(STATE_NOTIFY_PARENT);
    workflowInstance.addWork(this);
  }

  public void setWorkState(String workState) {
    // log.debug("Setting workstate of "+this+" from "+this.workState+" to "+workState);
    this.workState = workState;
    if (updates!=null) {
      getUpdates().isWorkStateChanged = true;
      if (parent!=null) {
        parent.propagateActivityInstanceChange();
      }
    }
  }

  public void setJoining() {
    setWorkState(STATE_JOINING);
  }
  
  public boolean isJoining() {
    return STATE_JOINING.equals(workState);
  }
  
  public void removeJoining(ActivityInstanceImpl activityInstance) {
    activityInstance.setWorkState(null);
  }

  /** Starts the to (destination) activity in the current (parent) scope.
   * This methods will also end the current activity instance.
   * This method can be called multiple times in one start() */
  public void takeTransition(TransitionImpl transition) {
    if (transition.id!=null || activity.activityType.saveTransitionsTaken()) {
      addTransitionTaken(transition.id);
    }
    ActivityInstanceImpl toActivityInstance = null;
    ActivityImpl to = transition.to;
    if (to!=null) {
      end(true);
      if (log.isDebugEnabled()) {
        log.debug("Taking transition to "+to);
      }
      toActivityInstance = parent.createActivityInstance(to);
    } else {
      end(false);
      notifyParent();
    }
    workflow.workflowEngine.notifyTransitionTaken(this, transition, toActivityInstance);
  }
  
  protected void addTransitionTaken(String transitionId) {
    if (transitionsTaken==null) {
      transitionsTaken = new ArrayList<>();
    }
    transitionsTaken.add(transitionId);
    if (updates!=null) {
      getUpdates().isTransitionsTakenChanged = true;
      if (parent!=null) {
        parent.propagateActivityInstanceChange();
      }
    }
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
    String activityDefinitionType = activity.activityType.getActivityApiClass().getSimpleName();
    return "("+(activity.id!=null?activity.id+"|":"")+id+"|"+activityDefinitionType+")";
  }
  
  public void setEnd(LocalDateTime end) {
    this.end = end;
    if (start!=null && end!=null) {
      this.duration = end.toDate().getTime()-start.toDate().getTime();
    }
    if (updates!=null) {
      updates.isEndChanged = true;
      if (parent!=null) {
        parent.propagateActivityInstanceChange();
      }
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
  public boolean isWorkflowInstance() {
    return false;
  }

  public void setCalledWorkflowInstanceId(WorkflowInstanceId calledWorkflowInstanceId) {
    this.calledWorkflowInstanceId = calledWorkflowInstanceId;
  }
  
  public WorkflowInstanceId getCalledWorkflowInstanceId() {
    return calledWorkflowInstanceId;
  }

  @Override
  public ActivityInstanceUpdates getUpdates() {
    return (ActivityInstanceUpdates) updates;
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

  public String getId() {
    return id;
  }

  // TODO add the expected type for conversion?
  public <T> T getInputValue(String key) {
    InputParameterImpl parameter = activity.inputs!=null ? activity.inputs.get(key) : null;
    if (parameter!=null) {
      if (parameter.binding != null) {
        return (T) getValue(parameter.binding);
      }
      if (parameter.bindings != null) {
        List<Object> values = new ArrayList<>();
        for (BindingImpl< ? > binding : parameter.bindings) {
          Object value = getValue(binding);
          if (value instanceof Collection) {
            values.addAll((Collection) value);
          } else {
            values.add(value);
          }
        }
        return (T) values;
      }
    }
    return null;
  }
}
