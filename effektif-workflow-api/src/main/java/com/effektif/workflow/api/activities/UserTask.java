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
import com.effektif.workflow.api.workflow.Binding;
import com.fasterxml.jackson.annotation.JsonTypeName;


@JsonTypeName("userTask")
public class UserTask extends NoneTask {
  
  @Configuration
  @Label("Name")
  protected Binding<String> name;
  
  @Configuration
  @Label("Candidates")
  protected List<Binding<String>> candidateIds;
  
  public UserTask name(String nameValue) {
    this.name = new Binding<String>().value(nameValue);
    return this;
  }

  public UserTask nameVariableId(String nameVariableId) {
    this.name = new Binding<String>().variableId(nameVariableId);
    return this;
  }

  public UserTask nameExpression(String nameExpression) {
    this.name = new Binding<String>().expression(nameExpression);
    return this;
  }

  public UserTask candidateId(String candidateId) {
    addCandidateIdBinding(new Binding().value(candidateId));
    return this;
  }

  public UserTask candidateIdVariableId(String candidateIdVariableId) {
    addCandidateIdBinding(new Binding().variableId(candidateIdVariableId));
    return this;
  }

  public UserTask candidateIdExpression(String candidateIdExpression) {
    addCandidateIdBinding(new Binding().expression(candidateIdExpression));
    return this;
  }

  protected void addCandidateIdBinding(Binding binding) {
    if (candidateIds==null) {
      candidateIds = new ArrayList<Binding<String>>();
    }
    candidateIds.add(binding);
  }
  
  public Binding<String> getName() {
    return name;
  }
  
  public List<Binding<String>> getCandidateIds() {
    return candidateIds;
  }
  
  public void setName(Binding<String> name) {
    this.name = name;
  }
  
  public void setCandidateIds(List<Binding<String>> candidateIds) {
    this.candidateIds = candidateIds;
  }
}
