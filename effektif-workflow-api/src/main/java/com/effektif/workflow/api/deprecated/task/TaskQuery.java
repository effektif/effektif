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
package com.effektif.workflow.api.deprecated.task;

import java.util.EnumSet;
import java.util.Set;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.LocalDateTime;

import com.effektif.workflow.api.deprecated.model.TaskId;
import com.effektif.workflow.api.deprecated.model.UserId;
import com.effektif.workflow.api.query.OrderDirection;
import com.effektif.workflow.api.query.Query;

import static com.effektif.workflow.api.deprecated.task.TaskQuery.DueDateFilter.LATER;
import static com.effektif.workflow.api.deprecated.task.TaskQuery.DueDateFilter.NOT_DUE;
import static com.effektif.workflow.api.deprecated.task.TaskQuery.DueDateFilter.OVERDUE;
import static com.effektif.workflow.api.deprecated.task.TaskQuery.DueDateFilter.THIS_WEEK;
import static com.effektif.workflow.api.deprecated.task.TaskQuery.DueDateFilter.TODAY;

/**
 * A query for finding {@link com.effektif.workflow.api.deprecated.task.Task} instances using
 * {@link com.effektif.workflow.api.deprecated.task.TaskService#findTasks(TaskQuery)}.
 *
 * @author Tom Baeyens
 */
public class TaskQuery extends Query {

  protected TaskId taskId;
  protected String taskName;
  // 3 state logic:
  //   - null means return both completed and open tasks
  //   - false means return only open tasks
  //   - true means return only completed tasks
  protected Boolean completed;
  protected UserId taskAssigneeId;

  /** Task due date categories. */
  public enum DueDateFilter { OVERDUE, TODAY, THIS_WEEK, LATER, NOT_DUE }

  /** The query matches when any of these dates apply. */
  protected EnumSet<DueDateFilter> dueDates = EnumSet.noneOf(DueDateFilter.class);
  
  public boolean meetsCriteria(Task task) {
    if (taskId!=null && !taskId.equals(task.getId())) {
      return false;
    }
    if (taskAssigneeId!=null) {
      if (!taskAssigneeId.equals(task.getAssigneeId())) {
        return false;
      }
    }
    if (completed!=null) {
      if (completed && !task.isCompleted()) {
        return false;
      }
      if (!completed && task.isCompleted()) {
        return false;
      }
    }
    if ( taskName!=null 
         && (task.getName()==null || !task.getName().contains(taskName))) {
      return false;
    }

    // Due dates
    if (!meetsDueDatesCriteria(task)) {
      return false;
    }

    return true;
  }

  // TODO Find or add test that tests this.
  private boolean meetsDueDatesCriteria(Task task) {
    if (dueDates.isEmpty()) {
      return true;
    }

    LocalDateTime taskDue = task.getDuedate();
    LocalDateTime today = new DateTime().withTimeAtStartOfDay().toLocalDateTime();
    LocalDateTime tomorrow = today.plusDays(1);
    LocalDateTime nextWeek = today.plusWeeks(1).withDayOfWeek(DateTimeConstants.MONDAY);

    if (dueDates.contains(OVERDUE) && taskDue.isBefore(today)) {
      return true;
    }
    if (dueDates.contains(TODAY) && !taskDue.isBefore(today) && taskDue.isBefore(tomorrow)) {
      return true;
    }
    if (dueDates.contains(THIS_WEEK) && !taskDue.isBefore(tomorrow) && taskDue.isBefore(nextWeek)) {
      return true;
    }
    if (dueDates.contains(LATER) && !taskDue.isBefore(nextWeek)) {
      return true;
    }
    if (dueDates.contains(NOT_DUE) && taskDue == null) {
      return true;
    }

    return false;
  }

  public Boolean getCompleted() {
    return this.completed;
  }
  public void setCompleted(Boolean completed) {
    this.completed = completed;
  }
  public TaskQuery completed() {
    this.completed = true;
    return this;
  }
  public TaskQuery open() {
    this.completed = false;
    return this;
  }

  public UserId getTaskAssigneeId() {
    return this.taskAssigneeId;
  }
  public void setTaskAssigneeId(UserId taskAssigneeId) {
    this.taskAssigneeId = taskAssigneeId;
  }
  public TaskQuery taskAssigneeId(UserId taskAssigneeId) {
    this.taskAssigneeId = taskAssigneeId;
    return this;
  }
  public TaskQuery taskAssigneeId(String taskAssigneeId) {
    taskAssigneeId(new UserId(taskAssigneeId));
    return this;
  }

  public String getTaskName() {
    return this.taskName;
  }
  public void setTaskName(String taskName) {
    this.taskName = taskName;
  }
  /** include only tasks that contain the string taskName in their name. */ 
  public TaskQuery taskName(String taskName) {
    this.taskName = taskName;
    return this;
  }

  public TaskId getTaskId() {
    return this.taskId;
  }
  public void setTaskId(TaskId taskId) {
    this.taskId = taskId;
  }
  public TaskQuery taskId(TaskId taskId) {
    this.taskId = taskId;
    return this;
  }
  public TaskQuery taskId(String taskIdInternal) {
    this.taskId = new TaskId(taskIdInternal);
    return this;
  }

  public Set<DueDateFilter> getDueDates() {
    return dueDates;
  }
  public void dueDate(DueDateFilter filter) {
    dueDates.add(filter);
  }

  @Override
  public TaskQuery skip(Integer skip) {
    super.skip(skip);
    return this;
  }

  @Override
  public TaskQuery limit(Integer limit) {
    super.limit(limit);
    return this;
  }

  @Override
  public TaskQuery orderBy(String field, OrderDirection direction) {
    super.orderBy(field, direction);
    return this;
  }
}