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
package com.effektif.workflow.impl.activitytypes;

import java.util.List;

import com.effektif.workflow.api.activities.UserTask;
import com.effektif.workflow.impl.BindingImpl;
import com.effektif.workflow.impl.definition.ActivityImpl;
import com.effektif.workflow.impl.plugin.AbstractActivityType;
import com.effektif.workflow.impl.plugin.ConfigurationClass;
import com.effektif.workflow.impl.plugin.ControllableActivityInstance;
import com.effektif.workflow.impl.plugin.Validator;
import com.effektif.workflow.impl.task.Task;
import com.effektif.workflow.impl.task.TaskService;


@ConfigurationClass(UserTask.class)
public class UserTaskImpl extends AbstractActivityType<UserTask> {
  
  protected TaskService taskService;
  protected BindingImpl<String> name;
  protected List<BindingImpl<String>> candidateIds;
  
  @Override
  public void validate(ActivityImpl activity, UserTask userTask, Validator validator) {
    this.taskService = validator.getServiceRegistry().getService(TaskService.class);
    this.name = validator.compileBinding(userTask.getName(), "name", false);
    this.candidateIds = validator.compileBinding(userTask.getCandidateIds(), "candidateIds", false);
  }

  @Override
  public void start(ControllableActivityInstance activityInstance) {
    String taskName = activityInstance.getValue(name);
    if (taskName==null) {
      taskName = activityInstance.getActivityId();
    }
    List<String> taskCandidateIds = activityInstance.getValue(candidateIds);
    String assigneeId = (taskCandidateIds!=null && taskCandidateIds.size()==1 ? taskCandidateIds.get(0) : null);
    
    taskService.saveTask(new Task()
      .name(taskName)
      .assigneeId(assigneeId)
      .candidateIds(taskCandidateIds)
      .activityInstance(activityInstance));
  }
}
