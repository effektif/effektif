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
import com.effektif.workflow.api.model.UserId;
import com.effektif.workflow.api.task.Task;
import com.effektif.workflow.api.task.TaskQuery;
import com.effektif.workflow.api.task.TaskService;
import com.effektif.workflow.impl.configuration.Brewable;
import com.effektif.workflow.impl.configuration.Brewery;
import com.effektif.workflow.impl.exceptions.BadRequestException;
import com.effektif.workflow.impl.util.Time;


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

    String taskId = task.getId();
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
    } else {
      task.setCaseId(taskId);
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
  public void assignTask(String taskId, UserId assignee) {
    Task task = taskStore.assignTask(taskId, assignee);
    if (notificationService!=null) {
      notificationService.notifyTaskAssigned(task);
    }
  }
  
  @Override
  public Task findTaskById(String taskId) {
    List<Task> tasks = findTasks(new TaskQuery().taskId(taskId));
    return !tasks.isEmpty() ? tasks.get(0) : null;
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
