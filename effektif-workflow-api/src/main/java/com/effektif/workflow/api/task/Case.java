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
import com.effektif.workflow.api.model.UserId;
import com.effektif.workflow.api.workflow.Extensible;


/**
 * @author Tom Baeyens
 */
public class Case extends Extensible implements AccessControlledObject {
  
  public static final Set<String> INVALID_PROPERTY_KEYS = new HashSet<>(Arrays.asList(
          "id", "organizationId", "name", "description", "access", "creatorId",
          "participantRefs", "taskRefs", "duedate", "lastUpdated", "canceled", "closed",
          "workflowInstanceId", "sourceWorkflowId", "workflowId", "properties"));
  
  protected CaseId id;
  protected String organizationId;
  protected String name;
  protected String description;
  protected AccessControlList access;

  // creator, people that add comments and people assigned to tasks are participants
  protected UserId creatorId;
  protected LocalDateTime createTime;
  protected LocalDateTime duedate;
  protected List<UserId> participantIds;

  protected List<String> taskIds;
  protected LocalDateTime lastUpdated;
  protected Boolean canceled;
  protected Boolean closed;

  // fields related to the workflow
  
  protected String workflowInstanceId;
  protected String sourceWorkflowId;
  protected String workflowId;
  
  public Case() {
  }
  
  /** shallow copy constructor */
  public Case(Case other) {
    this.id = other.id;
    this.organizationId = other.organizationId;
    this.name = other.name;
    this.description = other.description;
    this.access = other.access;
    this.creatorId = other.creatorId;
    this.createTime = other.createTime;
    this.participantIds= other.participantIds;
    this.taskIds = other.taskIds;
    this.duedate = other.duedate;
    this.lastUpdated = other.lastUpdated;
    this.canceled = other.canceled;
    this.closed = other.closed;
    this.workflowInstanceId = other.workflowInstanceId;
    this.sourceWorkflowId = other.sourceWorkflowId;
    this.workflowId = other.workflowId;
  }
  
  public Case name(String name) {
    this.name = name;
    return this;
  }
  
  public CaseId getId() {
    return id;
  }
  public void setId(CaseId id) {
    this.id = id;
  }
  public Case id(String id) {
    this.id = new CaseId(id);
    return this;
  }
  
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return this.description;
  }
  public void setDescription(String description) {
    this.description = description;
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
  
  public String getWorkflowId() {
    return workflowId;
  }
  public void setWorkflowId(String workflowId) {
    this.workflowId = workflowId;
  }

  /** id references to the subtasks.
   * Inverse relation of the {@link #getParentId()} */
  public List<String> getTaskIds() {
    return this.taskIds;
  }
  /** @see #getTaskIds() */
  public void setTaskIds(List<String> taskIds) {
    this.taskIds = taskIds;
  }
  public void addTaskId(String taskId) {
    if (taskIds==null) {
      taskIds = new ArrayList<>();
    }
    taskIds.add(taskId);
  }
  
  public Boolean isCanceled() {
    return Boolean.TRUE.equals(this.canceled);
  }
  public void setCanceled(Boolean canceled) {
    this.canceled = canceled;
  }
  
  public Boolean isClosed() {
    return Boolean.TRUE.equals(this.closed);
  }
  public void setClosed(Boolean closed) {
    this.closed = closed;
  }
  
  public UserId getCreatorId() {
    return this.creatorId;
  }
  public void setCreatorId(UserId creatorId) {
    this.creatorId = creatorId;
  }

  public LocalDateTime getCreateTime() {
    return this.createTime;
  }
  public void setCreateTime(LocalDateTime createTime) {
    this.createTime = createTime;
  }

  public LocalDateTime getDuedate() {
    return this.duedate;
  }
  public void setDuedate(LocalDateTime duedate) {
    this.duedate = duedate;
  }
  public Case duedate(LocalDateTime duedate) {
    this.duedate = duedate;
    return this;
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
  public Case property(String key, Object value) {
    super.property(key, value);
    return this;
  }
  @Override
  public Case propertyOpt(String key, Object value) {
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
