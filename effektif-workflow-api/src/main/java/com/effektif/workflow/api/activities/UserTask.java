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

import java.util.ArrayList;
import java.util.List;

import com.effektif.workflow.api.acl.AccessControlList;
import com.effektif.workflow.api.form.Form;
import com.effektif.workflow.api.model.RelativeTime;
import com.effektif.workflow.api.ref.GroupId;
import com.effektif.workflow.api.ref.UserId;
import com.effektif.workflow.api.types.Type;
import com.effektif.workflow.api.workflow.Activity;
import com.effektif.workflow.api.workflow.Binding;
import com.effektif.workflow.api.workflow.MultiInstance;
import com.effektif.workflow.api.workflow.Timer;
import com.effektif.workflow.api.workflow.Transition;
import com.effektif.workflow.api.workflow.Variable;
import com.fasterxml.jackson.annotation.JsonTypeName;


/**
 * A user task, also known as a human task.  
 * Tasks can be assigned and optionally have a form to complete.
 *
 * BPMN XML: {@code <userTask id="approveRequest" name="Approve vacation request"/>}
 *
 * @author Tom Baeyens
 */
@JsonTypeName("userTask")
public class UserTask extends NoneTask {
  
  protected AccessControlList access;
  protected String taskName;
  protected Binding<UserId> assigneeId;
  protected List<Binding<UserId>> candidateIds;
  protected List<Binding<GroupId>> candidateGroupIds;
  protected Form form;
  protected RelativeTime duedate;
  protected RelativeTime reminder;
  protected RelativeTime reminderRepeat;
  protected RelativeTime escalate;
  protected Binding<UserId> escalateToId;

  public UserTask() {
  }

  public UserTask(String id) {
    super(id);
  }

  public String getTaskName() {
    return taskName;
  }
  public void setTaskName(String nameBinding) {
    this.taskName = nameBinding;
  }
  /** expression to create the task name optionally using variable information
   * like eg "Clean room {{location.room}}" */
  public UserTask taskName(String taskName) {
    this.taskName = taskName;
    return this;
  }

  public Binding<UserId> getAssigneeId() {
    return assigneeId;
  }
  public void setAssigneeId(Binding<UserId> assignee) {
    this.assigneeId = assignee;
  }
  /** Sets the assignee to a user ID value. */
  public UserTask assigneeUserId(String assigneeUserId) {
    this.assigneeId = new Binding().value(new UserId(assigneeUserId));
    return this;
  }
  /** Sets the assignee to a variable value. */
  public UserTask assigneeVariableId(String assigneeVariableId) {
    this.assigneeId = new Binding().variableId(assigneeVariableId);
    return this;
  }
  /** Sets the assignee to an expression. */
  public UserTask assigneeExpression(String assigneeExpression) {
    this.assigneeId = new Binding().expression(assigneeExpression);
    return this;
  }
  
  public List<Binding<UserId>> getCandidateIds() {
    return candidateIds;
  }
  public void setCandidateIds(List<Binding<UserId>> candidates) {
    this.candidateIds = candidates;
  }
  /** adds a candidate id value to the list */
  public UserTask candidateUserId(String candidateUserId) {
    addCandidateBinding(new Binding().value(new UserId(candidateUserId)));
    return this;
  }
  /** adds a candidate id variable to the list */
  public UserTask candidateVariableId(String candidateVariableId) {
    addCandidateBinding(new Binding().variableId(candidateVariableId));
    return this;
  }

  /** adds a candidate id expression to the list */
  public UserTask candidateExpression(String candidateExpression) {
    addCandidateBinding(new Binding().expression(candidateExpression));
    return this;
  }
  protected void addCandidateBinding(Binding<UserId> binding) {
    if (candidateIds==null) {
      candidateIds = new ArrayList<>();
    }
    candidateIds.add(binding);
  }
  
  public List<Binding<GroupId>> getCandidateGroupIds() {
    return candidateGroupIds;
  }
  public void setCandidateGroupIds(List<Binding<GroupId>> candidateGroupIds) {
    this.candidateGroupIds = candidateGroupIds;
  }
  /** adds a candidate id value to the list */
  public UserTask candidateGroupId(String candidateGroupId) {
    addCandidateBinding(new Binding().value(new UserId(candidateGroupId)));
    return this;
  }
  /** adds a candidate id variable to the list */
  public UserTask candidateGroupVariableId(String candidateGroupVariableId) {
    addCandidateBinding(new Binding().variableId(candidateGroupVariableId));
    return this;
  }
  /** adds a candidate id expression to the list */
  public UserTask candidateGroupExpression(String candidateGroupExpression) {
    addCandidateBinding(new Binding().expression(candidateGroupExpression));
    return this;
  }
  protected void addCandidateGroupBinding(Binding<GroupId> candidateGroupBinding) {
    if (candidateGroupIds==null) {
      candidateGroupIds = new ArrayList<>();
    }
    candidateGroupIds.add(candidateGroupBinding);
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
  
  public Binding<UserId> getEscalateToId() {
    return this.escalateToId;
  }
  public void setEscalateToId(Binding<UserId> escalateTo) {
    this.escalateToId = escalateTo;
  }
  public UserTask escalateTo(Binding<UserId> escalateTo) {
    this.escalateToId = escalateTo;
    return this;
  }
  public UserTask escalateToUserId(String escalateToUserId) {
    escalateTo(new Binding<UserId>().value(new UserId(escalateToUserId)));
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
  public UserTask name(String name) {
    super.name(name);
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
