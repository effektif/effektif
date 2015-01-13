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
package com.effektif.workflow.impl;

import java.util.List;

import com.effektif.workflow.api.command.RequestContext;
import com.effektif.workflow.api.query.WorkflowQuery;
import com.effektif.workflow.api.workflow.Workflow;
import com.effektif.workflow.impl.workflow.WorkflowImpl;


/** stores and retrieves {@link Workflow workflows}.
 * 
 * The workflow store stores and retrieves the API form of the workflow.
 * Compiling the {@link Workflow} into an executable {@link WorkflowImpl} 
 * is done by the {@link WorkflowEngineImpl workflow engine} */
public interface WorkflowStore {

  /** creates a (globally) unique id to be used for a new workflow being deployed */
  String generateWorkflowId();

  void insertWorkflow(Workflow workflowApi, WorkflowImpl workflowImpl, RequestContext requestContext);

  /** loads the api workflow representation from the store 
   * @param requestContext */
  List<Workflow> findWorkflows(WorkflowQuery query, RequestContext requestContext);

  void deleteWorkflows(WorkflowQuery workflowQuery, RequestContext requestContext);

  String findLatestWorkflowIdByName(String workflowName, RequestContext requestContext);

  /** loads the executable workflow */
  Workflow loadWorkflowById(String workflowId, RequestContext requestContext);

}
