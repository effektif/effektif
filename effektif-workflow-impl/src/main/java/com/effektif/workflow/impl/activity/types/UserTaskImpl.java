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
package com.effektif.workflow.impl.activity.types;

import java.util.List;

import com.effektif.workflow.api.activities.StartEvent;
import com.effektif.workflow.api.activities.UserTask;
import com.effektif.workflow.api.ref.UserReference;
import com.effektif.workflow.api.task.Task;
import com.effektif.workflow.api.task.TaskService;
import com.effektif.workflow.api.types.ListType;
import com.effektif.workflow.api.types.TextType;
import com.effektif.workflow.api.types.UserReferenceType;
import com.effektif.workflow.api.xml.XmlElement;
import com.effektif.workflow.impl.WorkflowParser;
import com.effektif.workflow.impl.activity.AbstractActivityType;
import com.effektif.workflow.impl.activity.InputParameter;
import com.effektif.workflow.impl.bpmn.BpmnWriter;
import com.effektif.workflow.impl.workflow.ActivityImpl;
import com.effektif.workflow.impl.workflow.BindingImpl;
import com.effektif.workflow.impl.workflowinstance.ActivityInstanceImpl;


public class UserTaskImpl extends AbstractActivityType<UserTask> {
  
  public static final InputParameter<String> NAME = new InputParameter<>()
          .key("name")
          .type(new TextType());

  public static final InputParameter<List<UserReference>> ASSIGNEE = new InputParameter<>()
          .key("assignee")
          .type(new UserReferenceType());

  public static final InputParameter<List<UserReference>> CANDIDATES = new InputParameter<>()
          .key("candidates")
          .type(new ListType(new UserReferenceType()));

  protected TaskService taskService;
  protected BindingImpl<String> nameBinding;
  protected BindingImpl<UserReference> assigneeBinding;
  protected BindingImpl<UserReference> candidatesBinding;

  public UserTaskImpl() {
    super(UserTask.class);
  }
  
  @Override
  public void writeBpmn(UserTask userTask, XmlElement userTaskXml, BpmnWriter bpmnWriter) {
    bpmnWriter.setBpmnName(userTaskXml, "startEvent");
    bpmnWriter.writeBpmnAttribute(userTaskXml, "id", userTask.getId());
  }

  @Override
  public void parse(ActivityImpl activityImpl, UserTask userTaskApi, WorkflowParser parser) {
    super.parse(activityImpl, userTaskApi, parser);
    this.taskService = parser.getConfiguration(TaskService.class);
    this.nameBinding = parser.parseBinding(userTaskApi.getNameBinding(), userTaskApi, NAME);
    this.assigneeBinding = parser.parseBinding(userTaskApi.getAssigneeBinding(), userTaskApi, ASSIGNEE);
    this.candidatesBinding = parser.parseBinding(userTaskApi.getCandidatesBinding(), userTaskApi, CANDIDATES);
  }

  @Override
  public void execute(ActivityInstanceImpl activityInstance) {
    String taskName = activityInstance.getValue(nameBinding);
    if (taskName==null) {
      taskName = activityInstance.activity.id;
    }
    UserReference assignee = activityInstance.getValue(assigneeBinding);
    List<UserReference> candidates = activityInstance.getValues(candidatesBinding);
    if ( assignee==null 
         && candidates!=null
         && candidates.size()==1 ) {
      assignee = candidates.get(0);
    }
    
    Task task = new Task();
    task.setName(taskName);
    task.setAssignee(assignee);
    task.setCandidates(candidates);
    task.setActivityInstanceId(activityInstance.id);
    task.setWorkflowInstanceId(activityInstance.workflowInstance.id);
    task.setWorkflowId(activityInstance.workflow.id);
    task.setWorkflowName(activityInstance.workflow.source);
    taskService.saveTask(task);
  }
}
