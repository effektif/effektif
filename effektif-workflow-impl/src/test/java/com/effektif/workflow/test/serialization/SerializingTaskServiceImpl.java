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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.effektif.workflow.api.deprecated.form.FormInstance;
import com.effektif.workflow.api.deprecated.model.TaskId;
import com.effektif.workflow.api.deprecated.model.UserId;
import com.effektif.workflow.api.deprecated.task.Task;
import com.effektif.workflow.api.deprecated.task.TaskQuery;
import com.effektif.workflow.api.deprecated.task.TaskService;
import com.effektif.workflow.impl.deprecated.TaskServiceImpl;
import com.effektif.workflow.impl.json.JsonStreamMapper;


/**
 * @author Tom Baeyens
 */
public class SerializingTaskServiceImpl implements TaskService {
  
  private static final Logger log = LoggerFactory.getLogger(SerializingTaskServiceImpl.class+".JSON");
  
  protected TaskServiceImpl taskService;
  protected JsonStreamMapper jsonMapper;

  public SerializingTaskServiceImpl(TaskServiceImpl taskService, JsonStreamMapper jsonMapper) {
    this.taskService = taskService;
    this.jsonMapper = jsonMapper;
  }
  
  protected <T> T wireize(String name, T o) {
    if (o==null) return null;
    Class<T> clazz = (Class<T>) o.getClass();
    String jsonString = jsonMapper.write(o);
    log.debug(name+jsonString);
    return jsonMapper.readString(jsonString, clazz);
  }

  @Override
  public Task createTask(Task task) {
    log.debug("saveTask");
    task = wireize("  >>task>>", task);
    task = taskService.createTask(task);
    task = wireize("  <<task<<", task);
    return task;
  }

  @Override
  public Task findTaskById(TaskId taskId) {
    log.debug("  >>taskId>> "+taskId);
    Task task = taskService.findTaskById(taskId);
    task = wireize("  <<task<<", task);
    return task;
  }

  @Override
  public List<Task> findTasks(TaskQuery query) {
    log.debug("findTasks");
    query = wireize("  >>query>>", query);
    List<Task> tasks = taskService.findTasks(query);
    if (tasks==null) {
      return null;
    }
    List<Task> wireizedTasks = new ArrayList<>(tasks.size());
    for (Task task: tasks) {
      wireizedTasks.add(wireize("  <<task<<", task));
    }
    return tasks;
  }

  @Override
  public Task assignTask(TaskId taskId, UserId assignee) {
    log.debug("assignTask");
    assignee = wireize("  >>assignee>>", assignee);
    Task task = taskService.assignTask(taskId, assignee);
    task = wireize("  <<task<<", task);
    return task;
  }

  @Override
  public void deleteTasks(TaskQuery query) {
    log.debug("deleteTasks");
    query = wireize("  >>query>>", query);
    taskService.deleteTasks(query);
  }

  @Override
  public Task completeTask(TaskId taskId) {
    Task task = taskService.completeTask(taskId);
    task = wireize("  <<task<<", task);
    return task;
  }

  @Override
  public void saveFormInstance(TaskId taskId, FormInstance formInstance) {
    formInstance = wireize("  >>formInstance>>", formInstance);
    taskService.saveFormInstance(taskId, formInstance); 
  }
}
