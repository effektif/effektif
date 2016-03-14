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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.effektif.workflow.impl.job.Job;

import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.effektif.workflow.api.WorkflowEngine;
import com.effektif.workflow.api.model.RelativeTime;
import com.effektif.workflow.api.model.TypedValue;
import com.effektif.workflow.api.model.WorkflowInstanceId;
import com.effektif.workflow.api.workflowinstance.ActivityInstance;
import com.effektif.workflow.impl.conditions.ConditionImpl;
import com.effektif.workflow.impl.data.DataTypeImpl;
import com.effektif.workflow.impl.data.TypedValueImpl;
import com.effektif.workflow.impl.data.types.ListTypeImpl;
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
  public static final String STATE_PROPAGATE_TO_PARENT = "propagateToParent"; 
  public static final String STATE_JOINING = "joining"; 

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
  
  @Override
  protected String getActivityInstanceId() {
    return id;
  }
  
  public void execute() {
    if (workflow.workflowEngine.notifyActivityInstanceStarted(this)) {
      activity.activityType.execute(this);
    }
  }

  /** Default BPMN logic when an activity ends */
  @Override
  public void onwards() {
    if (log.isDebugEnabled()) {
      log.debug("Onwards "+this);
    }
    
    // First we end the activity instance.  
    // Subsequent invocations to end will be ignored.
    end();

    boolean isTransitionTaken = false;
    
    // Take each outgoing transition 'in parallel'
    // Note that process concurrency is not the same as java multithreaded computation
    if (activity.hasOutgoingTransitions()) {
      for (TransitionImpl transition: activity.outgoingTransitions) {
        // Only take a transition if there is no condition or if the condition resolves to true.
        ConditionImpl condition = transition.condition;
        if (condition!=null ? condition.eval(this) : true) {
          isTransitionTaken = true;
          takeTransition(transition);
        }
      }
    } 

    // if there were no outgoing transitions or none of them could be taken, 
    if (!isTransitionTaken) {
      // propagate the execution flow upwards to the parent
       propagateToParent();
    }
  }

  public void endAndPropagateToParent() {
    end();
    propagateToParent();
  }

  public void end() {
    if (end==null) {
      if (hasOpenActivityInstances()) {
        throw new RuntimeException("Can't end this activity instance. There are open activity instances: " +this);
      }
      setEnd(Time.now());
      workflow.workflowEngine.notifyActivityInstanceEnded(this);
      destroyScopeInstance();
      setWorkState(null);
    }
  }

  public void propagateToParent() {
    setWorkState(STATE_PROPAGATE_TO_PARENT);
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
      end();
      if (log.isDebugEnabled()) {
        log.debug("Taking transition to "+to);
      }
      toActivityInstance = parent.createActivityInstance(to);
    } else {
      log.debug("Dangling transition.  Propagating to parent.");
      end();
      propagateToParent();
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
    if (activityInstanceId == null) {
      return null;
    }
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
    String activityTypeName = activity.activityType.getActivityApiClass().getSimpleName();
    String activityId = activity.id; 
    String activityName = activity.activity.getName();
    return "("+activityTypeName+"|"+(activityName!=null?activityName+"|":"")+(activityId!=null?activityId+"|":"")+id+")";
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
    if (activity==null || activity.activityType==null || activity.activityType.getInputs()==null) {
      return null;
    }
    InputParameterImpl parameter = (InputParameterImpl) activity.activityType.getInputs().get(key);
    TypedValueImpl typedValue = getInputTypedValue(parameter);
    return (T) (typedValue!=null ? typedValue.value : null);
  }

  protected TypedValueImpl getInputTypedValue(InputParameterImpl parameter) {
    if (parameter!=null) {
      if (parameter.binding != null) {
        return getTypedValue(parameter.binding);
      }
      if (parameter.bindings != null) {
        DataTypeImpl<?> listType = null;
        List<Object> values = new ArrayList<>();
        for (BindingImpl< ? > binding : parameter.bindings) {
          TypedValueImpl typedValue = getTypedValue(binding);
          if (typedValue!=null) {
            if (typedValue.getValue() instanceof Collection) {
              if (listType==null && typedValue.getType()!=null) {
                listType = typedValue.getType(); 
              }
              Collection value = (Collection) typedValue.value;
              if (value!=null) {
                values.addAll(value);
              }
            } else {
              if (listType==null && typedValue.getType()!=null) {
                listType = new ListTypeImpl(typedValue.getType());
              }
              values.add(typedValue.value);
            }
          }
        }
        return new TypedValueImpl(listType, values);
      }
    }
    return null;
  }

  public Map<String, TypedValueImpl> getInputValueImpls() {
    Map<String,TypedValueImpl> inputValues = new HashMap<>();
    if (activity!=null && activity.activityType!=null && activity.activityType.getInputs()!=null) {
      Map<String,InputParameterImpl> inputs = activity.activityType.getInputs();
      for (String inputKey: inputs.keySet()) {
        InputParameterImpl inputParameter = inputs.get(inputKey);
        TypedValueImpl inputValue = getInputTypedValue(inputParameter);
        if (inputValue!=null) {
          inputValues.put(inputKey, inputValue);
        }
      }
    }
    return inputValues;
  }

  public Map<String, TypedValue> getInputValues() {
    Map<String, TypedValue> inputValues = new HashMap<>();
    Map<String,TypedValueImpl> inputValueImpls = getInputValueImpls();
    if (inputValueImpls!=null) {
      for (String key: inputValueImpls.keySet()) {
        TypedValueImpl typedValueImpl = inputValueImpls.get(key);
        inputValues.put(key, typedValueImpl.toTypedValue());
      }
    }
    return inputValues;
  }
}
