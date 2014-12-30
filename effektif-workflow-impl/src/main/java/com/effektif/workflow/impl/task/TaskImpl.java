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
package com.effektif.workflow.impl.task;

import java.util.List;

import com.effektif.task.Task;
import com.effektif.workflow.impl.plugin.ControllableActivityInstance;
import com.fasterxml.jackson.annotation.JsonIgnore;


/**
 * @author Walter White
 */
public class TaskImpl implements Task {
  
  @JsonIgnore
  protected TaskServiceImpl taskService;

  protected String id;
  protected String organizationId;
  protected String name;
  protected String assigneeId;
  protected List<String> candidateIds;
  protected Object activityInstanceId;
  protected String workflowInstanceId;
  
  public TaskImpl() {
  }

  public TaskImpl(TaskServiceImpl taskService) {
    this.taskService = taskService;
  }

  @Override
  public Task name(String name) {
    this.name = name;
    return this;
  }
  
  @Override
  public Task assigneeId(String assigneeId) {
    this.assigneeId = assigneeId;
    return this;
  }

  @Override
  public Task candidateIds(List<String> candidateIds) {
    this.candidateIds = candidateIds;
    return this;
  }

  @Override
  public Task activityInstance(ControllableActivityInstance activityInstance) {
    this.activityInstanceId = activityInstance.getId();
    return this;
  }

  
  public String getId() {
    return id;
  }

  
  public void setId(String id) {
    this.id = id;
  }

  
  public String getName() {
    return name;
  }

  
  public void setName(String name) {
    this.name = name;
  }

  
  public Object getAssigneeId() {
    return assigneeId;
  }

  
  public void setAssigneeId(String assigneeId) {
    this.assigneeId = assigneeId;
  }

  
  public List<String> getCandidateIds() {
    return candidateIds;
  }

  
  public void setCandidateIds(List<String> candidateIds) {
    this.candidateIds = candidateIds;
  }

  
  public Object getActivityInstanceId() {
    return activityInstanceId;
  }

  
  public void setActivityInstanceId(String activityInstanceId) {
    this.activityInstanceId = activityInstanceId;
  }

  
  public Object getWorkflowInstanceId() {
    return workflowInstanceId;
  }

  
  public void setWorkflowInstanceId(String workflowInstanceId) {
    this.workflowInstanceId = workflowInstanceId;
  }

  
  public String getOrganizationId() {
    return organizationId;
  }

  
  public void setOrganizationId(String organizationId) {
    this.organizationId = organizationId;
  }

  
  public void setActivityInstanceId(Object activityInstanceId) {
    this.activityInstanceId = activityInstanceId;
  }

  @Override
  public void save() {
    taskService.save(this);
  }
}
