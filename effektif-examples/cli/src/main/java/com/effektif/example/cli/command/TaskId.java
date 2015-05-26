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
package com.effektif.example.cli.command;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.effektif.workflow.api.model.WorkflowInstanceId;

/**
 * Wrapper for this application’s notion of a ‘task ID’, which combines a workflow instance ID, such as
 * <code>876700f9-6eeb-4dbe-99b6-fb1bf4432fed</code>, with an activity instance ID, such as <code>1</code>.
 */
public class TaskId {

  /** Regular expression that matches a workflow instance ID and task ID, in capturing groups. */
  private static final Pattern TASK_ID = Pattern.compile("([0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12})-([0-9])");

  private final String activityInstanceId;
  private final String workflowInstanceId;

  public TaskId(String taskId) {
    final Matcher matcher = TASK_ID.matcher(taskId);
    if (!matcher.matches()) {
      throw new IllegalArgumentException("Invalid task ID: " + taskId);
    }
    workflowInstanceId = matcher.group(1);
    activityInstanceId = matcher.group(2);
  }

  public String getActivityInstanceId() {
    return activityInstanceId;
  }

  public WorkflowInstanceId getWorkflowInstanceId() {
    return new WorkflowInstanceId(workflowInstanceId);
  }
}
