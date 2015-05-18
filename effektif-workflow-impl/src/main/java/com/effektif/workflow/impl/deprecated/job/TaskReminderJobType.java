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
package com.effektif.workflow.impl.deprecated.job;

import java.util.List;

import com.effektif.workflow.api.deprecated.model.TaskId;
import com.effektif.workflow.api.deprecated.task.Task;
import com.effektif.workflow.api.deprecated.task.TaskQuery;
import com.effektif.workflow.api.deprecated.task.TaskService;
import com.effektif.workflow.api.json.TypeName;
import com.effektif.workflow.impl.NotificationService;
import com.effektif.workflow.impl.job.AbstractJobType;
import com.effektif.workflow.impl.job.Job;
import com.effektif.workflow.impl.job.JobController;


/**
 * @author Tom Baeyens
 */
@TypeName("taskReminder")
public class TaskReminderJobType extends AbstractJobType {

  @Override
  public void execute(JobController jobController) {
    Job job = jobController.getJob();
    TaskId taskId = job.getTaskId();
    
    TaskService taskService = jobController.getConfiguration().get(TaskService.class);
    Task task = getTask(taskService, taskId);
    
    if (task!=null && !task.isCompleted()) {
      NotificationService notificationService = jobController.getConfiguration().get(NotificationService.class);
      notificationService.taskReminder(task);
    }
  }

  public Task getTask(TaskService taskService, TaskId taskId) {
    List<Task> tasks = taskService.findTasks(new TaskQuery().taskId(taskId));
    if (tasks!=null && !tasks.isEmpty()) {
      return tasks.get(0);
    }
    return null;
  }
}
