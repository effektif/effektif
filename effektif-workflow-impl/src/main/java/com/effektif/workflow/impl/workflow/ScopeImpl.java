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
package com.effektif.workflow.impl.workflow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.effektif.workflow.api.Configuration;
import com.effektif.workflow.api.workflow.Activity;
import com.effektif.workflow.api.workflow.Extensible;
import com.effektif.workflow.api.workflow.Scope;
import com.effektif.workflow.api.workflow.Timer;
import com.effektif.workflow.api.workflow.Transition;
import com.effektif.workflow.api.workflow.Variable;
import com.effektif.workflow.impl.WorkflowParser;


public abstract class ScopeImpl extends Extensible {

  public ScopeImpl parent;
  public Configuration configuration;
  public WorkflowImpl workflow;
  public Map<String, ActivityImpl> activities;
  public Map<String, VariableImpl> variables;
  public List<TimerImpl> timers;
  public List<TransitionImpl> transitions;

  public void parse(Scope scope, ScopeImpl parentImpl, WorkflowParser parser) {
    this.properties = scope.getProperties()!=null ? new HashMap<>(scope.getProperties()) : null;
    this.configuration = parser.configuration;
    if (parentImpl!=null) {
      this.parent = parentImpl;
      this.workflow = parentImpl.workflow;
    }
    
    List<Variable> variables = scope.getVariables();
    if (variables!=null && !variables.isEmpty()) {
      int i = 0;
      for (Variable variable: variables) {
        VariableImpl variableImpl = new VariableImpl();
        parser.pushContext("variables", variable, variableImpl, i);
        variableImpl.parse(variable, this, parser);
        addVariable(variableImpl);
        parser.popContext();
        i++;
      }
    } else {
      // ensures there are not empty collections in the persistent storage
      scope.setVariables(null);
    }
    
    List<Timer> timers = scope.getTimers();
    if (timers!=null && !timers.isEmpty()) {
      int i = 0;
      for (Timer timer: timers) {
        TimerImpl timerImpl = new TimerImpl();
        parser.pushContext("timers", timer, timerImpl, i);
        timerImpl.parse(timer, this, parser);
        addTimer(timerImpl);
        parser.popContext();
        i++;
      }
    } else {
      // ensures there are not empty collections in the persistent storage
      scope.setTimers(null);
    }

    Map<String, ActivityImpl> activitiesByDefaultTransitionId = new HashMap<>();
    List<Activity> activities = scope.getActivities();
    if (activities!=null && !activities.isEmpty()) {
      int i = 0;
      for (Activity activity: activities) {
        ActivityImpl activityImpl = new ActivityImpl();
        parser.pushContext("activities", activity, activityImpl, i);
        if (activity.getDefaultTransitionId()!=null) {
          activitiesByDefaultTransitionId.put(activity.getDefaultTransitionId(), activityImpl);
        }
        activityImpl.parse(activity, scope, this, parser);
        addActivity(activityImpl);
        parser.popContext();
        i++;
      }
    } else {
      // ensures there are not empty collections in the persistent storage
      scope.setActivities(null);
    }

    List<Transition> transitions = scope.getTransitions();
    if (transitions!=null && !transitions.isEmpty()) {
      int i = 0;
      for (Transition transition: transitions) {
        TransitionImpl transitionImpl = new TransitionImpl();
        parser.pushContext("transitions", transition, transitionImpl, i);
        transitionImpl.parse(transition, this, activitiesByDefaultTransitionId, parser);
        addTransition(transitionImpl);
        parser.popContext();
        i++;
      }
    } else {
      // ensures there are not empty collections in the persistent storage
      scope.setTransitions(null);
    }

    if (this.activities!=null) {
      // some activity types need to validate incoming and outgoing transitions, 
      // that's why they are validated after the transitions.
      int i = 0;
      for (ActivityImpl activityImpl : this.activities.values()) {
        Activity activity = activities.get(i);
        if (activityImpl.activityType != null) {
          parser.pushContext("activities", activity, activityImpl, i);
          activityImpl.activityType.parse(activityImpl, activity, parser);
          parser.popContext();
        }
        i++;
      }
    }
    
    if (!activitiesByDefaultTransitionId.isEmpty()) {
      for (String nonExistingDefaultTransitionId: activitiesByDefaultTransitionId.keySet()) {
        ActivityImpl activity = activitiesByDefaultTransitionId.get(nonExistingDefaultTransitionId);
        parser.addWarning("Activity '%s' has non existing default transition id '%s'", activity.id, nonExistingDefaultTransitionId);
      }
    }
  }
  
