/*
 * Copyright 2014 Effektif GmbH.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.effektif.example.cli.command;

import java.io.PrintWriter;
import java.util.List;

import com.effektif.workflow.api.Configuration;
import com.effektif.workflow.api.WorkflowEngine;
import com.effektif.workflow.api.query.WorkflowInstanceQuery;
import com.effektif.workflow.api.query.WorkflowQuery;
import com.effektif.workflow.api.workflow.Workflow;
import com.effektif.workflow.impl.WorkflowInstanceStore;
import com.effektif.workflow.impl.workflowinstance.WorkflowInstanceImpl;

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
