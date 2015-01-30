/* Copyright 2014 Effektif GmbH.
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
 * limitations under the License. */
package com.effektif.workflow.impl.memory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.effektif.workflow.api.TaskService;
import com.effektif.workflow.api.command.RequestContext;
import com.effektif.workflow.api.task.Task;
import com.effektif.workflow.api.task.TaskQuery;
import com.effektif.workflow.impl.ContextualTaskService;


public class MemoryTaskService implements TaskService {
  
  protected int nextId = 1;
  protected Map<Object, Task> tasks = Collections.synchronizedMap(new LinkedHashMap<Object,Task>());

  public MemoryTaskService() {
  }

  @Override
  public void saveTask(Task task) {
    if (task.getId()==null) {
      String taskId = Integer.toString(nextId++);
      task.setId(taskId);
    }
    tasks.put(task.getId(), task);
  }

  @Override
  public List<Task> findTasks(TaskQuery taskQuery) {
    return new ArrayList<>(tasks.values());
  }

  @Override
  public void deleteTasks(TaskQuery taskQuery) {
    tasks.clear();
  }

  @Override
  public TaskService createTaskService(RequestContext requestContext) {
    return new ContextualTaskService(this, requestContext);
  }
}
