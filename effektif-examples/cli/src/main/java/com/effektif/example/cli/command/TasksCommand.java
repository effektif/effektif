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

import com.effektif.workflow.api.Configuration;
import com.effektif.workflow.api.WorkflowEngine;
import com.effektif.workflow.api.query.WorkflowInstanceQuery;
import com.effektif.workflow.api.workflowinstance.ActivityInstance;
import com.effektif.workflow.api.workflowinstance.WorkflowInstance;

/**
 * Returns a list of open tasks for the running workflows.
 */
public class TasksCommand implements CommandImpl {

  @Override
  public void execute(CommandLine command, Configuration configuration, PrintWriter out) {
    out.println("Open tasks:");

    WorkflowEngine workflowEngine = configuration.getWorkflowEngine();

    for (WorkflowInstance workflowInstance : workflowEngine.findWorkflowInstances(new WorkflowInstanceQuery())) {
      for (ActivityInstance task : workflowInstance.getActivityInstances()) {
        if (task.isOpen()) {
          out.println(String.format("  %s-%s (%s)", workflowInstance.getId(), task.getId(), task.getActivityId()));
        }
      }
    }
    out.println();
  }
}