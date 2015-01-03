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

import com.effektif.workflow.api.query.WorkflowInstanceQuery;
import com.effektif.workflow.api.workflowinstance.WorkflowInstance;
import com.effektif.workflow.impl.definition.ActivityImpl;
import com.effektif.workflow.impl.definition.VariableImpl;
import com.effektif.workflow.impl.definition.WorkflowImpl;
import com.effektif.workflow.impl.instance.ActivityInstanceImpl;
import com.effektif.workflow.impl.instance.ScopeInstanceImpl;
import com.effektif.workflow.impl.instance.VariableInstanceImpl;
import com.effektif.workflow.impl.instance.WorkflowInstanceImpl;


public interface WorkflowInstanceStore {

  WorkflowInstanceImpl lockWorkflowInstance(String workflowInstanceId, String activityInstanceId);

  void insertWorkflowInstance(WorkflowInstanceImpl worklflowInstance);

  void flush(WorkflowInstanceImpl workflowInstance);

  void flushAndUnlock(WorkflowInstanceImpl workflowInstance);

  List<WorkflowInstance> findWorkflowInstances(WorkflowInstanceQuery workflowInstanceQuery);

  void deleteWorkflowInstances(WorkflowInstanceQuery workflowInstanceQuery);

  /** instantiates and assigns an id.
   * This method can choose to instantiate a subclass, but has to 
   * ensure that the constructor {@link WorkflowInstanceImpl#WorkflowInstanceImpl(WorkflowEngineImpl, WorkflowImpl, String)} 
   * is called for proper initialization.  */
  WorkflowInstanceImpl createWorkflowInstance(WorkflowImpl workflow);

  /** instantiates and assigns an id.
   * This method can choose to instantiate a subclass, but has to 
   * ensure that the constructor {@link ActivityInstanceImpl#ActivityInstanceImpl(ScopeInstanceImpl, ActivityImpl, String)} 
   * is called for proper initialization.  */
  ActivityInstanceImpl createActivityInstance(ScopeInstanceImpl parent, ActivityImpl activityDefinition);

  /** instantiates and assigns an id.
   * This method can choose to instantiate a subclass, but has to 
   * ensure that the constructor {@link VariableInstanceImpl#VariableInstanceImpl(ScopeInstanceImpl, VariableImpl, String)} 
   * is called for proper initialization.  */
  VariableInstanceImpl createVariableInstance(ScopeInstanceImpl parent, VariableImpl variable);
}
