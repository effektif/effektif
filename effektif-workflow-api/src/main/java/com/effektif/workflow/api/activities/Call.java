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
import com.fasterxml.jackson.annotation.JsonTypeName;


@JsonTypeName("call")
public class Call extends Activity {

  Binding subProcessNameBinding;

  Binding subProcessIdBinding;
  
  /** specifies which variables of this workflow instance (keys) have to be copied to 
   * variables in the called workflow instance (values). */
  List<CallMapping> inputMappings;
  
  /** specifies which variables of the called process (keys) have to be copied to 
   * variables in this process (values). */
  List<CallMapping> outputMappings;


  public Call subProcessId(String subProcessId) {
    return subProcessId(new Binding().value(subProcessId));
  }

  public Call subProcessIdExpression(String subProcessIdExpression) {
    return subProcessId(new Binding().expression(subProcessIdExpression));
  }

  public Call subProcessIdVariable(String subProcessIdVariableId) {
    return subProcessId(new Binding().variableId(subProcessIdVariableId));
  }

  public Call subProcessId(Binding subProcessIdBinding) {
    this.subProcessIdBinding = subProcessIdBinding;
    return this;
  }
  
  public Call inputMapping(String callerVariableId, String calledVariableId) {
    return inputMapping(new Binding().variableId(callerVariableId), calledVariableId);
  }

  public Call inputMapping(Binding callerBinding, String calledVariableId) {
    CallMapping inputMapping = new CallMapping()
      .sourceBinding(callerBinding)
      .destinationVariableId(calledVariableId);
    if (inputMappings==null) {
      inputMappings = new ArrayList<>();
    }
    inputMappings.add(inputMapping);
    return this;
  }

  public Call outputMapping(String calledVariableId, String callerVariableId) {
    return outputMapping(new Binding().variableId(calledVariableId), callerVariableId);
  }

  public Call outputMapping(Binding calledBinding, String callerVariableId) {
    CallMapping inputMapping = new CallMapping()
      .sourceBinding(calledBinding)
      .destinationVariableId(callerVariableId);
    if (inputMappings==null) {
      inputMappings = new ArrayList<>();
    }
    inputMappings.add(inputMapping);
    return this;
  }
}
