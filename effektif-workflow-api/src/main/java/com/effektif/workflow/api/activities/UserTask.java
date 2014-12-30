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

import com.effektif.workflow.api.workflow.Binding;
import com.fasterxml.jackson.annotation.JsonTypeName;


@JsonTypeName("userTask")
public class UserTask extends DefaultTask {
  
  Binding name;
  List<Binding> candidates;
  
  public UserTask name(String nameValue) {
    this.name = new Binding().value(nameValue);
    return this;
  }

  public UserTask nameVariable(String nameVariableId) {
    this.name = new Binding().variableId(nameVariableId);
    return this;
  }

  public UserTask nameExpression(String nameExpression) {
    this.name = new Binding().expression(nameExpression);
    return this;
  }

  public UserTask candidateId(String candidateId) {
    addCandidateBinding(new Binding().value(candidateId));
    return this;
  }

  public UserTask candidateVariable(String candidateVariableId) {
    addCandidateBinding(new Binding().variableId(candidateVariableId));
    return this;
  }

  public UserTask candidateExpression(String candidateExpression) {
    addCandidateBinding(new Binding().expression(candidateExpression));
    return this;
  }

  protected void addCandidateBinding(Binding binding) {
    if (candidates==null) {
      candidates = new ArrayList<Binding>();
    }
    candidates.add(binding);
  }
}
