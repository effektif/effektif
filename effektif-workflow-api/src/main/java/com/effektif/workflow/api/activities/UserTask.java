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
public class UserTask extends NoneTask {
  
  protected Binding nameBinding;
  protected List<Binding> candidateIdBindings;
  
  public UserTask() {
  }

  public UserTask(String id) {
    super(id);
  }

  public UserTask name(String name) {
    this.nameBinding = new Binding().value(name);
    return this;
  }

  public UserTask nameVariableId(String nameVariableId) {
    this.nameBinding = new Binding().variableId(nameVariableId);
    return this;
  }

  public UserTask nameExpression(String nameExpression) {
    this.nameBinding = new Binding().expression(nameExpression);
    return this;
  }

  /** adds a candidate id value to the list */
  public UserTask candidateId(String candidateId) {
    addCandidateId(new Binding().value(candidateId));
    return this;
  }

  /** adds a candidate id variable to the list */
  public UserTask candidateIdVariableId(String candidateIdVariableId) {
    addCandidateId(new Binding().variableId(candidateIdVariableId));
    return this;
  }

  /** adds a candidate id expression to the list */
  public UserTask candidateIdExpression(String candidateIdExpression) {
    addCandidateId(new Binding().expression(candidateIdExpression));
    return this;
  }

  protected void addCandidateId(Binding binding) {
    if (candidateIdBindings==null) {
      candidateIdBindings = new ArrayList<>();
    }
    candidateIdBindings.add(binding);
  }
  
  public Binding getNameBinding() {
    return nameBinding;
  }
  
  public void setNameBinding(Binding nameBinding) {
    this.nameBinding = nameBinding;
  }
  
  public List<Binding> getCandidateIdBindings() {
    return candidateIdBindings;
  }
  
  public void setCandidateIdBindings(List<Binding> candidateIdBindings) {
    this.candidateIdBindings = candidateIdBindings;
  }
}
