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
package com.effektif.workflow.api.workflow;

import java.util.ArrayList;
import java.util.List;

import com.effektif.workflow.api.bpmn.BpmnReader;
import com.effektif.workflow.api.bpmn.BpmnWriter;
import com.effektif.workflow.api.types.DataType;


/**
 * @author Tom Baeyens
 */
public abstract class Scope extends Element {

  protected List<Activity> activities;
  protected List<Transition> transitions;
  protected List<Variable> variables;
  protected List<Timer> timers;

  @Override
  public void readBpmn(BpmnReader r) {
    r.readScope();
    super.readBpmn(r);
  }

  @Override
  public void writeBpmn(BpmnWriter w) {
    super.writeBpmn(w);
    w.writeScope();
  }

  public List<Activity> getActivities() {
    return this.activities;
  }
  public void setActivities(List<Activity> activities) {
    this.activities = activities;
  }
  public Scope activity(String id, Activity activity) {
    activity.setId(id);
    activity(activity);
    return this;
  }
  public Scope activity(Activity activity) {
    if (this.activities==null) {
      this.activities = new ArrayList<>();
    }
    this.activities.add(activity);

    if (activity.outgoingTransitions!=null) {
      for (Transition outgoingTransition: activity.outgoingTransitions) {
        outgoingTransition.fromId = activity.id;
      }
      if (!hasOutgoingTransitionsToNext(activity)) {
        if (activity.outgoingTransitions!=null) {
          transitions(activity.outgoingTransitions);
          activity.outgoingTransitions = null;
        }
      }
    }
    
    int previousIndex = this.activities.size()-2;
    Activity previousActivity = previousIndex>=0 ? this.activities.get(previousIndex) : null;
    if (previousActivity!=null && hasOutgoingTransitionsToNext(previousActivity)) {
      for (Transition outgoingTransition: previousActivity.outgoingTransitions) {
        if (outgoingTransition.isToNext()) {
          outgoingTransition.toId = activity.id;
          outgoingTransition.isToNext = null;
        }
      }
      transitions(previousActivity.outgoingTransitions);
      previousActivity.outgoingTransitions = null;
    }

    return this;
  }
  
  /**
   * Returns the activity with the given ID, or null if not found.
   */
  public Activity findActivity(String activityId) {
    if (activities == null || activityId == null) {
      return null;
    }
    for (Activity activity : activities) {
      if (activityId.equals(activity.getId())) {
        return activity;
      }
    }
    return null;
  }

  /**
   * Returns the transition with the given ID, or null if not found.
   */
  public Transition findTransition(String transitionId) {
    if (activities == null || transitionId == null) {
      return null;
    }
    for (Transition transition : transitions) {
      if (transitionId.equals(transition.getId())) {
        return transition;
      }
    }
    return null;
  }

  private boolean hasOutgoingTransitionsToNext(Activity activity) {
    if (activity.outgoingTransitions==null) {
      return false;
    }
    for (Transition outgoingTransition: activity.outgoingTransitions) {
      if (outgoingTransition.isToNext()) {
        return true;
      }
    }
    return false;
  }

  public List<Transition> getTransitions() {
    return this.transitions;
  }
  public void setTransitions(List<Transition> transitions) {
    this.transitions = transitions;
  }
  public Scope transition(String id, Transition transition) {
    transition.setId(id);
    transition(transition);
    return this;
  }
  public Scope transition(Transition transition) {
    if (this.transitions==null) {
      this.transitions = new ArrayList<>();
    }
    this.transitions.add(transition);
    return this;
  }
  public Scope transitions(List<Transition> transitions) {
    if (transitions!=null) {
      for (Transition transition: transitions) {
        transition(transition);
      }
    }
    return this;
  }
  
  public List<Variable> getVariables() {
    return this.variables;
  }
  public void setVariables(List<Variable> variables) {
    this.variables = variables;
  }
  public Scope variable(String id, DataType type) {
    Variable variable = new Variable();
    variable.setId(id);
    variable.setType(type);
    variable(variable);
    return this;
  }
  public Scope variable(Variable variable) {
    if (this.variables==null) {
      this.variables = new ArrayList<>();
    }
    this.variables.add(variable);
    return this;
  }

  public List<Timer> getTimers() {
    return this.timers;
  }
  public void setTimers(List<Timer> timers) {
    this.timers = timers;
  }
  public Scope timer(Timer timer) {
    if (this.timers==null) {
      this.timers = new ArrayList<>();
    }
    this.timers.add(timer);
    return this;
  }
}
