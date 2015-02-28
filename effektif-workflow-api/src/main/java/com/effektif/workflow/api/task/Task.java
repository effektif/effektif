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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.joda.time.LocalDateTime;

import com.effektif.workflow.api.acl.AccessControlList;
import com.effektif.workflow.api.acl.AccessControlledObject;
import com.effektif.workflow.api.model.GroupId;
import com.effektif.workflow.api.model.UserId;
import com.effektif.workflow.api.workflow.Extensible;


/**
 * @author Tom Baeyens
 */
public class Task extends Extensible implements AccessControlledObject {
  
  public static final Set<String> INVALID_PROPERTY_KEYS = new HashSet<>(Arrays.asList(
          "id", "organizationId", "name", "description", "access", "createdBy",
          "participants", "assignee", "candidates", "candidateGroups", "caseId",
          "parentId", "subtaskIds", "duedate", "lastUpdated", "canceled", "completed",
          "activityId", "activityInstanceId", "activityNotify", "hasWorkflowForm", 
          "workflowInstanceId", "sourceWorkflowId", "workflowId", "properties"));
  
  protected String id;
  protected String organizationId;
  protected String name;
  protected String description;
  protected AccessControlList access;

  // creator, people that add comments and people assigned to tasks are participants
  protected UserId creatorId;
  protected List<UserId> participantIds;
  protected UserId assigneeId;
  protected List<UserId> candidateIds;
  protected List<GroupId> candidateGroupIds;

  protected String caseId; // id of the root task in the task parent-child relationship
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
  
  /** shallow copy constructor */
  public Task(Task other) {
    this.id = other.id;
    this.organizationId = other.organizationId;
    this.name = other.name;
    this.description = other.description;
    this.access = other.access;
    this.creatorId = other.creatorId;
    this.participantIds= other.participantIds;
    this.assigneeId = other.assigneeId;
    this.candidateIds = other.candidateIds;
    this.candidateGroupIds = other.candidateGroupIds;
    this.caseId = other.caseId;
    this.parentId = other.parentId;
    this.subtaskIds = other.subtaskIds;
    this.duedate = other.duedate;
    this.lastUpdated = other.lastUpdated;
    this.canceled = other.canceled;
    this.completed = other.completed;
    this.activityId = other.activityId;
    this.activityInstanceId = other.activityInstanceId;
    this.activityNotify = other.activityNotify;
    this.hasWorkflowForm = other.hasWorkflowForm;
    this.workflowInstanceId = other.workflowInstanceId;
    this.sourceWorkflowId = other.sourceWorkflowId;
    this.workflowId = other.workflowId;
  }
  
  public boolean isCase() {
    return this.id!=null && this.id.equals(this.caseId);
  }


  public Task name(String name) {
    this.name = name;
    return this;
  }
  
  public Task assigneeId(UserId assigneeId) {
    this.assigneeId = assigneeId;
    return this;
  }

  public Task assigneeId(String assigneeId) {
    this.assigneeId = new UserId(assigneeId);
    return this;
  }

  public Task candidateId(UserId candidateId) {
    if (this.candidateIds==null) {
      this.candidateIds = new ArrayList<>();
    }
    this.candidateIds.add(candidateId);
    return this;
  }

  public Task candidateId(String candidateId) {
    this.candidateId(new UserId(candidateId));
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

  
  public UserId getAssigneeId() {
    return assigneeId;
  }
  
  public void setAssigneeId(UserId assignee) {
    this.assigneeId = assignee;
  }
  
  public List<UserId> getCandidateIds() {
    return candidateIds;
  }

  
  public void setCandidateIds(List<UserId> candidates) {
    this.candidateIds = candidates;
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
  
  public void addSubtaskId(String subtaskId) {
    if (subtaskIds==null) {
      subtaskIds = new ArrayList<>();
    }
    subtaskIds.add(subtaskId);
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

  public List<GroupId> getCandidateGroupIds() {
    return this.candidateGroupIds;
  }
  public void setCandidateGroupIds(List<GroupId> candidateGroupIds) {
    this.candidateGroupIds = candidateGroupIds;
  }
  
  public Boolean isCanceled() {
    return Boolean.TRUE.equals(this.canceled);
  }
  public void setCanceled(Boolean canceled) {
    this.canceled = canceled;
  }
  
  public Boolean isCompleted() {
    return Boolean.TRUE.equals(this.completed);
  }
  public void setCompleted(Boolean completed) {
    this.completed = completed;
  }
  
  public UserId getCreatorId() {
    return this.creatorId;
  }
  public void setCreatorId(UserId creatorId) {
    this.creatorId = creatorId;
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

  public List<UserId> getParticipantIds() {
    return this.participantIds;
  }
  public void setParticipantIds(List<UserId> participantIds) {
    this.participantIds = participantIds;
  }
  
  public String getSourceWorkflowId() {
    return this.sourceWorkflowId;
  }
  public void setSourceWorkflowId(String sourceWorkflowId) {
    this.sourceWorkflowId = sourceWorkflowId;
  }


  @Override
  public Task property(String key, Object value) {
    super.property(key, value);
    return this;
  }

  @Override
  public Task propertyOpt(String key, Object value) {
    super.propertyOpt(key, value);
    return this;
  }

  protected void checkPropertyKey(String key) {
    if (key==null || INVALID_PROPERTY_KEYS.contains(key)) {
      throw new RuntimeException("Invalid property '"+key+"'");
    }
  }

  public AccessControlList getAccess() {
    return this.access;
  }
  public void setAccess(AccessControlList access) {
    this.access = access;
  }
  
}
