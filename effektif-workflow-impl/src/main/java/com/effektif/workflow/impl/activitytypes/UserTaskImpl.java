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
import com.effektif.workflow.api.workflow.Activity;
import com.effektif.workflow.impl.plugin.AbstractActivityType;
import com.effektif.workflow.impl.task.Task;
import com.effektif.workflow.impl.task.TaskService;
import com.effektif.workflow.impl.type.BindingType;
import com.effektif.workflow.impl.type.ListType;
import com.effektif.workflow.impl.type.ObjectField;
import com.effektif.workflow.impl.type.ObjectType;
import com.effektif.workflow.impl.type.TextType;
import com.effektif.workflow.impl.type.UserIdType;
import com.effektif.workflow.impl.workflow.ActivityImpl;
import com.effektif.workflow.impl.workflow.BindingImpl;
import com.effektif.workflow.impl.workflow.WorkflowParse;
import com.effektif.workflow.impl.workflowinstance.ActivityInstanceImpl;


public class UserTaskImpl extends AbstractActivityType<UserTask> {
  
  protected TaskService taskService;
  protected BindingImpl<String> nameBinding;
  protected List<BindingImpl<String>> candidateIdBindings;
  
  @Override
  public void parse(ActivityImpl activityImpl, Activity activityApi, WorkflowParse parser) {
    this.taskService = parser.getServiceRegistry().getService(TaskService.class);
    this.nameBinding = parser.parseBinding(activityApi, UserTask.KEY_NAME, String.class, false);
    this.candidateIdBindings = parser.parseBindings(activityApi, UserTask.KEY_CANDIDATE_IDS, String.class);
  }
  
  @Override
  public void execute(ActivityInstanceImpl activityInstance) {
    String taskName = activityInstance.getValue(nameBinding);
    if (taskName==null) {
      taskName = activityInstance.activity.id;
    }
    List<String> taskCandidateIds = activityInstance.getValue(candidateIdBindings);
    String assigneeId = (taskCandidateIds!=null && taskCandidateIds.size()==1 ? taskCandidateIds.get(0) : null);
    
    taskService.saveTask(new Task()
      .name(taskName)
      .assigneeId(assigneeId)
      .candidateIds(taskCandidateIds)
      .activityInstance(activityInstance));
  }
}
