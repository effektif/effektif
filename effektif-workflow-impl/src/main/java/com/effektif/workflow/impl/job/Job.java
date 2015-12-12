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
package com.effektif.workflow.impl.job;

import java.util.LinkedList;

import org.joda.time.LocalDateTime;

import com.effektif.workflow.api.json.JsonIgnore;
import com.effektif.workflow.api.model.WorkflowId;
import com.effektif.workflow.api.model.WorkflowInstanceId;
import com.effektif.workflow.impl.util.Time;
import com.effektif.workflow.impl.workflowinstance.ActivityInstanceImpl;
import com.effektif.workflow.impl.workflowinstance.LockImpl;


/**
 * @author Tom Baeyens
 */
public class Job {
  
  // private static final Logger log = LoggerFactory.getLogger(Job.class);

  public JobService jobService;

  public JobType jobType;
  
  public String id;
  public String key;
  public LocalDateTime dueDate;
  public LockImpl lock;
  public LinkedList<JobExecution> executions;
  /** retries left.  null when no retries have been performed. 0 when this job has permanently failed. */
  public Long retries;
  public Long retryDelay;
  public LocalDateTime done;
  public Boolean dead;

  public String organizationId;
  public String sourceWorkflowId;
  public WorkflowId workflowId;
  public WorkflowInstanceId workflowInstanceId;
  public String activityInstanceId;

  public boolean isDue() {
    return dueDate ==null || dueDate.compareTo(Time.now())<=0;
  }

  public boolean isDone() {
    return done!=null;
  }

  public boolean isDead() {
    return Boolean.TRUE.equals(dead);
  }
  
  /** setting the id means the job service will ensure there is 
   * exactly 1 such job in the system. */
  public Job key(String key) {
    this.key = key;
    return this;
  }

  /** convenience method to set all activity instance related properties in one go */
  public Job activityInstance(ActivityInstanceImpl activityInstance) {
    activityInstanceId(activityInstance.getId());
    workflowInstanceId(activityInstance.getWorkflowInstance().getId());
    workflowId(activityInstance.getWorkflowInstance().getWorkflow().getId());
    sourceWorkflowId(activityInstance.getWorkflowInstance().getWorkflow().getSourceWorkflowId());
    return this;
  }

  public Job activityInstanceId(String activityInstanceId) {
    this.activityInstanceId = activityInstanceId;
    return this;
  }
  
  public Job done(LocalDateTime done) {
    this.done = done;
    return this;
  }
  
  public Job dueDate(LocalDateTime dueDate) {
    this.dueDate = dueDate;
    return this;
  }
  
  public Job lock(LockImpl lock) {
    this.lock = lock;
    return this;
  }
  
  public Job organizationId(String organizationId) {
    this.organizationId = organizationId;
    return this;
  }
  
  public Job sourceWorkflowId(String sourceWorkflowId) {
    this.sourceWorkflowId = sourceWorkflowId;
    return this;
  }
  
  public Job workflowId(WorkflowId workflowId) {
    this.workflowId = workflowId;
    return this;
  }
  
  public Job workflowInstanceId(WorkflowInstanceId workflowInstanceId) {
    this.workflowInstanceId = workflowInstanceId;
    return this;
  }
  
  public Job jobType(JobType jobType) {
    this.jobType = jobType;
    return this;
  }
  
  public Boolean getDead() {
    return dead;
  }
  
  public void setDead(Boolean dead) {
    this.dead = dead;
  }

  public void rescheduleFromNow(int delayInMillis) {
    rescheduleFor(Time.now().plusMillis((int)delayInMillis));
  }

  public void rescheduleFor(LocalDateTime dueDate) {
    this.dueDate = dueDate;
  }

  
  public JobService getJobService() {
    return jobService;
  }

  
  public JobType getJobType() {
    return jobType;
  }

  
  public String getId() {
    return id;
  }

  
  public String getKey() {
    return key;
  }

  
  public LocalDateTime getDueDate() {
    return dueDate;
  }

  
  public LockImpl getLock() {
    return lock;
  }

  
  public LinkedList<JobExecution> getExecutions() {
    return executions;
  }

  
  public Long getRetries() {
    return retries;
  }

  
  public Long getRetryDelay() {
    return retryDelay;
  }

  
  public LocalDateTime getDone() {
    return done;
  }

  
  public String getOrganizationId() {
    return organizationId;
  }

  
  public String getSourceWorkflowId() {
    return sourceWorkflowId;
  }

  
  public WorkflowId getWorkflowId() {
    return workflowId;
  }

  
  public WorkflowInstanceId getWorkflowInstanceId() {
    return workflowInstanceId;
  }

  
  public String getActivityInstanceId() {
    return activityInstanceId;
  }
}