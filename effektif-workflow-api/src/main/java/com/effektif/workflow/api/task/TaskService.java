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
package com.effektif.workflow.api.task;

import java.util.List;

import com.effektif.workflow.api.model.RequestContext;


public interface TaskService {

  void saveTask(Task task);

  List<Task> findTasks(TaskQuery taskQuery);

  void deleteTasks(TaskQuery taskQuery);

  /** Creates a derived task service and applies the request context to all 
   * methods invoked on the returned task service.  Most commonly used to  
   * set the authenticated user invoking the operations on the returned.*/
  TaskService createTaskService(RequestContext requestContext);
}
