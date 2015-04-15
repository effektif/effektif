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
import com.effektif.workflow.api.form.FormField;
import com.effektif.workflow.api.mapper.BpmnElement;
import com.effektif.workflow.api.mapper.BpmnReader;
import com.effektif.workflow.api.mapper.JsonReader;
import com.effektif.workflow.api.mapper.JsonWriter;
import com.effektif.workflow.api.mapper.TypeName;
import com.effektif.workflow.api.mapper.XmlElement;
import com.effektif.workflow.api.model.GroupId;
import com.effektif.workflow.api.model.RelativeTime;
import com.effektif.workflow.api.model.UserId;
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
 * @see <a href="https://github.com/effektif/effektif/wiki/User-Task">User Task</a>
 * @author Tom Baeyens
 */
@JsonTypeName("userTask")
@TypeName("userTask")
@BpmnElement("userTask")
public class UserTask extends NoneTask {
  
  protected AccessControlList access;
  protected String taskName;

  /** User who has been assigned to complete this task. */
  protected Binding<UserId> assigneeId;

  /** User who is a candidate for being the assignee; can be used to allow users to pick tasks to work on. */
  protected List<Binding<UserId>> candidateIds;

  /** Organisation group (defined separately from the process) that contains candidates. */
  protected List<Binding<GroupId>> candidateGroupIds;

  /** Collection of fields that a user can fill in; can be used for a user task’s user interface. */
  protected Form form;

  /** Deadline for completing the task; can be used for sorting task lists. */
  protected RelativeTime duedate;

  /** When to send the assignee a reminder to complete the task; can be used for sending notifications. */
  protected RelativeTime reminder;

  /** When to send repeated reminders; can be used for follow-up notifications. */
  protected RelativeTime reminderRepeat;

  /** When to escalate the task by assigning the task to another user. */
  protected RelativeTime escalate;

  /** User to assign the task to when escalating. */
  protected Binding<UserId> escalateToId;

  @Override
  public void readBpmn(BpmnReader r) {
    super.readBpmn(r);
    r.startExtensionElements();
    candidateGroupIds = r.readBindings("candidateGroupId", GroupId.class);
    duedate = r.readRelativeTimeEffektif("dueDate");
    reminder = r.readRelativeTimeEffektif("reminder");
    reminderRepeat = r.readRelativeTimeEffektif("reminderRepeat");
    escalate = r.readRelativeTimeEffektif("escalate");
    escalateToId = r.readBinding("escalateToId", UserId.class);

    for (XmlElement formElement : r.readElementsEffektif("form")) {
      form = new Form();
      r.startElement(formElement);
      form.setDescription(r.readTextEffektif("description"));

      for (XmlElement fieldElement : r.readElementsEffektif("field")) {
        FormField field = new FormField();
        r.startElement(fieldElement);
        field.setId(r.readStringAttributeEffektif("id"));
        field.setName(r.readStringAttributeEffektif("name"));
        Binding<String> binding = new Binding<>();
        binding.setValue(r.readStringAttributeEffektif("value"));
        binding.setExpression(r.readStringAttributeEffektif("expression"));
        field.setBinding(binding);
        r.endElement();
        form.field(field);
      }
      r.endElement();
    }

    r.endExtensionElements();
  }

  @Override
  public void writeBpmn(com.effektif.workflow.api.mapper.BpmnWriter w) {
    super.writeBpmn(w);
    w.startExtensionElements();
    w.writeBindings("candidateGroupId", candidateGroupIds);
    w.writeRelativeTimeEffektif("dueDate", duedate);
    w.writeRelativeTimeEffektif("reminder", reminder);
    w.writeRelativeTimeEffektif("reminderRepeat", reminderRepeat);
    w.writeRelativeTimeEffektif("escalate", escalate);
    w.writeBinding("escalateToId", escalateToId);

    if (form != null) {
      w.startElementEffektif("form");
      w.writeTextEffektif("description", form.getDescription());

      for (FormField field : form.getFields()) {
        w.startElementEffektif("field");
        w.writeStringAttributeEffektif("id", field.getId());
        w.writeStringAttributeEffektif("name", field.getName());
        Binding<?> binding = field.getBinding();
        if (binding != null) {
          w.writeStringAttributeEffektif("expression", binding.getExpression());
          if (binding.getValue() != null) {
            w.writeStringAttributeEffektif("value", binding.getValue().toString());
          }
        }
        w.endElement();
      }

      w.endElement();
    }

    w.endExtensionElements();
  }

//  @Override
//  public void readJson(JsonReader r) {
//    super.readJson(r);
//  }
//
//  @Override
//  public void writeJson(JsonWriter w) {
//    w.writeWritable("access", access);
//    super.writeJson(w);
//  }

  @Override
  public UserTask id(String id) {
    super.id(id);
    return this;
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
  public UserTask assigneeId(String assigneeId) {
    this.assigneeId = new Binding().value(new UserId(assigneeId));
    return this;
  }
  /** Sets the assignee to a user ID value. */
  public UserTask assigneeId(UserId assigneeId) {
    this.assigneeId = new Binding().value(assigneeId);
    return this;
  }
  /** Sets the assignee to a variable value. */
  public UserTask assigneeExpression(String expression) {
    this.assigneeId = new Binding().expression(expression);
    return this;
  }
  
  public List<Binding<UserId>> getCandidateIds() {
    return candidateIds;
  }
  public void setCandidateIds(List<Binding<UserId>> candidates) {
    this.candidateIds = candidates;
  }
  /** adds a candidate id value to the list */
  public UserTask candidateId(String candidateId) {
    addCandidateBinding(new Binding().value(new UserId(candidateId)));
    return this;
  }
  /** adds a candidate id value to the list */
  public UserTask candidateId(UserId candidateId) {
    addCandidateBinding(new Binding().value(candidateId));
    return this;
  }
  /** adds a candidate id variable to the list */
  public UserTask candidateExpression(String expression) {
    addCandidateBinding(new Binding().expression(expression));
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
    addCandidateGroupBinding(new Binding().value(new GroupId(candidateGroupId)));
    return this;
  }
  /** adds a candidate id value to the list */
  public UserTask candidateGroupId(GroupId candidateGroupId) {
    addCandidateGroupBinding(new Binding().value(candidateGroupId));
    return this;
  }
  /** adds a candidate id variable to the list */
  public UserTask candidateGroupExpression(String expression) {
    addCandidateGroupBinding(new Binding().expression(expression));
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

  /**
   * @see <a href="https://github.com/effektif/effektif/wiki/Multi-instance-tasks">Multi-instance tasks</a>
   */
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
