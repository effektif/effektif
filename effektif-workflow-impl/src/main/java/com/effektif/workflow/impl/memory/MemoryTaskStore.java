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
package com.effektif.workflow.impl.memory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.effektif.workflow.api.model.UserId;
import com.effektif.workflow.api.task.Task;
import com.effektif.workflow.api.task.TaskQuery;
import com.effektif.workflow.impl.TaskStore;
import com.effektif.workflow.impl.util.Time;


/**
 * @author Tom Baeyens
 */
public class MemoryTaskStore implements TaskStore {
  
  protected int nextId = 1;
  protected Map<String, Task> tasks = Collections.synchronizedMap(new LinkedHashMap<String,Task>());

  @Override
  public void insertTask(Task task) {
    if (task.getId()==null) {
      String taskId = Integer.toString(nextId++);
      task.setId(taskId);
    }
    task.setLastUpdated(Time.now());
    tasks.put(task.getId(), task);
  }

  @Override
  public List<Task> findTasks(TaskQuery query) {
    List<Task> result = new ArrayList<>();
    for (Task task: tasks.values()) {
      if (query==null || query.meetsCriteria(task)) {
        result.add(task);
      }
    }
    return result;
  }

  @Override
  public void deleteTasks(TaskQuery taskQuery) {
    tasks.clear();
  }

  @Override
  public Task assignTask(String taskId, UserId assignee) {
    Task task = tasks.get(taskId);
    if (task!=null) {
      task.assigneeId(assignee);
      task.setLastUpdated(Time.now());
    }
    return task;
  }
  
  @Override
  public Task completeTask(String taskId) {
    // the task service will set the completed to true
    return findTaskById(taskId);
  }

  @Override
  public String generateTaskId() {
    return Integer.toString(nextId++);
  }

  @Override
  public Task addSubtask(String parentId, Task subtask) {
    Task parentTask = tasks.get(parentId);
    if (parentTask!=null) {
      parentTask.addSubtaskId(subtask.getId());
      parentTask.setLastUpdated(Time.now());
    }
    return parentTask;
  }

  public Task findTaskById(String taskId) {
    return tasks.get(taskId);
  }
}
