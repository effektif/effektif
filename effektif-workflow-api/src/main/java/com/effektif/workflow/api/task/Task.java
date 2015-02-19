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

import org.joda.time.LocalDateTime;

import com.effektif.workflow.api.acl.AccessControlList;
import com.effektif.workflow.api.ref.GroupReference;
import com.effektif.workflow.api.ref.UserReference;


/**
 * @author Tom Baeyens
 */
public class Task {
  
  protected String id;
  protected String organizationId;
  protected String name;
  protected String description;
  
  protected AccessControlList access;

  public AccessControlList getAccess() {
    return this.access;
  }
  public void setAccess(AccessControlList access) {
    this.access = access;
  }
  
  // creator, people that add comments and people assigned to tasks are participants
  protected UserReference createdBy;
  protected List<UserReference> participants;
  protected UserReference assignee;
  protected List<UserReference> candidates;
  protected List<GroupReference> candidateGroups;

  protected String caseId;
  protected String parentId;
  protected List<String> subtaskIds;
  protected LocalDateTime duedate;
  protected LocalDateTime lastUpdated;
  // cancelled==true ==> completed==true 
  protected Boolean canceled;
  protected Boolean completed;

  // fields related to the workflow
  
  protected String activityId;
  protected String activityInstanceId;
  // used to be endsActivityInstance (REST), eai (DB)
  // case ==> eai==null (absent)
  // task created by UserTask 
  //    initial value: eai == true 
  //    when completed: co==true & eai == null (absent)
  protected Boolean activityNotify;
  protected Boolean hasWorkflowForm;
  protected String workflowInstanceId;
  protected String sourceWorkflowId;
  protected String workflowId;

  public Task() {
  }
  
  public boolean isCase() {
    return this.id!=null && this.id.equals(this.caseId);
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

  /** the parent task.
   * Inverse relation of the {@link #getSubtaskIds()} */
  public String getParentId() {
    return this.parentId;
  }
  /** @see #getParentId() */
  public void setParentId(String parentId) {
    this.parentId = parentId;
  }
  
  /** id references to the subtasks.
   * Inverse relation of the {@link #getParentId()} */
  public List<String> getSubtaskIds() {
    return this.subtaskIds;
  }
  /** @see #getSubtaskIds() */
  public void setSubtaskIds(List<String> subtaskIds) {
    this.subtaskIds = subtaskIds;
  }
  
  /** id reference to the root task in the task 
   * {@link #getParentId() parent} - {@link #getSubtaskIds() child} relation */
  public String getCaseId() {
    return this.caseId;
  }
  /** @see #getCaseId() */
  public void setCaseId(String caseId) {
    this.caseId = caseId;
  }

  public String getActivityId() {
    return this.activityId;
  }
  public void setActivityId(String activityId) {
    this.activityId = activityId;
  }

  public List<GroupReference> getCandidateGroups() {
    return this.candidateGroups;
  }
  public void setCandidateGroups(List<GroupReference> candidateGroups) {
    this.candidateGroups = candidateGroups;
  }
  
  public Boolean getCanceled() {
    return this.canceled;
  }
  public void setCanceled(Boolean canceled) {
    this.canceled = canceled;
  }
  
  public Boolean isCompleted() {
    return this.completed;
  }
  public void setCompleted(Boolean completed) {
    this.completed = completed;
  }
  
  public UserReference getCreatedBy() {
    return this.createdBy;
  }
  public void setCreatedBy(UserReference createdBy) {
    this.createdBy = createdBy;
  }

  public String getDescription() {
    return this.description;
  }
  public void setDescription(String description) {
    this.description = description;
  }

  public LocalDateTime getDuedate() {
    return this.duedate;
  }
  public void setDuedate(LocalDateTime duedate) {
    this.duedate = duedate;
  }
  public Task duedate(LocalDateTime duedate) {
    this.duedate = duedate;
    return this;
  }

  public Boolean getActivityNotify() {
    return this.activityNotify;
  }
  public void setActivityNotify(Boolean activityNotify) {
    this.activityNotify = activityNotify;
  }
  
  public Boolean hasWorkflowForm() {
    return this.hasWorkflowForm;
  }
  public void setWorkflowForm(Boolean hasWorkflowForm) {
    this.hasWorkflowForm = hasWorkflowForm;
  }
  
  public LocalDateTime getLastUpdated() {
    return this.lastUpdated;
  }
  public void setLastUpdated(LocalDateTime lastUpdated) {
    this.lastUpdated = lastUpdated;
  }

  public List<UserReference> getParticipants() {
    return this.participants;
  }
  public void setParticipants(List<UserReference> participants) {
    this.participants = participants;
  }
  
  public String getSourceWorkflowId() {
    return this.sourceWorkflowId;
  }
  public void setSourceWorkflowId(String sourceWorkflowId) {
    this.sourceWorkflowId = sourceWorkflowId;
  }
  
}
