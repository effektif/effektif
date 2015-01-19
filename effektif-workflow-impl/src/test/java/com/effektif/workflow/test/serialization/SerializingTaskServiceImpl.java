/* Copyright (c) 2014, Effektif GmbH.
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
package com.effektif.workflow.test.serialization;

import java.util.List;

import com.effektif.workflow.impl.json.JsonService;
import com.effektif.workflow.impl.task.Task;
import com.effektif.workflow.impl.task.TaskQuery;
import com.effektif.workflow.impl.task.TaskService;


public class SerializingTaskServiceImpl implements TaskService {
  
  protected TaskService taskService;
  protected JsonService jsonService;

  public SerializingTaskServiceImpl(TaskService taskService, JsonService jsonService) {
    this.taskService = taskService;
    this.jsonService = jsonService;
  }

  @Override
  public void saveTask(Task task) {
  }

  @Override
  public List<Task> findTasks(TaskQuery taskQuery) {
    return null;
  }

  @Override
  public void deleteTasks(TaskQuery taskQuery) {
  }

}
