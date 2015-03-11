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
    out.println("Open tasks:");
    final TaskService taskService = configuration.getTaskService();
    for (Task task : taskService.findTasks(new TaskQuery())) {
      if (!task.isCompleted()) {
        out.println(String.format("  %s: %s (%s)", task.getId(), task.getName(), task.getSourceWorkflowId()));
      }
    }
    out.println();
  }
}
