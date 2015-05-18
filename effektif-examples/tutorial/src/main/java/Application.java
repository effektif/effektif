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
import java.io.IOException;
import java.io.InputStreamReader;

import com.effektif.workflow.api.Configuration;
import com.effektif.workflow.api.WorkflowEngine;
import com.effektif.workflow.api.deprecated.model.TaskId;
import com.effektif.workflow.api.deprecated.task.Task;
import com.effektif.workflow.api.deprecated.task.TaskQuery;
import com.effektif.workflow.api.deprecated.task.TaskService;
import com.effektif.workflow.api.model.Deployment;
import com.effektif.workflow.api.model.TriggerInstance;
import com.effektif.workflow.api.query.WorkflowQuery;
import com.effektif.workflow.api.workflow.Workflow;
import com.effektif.workflow.impl.deprecated.json.JsonMapper;
import com.effektif.workflow.impl.memory.MemoryConfiguration;

/**
 * Simplified version of the command line application in <code>/effektif-examples/cli</code>
 * for the <a href="https://github.com/effektif/effektif/wiki/Tutorial">tutorial</a>.
 */
public class Application {

  private static Configuration configuration = new MemoryConfiguration();
  private static WorkflowEngine engine = configuration.getWorkflowEngine();
//  private static JsonMapper jsonMapper = configuration.get(JsonMapper.class);

  public static void main(String... arguments) {
    Deployment deployment = engine.deployWorkflow(SoftwareRelease.workflow).checkNoErrorsAndNoWarnings();
    System.out.println("Deployed workflow " + deployment.getWorkflowId());

    while (true) {
      System.out.print("> ");
      BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
      try {
        String commandLine = input.readLine().trim();
        switch (commandLine.split("\\s+")[0]) {
//          case "complete":
//            completeTask(commandLine);
//            break;
          case "quit":
            System.exit(0);
          case "start":
            startWorkflow(commandLine);
            break;
//          case "task":
//            showTask(commandLine);
//            break;
//          case "tasks":
//            listOpenTasks();
//            break;
          case "workflows":
            listWorkflows();
            break;
          default:
            System.out.println("unknown command\n");
        }
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

//  private static void completeTask(String commandLine) {
//    String taskId = commandLine.substring("complete".length()).trim();
//    Task task = taskService.completeTask(new TaskId(taskId));
//    System.out.println("Completed task " + task.getId());
//  }
//
//  private static void listOpenTasks() {
//    for (Task task : taskService.findTasks(new TaskQuery().open())) {
//      System.out.println(String.format("%s: %s (%s)",
//        task.getId(), task.getName(), task.getSourceWorkflowId()));
//    }
//  }

  private static void listWorkflows() {
    for (Workflow workflow : engine.findWorkflows(new WorkflowQuery())) {
      System.out.println(workflow.getSourceWorkflowId());
    }
  }

//  private static void showTask(String commandLine) {
//    String taskId = commandLine.substring("task".length()).trim();
//    Task task = taskService.findTaskById(new TaskId(taskId));
//    System.out.println(jsonMapper.writeToStringPretty(task));
//  }

  private static void startWorkflow(String commandLine) {
    String sourceWorkflowId = commandLine.substring("start".length()).trim();
    final TriggerInstance trigger = new TriggerInstance().sourceWorkflowId(sourceWorkflowId);
    engine.start(trigger);
    System.out.println("Started workflow " + sourceWorkflowId);
  }
}
