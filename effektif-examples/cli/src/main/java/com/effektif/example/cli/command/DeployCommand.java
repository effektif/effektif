package com.effektif.example.cli.command;

import com.effektif.example.cli.SoftwareRelease;
import com.effektif.workflow.api.Configuration;
import com.effektif.workflow.api.WorkflowEngine;
import com.effektif.workflow.api.model.Deployment;

import java.io.PrintWriter;

/**
 * Deploys a workflow to the engine.
 */
public class DeployCommand implements CommandImpl {

  @Override
  public void execute(CommandLine command, Configuration configuration, PrintWriter out) {
    final WorkflowEngine engine = configuration.getWorkflowEngine();
    Deployment deployment = engine.deployWorkflow(SoftwareRelease.workflow).checkNoErrorsAndNoWarnings();
    System.out.println("Deployed workflow " + deployment.getWorkflowId());
  }
}
