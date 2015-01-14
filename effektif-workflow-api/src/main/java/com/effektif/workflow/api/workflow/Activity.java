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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;


@JsonTypeInfo(use=Id.NAME, include=As.PROPERTY, property="type")
public class Activity extends Scope {
  
  protected String defaultTransitionId;
  protected MultiInstance multiInstance;
  protected List<Transition> outgoingTransitions;
  protected Map<String, Object> configuration;
  
  public Activity() {
  }

  public Activity(String id) {
    this.id = id;
  }
  
  public String getDefaultTransitionId() {
    return this.defaultTransitionId;
  }
  public void setDefaultTransitionId(String defaultTransitionId) {
    this.defaultTransitionId = defaultTransitionId;
  }
  public Activity defaultTransitionId(String defaultTransitionId) {
    this.defaultTransitionId = defaultTransitionId;
    return this;
  }

  public MultiInstance getMultiInstance() {
    return this.multiInstance;
  }
  public void setMultiInstance(MultiInstance multiInstance) {
    this.multiInstance = multiInstance;
  }
  public Activity multiInstance(MultiInstance multiInstance) {
    this.multiInstance = multiInstance;
    return this;
  }

  public Activity transitionTo(String toActivityId) {
    transitionTo(new Transition().to(toActivityId));
    return this;
  }

  public Activity transitionTo(Transition transition) {
    if (this.outgoingTransitions==null) {
      this.outgoingTransitions = new ArrayList<>();
    }
    this.outgoingTransitions.add(transition);
    return this;
  }
  
  public List<Transition> getOutgoingTransitions() {
    return outgoingTransitions;
  }
  
  public void setOutgoingTransitions(List<Transition> outgoingTransitions) {
    this.outgoingTransitions = outgoingTransitions;
  }

  protected Scope setConfigurationBindingValue(String key, Object value) {
    setConfiguration(key, new Binding().value(value));
    return this;
  }

  protected Scope setConfigurationBindingVariableId(String key, String variableId) {
    setConfiguration(key, new Binding().variableId(variableId));
    return this;
  }

  protected Scope setConfigurationBindingExpression(String key, String expression) {
    setConfiguration(key, new Binding().expression(expression));
    return this;
  }

  protected Scope setConfiguration(String key, Object value) {
    if (configuration==null) {
      configuration = new HashMap<>(); 
    }
    configuration.put(key, value);
    return this;
  }
  
  protected Scope addConfigurationBindingValue(String key, Object value) {
    addConfiguration(key, new Binding().value(value));
    return this;
  }

  protected Scope addConfigurationBindingVariableId(String key, String variableId) {
    addConfiguration(key, new Binding().variableId(variableId));
    return this;
  }

  protected Scope addConfigurationBindingExpression(String key, String expression) {
    addConfiguration(key, new Binding().expression(expression));
    return this;
  }

  protected Scope addConfiguration(String key, Object value) {
    if (configuration==null) {
      configuration = new HashMap<>(); 
    }
    List<Object> values = (List<Object>) configuration.get(key);
    if (values==null) {
      values = new ArrayList<>();
      configuration.put(key, values);
    }
    values.add(value);
    return this;
  }
  
  
  
//  protected Object getInputValue(String key) {
//    if (inputs!=null) {
//      for (Input input : inputs) {
//        if (key.equals(input.key)
//            && input.binding instanceof BindingValue) {
//          return ((BindingValue)input.binding).value;
//        }
//      }
//    }
//    return null;
//  }
//  
  public Object getConfiguration(String key) {
    return configuration!=null ? configuration.get(key) : null;
  }

//  public List<Input> getInputs(String key) {
//    List<Input> keyInputs = new ArrayList<>();
//    if (inputs!=null) {
//      for (Input input : inputs) {
//        if (key.equals(input.key)) {
//          keyInputs.add(input);
//        }
//      }
//    }
//    return keyInputs;
//  }

  @Override
  public Activity activity(Activity activity) {
    super.activity(activity);
    return this;
  }
  @Override
  public Activity transition(Transition transition) {
    super.transition(transition);
    return this;
  }
  @Override
  public Activity variable(Variable variable) {
    super.variable(variable);
    return this;
  }
  @Override
  public Activity timer(Timer timer) {
    super.timer(timer);
    return this;
  }
  @Override
  public Activity id(String id) {
    super.id(id);
    return this;
  }
  @Override
  public Activity property(String key, Object value) {
    super.property(key, value);
    return this;
  }
}
