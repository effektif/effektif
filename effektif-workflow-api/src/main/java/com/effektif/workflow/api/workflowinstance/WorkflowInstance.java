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

import com.effektif.workflow.api.mapper.Writer;
import com.effektif.workflow.api.model.UserId;
import com.effektif.workflow.api.model.WorkflowId;
import com.effektif.workflow.api.model.WorkflowInstanceId;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


/**
 * @author Tom Baeyens
 */
@JsonPropertyOrder({"id", "organizationId", "workflowId", "start", "end", "duration", "activityInstances", "variableInstances", "timerInstances"})
public class WorkflowInstance extends ScopeInstance {

  protected WorkflowInstanceId id;
  protected String organizationId;
  protected WorkflowId workflowId;
  protected String businessKey;
  protected UserId creatorId;
  protected WorkflowInstanceId callerWorkflowInstanceId;
  protected String callerActivityInstanceId;
  protected String caseId;

  @Override
  public void writeFields(Writer w) {
    
  }

  public WorkflowInstanceId getId() {
    return this.id;
  }
  public void setId(WorkflowInstanceId id) {
    this.id = id;
  }

  public String getOrganizationId() {
    return this.organizationId;
  }
  public void setOrganizationId(String organizationId) {
    this.organizationId = organizationId;
  }

  public WorkflowInstanceId getCallerWorkflowInstanceId() {
    return this.callerWorkflowInstanceId;
  }
  public void setCallerWorkflowInstanceId(WorkflowInstanceId callerWorkflowInstanceId) {
    this.callerWorkflowInstanceId = callerWorkflowInstanceId;
  }
  
  public String getCallerActivityInstanceId() {
    return this.callerActivityInstanceId;
  }
  public void setCallerActivityInstanceId(String callerActivityInstanceId) {
    this.callerActivityInstanceId = callerActivityInstanceId;
  }

  public WorkflowId getWorkflowId() {
    return this.workflowId;
  }
  public void setWorkflowId(WorkflowId workflowId) {
    this.workflowId = workflowId;
  }
  
  public UserId getCreatorId() {
    return this.creatorId;
  }
  public void setCreatorId(UserId creatorId) {
    this.creatorId = creatorId;
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
}
