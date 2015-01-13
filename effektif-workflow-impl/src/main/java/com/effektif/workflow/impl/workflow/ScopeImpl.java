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
package com.effektif.workflow.impl.workflow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.effektif.workflow.api.workflow.Activity;
import com.effektif.workflow.api.workflow.Scope;
import com.effektif.workflow.api.workflow.Timer;
import com.effektif.workflow.api.workflow.Transition;
import com.effektif.workflow.api.workflow.Variable;


public abstract class ScopeImpl extends BaseImpl {

  public Map<String, ActivityImpl> activities;
  public Map<String, VariableImpl> variables;
  public List<TimerImpl> timers;
  public List<TransitionImpl> transitions;
  
  public void parse(Scope apiScope, WorkflowParser workflowParser, ScopeImpl parent) {
    super.parse(apiScope, workflowParser, parent);
    
    List<Variable> apiVariables = apiScope.getVariables();
    if (apiVariables!=null) {
      Set<String> variableIds = new HashSet<>();
      int i = 0;
      for (Variable apiVariable: apiVariables) {
        VariableImpl variable = new VariableImpl();
        workflowParser.pushContext("variables", apiVariable, i);
        variableIds.add(variable.id);
        if (variableIds.contains(variable.id)) {
          workflowParser.addError("Duplicate variable id %s. Variables ids have to be unique in their scope.", variable.id);
        }
        variable.parse(apiVariable, this, workflowParser);
        addVariable(variable);
        workflowParser.popContext();
        i++;
      }
    }
    
    List<Timer> apiTimers = apiScope.getTimers();
    if (apiTimers!=null) {
      int i = 0;
      for (Timer apiTimer: apiTimers) {
        TimerImpl timer = new TimerImpl();
        workflowParser.pushContext("timers", apiTimer, i);
        timer.parse(apiTimer, this, workflowParser);
        addTimer(timer);
        workflowParser.popContext();
        i++;
      }
    }

    Map<String, ActivityImpl> activitiesByDefaultTransitionId = new HashMap<>();
    List<Activity> apiActivities = apiScope.getActivities();
    if (apiActivities!=null) {
      Set<String> activityIds = new HashSet<>();
      int i = 0;
      for (Activity apiActivity: apiActivities) {
        ActivityImpl activity = new ActivityImpl();
        workflowParser.pushContext("activities", apiActivity, i);
        activity.parse(apiActivity, apiScope, workflowParser, this);
        addActivity(activity);
        if (activityIds.contains(activity.id)) {
          workflowParser.addError("Duplicate activity id '%s'. Activity ids have to be unique in their scope.", activity.id);
        }
        if (apiActivity.getDefaultTransitionId()!=null) {
          activitiesByDefaultTransitionId.put(apiActivity.getDefaultTransitionId(), activity);
        }
        workflowParser.popContext();
        i++;
      }
    }

    List<Transition> apiTransitions = apiScope.getTransitions();
    if (apiTransitions!=null) {
      int i = 0;
      for (Transition apiTransition: apiTransitions) {
        TransitionImpl transition = new TransitionImpl();
        workflowParser.pushContext("transitions", apiTransition, i);
        transition.parse(apiTransition, this, workflowParser, activitiesByDefaultTransitionId);
        addTransition(transition);
        workflowParser.popContext();
        i++;
      }
    }

    if (activities!=null) {
      // some activity types need to validate incoming and outgoing transitions, 
      // that's why they are validated after the transitions.
      int i = 0;
      for (ActivityImpl activity : activities.values()) {
        Activity apiActivity = apiActivities.get(i);
        if (activity.activityType != null) {
          workflowParser.pushContext("activities", apiActivity, i);
          activity.activityType.parse(activity, apiActivity, workflowParser);
          workflowParser.popContext();
        }
        i++;
      }
    }
    
    if (!activitiesByDefaultTransitionId.isEmpty()) {
      for (String nonExistingDefaultTransitionId: activitiesByDefaultTransitionId.keySet()) {
        ActivityImpl activity = activitiesByDefaultTransitionId.get(nonExistingDefaultTransitionId);
        workflowParser.addError("Activity '%s' has non existing default transition id '%s'", activity.id, nonExistingDefaultTransitionId);
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

  protected TransitionImpl findTransitionByIdLocal(String transitionId) {
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


  public boolean isProcessDefinition() {
    return parent!=null;
  }

  public boolean hasActivities() {
    return activities!=null && !activities.isEmpty();
  }

  public boolean hasVariables() {
    return variables!=null && !variables.isEmpty();
  }

  public boolean hasTransitions() {
    return transitions!=null && !transitions.isEmpty();
  }
  
  public boolean hasVariable(String variableId) {
    return variables!=null && variables.containsKey(variableId);
  }

  public ActivityImpl getActivityByIdLocal(String activityId) {
    return activities!=null ? activities.get(activityId) : null;
  } 
}
