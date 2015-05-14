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
package com.effektif.workflow.impl.deprecated.activity.types;

import java.util.List;

import org.joda.time.LocalDateTime;

import com.effektif.workflow.api.deprecated.activities.UserTask;
import com.effektif.workflow.api.deprecated.form.Form;
import com.effektif.workflow.api.deprecated.model.GroupId;
import com.effektif.workflow.api.deprecated.model.TaskId;
import com.effektif.workflow.api.deprecated.model.UserId;
import com.effektif.workflow.api.deprecated.task.Task;
import com.effektif.workflow.api.deprecated.types.UserIdType;
import com.effektif.workflow.api.model.RelativeTime;
import com.effektif.workflow.api.workflow.Binding;
import com.effektif.workflow.impl.WorkflowParser;
import com.effektif.workflow.impl.activity.AbstractActivityType;
import com.effektif.workflow.impl.deprecated.CaseStore;
import com.effektif.workflow.impl.deprecated.FormBindings;
import com.effektif.workflow.impl.deprecated.TaskStore;
import com.effektif.workflow.impl.deprecated.identity.Group;
import com.effektif.workflow.impl.deprecated.identity.IdentityService;
import com.effektif.workflow.impl.deprecated.job.TaskEscalateJobType;
import com.effektif.workflow.impl.deprecated.job.TaskReminderJobType;
import com.effektif.workflow.impl.deprecated.types.UserIdTypeImpl;
import com.effektif.workflow.impl.job.Job;
import com.effektif.workflow.impl.template.Hint;
import com.effektif.workflow.impl.template.TextTemplate;
import com.effektif.workflow.impl.workflow.ActivityImpl;
import com.effektif.workflow.impl.workflow.BindingImpl;
import com.effektif.workflow.impl.workflow.VariableImpl;
import com.effektif.workflow.impl.workflowinstance.ActivityInstanceImpl;
import com.effektif.workflow.impl.workflowinstance.WorkflowInstanceImpl;


/**
 * @author Tom Baeyens
 */
public class UserTaskImpl extends AbstractActivityType<UserTask> {
  
  protected TaskStore taskStore;
  protected CaseStore caseStore;
  protected IdentityService identityService;
  protected TextTemplate taskName;
  protected BindingImpl<UserId> assigneeId;
  protected List<BindingImpl<UserId>> candidateIds;
  protected List<BindingImpl<GroupId>> candidateGroupIds;
  protected VariableImpl roleVariable;
  protected BindingImpl<UserId> escalateTo;
  public FormBindings formBindings;

  public UserTaskImpl() {
    super(UserTask.class);
  }

  @Override
  public void parse(ActivityImpl activityImpl, UserTask userTask, WorkflowParser parser) {
    super.parse(activityImpl, userTask, parser);
    this.taskStore = parser.getConfiguration(TaskStore.class);
    this.caseStore = parser.getConfiguration(CaseStore.class);
    this.identityService = parser.getConfiguration(IdentityService.class);
    this.taskName = parser.parseTextTemplate(userTask.getTaskName(), Hint.TASK_NAME);
    this.assigneeId = parser.parseBinding(userTask.getAssigneeId(), "assigneeId");
    this.candidateIds = parser.parseBindings(userTask.getCandidateIds(), "candidateIds");
    this.candidateGroupIds = parser.parseBindings(userTask.getCandidateGroupIds(), "candidateGroupIds");
    this.escalateTo = parser.parseBinding(userTask.getEscalateToId(), "escalateTo");

    String roleVariableId = 
            assigneeId!=null
            && assigneeId.value==null
            && assigneeId.expression!=null
            && assigneeId.expression.variableId!=null
            && assigneeId.expression.fields==null ? assigneeId.expression.variableId : null;
    this.roleVariable = roleVariableId!=null ? activityImpl.findVariableByIdRecursive(roleVariableId) : null; 
    
    Form form = userTask.getForm();
    if (form!=null) {
      formBindings = new FormBindings();
      parser.pushContext("form", userTask, formBindings, null);
      formBindings.parse(form, parser);
      parser.popContext();
    }
  }

  @Override
  public void execute(ActivityInstanceImpl activityInstance) {
    String resolvedTaskName = resolveTaskName(activityInstance);
    
    UserId assigneeId = activityInstance.getValue(this.assigneeId);
    List<UserId> candidateIds = null;
    List<GroupId> candidateGroupIds = null;

    if (roleVariable!=null) {
      UserIdTypeImpl roleTypeImpl = (UserIdTypeImpl) roleVariable.type;
      UserIdType roleType = roleTypeImpl!=null ? roleTypeImpl.getDataType() : null;
      candidateIds = roleType.getCandidateIds();
      candidateGroupIds = roleType.getCandidateGroupIds();
      UserId roleValue = (UserId) activityInstance.getValue(roleVariable.id);
      if (roleValue!=null) {
        assigneeId = roleValue;
      }
    } else {
      candidateIds = activityInstance.getValues(this.candidateIds);
      candidateGroupIds = activityInstance.getValues(this.candidateGroupIds);
    }

    if (assigneeId==null) {
      assigneeId = findSingleUserId(candidateIds, candidateGroupIds);
      if (assigneeId!=null && roleVariable!=null) {
        // update the variable value
        activityInstance.setVariableValue(roleVariable.id, assigneeId);
      }
    }
    
    WorkflowInstanceImpl workflowInstance = activityInstance.workflowInstance;
    TaskId taskId = taskStore.generateTaskId();
    
    Task task = new Task();
    task.setId(taskId);
    task.setCaseId(workflowInstance.caseId);
    task.setName(resolvedTaskName);
    task.setDescription(activity.getDescription());
    task.setAssigneeId(assigneeId);
    task.setCandidateIds(candidateIds);
    task.setCandidateGroupIds(candidateGroupIds);
    task.setActivityNotify(true);
    task.setActivityId(activity.getId());
    task.setActivityInstanceId(activityInstance.id);
    task.setWorkflowInstanceId(activityInstance.workflowInstance.id);
    task.setWorkflowId(activityInstance.workflow.id);
    task.setSourceWorkflowId(activityInstance.workflow.sourceWorkflowId);
    task.setWorkflowForm(formBindings!=null ? true : null);
    task.setRoleVariableId(roleVariable!=null ? roleVariable.id : null);
    
    TaskId parentTaskId = activityInstance.parent.findTaskIdRecursive();
    if (parentTaskId!=null) {
      Task parentTask = taskStore.addSubtask(parentTaskId, task);
      task.setCaseId(parentTask.getCaseId());
      task.setParentId(parentTaskId);
    } else if (workflowInstance.caseId!=null) {
      caseStore.addTask(workflowInstance.caseId, taskId);
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
  
  protected UserId findSingleUserId(List<UserId> candidateIds, List<GroupId> candidateGroupIds) {
    UserId singleUserId = null;
    if (candidateIds!=null) {
      if (candidateIds.size()==1) {
        singleUserId = candidateIds.get(0); 
      } else if (candidateIds.size()>=1) {
        return null;
      }
    }
    if (candidateGroupIds!=null) {
      List<Group> groups = identityService.findGroupByIds(candidateGroupIds);
      if (groups!=null) {
        for (Group group : groups) {
          List<UserId> memberIds = group.getMemberIds();
          if (memberIds!=null && !memberIds.isEmpty()) {
            if (memberIds.size()==1) {
              if (singleUserId==null) {
                singleUserId = memberIds.get(0); 
              } else {
                return null;
              }
            } else {
              return null;
            }
          }
        }
      }
    }
    return singleUserId;
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
