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

import com.effektif.workflow.api.ref.UserReference;
import com.effektif.workflow.api.task.Task;
import com.effektif.workflow.api.task.TaskQuery;
import com.effektif.workflow.api.task.TaskService;
import com.effektif.workflow.impl.NotificationService;
import com.effektif.workflow.impl.configuration.Brewable;
import com.effektif.workflow.impl.configuration.Brewery;


/**
 * @author Tom Baeyens
 */
public class MemoryTaskService implements TaskService, Brewable {
  
  protected int nextId = 1;
  protected Map<String, Task> tasks = Collections.synchronizedMap(new LinkedHashMap<String,Task>());
  protected NotificationService notificationService;

  @Override
  public void brew(Brewery brewery) {
    this.notificationService = brewery.getOpt(NotificationService.class);
  }

  @Override
  public void insertTask(Task task) {
    if (task.getId()==null) {
      String taskId = Integer.toString(nextId++);
      task.setId(taskId);
    }
    tasks.put(task.getId(), task);
  }

  @Override
  public List<Task> findTasks(TaskQuery taskQuery) {
    List<Task> result = new ArrayList<>();
    for (Task task: tasks.values()) {
      if (taskQuery.meetsCriteria(task)) {
        result.add(task);
      }
    }
    return new ArrayList<>(tasks.values());
  }

  @Override
  public void deleteTasks(TaskQuery taskQuery) {
    tasks.clear();
  }

  @Override
  public void assignTask(String taskId, UserReference assignee) {
    Task task = tasks.get(taskId);
    if (task!=null) {
      UserReference original = task.getAssignee();
      task.assignee(assignee);
      if (notificationService!=null) {
        String assigneeId = assignee!=null ? assignee.getId() : null;  
        if (assigneeId!=null) {
          if (original==null || !assigneeId.equals(original.getId())) {
            notificationService.notifyTaskAssigned(task, original, assignee);
          }
        }
      }
    }
  }
}
