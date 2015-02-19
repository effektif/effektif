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
package com.effektif.workflow.impl;

import java.util.List;

import com.effektif.workflow.api.model.RequestContext;
import com.effektif.workflow.api.task.Task;
import com.effektif.workflow.api.task.TaskQuery;
import com.effektif.workflow.api.task.TaskService;


/**
 * @author Tom Baeyens
 */
public class ContextualTaskService implements TaskService {
  
  TaskService taskService;
  RequestContext requestContext;
  
  public ContextualTaskService(TaskService taskService, RequestContext requestContext) {
    this.taskService = taskService;
    this.requestContext = requestContext;
  }

  @Override
  public void saveTask(Task task) {
    try {
      RequestContext.set(requestContext);
      taskService.saveTask(task);
    } finally {
      RequestContext.unset();
    }
  }

  @Override
  public List<Task> findTasks(TaskQuery taskQuery) {
    try {
      RequestContext.set(requestContext);
      return taskService.findTasks(taskQuery);
    } finally {
      RequestContext.unset();
    }
  }

  @Override
  public void deleteTasks(TaskQuery taskQuery) {
    try {
      RequestContext.set(requestContext);
      taskService.deleteTasks(taskQuery);
    } finally {
      RequestContext.unset();
    }
  }

  @Override
  public TaskService createTaskService(RequestContext requestContext) {
    return new ContextualTaskService(taskService, requestContext);
  }
}
