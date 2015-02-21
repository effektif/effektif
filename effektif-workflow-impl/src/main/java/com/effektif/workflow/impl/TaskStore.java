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
package com.effektif.workflow.impl;

import java.util.List;

import com.effektif.workflow.api.ref.UserReference;
import com.effektif.workflow.api.task.Task;
import com.effektif.workflow.api.task.TaskQuery;


/**
 * @author Tom Baeyens
 */
public interface TaskStore {

  String generateTaskId();

  void insertTask(Task task);

  /** @return the task with the new assignee if it exists */
  Task assignTask(String taskId, UserReference assignee);

  List<Task> findTasks(TaskQuery taskQuery);
  
  void deleteTasks(TaskQuery taskQuery);

  /** adds the subtaskId to the parent and returns the parent task.
   * If authentication is set (see AuthenticationThreadLocal) then 
   * the operation will only be performed if the authenticated user has 
   * EDIT access on the parent task. */
  Task addSubtask(String parentId, String subtaskId);
}
