package com.effektif.example.cli.command;

import com.effektif.workflow.api.Configuration;
import com.effektif.workflow.api.task.Task;
import com.effektif.workflow.api.task.TaskQuery;
import com.effektif.workflow.api.task.TaskService;

import java.io.PrintWriter;

/**
 * Returns a list of open tasks for the running workflows.
 */
public class TasksCommand implements CommandImpl {

  @Override
  public void execute(CommandLine command, Configuration configuration, PrintWriter out) {

    // TODO Work out why this lists both tasks 1 and 2, after 1 is completed, and not just 2.
    out.println("Open tasks:");
    final TaskService taskService = configuration.getTaskService();
    for (Task task : taskService.findTasks(new TaskQuery())) {
      if (!task.isCompleted()) {
        out.println(String.format("  %s: %s (%s)", task.getId(), task.getName(), task.getSourceWorkflowId()));
      }
    }
    out.println();

//    final WorkflowInstanceStore instanceStore = configuration.get(WorkflowInstanceStore.class);
//    final List<WorkflowInstanceImpl> instances = instanceStore.findWorkflowInstances(new WorkflowInstanceQuery());
//    out.println("Open tasks:");
//    for (WorkflowInstanceImpl instance : instances) {
//      WorkflowInstance workflowInstance = instance.toWorkflowInstance();
//      for (ActivityInstance activity : workflowInstance.getActivityInstances()) {
//        if (!activity.isEnded()) {
//          out.println("  " + activity.getTaskId() + ": " + activity.getActivityId());
//        }
//      }
//    }
//    out.println();
  }
}
