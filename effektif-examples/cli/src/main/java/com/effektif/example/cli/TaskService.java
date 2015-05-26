/* Copyright (c) 2015, Effektif GmbH.
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
package com.effektif.example.cli;

import java.util.List;

import com.effektif.workflow.api.Configuration;
import com.effektif.workflow.api.WorkflowEngine;
import com.effektif.workflow.api.model.WorkflowInstanceId;
import com.effektif.workflow.api.query.WorkflowInstanceQuery;
import com.effektif.workflow.api.workflowinstance.ActivityInstance;
import com.effektif.workflow.api.workflowinstance.WorkflowInstance;

/**
 * Repository for workflow tasks.
 */
public class TaskService {

  private Configuration configuration;

  public TaskService(Configuration configuration) {
    this.configuration = configuration;
  }

  /**
   * Returns the activity instance with the given ID, or <code>null</code> if not found.
   */
  public ActivityInstance findById(WorkflowInstanceId workflowInstanceId, String activityInstanceId) {
    if (workflowInstanceId == null || activityInstanceId == null) {
      return null;
    }

    final WorkflowEngine workflowEngine = configuration.getWorkflowEngine();
    final WorkflowInstanceQuery query = new WorkflowInstanceQuery().workflowInstanceId(workflowInstanceId);
    List<WorkflowInstance> workflows = workflowEngine.findWorkflowInstances(query);
    if (workflows.size() == 0) {
      return null;
    }

    for (WorkflowInstance workflowInstance : workflows) {
      for (ActivityInstance task : workflowInstance.getActivityInstances()) {
        if (task.getId().equals(activityInstanceId)) {
          return task;
        }
      }
    }
    return null;
  }
}
