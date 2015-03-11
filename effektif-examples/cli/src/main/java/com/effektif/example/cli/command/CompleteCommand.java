package com.effektif.example.cli.command;

import java.io.PrintWriter;

import com.effektif.workflow.api.Configuration;
import com.effektif.workflow.api.model.TaskId;
import com.effektif.workflow.api.task.Task;

/**
 * Completes the task with the given ID.
 */
public class CompleteCommand implements CommandImpl {

  @Override
  public void execute(CommandLine command, Configuration configuration, PrintWriter out) {
    final String taskId = command.getArgument();
    final Task task = configuration.getTaskService().completeTask(new TaskId(taskId));
  }
}
