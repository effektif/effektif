package com.effektif.example.cli.command;

import com.effektif.workflow.api.Configuration;
import com.effektif.workflow.api.task.Task;
import com.effektif.workflow.api.task.TaskQuery;
import com.effektif.workflow.api.task.TaskService;
import com.effektif.workflow.impl.json.JsonService;

import java.io.PrintWriter;

/**
 * Outputs details of the task with the given ID, in JSON format.
 */
public class TaskCommand implements CommandImpl {

  @Override
  public void execute(CommandLine command, Configuration configuration, PrintWriter out) {
    final String taskId = command.getArgument();
    final TaskService taskService = configuration.getTaskService();
    final Task task = taskService.findTaskById(taskId);

    final JsonService jsonService = configuration.get(JsonService.class);
    out.println(jsonService.objectToJsonStringPretty(task));
    out.println();
  }
}
