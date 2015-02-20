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
import com.effektif.workflow.api.model.RelativeTime;
import com.effektif.workflow.api.ref.GroupReference;
import com.effektif.workflow.api.ref.UserReference;
import com.effektif.workflow.api.types.Type;
import com.effektif.workflow.api.workflow.Activity;
import com.effektif.workflow.api.workflow.Binding;
import com.effektif.workflow.api.workflow.MultiInstance;
import com.effektif.workflow.api.workflow.Timer;
import com.effektif.workflow.api.workflow.Transition;
import com.effektif.workflow.api.workflow.Variable;
import com.fasterxml.jackson.annotation.JsonTypeName;


/**
 * @author Tom Baeyens
 */
@JsonTypeName("userTask")
public class UserTask extends NoneTask {
  
  protected Binding<String> taskName;
  protected Binding<UserReference> assignee;
  protected Binding<UserReference> candidates;
  protected Binding<GroupReference> candidateGroups;
  protected RelativeTime duedate;
  protected Form form;
  protected AccessControlList access;
  protected RelativeTime escalate;
  protected Binding<UserReference> escalateTo;
  
  protected RelativeTime reminder;
  protected RelativeTime reminderRepeat;

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
    this.assignee = new Binding().value(new UserReference(assigneeUserId));
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
    addCandidateBinding(new Binding().value(new UserReference(assigneeUserId)));
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
  public AccessControlList getAccess() {
    return this.access;
  }
  /** the access control list specifies which actions are permitted by whom.
   * If not specified, all is allowed. */
  public void setAccess(AccessControlList access) {
    this.access = access;
  }

  public RelativeTime getDuedate() {
    return this.duedate;
  }
  public void setDuedate(RelativeTime duedate) {
    this.duedate = duedate;
  }
  public UserTask duedate(RelativeTime duedate) {
    this.duedate = duedate;
    return this;
  }

  public RelativeTime getEscalate() {
    return this.escalate;
  }
  public void setEscalate(RelativeTime escalate) {
    this.escalate = escalate;
  }
  public UserTask escalate(RelativeTime escalate) {
    this.escalate = escalate;
    return this;
  }
  
  public Binding<UserReference> getEscalateTo() {
    return this.escalateTo;
  }
  public void setEscalateTo(Binding<UserReference> escalateTo) {
    this.escalateTo = escalateTo;
  }
  public UserTask escalateTo(Binding<UserReference> escalateTo) {
    this.escalateTo = escalateTo;
    return this;
  }
  public UserTask escalateToUserId(String escalateToUserId) {
    escalateTo(new Binding<UserReference>().value(new UserReference(escalateToUserId)));
    return this;
  }
  
  public RelativeTime getReminder() {
    return this.reminder;
  }
  public void setReminder(RelativeTime reminder) {
    this.reminder = reminder;
  }
  public UserTask reminder(RelativeTime reminder) {
    this.reminder = reminder;
    return this;
  }

  public RelativeTime getReminderRepeat() {
    return this.reminderRepeat;
  }
  public void setReminderRepeat(RelativeTime reminderRepeat) {
    this.reminderRepeat = reminderRepeat;
  }
  public UserTask reminderRepeat(RelativeTime reminderRepeat) {
    this.reminderRepeat = reminderRepeat;
    return this;
  }

  @Override
  public UserTask defaultTransitionId(String defaultTransitionId) {
    super.defaultTransitionId(defaultTransitionId);
    return this;
  }

  @Override
  public UserTask multiInstance(MultiInstance multiInstance) {
    super.multiInstance(multiInstance);
    return this;
  }

  @Override
  public UserTask transitionTo(String toActivityId) {
    super.transitionTo(toActivityId);
    return this;
  }

  @Override
  public UserTask transitionToNext() {
    super.transitionToNext();
    return this;
  }

  @Override
  public UserTask transitionTo(Transition transition) {
    super.transitionTo(transition);
    return this;
  }

  @Override
  public UserTask activity(Activity activity) {
    super.activity(activity);
    return this;
  }

  @Override
  public UserTask activity(String id, Activity activity) {
    super.activity(id, activity);
    return this;
  }

  @Override
  public UserTask transition(Transition transition) {
    super.transition(transition);
    return this;
  }

  @Override
  public UserTask transition(String id, Transition transition) {
    super.transition(id, transition);
    return this;
  }

  @Override
  public UserTask variable(Variable variable) {
    super.variable(variable);
    return this;
  }

  @Override
  public UserTask timer(Timer timer) {
    super.timer(timer);
    return this;
  }

  @Override
  public UserTask id(String id) {
    super.id(id);
    return this;
  }

  @Override
  public UserTask property(String key, Object value) {
    super.property(key, value);
    return this;
  }

  @Override
  public UserTask variable(String id, Type type) {
    super.variable(id, type);
    return this;
  }

  @Override
  public UserTask propertyOpt(String key, Object value) {
    super.propertyOpt(key, value);
    return this;
  }

  @Override
  public UserTask description(String description) {
    super.description(description);
    return this;
  }
}
