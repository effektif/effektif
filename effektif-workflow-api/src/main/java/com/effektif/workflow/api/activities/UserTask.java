/*
 * Copyright 2014 Effektif GmbH.
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
 * limitations under the License.
 */
package com.effektif.workflow.api.activities;

import com.effektif.workflow.api.acl.AccessControlList;
import com.effektif.workflow.api.form.Form;
import com.effektif.workflow.api.ref.GroupReference;
import com.effektif.workflow.api.ref.UserReference;
import com.effektif.workflow.api.task.RelativeTime;
import com.effektif.workflow.api.workflow.Binding;
import com.fasterxml.jackson.annotation.JsonTypeName;


@JsonTypeName("userTask")
public class UserTask extends NoneTask {
  
  protected Binding<String> taskName;
  protected Binding<UserReference> assignee;
  protected Binding<UserReference> candidates;
  protected Binding<GroupReference> candidateGroups;
  
  protected Binding<RelativeTime> duedate;

  public Binding<RelativeTime> getDuedate() {
    return this.duedate;
  }
  public void setDuedate(Binding<RelativeTime> duedate) {
    this.duedate = duedate;
  }
  public UserTask duedate(Binding<RelativeTime> duedate) {
    this.duedate = duedate;
    return this;
  }
  
  protected Binding<RelativeTime> escalate;

  public Binding<RelativeTime> getEscalate() {
    return this.escalate;
  }
  public void setEscalate(Binding<RelativeTime> escalate) {
    this.escalate = escalate;
  }
  public UserTask escalate(Binding<RelativeTime> escalate) {
    this.escalate = escalate;
    return this;
  }
  
  protected Binding<UserReference> escalateTo;

  public Binding<UserReference> getExcalateTo() {
    return this.escalateTo;
  }
  public void setExcalateTo(Binding<UserReference> escalateTo) {
    this.escalateTo = escalateTo;
  }
  public UserTask escalateTo(Binding<UserReference> escalateTo) {
    this.escalateTo = escalateTo;
    return this;
  }
  
  protected Binding<RelativeTime> reminder;

  public Binding<RelativeTime> getReminder() {
    return this.reminder;
  }
  public void setReminder(Binding<RelativeTime> reminder) {
    this.reminder = reminder;
  }
  public UserTask reminder(Binding<RelativeTime> reminder) {
    this.reminder = reminder;
    return this;
  }
  
  protected Binding<RelativeTime> reminderRepeat;

  public Binding<RelativeTime> getReminderRepeat() {
    return this.reminderRepeat;
  }
  public void setReminderRepeat(Binding<RelativeTime> reminderRepeat) {
    this.reminderRepeat = reminderRepeat;
  }
  public UserTask reminderRepeat(Binding<RelativeTime> reminderRepeat) {
    this.reminderRepeat = reminderRepeat;
    return this;
  }
  
  

  protected Form form;
  protected AccessControlList acl;

//  
//  public static final String DB_form = "f";
//  @Embedded(DB_form)
//  public Form form;
//  public UserTask form(Form form) {
//    this.form = form;
//    return this;
//  }
//
//  public static final String DB_reminder = "rmd";
//  @Embedded(DB_reminder)
//  public RelativeTime reminder;
//  public UserTask reminder(RelativeTime reminder) {
//    this.reminder = reminder;
//    return this;
//  }
//
//  public static final String DB_reminderRepeat = "rmdrpt";
//  @Embedded(DB_reminderRepeat)
//  public RelativeTime reminderRepeat;
//  public UserTask reminderRepeat(RelativeTime reminderRepeat) {
//    this.reminderRepeat = reminderRepeat;
//    return this;
//  }
//  
//  public static final String DB_roleId = "r";
//  @Property(DB_roleId)
//  public ObjectId roleId;
//  public UserTask roleId(ObjectId roleId) {
//    this.roleId = roleId;
//    return this;
//  }
  
  public UserTask() {
  }

  public UserTask(String id) {
    super(id);
  }

  public UserTask name(String name) {
    this.taskName = new Binding().value(name);
    return this;
  }

  public UserTask nameVariableId(String nameVariableId) {
    this.taskName = new Binding().variableId(nameVariableId);
    return this;
  }

  public UserTask nameExpression(String nameExpression) {
    this.taskName = new Binding().expression(nameExpression);
    return this;
  }

  /** adds a candidate id value to the list */
  public UserTask assigneeUserId(String assigneeUserId) {
    this.assignee = new Binding().value(new UserReference().id(assigneeUserId));
    return this;
  }

  /** adds a candidate id variable to the list */
  public UserTask assigneeVariableId(String assigneeVariableId) {
    this.assignee = new Binding().variableId(assigneeVariableId);
    return this;
  }

  /** adds a candidate id expression to the list */
  public UserTask assigneeExpression(String assigneeExpression) {
    this.assignee = new Binding().expression(assigneeExpression);
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
    if (candidates==null) {
      candidates = new Binding<UserReference>();
    }
    candidates.binding(binding);
  }
  
  public Binding<String> getTaskName() {
    return taskName;
  }
  
  public void setTaskName(Binding<String> nameBinding) {
    this.taskName = nameBinding;
  }
  
  public Binding<UserReference> getCandidates() {
    return candidates;
  }
  
  public void setCandidates(Binding<UserReference> candidates) {
    this.candidates = candidates;
  }
  
  public Binding<UserReference> getAssignee() {
    return assignee;
  }
  
  public void setAssignee(Binding<UserReference> assignee) {
    this.assignee = assignee;
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
  
  /** the access control list specifies which actions are permitted by whom.
   * If not specified, all is allowed. */
  public AccessControlList getAcl() {
    return this.acl;
  }
  /** the access control list specifies which actions are permitted by whom.
   * If not specified, all is allowed. */
  public void setAcl(AccessControlList acl) {
    this.acl = acl;
  }
  /** the access control list specifies which actions are permitted by whom.
   * If not specified, all is allowed. */
  public UserTask acl(AccessControlList acl) {
    this.acl = acl;
    return this;
  }

}
