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
package com.effektif.workflow.impl.definition;

import java.util.ArrayList;
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
import com.effektif.workflow.impl.WorkflowEngineImpl;
import com.effektif.workflow.impl.util.Exceptions;


/**
 * @author Walter White
 */
public abstract class ScopeImpl extends BaseImpl {

  public List<ActivityImpl> startActivities;
  public Map<String, ActivityImpl> activities;
  public Map<String, VariableImpl> variables;
  public List<TimerImpl> timers;
  public List<TransitionImpl> transitions;

  public ScopeImpl(Scope apiScope) {
    super(apiScope);
    List<Activity> apiActivities = apiScope.getActivities();
    if (apiActivities!=null) {
      for (Activity apiActivity: apiActivities) {
        ActivityImpl activity = new ActivityImpl(apiActivity);
        if (activities==null) {
          activities = new LinkedHashMap<>();
        }
        activities.put(activity.id, activity);
      }
    }
    List<Transition> apiTransitions = apiScope.getTransitions();
    if (apiTransitions!=null) {
      for (Transition apiTransition: apiTransitions) {
        TransitionImpl transition = new TransitionImpl(apiTransition);
        if (transitions==null) {
          transitions = new ArrayList<>(apiTransitions.size());
        }
        transitions.add(transition);
      }
    }
    List<Variable> apiVariables = apiScope.getVariables();
    if (apiVariables!=null) {
      for (Variable apiVariable: apiVariables) {
        VariableImpl variable = new VariableImpl(apiVariable);
        if (variables==null) {
          variables = new LinkedHashMap<>();
        }
        variables.put(variable.id, variable);
      }
    }
    List<Timer> apiTimers = apiScope.getTimers();
    if (apiTimers!=null) {
      for (Timer apiTimer: apiTimers) {
        TimerImpl timer = new TimerImpl(apiTimer);
        if (timers==null) {
          timers = new ArrayList<>(apiTimers.size());
        }
        timers.add(timer);
      }
    }
  }

  public void validate(WorkflowValidator validator) {
    if (transitions!=null) {
      int i = 0;
      for (TransitionImpl transition: transitions) {
        validator.pushContext(transition, i);
        transition.parent = this;
        transition.validate(validator);
        validator.popContext();
        i++;
      }
    }
    if (activities!=null) {
      Set<String> activityIds = new HashSet<>();
      int i = 0;
      for (ActivityImpl activity: activities.values()) {
        validator.pushContext(activity, i);
        activity.parent = this;
        activityIds.add(activity.id);
        if (activityIds.contains(activity.id)) {
          validator.addError("Duplicate activity id '%s'. Activity ids have to be unique in the scope they are defined.", activity.id);
        }
        activity.validate(validator);
        validator.popContext();
        i++;
      }
      this.startActivities = new ArrayList<>(activities.values());
      if (transitions!=null) {
        for (TransitionImpl transition: transitions) {
          this.startActivities.remove(transition.to);
        }
      }
      if (startActivities.isEmpty()) {
        validator.addWarning("No start activities in %s", id);
      }
    }
    if (variables!=null) {
      int i = 0;
      Set<String> variableIds = new HashSet<>();
      for (VariableImpl variable: variables.values()) {
        validator.pushContext(variable, i);
        variable.parent = this;
        variableIds.add(variable.id);
        if (variableIds.contains(variable.id)) {
          validator.addError("Duplicate variable name %s. Variables have to be unique in the process.", variable.id);
        }
        variable.validate(validator);
        validator.popContext();
        i++;
      }
    }
    if (timers!=null) {
      int i = 0;
      for (TimerImpl timer: timers) {
        validator.pushContext(timer, i);
        timer.parent = this;
        timer.validate(validator);
        validator.popContext();
        i++;
      }
    }
  }
  
  protected TransitionImpl findTransitionById(String transitionId) {
    if (transitions!=null) {
      for (TransitionImpl transition: transitions) {
        if (transitionId.equals(transition.id)) {
          return transition;
        }
      }
    }
    return null;
  }

  public ActivityImpl findActivityById(String activityId) {
    return activities.get(activityId);
  }


