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
package com.effektif.workflow.api.activities;

import java.util.ArrayList;
import java.util.List;

import com.effektif.workflow.api.workflow.Activity;
import com.effektif.workflow.api.workflow.Binding;
import com.effektif.workflow.api.workflow.MultiInstance;
import com.effektif.workflow.api.workflow.Timer;
import com.effektif.workflow.api.workflow.Transition;
import com.effektif.workflow.api.workflow.Variable;
import com.fasterxml.jackson.annotation.JsonTypeName;

/* invokes another workflow and ends when the other workflow instance completes */ 
@JsonTypeName("call")
public class Call extends Activity {

  protected Binding<String> subWorkflowIdBinding; 
  protected Binding<String> subWorkflowNameBinding; 
  protected List<Mapping> inputMappings; 
  protected List<Mapping> outputMappings; 
  
  public Call() {
  }

  public Call(String id) {
    super(id);
  }

  public Call subWorkflowId(String subWorkflowId) {
    this.subWorkflowIdBinding = new Binding().value(subWorkflowId);
    return this;
  }

  public Call subWorkflowName(String subWorkflowName) {
    this.subWorkflowNameBinding = new Binding().value(subWorkflowName);
    return this;
  }
  
  public Binding<String> getSubWorkflowIdBinding() {
    return subWorkflowIdBinding;
  }
  
  public Binding<String> getSubWorkflowNameBinding() {
    return subWorkflowNameBinding;
  }

  public Call inputMappingValue(Object value, String subWorkflowVariableId) {
    addInputMapping(new Binding().value(value), subWorkflowVariableId);
    return this;
  }

  public Call inputMappingVariable(String variableId, String subWorkflowVariableId) {
    addInputMapping(new Binding().variableId(variableId), subWorkflowVariableId);
    return this;
  }

  public Call inputMappingExpression(String expression, String subWorkflowVariableId) {
    addInputMapping(new Binding().expression(expression), subWorkflowVariableId);
    return this;
  }

  public Call addInputMapping(Binding sourceBinding, String subWorkflowVariableId) {
    if (inputMappings==null) {
      inputMappings = new ArrayList<>();
    }
    inputMappings.add(new Mapping()
      .sourceBinding(sourceBinding)
      .destinationKey(subWorkflowVariableId));
    return this;
  }

  public Call outputMapping(String subWorkflowVariableId, String variableId) {
    outputMapping(new Binding().variableId(subWorkflowVariableId), variableId);
    return this;
  }

  public Call outputMapping(Binding calledBinding, String callerVariableId) {
    if (outputMappings==null) {
      outputMappings = new ArrayList<>();
    }
    outputMappings.add(new Mapping()
      .sourceBinding(calledBinding)
      .destinationKey(callerVariableId));
    return this;
  }
  
  @Override
  public Call multiInstance(MultiInstance multiInstance) {
    super.multiInstance(multiInstance);
    return this;
  }

  @Override
  public Call transitionTo(String toActivityId) {
    super.transitionTo(toActivityId);
    return this;
  }

  @Override
  public Call transitionTo(Transition transition) {
    super.transitionTo(transition);
    return this;
  }

  @Override
  public Call activity(Activity activity) {
    super.activity(activity);
    return this;
  }

  @Override
  public Call transition(Transition transition) {
    super.transition(transition);
    return this;
  }

  @Override
  public Call variable(Variable variable) {
    super.variable(variable);
    return this;
  }

  @Override
  public Call timer(Timer timer) {
    super.timer(timer);
    return this;
  }

  @Override
  public Call id(String id) {
    super.id(id);
    return this;
  }

  @Override
  public Call property(String key, Object value) {
    super.property(key, value);
    return this;
  }

  public List<Mapping> getInputMappings() {
    return inputMappings;
  }

  
  public void setInputMappings(List<Mapping> inputMappings) {
    this.inputMappings = inputMappings;
  }

  
  public List<Mapping> getOutputMappings() {
    return outputMappings;
  }

  
  public void setOutputMappings(List<Mapping> outputMappings) {
    this.outputMappings = outputMappings;
  }

  
  public void setSubWorkflowIdBinding(Binding<String> subWorkflowIdBinding) {
    this.subWorkflowIdBinding = subWorkflowIdBinding;
  }

  
  public void setSubWorkflowNameBinding(Binding<String> subWorkflowNameBinding) {
    this.subWorkflowNameBinding = subWorkflowNameBinding;
  }
}
