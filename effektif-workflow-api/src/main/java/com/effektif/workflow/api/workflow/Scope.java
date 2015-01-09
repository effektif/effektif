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
package com.effektif.workflow.api.workflow;

import java.util.ArrayList;
import java.util.List;


public class Scope extends Base {

  protected List<Activity> activities;
  protected List<Transition> transitions;
  protected List<Variable> variables;
  protected List<Timer> timers;
  protected List<InputBinding> inputs;
  protected List<OutputBinding> outputs; 

  public List<Activity> getActivities() {
    return this.activities;
  }
  public void setActivities(List<Activity> activities) {
    this.activities = activities;
  }
  public Scope activity(Activity activity) {
    if (this.activities==null) {
      this.activities = new ArrayList<>();
    }
    this.activities.add(activity);
    return this;
  }
  
  public List<Transition> getTransitions() {
    return this.transitions;
  }
  public void setTransitions(List<Transition> transitions) {
    this.transitions = transitions;
  }
  public Scope transition(Transition transition) {
    if (this.transitions==null) {
      this.transitions = new ArrayList<>();
    }
    this.transitions.add(transition);
    return this;
  }
  
  public List<Variable> getVariables() {
    return this.variables;
  }
  public void setVariables(List<Variable> variables) {
    this.variables = variables;
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
  
  public List<InputBinding> getInputs() {
    return inputs;
  }
  
  public void setInputs(List<InputBinding> inputs) {
    this.inputs = inputs;
  }

  protected Scope inputValue(String key, Object value) {
    input(new InputBindingValue(key, value));
    return this;
  }

  protected Scope inputVariableId(String key, String variableId) {
    input(new InputBindingVariable(key, variableId));
    return this;
  }

  protected Scope inputExpression(String key, String expression) {
    input(new InputBindingExpression(key, expression));
    return this;
  }

  protected Scope input(InputBinding input) {
    if (this.inputs==null) {
      this.inputs = new ArrayList<>();
    }
    this.inputs.add(input);
    return this;
  }
  
  protected Object getInputBindingValue(String key) {
    if (inputs!=null) {
      for (InputBinding input : inputs) {
        if (key.equals(input.key)
            && input instanceof InputBindingValue) {
          return ((InputBindingValue)input).value;
        }
      }
    }
    return null;
  }
  
  public List<InputBinding> getInputs(String key) {
    List<InputBinding> keyInputs = new ArrayList<>();
    if (inputs!=null) {
      for (InputBinding input : inputs) {
        if (key.equals(input.key)) {
          keyInputs.add(input);
        }
      }
    }
    return keyInputs;
  }

  public List<OutputBinding> getOutputs() {
    return outputs;
  }
  
  public void setOutputs(List<OutputBinding> outputs) {
    this.outputs = outputs;
  }
  
  protected Scope output(OutputBinding output) {
    if (this.outputs==null) {
      this.outputs = new ArrayList<>();
    }
    this.outputs.add(output);
    return this;
  }
}
