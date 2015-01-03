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

import com.effektif.workflow.api.annotations.Configuration;
import com.effektif.workflow.api.annotations.Label;
import com.effektif.workflow.api.workflow.Activity;
import com.effektif.workflow.api.workflow.Binding;
import com.effektif.workflow.api.workflow.MultiInstance;
import com.effektif.workflow.api.workflow.Timer;
import com.effektif.workflow.api.workflow.Transition;
import com.effektif.workflow.api.workflow.Variable;
import com.fasterxml.jackson.annotation.JsonTypeName;


@JsonTypeName("call")
public class Call extends Activity {

  @Configuration
  @Label("Subprocess name")
  Binding<String> subProcessName;

  @Configuration
  @Label("Subprocess id")
  Binding<String> subProcessId;
  
  /** specifies which variables of this workflow instance (keys) have to be copied to 
   * variables in the called workflow instance (values). */
  @Configuration
  @Label("Input variable mappings")
  List<CallMapping> inputMappings;
  
  /** specifies which variables of the called process (keys) have to be copied to 
   * variables in this process (values). */
  @Configuration
  @Label("Output variable mappings")
  List<CallMapping> outputMappings;

  public Call subProcessId(String subProcessId) {
    return subProcessId(new Binding<String>().value(subProcessId));
  }

  public Call subProcessIdExpression(String subProcessIdExpression) {
    return subProcessId(new Binding<String>().expression(subProcessIdExpression));
  }

  public Call subProcessIdVariableId(String subProcessIdVariableId) {
    return subProcessId(new Binding<String>().variableId(subProcessIdVariableId));
  }

  protected Call subProcessId(Binding<String> subProcessIdBinding) {
    this.subProcessId = subProcessIdBinding;
    return this;
  }
  
  public Call inputMapping(String callerVariableId, String calledVariableId) {
    return inputMapping(new Binding<Object>().variableId(callerVariableId), calledVariableId);
  }

  public Call inputMapping(Binding<Object> callerBinding, String calledVariableId) {
    CallMapping inputMapping = new CallMapping()
      .source(callerBinding)
      .destinationVariableId(calledVariableId);
    if (inputMappings==null) {
      inputMappings = new ArrayList<>();
    }
    inputMappings.add(inputMapping);
    return this;
  }

  public Call outputMapping(String calledVariableId, String callerVariableId) {
    return outputMapping(new Binding<Object>().variableId(calledVariableId), callerVariableId);
  }

  public Call outputMapping(Binding<Object> calledBinding, String callerVariableId) {
    CallMapping inputMapping = new CallMapping()
      .source(calledBinding)
      .destinationVariableId(callerVariableId);
    if (inputMappings==null) {
      inputMappings = new ArrayList<>();
    }
    inputMappings.add(inputMapping);
    return this;
  }
  
  public Binding<String> getSubProcessName() {
    return subProcessName;
  }
  
  public void setSubProcessName(Binding<String> subProcessName) {
    this.subProcessName = subProcessName;
  }

  public Binding<String> getSubProcessId() {
    return subProcessId;
  }
  
  public void setSubProcessId(Binding<String> subProcessId) {
    this.subProcessId = subProcessId;
  }
  
  public List<CallMapping> getInputMappings() {
    return inputMappings;
  }
  
  public List<CallMapping> getOutputMappings() {
    return outputMappings;
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
}
