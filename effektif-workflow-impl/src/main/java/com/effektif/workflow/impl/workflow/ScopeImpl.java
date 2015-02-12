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
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.effektif.workflow.api.Configuration;
import com.effektif.workflow.api.workflow.Activity;
import com.effektif.workflow.api.workflow.Scope;
import com.effektif.workflow.api.workflow.Timer;
import com.effektif.workflow.api.workflow.Transition;
import com.effektif.workflow.api.workflow.Variable;
import com.effektif.workflow.impl.WorkflowParser;


public abstract class ScopeImpl {

  public String id;
  public ScopeImpl parent;
  public Configuration configuration;
  public WorkflowImpl workflow;
  public Map<String, ActivityImpl> activities;
  public Map<String, VariableImpl> variables;
  public List<TimerImpl> timers;
  public List<TransitionImpl> transitions;
  
  public void parse(Scope scopeApi, WorkflowParser parser, ScopeImpl parent) {
    this.id = scopeApi.getId();
    this.configuration = parser.configuration;
    if (parent!=null) {
      this.parent = parent;
      this.workflow = parent.workflow;
    }
    
    List<Variable> variableApi = scopeApi.getVariables();
    if (variableApi!=null) {
      int i = 0;
      for (Variable apiVariable: variableApi) {
        VariableImpl variableImpl = new VariableImpl();
        parser.pushContext("variables", apiVariable, i);
        variableImpl.parse(apiVariable, parser, this);
        addVariable(variableImpl);
        parser.popContext();
        i++;
      }
    }
    
    List<Timer> timersApi = scopeApi.getTimers();
    if (timersApi!=null) {
      int i = 0;
      for (Timer timerApi: timersApi) {
        TimerImpl timer = new TimerImpl();
        parser.pushContext("timers", timerApi, i);
        timer.parse(timerApi, this, parser);
        addTimer(timer);
        parser.popContext();
        i++;
      }
    }

    Map<String, ActivityImpl> activitiesByDefaultTransitionId = new HashMap<>();
    List<Activity> activitiesApi = scopeApi.getActivities();
    if (activitiesApi!=null) {
      int i = 0;
      for (Activity activityApi: activitiesApi) {
        ActivityImpl activityImpl = new ActivityImpl();
        parser.pushContext("activities", activityApi, i);
        if (activityApi.getDefaultTransitionId()!=null) {
          activitiesByDefaultTransitionId.put(activityApi.getDefaultTransitionId(), activityImpl);
        }
        activityImpl.parse(activityApi, scopeApi, parser, this);
        addActivity(activityImpl);
        parser.popContext();
        i++;
      }
    }

    List<Transition> transitionsApi = scopeApi.getTransitions();
    if (transitionsApi!=null) {
      int i = 0;
      for (Transition transitionApi: transitionsApi) {
        TransitionImpl transitionImpl = new TransitionImpl();
        parser.pushContext("transitions", transitionApi, i);
        transitionImpl.parse(transitionApi, this, parser, activitiesByDefaultTransitionId);
        addTransition(transitionImpl);
        parser.popContext();
        i++;
      }
    }

    if (activities!=null) {
      // some activity types need to validate incoming and outgoing transitions, 
      // that's why they are validated after the transitions.
      int i = 0;
      for (ActivityImpl activity : activities.values()) {
        Activity apiActivity = activitiesApi.get(i);
        if (activity.activityType != null) {
          parser.pushContext("activities", apiActivity, i);
          activity.activityType.parse(activity, apiActivity, parser);
          parser.popContext();
        }
        i++;
      }
    }
    
    if (!activitiesByDefaultTransitionId.isEmpty()) {
      for (String nonExistingDefaultTransitionId: activitiesByDefaultTransitionId.keySet()) {
        ActivityImpl activity = activitiesByDefaultTransitionId.get(nonExistingDefaultTransitionId);
        parser.addError("Activity '%s' has non existing default transition id '%s'", activity.id, nonExistingDefaultTransitionId);
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
}
