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
package com.effektif.workflow.api.task;

import java.util.List;

import org.joda.time.LocalDateTime;

import com.effektif.workflow.api.acl.AccessControlList;
import com.effektif.workflow.api.model.TriggerInstance;
import com.effektif.workflow.api.ref.GroupReference;
import com.effektif.workflow.api.ref.UserReference;


/** All the information that can be passed when creating a new task wiht {@link TaskService#createTask(Task)}.
 *  
 * @author Tom Baeyens
 */
public class NewTask {

  // task properties that can be set upon creation
  
  protected String name;
  protected String description;
  protected List<UserReference> participants;
  protected UserReference assignee;
  protected List<UserReference> candidates;
  protected List<GroupReference> candidateGroups;
  protected String parentId;
  protected LocalDateTime duedate;
  protected AccessControlList access;

  // if this the created task should be coupled to a workflow instance, 
  // specify the trigger instance
  protected TriggerInstance triggerInstance;

  public TriggerInstance getTriggerInstance() {
    return this.triggerInstance;
  }
  public void setTriggerInstance(TriggerInstance triggerInstance) {
    this.triggerInstance = triggerInstance;
  }
  public NewTask triggerInstance(TriggerInstance triggerInstance) {
    this.triggerInstance = triggerInstance;
    return this;
  }


  public NewTask name(String name) {
    this.name = name;
    return this;
  }
  
  public NewTask assignee(UserReference assignee) {
    this.assignee = assignee;
    return this;
  }

  public NewTask candidates(List<UserReference> candidates) {
    this.candidates = candidates;
    return this;
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

  /** the parent task.
   * Inverse relation of the {@link #getSubtaskIds()} */
  public String getParentId() {
    return this.parentId;
  }
  /** @see #getParentId() */
  public void setParentId(String parentId) {
    this.parentId = parentId;
  }
  
  public List<GroupReference> getCandidateGroups() {
    return this.candidateGroups;
  }
  public void setCandidateGroups(List<GroupReference> candidateGroups) {
    this.candidateGroups = candidateGroups;
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
  public NewTask duedate(LocalDateTime duedate) {
    this.duedate = duedate;
    return this;
  }

  public List<UserReference> getParticipants() {
    return this.participants;
  }
  public void setParticipants(List<UserReference> participants) {
    this.participants = participants;
  }

  public AccessControlList getAccess() {
    return this.access;
  }
  public void setAccess(AccessControlList access) {
    this.access = access;
  }
}
