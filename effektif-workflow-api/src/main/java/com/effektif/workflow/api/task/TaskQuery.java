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
package com.effektif.workflow.api.task;

import com.effektif.workflow.api.model.UserId;
import com.effektif.workflow.api.query.OrderDirection;
import com.effektif.workflow.api.query.Query;


/**
 * @author Tom Baeyens
 */
public class TaskQuery extends Query {

  protected String taskId;
  protected String taskName;
  // 3 state logic:
  //   - null means return both completed and open tasks
  //   - false means return only open tasks 
  //   - true means return only completed tasks 
  protected Boolean completed;
  protected UserId taskAssigneeId;
  
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
    return true;
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

  public String getTaskId() {
    return this.taskId;
  }
  public void setTaskId(String taskId) {
    this.taskId = taskId;
  }
  public TaskQuery taskId(String taskId) {
    this.taskId = taskId;
    return this;
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