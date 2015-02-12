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
package com.effektif.workflow.api.task;

import java.util.List;

import com.effektif.workflow.api.form.FormInstance;
import com.effektif.workflow.api.ref.UserReference;


public class Task {
  
  protected String id;
  protected String organizationId;
  protected String name;
  protected UserReference assignee;
  protected List<UserReference> candidates;
  protected FormInstance formInstance;

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
  
  public Task assignee(UserReference assignee) {
    this.assignee = assignee;
    return this;
  }

  public Task candidates(List<UserReference> candidates) {
    this.candidates = candidates;
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

  
  public UserReference getAssignee() {
    return assignee;
  }

  
  public void setAssignee(UserReference assignee) {
    this.assignee = assignee;
  }

  
  
  public List<UserReference> getCandidates() {
    return candidates;
  }

  
  public void setCandidates(List<UserReference> candidates) {
    this.candidates = candidates;
  }

  public FormInstance getFormInstance() {
    return this.formInstance;
  }

  public void setFormInstance(FormInstance formInstance) {
    this.formInstance = formInstance;
  }
  
  public Task formInstance(FormInstance formInstance) {
    this.formInstance = formInstance;
    return this;
  }

  
  public String getActivityInstanceId() {
    return activityInstanceId;
  }

  
  public String getWorkflowInstanceId() {
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

  
  public String getWorkflowId() {
    return workflowId;
  }

  
  public void setWorkflowId(String workflowId) {
    this.workflowId = workflowId;
  }

  
  public String getWorkflowName() {
    return workflowName;
  }

  
  public void setWorkflowName(String workflowName) {
    this.workflowName = workflowName;
  }
}
