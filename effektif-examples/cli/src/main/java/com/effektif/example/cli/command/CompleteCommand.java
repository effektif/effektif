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
import com.effektif.workflow.api.model.Message;
import com.effektif.workflow.api.model.WorkflowInstanceId;
import com.effektif.workflow.api.query.WorkflowInstanceQuery;
import com.effektif.workflow.api.workflowinstance.WorkflowInstance;

/**
 * Completes the task with the given ID.
 */
public class CompleteCommand implements CommandImpl {

  @Override
  public void execute(CommandLine command, Configuration configuration, PrintWriter out) {
    final String workflowInstanceIdString = command.getArgument();
    // TODO also get the second argument
    final String activityInstanceId = null;
    final WorkflowInstanceId workflowInstanceId = new WorkflowInstanceId(workflowInstanceIdString);
    
    WorkflowEngine workflowEngine = configuration.getWorkflowEngine();
    List<WorkflowInstance> workflowInstances = workflowEngine.findWorkflowInstances(new WorkflowInstanceQuery()
      .workflowInstanceId(workflowInstanceId));
    
    WorkflowInstance workflowInstance = workflowInstances.get(0);
    workflowInstance.findOpenActivityInstance(null);
    workflowEngine.send(new Message()
      .workflowInstanceId(workflowInstanceId)
      .activityInstanceId(activityInstanceId));
  }
}
