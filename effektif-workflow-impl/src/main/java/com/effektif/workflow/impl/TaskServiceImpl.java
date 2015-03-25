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

import java.util.ArrayList;
import java.util.List;

import com.effektif.workflow.api.acl.Access;
import com.effektif.workflow.api.acl.AccessControlList;
import com.effektif.workflow.api.acl.Authentication;
import com.effektif.workflow.api.acl.Authentications;
import com.effektif.workflow.api.form.FormInstance;
import com.effektif.workflow.api.model.Message;
import com.effektif.workflow.api.model.TaskId;
import com.effektif.workflow.api.model.UserId;
import com.effektif.workflow.api.task.Task;
import com.effektif.workflow.api.task.TaskQuery;
import com.effektif.workflow.api.task.TaskService;
import com.effektif.workflow.impl.activity.types.UserTaskImpl;
import com.effektif.workflow.impl.configuration.Brewable;
import com.effektif.workflow.impl.configuration.Brewery;
import com.effektif.workflow.impl.exceptions.BadRequestException;
import com.effektif.workflow.impl.json.SerializedFormInstance;
import com.effektif.workflow.impl.workflowinstance.ActivityInstanceImpl;
import com.effektif.workflow.impl.workflowinstance.WorkflowInstanceImpl;


/**
 * @author Tom Baeyens
 */
public class TaskServiceImpl implements TaskService, Brewable {
  
  protected TaskStore taskStore;
  protected NotificationService notificationService;
  protected WorkflowEngineImpl workflowEngine;

  @Override
  public void brew(Brewery brewery) {
    this.taskStore = brewery.get(TaskStore.class);
    this.notificationService = brewery.getOpt(NotificationService.class);
    this.workflowEngine = brewery.get(WorkflowEngineImpl.class);
  }

  @Override
  public Task createTask(Task task) {
    if (task==null) {
      task = new Task();
    }
    
    Authentication authentication = Authentications.current();
    String organizationId = authentication!=null ? authentication.getOrganizationId() : null;
    String actorId = authentication!=null ? authentication.getUserId() : null;
    UserId actorUserId = actorId!=null ? new UserId(actorId) : null;

    TaskId taskId = task.getId();
    if (taskId==null) {
      taskId = taskStore.generateTaskId();
    }
    
    task.setId(taskId);
    task.setOrganizationId(organizationId);
    task.setCreatorId(actorUserId);
    
    List<UserId> participants = task.getParticipantIds();
    if (actorUserId!=null) {
      if (participants==null) {
        participants = new ArrayList<>();
      }
      // we want to add the actor if not already present
      // and we want the creator to be the first in the list
      if (participants.contains(actorUserId)) {
        participants.remove(actorUserId);
      }
      participants.add(0, actorUserId);
    }
    task.setParticipantIds(participants);

    if (task.getParentId()!=null) {
      Task parentTask = taskStore.addSubtask(task.getParentId(), task);
      if (parentTask==null) {
        throw new BadRequestException("Parent "+task.getParentId()+" does not exist or you don't have permission");
      }
      // if the new task doesn't specify any particular access control
      // and the parent task has access control specified, 
      if (task.getAccess()==null && parentTask.getAccess()!=null) {
        // then copy the parent access control as the default.
        task.setAccess(parentTask.getAccess());
      }
      task.setParentId(task.getParentId());
      task.setCaseId(parentTask.getCaseId());
    }

    AccessControlList access = task.getAccess();
    // if access is specified 
    // and the current authenticated user does not have access 
    if ( access!=null // if access is specified  
         && ! ( access.hasPermission(authentication, Access.EDIT)
                && access.hasPermission(authentication, Access.VIEW) 
              )
       ) {
      throw new BadRequestException("If you specify access control, the creator must at least have view and edit access");
    }
    task.setAccess(access);
    
    taskStore.insertTask(task);

    if (notificationService!=null) {
      notificationService.taskCreated(task);
    }
    
    return task;
  }

  @Override
  public Task assignTask(TaskId taskId, UserId assigneeId) {
    Task task = taskStore.assignTask(taskId, assigneeId);
    if (notificationService!=null) {
      notificationService.taskAssigned(task);
    }
    if (task.getRoleVariableId()!=null) {
      workflowEngine.setVariableValue(
              task.getWorkflowInstanceId(), 
              task.getActivityInstanceId(),
              task.getRoleVariableId(),
              assigneeId);
    }
    return task;
  }
  
  @Override
  public void saveFormInstance(TaskId taskId, FormInstance formInstance) {
    List<Task> tasks = findTasks(new TaskQuery().taskId(taskId));
    if (tasks.isEmpty()) {
      return;
    }
    Task task = tasks.get(0);
    if (!task.hasWorkflowForm()) {
      return;
    }
    
    String activityInstanceId = task.getActivityInstanceId();
    WorkflowInstanceImpl workflowInstance = workflowEngine.workflowInstanceStore
            .lockWorkflowInstance(task.getWorkflowInstanceId(), activityInstanceId);
    ActivityInstanceImpl activityInstance = workflowInstance.findActivityInstance(activityInstanceId);
    UserTaskImpl userTask = (UserTaskImpl) activityInstance.activity.activityType;
    FormBindings formBindings = userTask.formBindings;

    if (formInstance instanceof SerializedFormInstance) {
      formBindings.deserializeFormInstance(formInstance);
    }
    
    formBindings.applyFormInstanceData(formInstance, activityInstance);
    workflowEngine.workflowInstanceStore.flushAndUnlock(workflowInstance);
  }
  
  @Override
  public Task completeTask(TaskId taskId) {
    Task task = taskStore.completeTask(taskId);
    task.setCompleted(true);
    if (notificationService!=null) {
      notificationService.taskCompleted(task);
    }
    if (Boolean.TRUE.equals(task.getActivityNotify())) {
      task.setActivityNotify(null); // db update was already done. ensuring here that the object model is in sync with the db
      Message message = new Message()
        .workflowInstanceId(task.getWorkflowInstanceId())
        .activityInstanceId(task.getActivityInstanceId());
      // TODO send the task form values
      workflowEngine.send(message);
    }
    return task;
  }
  
  @Override
  public Task findTaskById(TaskId taskId) {
    List<Task> tasks = findTasks(new TaskQuery().taskId(taskId));
    if (tasks.isEmpty()) {
      return null;
    }
    Task task = tasks.get(0);
    if (task.hasWorkflowForm()) {
      WorkflowInstanceImpl workflowInstance = workflowEngine.workflowInstanceStore
              .getWorkflowInstanceImplById(task.getWorkflowInstanceId());
      String activityInstanceId = task.getActivityInstanceId();
      ActivityInstanceImpl activityInstance = workflowInstance.findActivityInstance(activityInstanceId);
      UserTaskImpl userTask = (UserTaskImpl) activityInstance.activity.activityType;
      FormInstance formInstance = userTask.formBindings.createFormInstance(activityInstance);
      task.setFormInstance(formInstance);
    }
    return task;
  }

  @Override
  public List<Task> findTasks(TaskQuery taskQuery) {
    return taskStore.findTasks(taskQuery);
  }

  @Override
  public void deleteTasks(TaskQuery taskQuery) {
    taskStore.deleteTasks(taskQuery);
  }
}
