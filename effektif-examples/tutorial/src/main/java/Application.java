import com.effektif.workflow.api.Configuration;
import com.effektif.workflow.api.WorkflowEngine;
import com.effektif.workflow.api.model.Deployment;
import com.effektif.workflow.api.model.TriggerInstance;
import com.effektif.workflow.api.query.WorkflowQuery;
import com.effektif.workflow.api.task.Task;
import com.effektif.workflow.api.task.TaskQuery;
import com.effektif.workflow.api.task.TaskService;
import com.effektif.workflow.api.workflow.Workflow;
import com.effektif.workflow.impl.json.JsonService;
import com.effektif.workflow.impl.memory.MemoryConfiguration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Simplified version of the command line application in <code>/effektif-examples/cli</code>
 * for the <a href="https://github.com/effektif/effektif/wiki/Tutorial">tutorial</a>.
 */
public class Application {

  private static Configuration configuration = new MemoryConfiguration();
  private static WorkflowEngine engine = configuration.getWorkflowEngine();
  private static TaskService taskService = configuration.getTaskService();
  private static JsonService jsonService = configuration.get(JsonService.class);

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
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  private static void completeTask(String commandLine) {
    String taskId = commandLine.substring("complete".length()).trim();
    Task task = taskService.completeTask(taskId);
    System.out.println("Completed task " + task.getId());
  }

  private static void listOpenTasks() {
    for (Task task : taskService.findTasks(new TaskQuery().open())) {
      System.out.println(String.format("%s: %s (%s)",
        task.getId(), task.getName(), task.getSourceWorkflowId()));
    }
  }

  private static void listWorkflows() {
    for (Workflow workflow : engine.findWorkflows(new WorkflowQuery())) {
      System.out.println(workflow.getSourceWorkflowId());
    }
  }

  private static void showTask(String commandLine) {
    String taskId = commandLine.substring("task".length()).trim();
    Task task = taskService.findTaskById(taskId);
    System.out.println(jsonService.objectToJsonStringPretty(task));
  }

  private static void startWorkflow(String commandLine) {
    String sourceWorkflowId = commandLine.substring("start".length()).trim();
    final TriggerInstance trigger = new TriggerInstance().sourceWorkflowId(sourceWorkflowId);
    engine.start(trigger);
    System.out.println("Started workflow " + sourceWorkflowId);
  }
}
