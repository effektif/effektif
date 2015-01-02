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

import com.effektif.workflow.api.query.WorkflowQuery;
import com.effektif.workflow.api.workflow.Workflow;
import com.effektif.workflow.impl.definition.WorkflowImpl;


public interface WorkflowStore {
  
  String createWorkflowId(WorkflowImpl workflow);

  void insertWorkflow(WorkflowImpl workflow);

  List<WorkflowImpl> loadWorkflows(WorkflowQuery workflowQuery);

  List<Workflow> findWorkflows(WorkflowQuery query);

  void deleteWorkflows(WorkflowQuery workflowQuery);

  String findLatestWorkflowIdByName(String workflowName, String organizationId);

  WorkflowImpl findWorkflowImplById(String workflowId, String organizationId);

  WorkflowImpl createWorkflow();
}
