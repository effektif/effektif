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

import com.effektif.workflow.api.form.Form;
import com.effektif.workflow.api.ref.UserReference;
import com.effektif.workflow.api.workflow.Binding;
import com.fasterxml.jackson.annotation.JsonTypeName;


@JsonTypeName("userTask")
public class UserTask extends NoneTask {
  
  protected Binding<String> nameBinding;
  protected Binding<UserReference> assigneeBinding;
  protected Binding<UserReference> candidatesBinding;
  protected Form form;

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
  public UserTask assigneeUserId(String assigneeUserId) {
    this.assigneeBinding = new Binding().value(new UserReference().id(assigneeUserId));
    return this;
  }

  /** adds a candidate id variable to the list */
  public UserTask assigneeVariableId(String assigneeVariableId) {
    this.assigneeBinding = new Binding().variableId(assigneeVariableId);
    return this;
  }

  /** adds a candidate id expression to the list */
  public UserTask assigneeExpression(String assigneeExpression) {
    this.assigneeBinding = new Binding().expression(assigneeExpression);
    return this;
  }

  /** adds a candidate id value to the list */
  public UserTask candidateUserId(String assigneeUserId) {
    addCandidateBinding(new Binding().value(new UserReference().id(assigneeUserId)));
    return this;
  }

  /** adds a candidate id variable to the list */
  public UserTask candidateVariableId(String assigneeVariableId) {
    addCandidateBinding(new Binding().variableId(assigneeVariableId));
    return this;
  }

  /** adds a candidate id expression to the list */
  public UserTask candidateExpression(String assigneeExpression) {
    addCandidateBinding(new Binding().expression(assigneeExpression));
    return this;
  }

  protected void addCandidateBinding(Binding<UserReference> binding) {
    if (candidatesBinding==null) {
      candidatesBinding = new Binding<UserReference>();
    }
    candidatesBinding.binding(binding);
  }
  
  public Binding getNameBinding() {
    return nameBinding;
  }
  
  public void setNameBinding(Binding nameBinding) {
    this.nameBinding = nameBinding;
  }
  
  public Binding<UserReference> getCandidatesBinding() {
    return candidatesBinding;
  }
  
  public void setCandidatesBinding(Binding<UserReference> assigneeReferenceBindings) {
    this.candidatesBinding = assigneeReferenceBindings;
  }
  
  public Binding<UserReference> getAssigneeBinding() {
    return assigneeBinding;
  }
  
  public void setAssigneeBinding(Binding<UserReference> assigneeBinding) {
    this.assigneeBinding = assigneeBinding;
  }

  public Form getForm() {
    return this.form;
  }
  public void setForm(Form form) {
    this.form = form;
  }
  public UserTask form(Form form) {
    this.form = form;
    return this;
  }
}
