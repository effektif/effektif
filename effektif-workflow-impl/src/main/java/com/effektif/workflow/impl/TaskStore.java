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

import com.effektif.workflow.api.model.TaskId;
import com.effektif.workflow.api.model.UserId;
import com.effektif.workflow.api.task.Task;
import com.effektif.workflow.api.task.TaskQuery;


/**
 * @author Tom Baeyens
 */
public interface TaskStore {

  TaskId generateTaskId();

  void insertTask(Task task);

  List<Task> findTasks(TaskQuery taskQuery);
  
  /** @return the task with the new assignee if it exists */
  Task assignTask(TaskId taskId, UserId assignee);

  /** adds the subtaskId to the parent and returns the parent task.
   * If authentication is set (see AuthenticationThreadLocal) then 
   * the operation will only be performed if the authenticated user has 
   * EDIT access on the parent task. */
  Task addSubtask(TaskId parentId, Task subtask);

  /** sets the completed to true, removes the activityNotify and 
   * returns the old task so the task service impl knows if the 
   * activity should be notified. */
  Task completeTask(TaskId taskId);

  void deleteTasks(TaskQuery taskQuery);
}
