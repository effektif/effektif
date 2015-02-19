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
package com.effektif.workflow.api;

import java.util.List;

import com.effektif.workflow.api.model.Deployment;
import com.effektif.workflow.api.model.Message;
import com.effektif.workflow.api.model.RequestContext;
import com.effektif.workflow.api.model.Start;
import com.effektif.workflow.api.query.WorkflowInstanceQuery;
import com.effektif.workflow.api.query.WorkflowQuery;
import com.effektif.workflow.api.workflow.Workflow;
import com.effektif.workflow.api.workflowinstance.WorkflowInstance;


/** Main interface to the workflow engine. 
 * 
 * See <a href="https://github.com/effektif/effektif-oss/wiki/Workflow-engine-types">Workflow engine types</a>
 * for how to obtain a WorkflowEngine object. 
 * 
 * @author Tom Baeyens
 */
public interface WorkflowEngine {

  /** Validates and deploys if there are no errors. */
  Deployment deployWorkflow(Workflow workflow);
  
  List<Workflow> findWorkflows(WorkflowQuery workflowQuery);

  void deleteWorkflows(WorkflowQuery workflowQuery);

  /** starts a new workflow instance with the data specified in the Start object. */
  WorkflowInstance startWorkflowInstance(Start start);

  /** Sends a {@link Message message} to an activity instance, most likely this is invoked 
   * to end the specified activity instance and move workflow execution forward from there. */
  WorkflowInstance sendMessage(Message message);

  List<WorkflowInstance> findWorkflowInstances(WorkflowInstanceQuery query);
  
  void deleteWorkflowInstances(WorkflowInstanceQuery query);

  /** Creates a derived workflow engine and applies the request context to all 
   * methods invoked on the returned workflow engine.  Most commonly used to  
   * set the authenticated user invoking the operations on the returned
   * workflow engine.*/
  WorkflowEngine createWorkflowEngine(RequestContext requestContext);
}
