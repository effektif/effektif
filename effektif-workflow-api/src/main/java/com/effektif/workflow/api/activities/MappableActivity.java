/* Copyright (c) 2014, Effektif GmbH.
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


public abstract class MappableActivity extends Activity {

  protected List<Mapping> inputMappings; 
  protected List<Mapping> outputMappings; 
  
  public MappableActivity() {
  }

  public MappableActivity(String id) {
    super(id);
  }

  public MappableActivity inputMappingValue(Object value, String subWorkflowVariableId) {
    addInputMapping(new Binding().value(value), subWorkflowVariableId);
    return this;
  }

  public MappableActivity inputMappingVariable(String variableId, String subWorkflowVariableId) {
    addInputMapping(new Binding().variableId(variableId), subWorkflowVariableId);
    return this;
  }

  public MappableActivity inputMappingExpression(String expression, String subWorkflowVariableId) {
    addInputMapping(new Binding().expression(expression), subWorkflowVariableId);
    return this;
  }

  public MappableActivity addInputMapping(Binding sourceBinding, String subWorkflowVariableId) {
    if (inputMappings==null) {
      inputMappings = new ArrayList<>();
    }
    inputMappings.add(new Mapping()
      .sourceBinding(sourceBinding)
      .destinationKey(subWorkflowVariableId));
    return this;
  }

  public MappableActivity outputMapping(String subWorkflowVariableId, String variableId) {
    outputMapping(new Binding().variableId(subWorkflowVariableId), variableId);
    return this;
  }

  public MappableActivity outputMapping(Binding calledBinding, String callerVariableId) {
    if (outputMappings==null) {
      outputMappings = new ArrayList<>();
    }
    outputMappings.add(new Mapping()
      .sourceBinding(calledBinding)
      .destinationKey(callerVariableId));
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
}
