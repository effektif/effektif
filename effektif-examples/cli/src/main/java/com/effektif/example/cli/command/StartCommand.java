package com.effektif.example.cli.command;

import com.effektif.workflow.api.Configuration;
import com.effektif.workflow.api.WorkflowEngine;
import com.effektif.workflow.api.model.TriggerInstance;
import com.effektif.workflow.api.query.WorkflowQuery;
import com.effektif.workflow.api.workflow.Workflow;

import java.io.PrintWriter;

/**
 * Starts the workflow with the given source ID.
 */
public class StartCommand implements CommandImpl {

  @Override
  public void execute(CommandLine command, Configuration configuration, PrintWriter out) {
    final String sourceWorkflowId = command.getArgument();
    final TriggerInstance trigger = new TriggerInstance().sourceWorkflowId(sourceWorkflowId);
    configuration.getWorkflowEngine().start(trigger);
    out.println("Started workflow " + sourceWorkflowId);
  }
}
