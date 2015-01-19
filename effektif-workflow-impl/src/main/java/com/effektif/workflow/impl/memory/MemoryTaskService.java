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
import java.util.UUID;

import com.effektif.workflow.impl.WorkflowEngineConfiguration;
import com.effektif.workflow.impl.plugin.Initializable;
import com.effektif.workflow.impl.plugin.ServiceRegistry;
import com.effektif.workflow.impl.task.Task;
import com.effektif.workflow.impl.task.TaskQuery;
import com.effektif.workflow.impl.task.TaskService;


public class MemoryTaskService implements TaskService, Initializable<WorkflowEngineConfiguration> {
  
  protected Map<Object, Task> tasks = Collections.synchronizedMap(new LinkedHashMap<Object,Task>());

  public MemoryTaskService() {
  }

  @Override
  public void initialize(ServiceRegistry serviceRegistry, WorkflowEngineConfiguration configuration) {
    this.tasks = Collections.synchronizedMap(new LinkedHashMap<Object,Task>());
  }

  @Override
  public List<Task> findTasks(TaskQuery taskQuery) {
    return new ArrayList<Task>(tasks.values());
  }

  @Override
  public void saveTask(Task task) {
    String taskId = UUID.randomUUID().toString();
    task.setId(taskId);
    tasks.put(taskId, task);
  }

  @Override
  public void deleteTasks(TaskQuery taskQuery) {
    tasks.clear();
  }
}