  public void addTimer(TimerImpl timer) {
    if (timers==null) {
      timers = new ArrayList<>();
    }
    timers.add(timer);
    timer.parent = this;
  }

  public void addVariable(VariableImpl variable) {
    if (variables==null) {
      variables = new LinkedHashMap<>();
    }
    variables.put(variable.id, variable);
    variable.parent = this;
  }

  public void addTransition(TransitionImpl transition) {
    if (transitions==null) {
      transitions = new ArrayList<>();
    }
    transitions.add(transition);
    transition.parent = this;
  }
  
  public void addActivity(ActivityImpl activity) {
    if (activities==null) {
      activities = new LinkedHashMap<>();
    }
    activities.put(activity.id, activity);
    activity.parent = this;
  }

  public TransitionImpl findTransitionByIdLocal(String transitionId) {
    if (transitions!=null) {
      for (TransitionImpl transition: transitions) {
        if (transitionId.equals(transition.id)) {
          return transition;
        }
      }
    }
    return null;
  }

  public ActivityImpl findActivityByIdLocal(String activityId) {
    return activities.get(activityId);
  }


  public boolean isWorkflow() {
    return parent!=null;
  }

  public boolean hasActivitiesLocal() {
    return activities!=null && !activities.isEmpty();
  }

  public boolean hasVariablesLocal() {
    return variables!=null && !variables.isEmpty();
  }

  public boolean hasTransitionsLocal() {
    return transitions!=null && !transitions.isEmpty();
  }
  
  public boolean hasVariableRecursive(String variableId) {
    if (hasVariableLocal(variableId)) {
      return true;
    }
    if (parent!=null) {
      return parent.hasVariableRecursive(variableId);
    }
    return false;
  }

  public boolean hasVariableLocal(String variableId) {
    return variables!=null && variables.containsKey(variableId);
  }

  public ActivityImpl getActivityByIdLocal(String activityId) {
    return activities!=null ? activities.get(activityId) : null;
  }

  public VariableImpl findVariableByIdLocal(String variableId) {
    return variables!=null ? variables.get(variableId) : null;
  }
  
  public VariableImpl findVariableByIdRecursive(String variableId) {
    if (variables!=null && variables.containsKey(variableId)) {
      return variables.get(variableId);
    }
    if (parent!=null) {
      return parent.findVariableByIdRecursive(variableId);
    }
    return null;
  }


  public ActivityImpl getNextActivity(ActivityImpl previous) {
    if (activities!=null) {
      // this depends on the map being a *Linked*HashMap that preserves the order
      Iterator<ActivityImpl> iterator = activities.values().iterator();
      while (iterator.hasNext()) {
        ActivityImpl activity = iterator.next();
        if (previous==activity) {
          if (iterator.hasNext()) {
            return iterator.next();
          } else {
            return null;
          }
        }
      }
    }
    return null;
  }

  public ScopeImpl getParent() {
    return parent;
  }

  
  public Configuration getConfiguration() {
    return configuration;
  }

  
  public WorkflowImpl getWorkflow() {
    return workflow;
  }

  
  public Map<String, ActivityImpl> getActivities() {
    return activities;
  }

  
  public Map<String, VariableImpl> getVariables() {
    return variables;
  }

  
  public List<TimerImpl> getTimers() {
    return timers;
  }

  
  public List<TransitionImpl> getTransitions() {
    return transitions;
  }

  public abstract String getIdText();
}
