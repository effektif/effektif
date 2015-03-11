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
package com.effektif.workflow.test.serialization;

import java.util.ArrayList;
import java.util.List;

import com.effektif.workflow.api.model.TaskId;
import com.effektif.workflow.api.model.UserId;
import com.effektif.workflow.api.task.Task;
import com.effektif.workflow.api.task.TaskQuery;
import com.effektif.workflow.api.task.TaskService;
import com.effektif.workflow.impl.json.JsonService;


/**
 * @author Tom Baeyens
 */
public class SerializingTaskServiceImpl extends AbstractSerializingService implements TaskService {
  
  protected TaskService taskService;

  public SerializingTaskServiceImpl(TaskService taskService, JsonService jsonService) {
    super(jsonService);
    this.taskService = taskService;
  }

  @Override
  public Task createTask(Task task) {
    log.debug("saveTask");
    task = wireize("  >>task>>", task, Task.class);
    task = taskService.createTask(task);
    task = wireize("  <<task<<", task, Task.class);
    return task;
  }

  @Override
  public Task findTaskById(TaskId taskId) {
    log.debug("  >>taskId>> "+taskId);
    Task task = taskService.findTaskById(taskId);
    task = wireize("  <<task<<", task, Task.class);
    return task;
  }

  @Override
  public List<Task> findTasks(TaskQuery query) {
    log.debug("findTasks");
    query = wireize("  >>query>>", query, TaskQuery.class);
    List<Task> tasks = taskService.findTasks(query);
    if (tasks==null) {
      return null;
    }
    List<Task> wireizedTasks = new ArrayList<>(tasks.size());
    for (Task task: tasks) {
      wireizedTasks.add(wireize("  <<task<<", task, Task.class));
    }
    return tasks;
  }

  @Override
  public Task assignTask(TaskId taskId, UserId assignee) {
    log.debug("assignTask");
    assignee = wireize("  >>assignee>>", assignee, UserId.class);
    Task task = taskService.assignTask(taskId, assignee);
    task = wireize("  <<task<<", task, Task.class);
    return task;
  }

  @Override
  public void deleteTasks(TaskQuery query) {
    log.debug("deleteTasks");
    query = wireize("  >>query>>", query, TaskQuery.class);
    taskService.deleteTasks(query);
  }

  @Override
  public Task completeTask(TaskId taskId) {
    Task task = taskService.completeTask(taskId);
    task = wireize("  <<task<<", task, Task.class);
    return task;
  }
}
