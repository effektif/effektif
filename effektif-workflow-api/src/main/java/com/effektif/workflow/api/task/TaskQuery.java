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

import com.effektif.workflow.api.query.OrderDirection;
import com.effektif.workflow.api.query.Query;


/**
 * @author Tom Baeyens
 */
public class TaskQuery extends Query {

  protected String taskId;

  public boolean meetsCriteria(Task task) {
    if (taskId!=null && !taskId.equals(task.getId())) {
      return false;
    }
    return true;
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