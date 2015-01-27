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

import com.effektif.workflow.api.workflow.Activity;
import com.effektif.workflow.api.workflow.Binding;
import com.effektif.workflow.api.workflow.MultiInstance;
import com.effektif.workflow.api.workflow.Timer;
import com.effektif.workflow.api.workflow.Transition;
import com.effektif.workflow.api.workflow.Variable;
import com.fasterxml.jackson.annotation.JsonTypeName;

/* invokes another workflow and ends when the other workflow instance completes */ 
@JsonTypeName("call")
public class Call extends MappableActivity {

  protected Binding<String> subWorkflowIdBinding; 
  protected Binding<String> subWorkflowNameBinding; 
  
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

  public void setSubWorkflowIdBinding(Binding<String> subWorkflowIdBinding) {
    this.subWorkflowIdBinding = subWorkflowIdBinding;
  }
  
  public void setSubWorkflowNameBinding(Binding<String> subWorkflowNameBinding) {
    this.subWorkflowNameBinding = subWorkflowNameBinding;
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
  
  
  @Override
  public Call inputMappingValue(Object value, String subWorkflowVariableId) {
    super.inputMappingValue(value, subWorkflowVariableId);
    return this;
  }

  @Override
  public Call inputMappingVariable(String variableId, String subWorkflowVariableId) {
    super.inputMappingVariable(variableId, subWorkflowVariableId);
    return this;
  }

  @Override
  public Call inputMappingExpression(String expression, String subWorkflowVariableId) {
    super.inputMappingExpression(expression, subWorkflowVariableId);
    return this;
  }

  @Override
  public Call outputMapping(String subWorkflowVariableId, String variableId) {
    super.outputMapping(subWorkflowVariableId, variableId);
    return this;
  }

  @Override
  public Call outputMapping(Binding calledBinding, String callerVariableId) {
    super.outputMapping(calledBinding, callerVariableId);
    return this;
  }
}
