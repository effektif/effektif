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
package com.effektif.workflow.api.workflowinstance;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;


@JsonPropertyOrder({"id", "activityId", "start", "end", "duration", "activityInstances", "variableInstances", "timerInstances"})
public class ActivityInstance extends ScopeInstance {
  
  protected String activityId;
  protected String calledWorkflowInstanceId;

  public String getActivityId() {
    return this.activityId;
  }
  public void setActivityId(String activityId) {
    this.activityId = activityId;
  }

  public String getCalledWorkflowInstanceId() {
    return this.calledWorkflowInstanceId;
  }
  public void setCalledWorkflowInstanceId(String calledWorkflowInstanceId) {
    this.calledWorkflowInstanceId = calledWorkflowInstanceId;
  }

  public ActivityInstance findOpenActivityInstance(String activityId) {
    if (activityId!=null && activityId.equals(this.activityId)) {
      return this;
    }
    return super.findOpenActivityInstance(activityId);
  }
}
