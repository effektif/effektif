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
package com.effektif.workflow.impl.activity.types;

import java.util.List;

import org.joda.time.LocalDateTime;

import com.effektif.workflow.api.activities.UserTask;
import com.effektif.workflow.api.model.GroupId;
import com.effektif.workflow.api.model.RelativeTime;
import com.effektif.workflow.api.model.UserId;
import com.effektif.workflow.api.task.Task;
import com.effektif.workflow.api.types.GroupIdType;
import com.effektif.workflow.api.types.UserIdType;
import com.effektif.workflow.api.workflow.Binding;
import com.effektif.workflow.api.xml.XmlElement;
import com.effektif.workflow.impl.TaskStore;
import com.effektif.workflow.impl.WorkflowParser;
import com.effektif.workflow.impl.activity.AbstractActivityType;
import com.effektif.workflow.impl.bpmn.BpmnReader;
import com.effektif.workflow.impl.bpmn.BpmnWriter;
import com.effektif.workflow.impl.job.Job;
import com.effektif.workflow.impl.job.types.TaskEscalateJobType;
import com.effektif.workflow.impl.job.types.TaskReminderJobType;
import com.effektif.workflow.impl.template.Hint;
import com.effektif.workflow.impl.template.TextTemplate;
import com.effektif.workflow.impl.workflow.ActivityImpl;
import com.effektif.workflow.impl.workflow.BindingImpl;
import com.effektif.workflow.impl.workflowinstance.ActivityInstanceImpl;


/**
 * @author Tom Baeyens
 */
public class UserTaskImpl extends AbstractActivityType<UserTask> {
  
  private static final String BPMN_ELEMENT_NAME = "userTask";

  protected TaskStore taskStore;
  protected TextTemplate taskName;
  protected BindingImpl<UserId> assigneeId;
  protected List<BindingImpl<UserId>> candidateIds;
  protected List<BindingImpl<GroupId>> candidateGroupIds;
  protected BindingImpl<UserId> escalateTo;

  public UserTaskImpl() {
    super(UserTask.class);
  }

  @Override
  public UserTask readBpmn(XmlElement xml, BpmnReader reader) {
    if (!reader.isLocalPart(xml, BPMN_ELEMENT_NAME)) {
      return null;
    }
    UserTask task = new UserTask();

    task.setTaskName(reader.readStringValue(xml, "taskName"));
    task.setAssigneeId(reader.readBinding(UserId.class, UserIdType.INSTANCE, xml, "assignee"));
    task.setCandidateIds(reader.readBindings(UserId.class, UserIdType.INSTANCE, xml, "candidate"));
    task.setCandidateGroupIds(reader.readBindings(GroupId.class, GroupIdType.INSTANCE, xml, "candidate"));
    task.setForm(reader.readForm(xml));
    
    return task;
  }

  @Override
  public void writeBpmn(UserTask task, XmlElement xml, BpmnWriter writer) {
    writer.setBpmnName(xml, BPMN_ELEMENT_NAME);
    writer.writeStringValue(xml, "taskName", task.getTaskName());
    writer.writeBinding(xml, "assignee", task.getAssigneeId(), UserIdType.INSTANCE);
    writer.writeBindings(xml, "candidate", (List) task.getCandidateIds(), UserIdType.INSTANCE);
    writer.writeBindings(xml, "candidate", (List) task.getCandidateGroupIds(), GroupIdType.INSTANCE);
  }
  
  @Override
  public void parse(ActivityImpl activityImpl, UserTask userTaskApi, WorkflowParser parser) {
    super.parse(activityImpl, userTaskApi, parser);
    this.taskStore = parser.getConfiguration(TaskStore.class);
    this.multiInstance = parser.parseMultiInstance(userTaskApi.getMultiInstance());
    this.taskName = parser.parseTextTemplate(userTaskApi.getTaskName(), Hint.TASK_NAME);
    this.assigneeId = parser.parseBinding(userTaskApi.getAssigneeId(), "assigneeId");
    this.candidateIds = parser.parseBindings(userTaskApi.getCandidateIds(), "candidateIds");
    this.candidateGroupIds = parser.parseBindings(userTaskApi.getCandidateGroupIds(), "candidateGroupIds");
    this.escalateTo = parser.parseBinding(userTaskApi.getEscalateToId(), "escalateTo");
  }

  @Override
  public void execute(ActivityInstanceImpl activityInstance) {
    String resolvedTaskName = resolveTaskName(activityInstance);
    UserId assignee = activityInstance.getValue(assigneeId);
    List<UserId> candidates = activityInstance.getValues(candidateIds);
    if ( assignee==null 
         && candidates!=null
         && candidates.size()==1 ) {
      assignee = candidates.get(0);
    }
    
    String taskId = taskStore.generateTaskId();
    activityInstance.setTaskId(taskId);
    
    Task task = new Task();
    task.setId(taskId);
    task.setCaseId(activityInstance.workflowInstance.taskId);
    task.setName(resolvedTaskName);
    task.setDescription(activity.getDescription());
    task.setAssigneeId(assignee);
    task.setCandidateIds(candidates);
    task.setActivityId(activity.getId());
    task.setActivityInstanceId(activityInstance.id);
    task.setWorkflowInstanceId(activityInstance.workflowInstance.id);
    task.setWorkflowId(activityInstance.workflow.id);
    task.setSourceWorkflowId(activityInstance.workflow.sourceWorkflowId);
    
    String parentTaskId = activityInstance.parent.findTaskIdRecursive();
    if (parentTaskId!=null) {
      Task parentTask = taskStore.addSubtask(parentTaskId, task);
      task.setCaseId(parentTask.getCaseId());
      task.setParentId(parentTaskId);
    }
    
    RelativeTime duedate = activity.getDuedate();
    if (duedate!=null) {
      task.setDuedate(duedate.resolve());
    }
    
    taskStore.insertTask(task);

    RelativeTime escalate = activity.getEscalate();
    Binding<UserId> escalateTo = activity.getEscalateToId();
    if (escalate!=null && escalateTo!=null) {
      LocalDateTime escalateTime = escalate.resolve();
      activityInstance.getWorkflowInstance().addJob(new Job()        
        .duedate(escalateTime)
        .jobType(new TaskEscalateJobType())
        .taskId(task.getId())
        .activityInstance(activityInstance));
    }
    
    RelativeTime reminder = activity.getReminder();
    if (reminder!=null) {
      LocalDateTime reminderTime = reminder.resolve();
      activityInstance.getWorkflowInstance().addJob(new Job()        
        .duedate(reminderTime)
        .jobType(new TaskReminderJobType())
        .taskId(task.getId())
        .activityInstance(activityInstance));
    }
  }

  protected String resolveTaskName(ActivityInstanceImpl activityInstance) {
    String resolvedTaskName = this.taskName !=null ? this.taskName.resolve(activityInstance) : null;
    if (resolvedTaskName==null) {
      resolvedTaskName = activityInstance.activity.activity.getName();
    }
    if (resolvedTaskName==null) {
      resolvedTaskName = activityInstance.activity.id;
    }
    return resolvedTaskName;
  }
  
  public BindingImpl<UserId> getEscalateTo() {
    return escalateTo;
  }
}
