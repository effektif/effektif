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
package com.effektif.workflow.impl;

import java.util.List;

import com.effektif.workflow.api.model.WorkflowInstanceId;
import com.effektif.workflow.api.query.WorkflowInstanceQuery;
import com.effektif.workflow.impl.workflowinstance.WorkflowInstanceImpl;


public interface WorkflowInstanceStore {
  
  WorkflowInstanceId generateWorkflowInstanceId();

  void insertWorkflowInstance(WorkflowInstanceImpl worklflowInstance);

  /** used when rendering a form */
  WorkflowInstanceImpl getWorkflowInstanceImplById(WorkflowInstanceId workflowInstanceId);

  WorkflowInstanceImpl lockWorkflowInstance(WorkflowInstanceId workflowInstanceId);

  WorkflowInstanceImpl lockWorkflowInstanceWithJobsDue();

  void flush(WorkflowInstanceImpl workflowInstance);

  void flushAndUnlock(WorkflowInstanceImpl workflowInstance);

  void unlockWorkflowInstance(WorkflowInstanceImpl workflowInstance);

  List<WorkflowInstanceImpl> findWorkflowInstances(WorkflowInstanceQuery workflowInstanceQuery);

  void deleteWorkflowInstances(WorkflowInstanceQuery workflowInstanceQuery);

  void deleteAllWorkflowInstances();

}
