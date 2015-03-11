package com.effektif.example.cli.command;

import com.effektif.workflow.api.Configuration;
import com.effektif.workflow.api.WorkflowEngine;
import com.effektif.workflow.api.model.Message;
import com.effektif.workflow.api.query.WorkflowInstanceQuery;
import com.effektif.workflow.api.task.Task;
import com.effektif.workflow.api.workflowinstance.ActivityInstance;
import com.effektif.workflow.api.workflowinstance.WorkflowInstance;
import com.effektif.workflow.impl.WorkflowInstanceStore;
import com.effektif.workflow.impl.workflowinstance.WorkflowInstanceImpl;

import java.io.PrintWriter;
import java.util.List;

/**
 * Completes the task with the given ID.
 */
public class CompleteCommand implements CommandImpl {

  @Override
  public void execute(CommandLine command, Configuration configuration, PrintWriter out) {
    final String taskId = command.getArgument();
    final Task task = configuration.getTaskService().completeTask(taskId);
  }
}
