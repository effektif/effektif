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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.effektif.workflow.api.Configuration;
import com.effektif.workflow.api.WorkflowEngine;
import com.effektif.workflow.api.model.Deployment;
import com.effektif.workflow.api.model.Message;
import com.effektif.workflow.api.model.TriggerInstance;
import com.effektif.workflow.api.model.WorkflowInstanceId;
import com.effektif.workflow.api.query.WorkflowInstanceQuery;
import com.effektif.workflow.api.query.WorkflowQuery;
import com.effektif.workflow.api.workflow.ExecutableWorkflow;
import com.effektif.workflow.api.workflowinstance.ActivityInstance;
import com.effektif.workflow.api.workflowinstance.WorkflowInstance;
import com.effektif.workflow.impl.json.JsonStreamMapper;
import com.effektif.workflow.impl.memory.MemoryConfiguration;

/**
 * Simplified version of the command line application in <code>/effektif-examples/cli</code>
 * for the <a href="https://github.com/effektif/effektif/wiki/Tutorial">tutorial</a>.
 */
public class Application {

  private static final Configuration configuration;
  private static final WorkflowEngine engine;
  private static final JsonStreamMapper jsonMapper;

  static {
    configuration = new MemoryConfiguration();
    configuration.start();
    engine = configuration.getWorkflowEngine();
    jsonMapper = configuration.get(JsonStreamMapper.class);
    jsonMapper.pretty();
  }

  private static final Pattern TASK_ID = Pattern.compile("([0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12})-([0-9])");

  public static void main(String... arguments) {
    Deployment deployment = engine.deployWorkflow(SoftwareRelease.workflow).checkNoErrorsAndNoWarnings();
    System.out.println("Deployed workflow " + deployment.getWorkflowId());

    while (true) {
      System.out.print("> ");
      BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
      try {
        String commandLine = input.readLine().trim();
        switch (commandLine.split("\\s+")[0]) {
          case "complete":
            completeTask(commandLine);
            break;
          case "quit":
            System.exit(0);
          case "start":
            startWorkflow(commandLine);
            break;
          case "task":
            showTask(commandLine);
            break;
          case "tasks":
            listOpenTasks();
            break;
          case "workflows":
            listWorkflows();
            break;
          default:
            System.out.println("unknown command\n");
        }
      } catch (Exception e) {
        System.out.println("Error executing command: " + e.getMessage());
      }
    }
  }

  private static void completeTask(String commandLine) {
    final Matcher taskIdMatcher = parseTaskId(commandLine);
    String workflowInstanceId = taskIdMatcher.group(1);
    String activityInstanceId = taskIdMatcher.group(2);

    final Message message = new Message()
      .workflowInstanceId(new WorkflowInstanceId(workflowInstanceId))
      .activityInstanceId(activityInstanceId);
    engine.send(message);
    System.out.println(String.format("Completed task %s-%s", workflowInstanceId, activityInstanceId));
  }

  private static void listOpenTasks() {
    for (WorkflowInstance workflowInstance : engine.findWorkflowInstances(new WorkflowInstanceQuery())) {
      for (ActivityInstance task : workflowInstance.getActivityInstances()) {
        if (task.isOpen()) {
          System.out.println(String.format("  %s-%s: %s", workflowInstance.getId(), task.getId(), task.getActivityId()));
        }
      }
    }
  }

  private static void listWorkflows() {
    for (ExecutableWorkflow workflow : engine.findWorkflows(new WorkflowQuery())) {
      System.out.println(workflow.getSourceWorkflowId());
    }
  }

  private static void showTask(String commandLine) {
    final Matcher taskIdMatcher = parseTaskId(commandLine);
    final WorkflowInstance workflowInstance = findWorkflowInstance(taskIdMatcher.group(1));
    final ActivityInstance activityInstance = findActivityInstance(workflowInstance, taskIdMatcher.group(2));
    System.out.println(jsonMapper.write(activityInstance));
  }

  private static void startWorkflow(String commandLine) {
    final String sourceWorkflowId = commandLine.substring("start".length()).trim();
    final TriggerInstance trigger = new TriggerInstance().sourceWorkflowId(sourceWorkflowId);
    engine.start(trigger);
    System.out.println("Started workflow " + sourceWorkflowId);
  }

  private static Matcher parseTaskId(String commandLine) {
    final String taskId = commandLine.substring(commandLine.indexOf(' ')).trim();
    final Matcher matcher = TASK_ID.matcher(taskId);
    if (!matcher.matches()) {
      throw new IllegalArgumentException("Invalid task ID " + taskId);
    }
    return matcher;
  }

  /**
   * Returns the workflow instance with the given ID, or null if not found.
   */
  private static WorkflowInstance findWorkflowInstance(String workflowInstanceId) {
    if (workflowInstanceId == null) {
      return null;
    }
    final WorkflowInstanceQuery query = new WorkflowInstanceQuery().workflowInstanceId(new WorkflowInstanceId(workflowInstanceId));
    List<WorkflowInstance> workflows = engine.findWorkflowInstances(query);
    if (workflows.size() == 0) {
      return null;
    }
    return workflows.get(0);
  }

  /**
   * Returns the activity instance with the given ID, or null if not found in the given workflow instance.
   */
  private static ActivityInstance findActivityInstance(WorkflowInstance workflowInstance, String activityInstanceId) {
    if (workflowInstance == null || activityInstanceId == null) {
      return null;
    }
    for (ActivityInstance task : workflowInstance.getActivityInstances()) {
      if (task.getId().equals(activityInstanceId)) {
        return task;
      }
    }
    return null;
  }
}