  public ActivityImpl getActivity(String activityDefinitionId) {
    return workflow.findActivity(activityDefinitionId);
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  public List<Activity> getStartActivities() {
    return (List)startActivities;
  }
  
  public void setStartActivities(List<ActivityImpl> startActivityDefinitions) {
    this.startActivities = startActivityDefinitions;
  }

  public WorkflowImpl getProcessDefinition() {
    return workflow;
  }

  public void setWorkflow(WorkflowImpl processDefinition) {
    this.workflow = processDefinition;
  }
  
  public WorkflowEngineImpl getProcessEngine() {
    return workflowEngine;
  }
  
  public void setWorkflowEngine(WorkflowEngineImpl processEngine) {
    this.workflowEngine = processEngine;
  }

  public ScopeImpl getParent() {
    return parent;
  }

  public void setParent(ScopeImpl parent) {
    this.parent = parent;
  }
  
  public boolean isProcessDefinition() {
    return parent!=null;
  }

  public <T extends ActivityImpl> T addActivityDefinition(T activityDefinition) {
    Exceptions.checkNotNull(activityDefinition, "activityDefinition");
    if (activityDefinitions==null)  {
      activityDefinitions = new ArrayList<>();
    }
    activityDefinitions.add(activityDefinition);
    return activityDefinition;
  }
  
  public boolean hasActivityDefinitions() {
    return activityDefinitions!=null && !activityDefinitions.isEmpty();
  }

  public  ScopeImpl addVariableDefinition(VariableImpl variableDefinition) {
    Exceptions.checkNotNull(variableDefinition, "variableDefinition");
    if (variableDefinitions==null)  {
      variableDefinitions = new ArrayList<>();
    }
    variableDefinitions.add(variableDefinition);
    return this;
  }
  
  public boolean hasVariableDefinitions() {
    return variableDefinitions!=null && !variableDefinitions.isEmpty();
  }

  public ScopeImpl addTransitionDefinition(TransitionImpl transitionDefinition) {
    Exceptions.checkNotNull(transitionDefinition, "transitionDefinition");
    if (transitionDefinitions==null)  {
      transitionDefinitions = new ArrayList<>();
    }
    transitionDefinitions.add(transitionDefinition);
    return this;
  }
  
  /// visistor ////////////////////////////////////////////////////////////

  public void visit(WorkflowVisitor visitor) {
    // If some visitor needs to control the order of types vs other content visited, 
    // then this is the idea you should consider 
    //   if (visitor instanceof OrderedProcessDefinitionVisitor) {
    //     ... also delegate the ordering of this visit to the visitor ... 
    //   } else { ... perform the default as below
    visitCompositeActivityDefinitions(visitor);
    visitCompositeTransitionDefinitions(visitor);
    visitCompositeVariableDefinitions(visitor);
  }

  protected void visitCompositeActivityDefinitions(WorkflowVisitor visitor) {
    if (activityDefinitions!=null) {
      for (int i=0; i<activityDefinitions.size(); i++) {
        ActivityImpl activityDefinition = activityDefinitions.get(i);
        activityDefinition.visit(visitor, i);
      }
    }
  }

  protected void visitCompositeVariableDefinitions(WorkflowVisitor visitor) {
    if (variableDefinitions!=null) {
      for (int i=0; i<variableDefinitions.size(); i++) {
        VariableImpl variableDefinition = variableDefinitions.get(i);
        visitor.variableDefinition(variableDefinition, i);
      }
    }
  }

  protected void visitCompositeTransitionDefinitions(WorkflowVisitor visitor) {
    if (transitionDefinitions!=null) {
      for (int i=0; i<transitionDefinitions.size(); i++) {
        TransitionImpl transitionDefinition = transitionDefinitions.get(i);
        visitor.transitionDefinition(transitionDefinition, i);
      }
    }
  }

  public boolean containsVariable(Object variableDefinitionId) {
    if (variableDefinitionId==null) {
      return false;
    }
    if (variableDefinitions!=null) {
      for (VariableImpl variableDefinition: variableDefinitions) {
        if (variableDefinitionId.equals(variableDefinition.id)) {
          return true;
        }
      }
    }
    ScopeImpl parent = getParent();
    if (parent!=null) {
      return parent.containsVariable(variableDefinitionId);
    }
    return false;
  }

  // getters and setters ////////////////////////////////////////////////////////////
  
  public boolean hasTransitionDefinitions() {
    return transitionDefinitions!=null && !transitionDefinitions.isEmpty();
  } 
  
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public List<Activity> getActivityDefinitions() {
    return (List<Activity>) (List) activityDefinitions;
  }
  
  public void setActivityDefinitions(List<ActivityImpl> activityDefinitions) {
    this.activityDefinitions = activityDefinitions;
  }

  public List<VariableImpl> getVariableDefinitions() {
    return variableDefinitions;
  }

  public void setVariableDefinitions(List<VariableImpl> variableDefinitions) {
    this.variableDefinitions = variableDefinitions;
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  public List<Transition> getTransitions() {
    return (List) transitionDefinitions;
  }
  
  public void setTransitionDefinitions(List<TransitionImpl> transitionDefinitions) {
    this.transitionDefinitions = transitionDefinitions;
  }

  public List<TimerDefinitionImpl> getTimerDefinitions() {
    return timerDefinitions;
  }
  
  public void setTimerDefinitions(List<TimerDefinitionImpl> timerDefinitions) {
    this.timerDefinitions = timerDefinitions;
  }
}
