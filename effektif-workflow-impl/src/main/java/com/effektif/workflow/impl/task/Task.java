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

import com.effektif.workflow.impl.workflowinstance.ActivityInstanceImpl;


public class Task {
  
  protected String id;
  protected String organizationId;
  protected String name;
  protected String assigneeId;
  protected List<String> candidateIds;
  
  protected String activityInstanceId;
  protected String workflowInstanceId;
  protected String workflowId;
  protected String workflowName;
  
  public Task() {
  }

  public Task name(String name) {
    this.name = name;
    return this;
  }
  
  public Task assigneeId(String assigneeId) {
    this.assigneeId = assigneeId;
    return this;
  }

  public Task candidateIds(List<String> candidateIds) {
    this.candidateIds = candidateIds;
    return this;
  }

  public Task activityInstance(ActivityInstanceImpl activityInstance) {
    this.activityInstanceId = activityInstance.id;
    this.workflowInstanceId = activityInstance.workflowInstance.id;
    this.workflowId = activityInstance.workflow.id;
    this.workflowName = activityInstance.workflow.name;
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

  
  public void setActivityInstanceId(String activityInstanceId) {
    this.activityInstanceId = activityInstanceId;
  }
}
