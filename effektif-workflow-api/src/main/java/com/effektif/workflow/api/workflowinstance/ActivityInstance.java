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

import com.effektif.workflow.api.mapper.JsonWriter;
import com.effektif.workflow.api.model.TaskId;
import com.effektif.workflow.api.model.WorkflowInstanceId;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


/**
 * @author Tom Baeyens
 */
@JsonPropertyOrder({"id", "activityId", "start", "end", "duration", "activityInstances", "variableInstances", "timerInstances"})
public class ActivityInstance extends ScopeInstance {
  
  protected String id;
  protected String activityId;
  protected WorkflowInstanceId calledWorkflowInstanceId;
  protected TaskId taskId;

  public String getId() {
    return id;
  }
  
  public void setId(String id) {
    this.id = id;
  }

  public String getActivityId() {
    return this.activityId;
  }
  public void setActivityId(String activityId) {
    this.activityId = activityId;
  }

  public WorkflowInstanceId getCalledWorkflowInstanceId() {
    return this.calledWorkflowInstanceId;
  }
  public void setCalledWorkflowInstanceId(WorkflowInstanceId calledWorkflowInstanceId) {
    this.calledWorkflowInstanceId = calledWorkflowInstanceId;
  }

  public ActivityInstance findOpenActivityInstance(String activityId) {
    if (activityId!=null && activityId.equals(this.activityId)) {
      return this;
    }
    return super.findOpenActivityInstance(activityId);
  }

  public TaskId getTaskId() {
    return this.taskId;
  }
  public void setTaskId(TaskId taskId) {
    this.taskId = taskId;
  }
  public ActivityInstance taskId(TaskId taskId) {
    this.taskId = taskId;
    return this;
  }
}
