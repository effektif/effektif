package com.effektif.example.cli.command;

import com.effektif.workflow.api.Configuration;
import com.effektif.workflow.api.WorkflowEngine;
import com.effektif.workflow.api.query.WorkflowInstanceQuery;
import com.effektif.workflow.api.query.WorkflowQuery;
import com.effektif.workflow.api.workflow.Workflow;
import com.effektif.workflow.api.workflowinstance.WorkflowInstance;
import com.effektif.workflow.impl.WorkflowInstanceStore;
import com.effektif.workflow.impl.workflowinstance.WorkflowInstanceImpl;

import java.io.PrintWriter;
import java.util.List;

/**
 * Lists workflows.
 */
public class WorkflowsCommand implements CommandImpl {

  @Override
  public void execute(CommandLine command, Configuration configuration, PrintWriter out) {
    out.println("Deployed workflows:");
    final WorkflowEngine engine = configuration.getWorkflowEngine();
    for (Workflow workflow : engine.findWorkflows(new WorkflowQuery())) {
      out.println("  " + workflow.getSourceWorkflowId());
    }
    out.println();

    out.println("Running workflows:");
    final WorkflowInstanceStore instanceStore = configuration.get(WorkflowInstanceStore.class);
    final List<WorkflowInstanceImpl> instances = instanceStore.findWorkflowInstances(new WorkflowInstanceQuery());
    for (WorkflowInstanceImpl instance : instances) {
      out.println("  " + instance.getWorkflow().getSourceWorkflowId());
    }
    out.println();
  }
}
