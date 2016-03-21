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
package com.effektif.workflow.api.workflowinstance;

import com.effektif.workflow.api.activities.SubProcess;
import com.effektif.workflow.api.json.JsonPropertyOrder;
import com.effektif.workflow.api.model.WorkflowId;
import com.effektif.workflow.api.model.WorkflowInstanceId;

import java.util.List;


/**
 * @author Tom Baeyens
 */
@JsonPropertyOrder({"id", "workflowId", "start", "end", "duration", "activityInstances", "variableInstances", "timerInstances"})
public class WorkflowInstance extends ScopeInstance {

  protected WorkflowInstanceId id;
  protected WorkflowId workflowId;
  protected String businessKey;
  protected String creatorId;

  /** When a {@link SubProcess} is used, the workflow instance that called this one. */
  protected WorkflowInstanceId callingWorkflowInstanceId;

  /** When a {@link SubProcess} is used, the call activity in the calling workflow. */
  protected String callingActivityInstanceId;

  protected String caseId;
  public List<TimerInstance> jobs;

  public WorkflowInstanceId getId() {
    return this.id;
  }
  public void setId(WorkflowInstanceId id) {
    this.id = id;
  }

  public WorkflowInstanceId getCallingWorkflowInstanceId() {
    return this.callingWorkflowInstanceId;
  }
  public void setCallingWorkflowInstanceId(WorkflowInstanceId callingWorkflowInstanceId) {
    this.callingWorkflowInstanceId = callingWorkflowInstanceId;
  }
  
  public String getCallingActivityInstanceId() {
    return this.callingActivityInstanceId;
  }
  public void setCallingActivityInstanceId(String callingActivityInstanceId) {
    this.callingActivityInstanceId = callingActivityInstanceId;
  }

  public WorkflowId getWorkflowId() {
    return this.workflowId;
  }
  public void setWorkflowId(WorkflowId workflowId) {
    this.workflowId = workflowId;
  }
  
  public String getCreatorId() {
    return this.creatorId;
  }
  public void setCreatorId(String id) {
    this.creatorId = id;
  }

  public String getCaseId() {
    return this.caseId;
  }
  public void setCaseId(String caseId) {
    this.caseId = caseId;
  }
  public WorkflowInstance caseId(String caseId) {
    this.caseId = caseId;
    return this;
  }

  public String getBusinessKey() {
    return this.businessKey;
  }
  public void setBusinessKey(String businessKey) {
    this.businessKey = businessKey;
  }
  public WorkflowInstance businessKey(String businessKey) {
    this.businessKey = businessKey;
    return this;
  }

  public List<TimerInstance> getJobs() {
    return jobs;
  }

  public void setJobs(List<TimerInstance> jobs) {
    this.jobs = jobs;
  }
}
