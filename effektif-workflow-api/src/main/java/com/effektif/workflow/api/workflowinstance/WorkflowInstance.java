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

import com.fasterxml.jackson.annotation.JsonPropertyOrder;


@JsonPropertyOrder({"id", "organizationId", "workflowId", "start", "end", "duration", "activityInstances", "variableInstances", "timerInstances"})
public class WorkflowInstance extends ScopeInstance {

  protected String organizationId;
  protected String workflowId;
  protected String callerWorkflowInstanceId;
  protected String callerActivityInstanceId;

  public String getOrganizationId() {
    return this.organizationId;
  }
  public void setOrganizationId(String organizationId) {
    this.organizationId = organizationId;
  }

  public String getCallerWorkflowInstanceId() {
    return this.callerWorkflowInstanceId;
  }
  public void setCallerWorkflowInstanceId(String callerWorkflowInstanceId) {
    this.callerWorkflowInstanceId = callerWorkflowInstanceId;
  }
  
  public String getCallerActivityInstanceId() {
    return this.callerActivityInstanceId;
  }
  public void setCallerActivityInstanceId(String callerActivityInstanceId) {
    this.callerActivityInstanceId = callerActivityInstanceId;
  }

  public String getWorkflowId() {
    return this.workflowId;
  }
  public void setWorkflowId(String workflowId) {
    this.workflowId = workflowId;
  }
}
