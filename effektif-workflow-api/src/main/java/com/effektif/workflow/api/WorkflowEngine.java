/* Copyright 2014 Effektif GmbH.
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
 * limitations under the License. */
package com.effektif.workflow.api;

import java.util.List;

import com.effektif.workflow.api.command.MessageCommand;
import com.effektif.workflow.api.command.StartCommand;
import com.effektif.workflow.api.query.WorkflowInstanceQuery;
import com.effektif.workflow.api.query.WorkflowQuery;
import com.effektif.workflow.api.validate.DeployResult;
import com.effektif.workflow.api.validate.ParseIssues;
import com.effektif.workflow.api.workflow.Workflow;
import com.effektif.workflow.api.workflowinstance.WorkflowInstance;


/** Main interface to the workflow engine. 
 * 
 * Obtain an in memory workflow engine like this:
 * <pre>
 * WorkflowEngine workflowEngine = new SimpleWorkflowEngine();
 * </pre>
 * 
 * Or get a mongo db workflow engine like this:
 * <pre>
 * MongoWorkflowEngineConfiguration cfg = ...
 * WorkflowEngine workflowEngine = new MongoWorkflowEngine(cfg);
 * </pre>
 */
public interface WorkflowEngine {

  /** Validates and deploys if there are no errors. */
  DeployResult deployWorkflow(Workflow workflow);

  /** Only validates the given workflow and reports any issues. */
  ParseIssues validateWorkflow(Workflow workflow);

  List<Workflow> findWorkflows(WorkflowQuery workflowQuery);
  
  void deleteWorkflows(WorkflowQuery workflowQuery);
  
  /** Use a {@link StartCommand trigger} to start a new process instance for a process definition. */
  WorkflowInstance startWorkflowInstance(StartCommand startCommand);

  /** Use a {@link MessageCommand message} to end a waiting activity instance in a process instance. */
  WorkflowInstance sendMessage(MessageCommand messageCommand);

  List<WorkflowInstance> findWorkflowInstances(WorkflowInstanceQuery query);
  
  void deleteWorkflowInstances(WorkflowInstanceQuery query);
}
