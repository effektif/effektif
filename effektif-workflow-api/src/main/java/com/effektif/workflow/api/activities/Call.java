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
import com.effektif.workflow.api.workflow.InputBinding;
import com.effektif.workflow.api.workflow.InputBindingValue;
import com.effektif.workflow.api.workflow.MultiInstance;
import com.effektif.workflow.api.workflow.Timer;
import com.effektif.workflow.api.workflow.Transition;
import com.effektif.workflow.api.workflow.Variable;
import com.fasterxml.jackson.annotation.JsonTypeName;

/* invokes another workflow and ends when the other workflow instance completes */ 
@JsonTypeName("call")
public class Call extends Activity {

  public static final String SUB_PROCESS_ID = "subProcessId";
  public static final String SUB_PROCESS_NAME = "subProcessName";
  public static final String INPUT_MAPPINGS = "inputMappings";
  public static final String OUTPUT_MAPPINGS = "outputMappings";

  public Call subProcessId(String subProcessId) {
    inputValue(SUB_PROCESS_ID, subProcessId);
    return this;
  }

  public Call subProcessIdExpression(String subProcessIdExpression) {
    inputExpression(SUB_PROCESS_ID,subProcessIdExpression);
    return this;
  }

  public Call subProcessIdVariableId(String subProcessIdVariableId) {
    inputVariableId(SUB_PROCESS_ID, subProcessIdVariableId);
    return this;
  }

  public Call inputMappingValue(Object value, String subWorkflowVariableId) {
    return inputMapping(new InputBindingValue(subWorkflowVariableId, value));
  }

  public Call inputMappingVariable(String callerVariableId, String calledVariableId) {
    return inputMapping(new InputBindingVariable().variableId(callerVariableId), calledVariableId);
  }

  public Call inputMappingExpression(String callerVariableId, String calledVariableId) {
    return inputMapping(new InputBindingVariable().variableId(callerVariableId), calledVariableId);
  }

  public Call inputMapping(InputBinding callerBinding, String calledVariableId) {
    CallMapping inputMapping = new CallMapping()
      .source(callerBinding)
      .destinationVariableId(calledVariableId);
    inputValue(INPUT_MAPPINGS, inputMapping);
    return this;
  }

  public Call outputMapping(String calledVariableId, String callerVariableId) {
    return outputMapping(new InputBinding().variableId(calledVariableId), callerVariableId);
  }

  public Call outputMapping(InputBinding calledBinding, String callerVariableId) {
    CallMapping outputMapping = new CallMapping()
      .source(calledBinding)
      .destinationVariableId(callerVariableId);
    inputValue(OUTPUT_MAPPINGS, outputMapping);
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
}
