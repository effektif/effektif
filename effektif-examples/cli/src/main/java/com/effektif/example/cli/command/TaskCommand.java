package com.effektif.example.cli.command;

import java.io.PrintWriter;

import com.effektif.workflow.api.Configuration;
import com.effektif.workflow.api.model.TaskId;
import com.effektif.workflow.api.task.Task;
import com.effektif.workflow.impl.json.JsonService;

/**
 * Outputs details of the task with the given ID, in JSON format.
 */
public class TaskCommand implements CommandImpl {

  @Override
  public void execute(CommandLine command, Configuration configuration, PrintWriter out) {
    final String taskId = command.getArgument();
    final Task task = configuration.getTaskService().findTaskById(new TaskId(taskId));
    final JsonService jsonService = configuration.get(JsonService.class);
    out.println(jsonService.objectToJsonStringPretty(task));
    out.println();
  }
}
