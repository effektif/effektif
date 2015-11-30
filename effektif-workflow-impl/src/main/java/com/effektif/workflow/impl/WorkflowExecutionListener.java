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


import com.effektif.workflow.impl.workflow.TransitionImpl;
import com.effektif.workflow.impl.workflowinstance.ActivityInstanceImpl;
import com.effektif.workflow.impl.workflowinstance.WorkflowInstanceImpl;

/**
 * A listener that notifies workflow execution (instance) related events and can be used to
 * track the progress of individual workflow instances.
 *
 * It can be registered with the {@link com.effektif.workflow.impl.WorkflowEngineImpl#addWorkflowExecutionListener(WorkflowExecutionListener)}
 * method. At the moment the listener is used internally only and not exposed as the public API, therefore typical registration
 * runs by casting the {@link com.effektif.workflow.api.WorkflowEngine} to {@link com.effektif.workflow.impl.WorkflowEngineImpl}.
 *
 * For an example see the unit test suite.
 */
public interface WorkflowExecutionListener {
  
  /**
   * Called whenever a new workflow instance is started.
   * 
   * It will be called after the variables are initialized with the data 
   * passed into the workflow instance, but before the first activity is executed.
   *
   * @param workflowInstance the {@link WorkflowInstanceImpl} that is started.
   */
  void starting(WorkflowInstanceImpl workflowInstance);
  
  /**
   * Called whenever an activity is started on a workflow instance. It will be called after
   * {@link #transitioning(ActivityInstanceImpl, TransitionImpl, ActivityInstanceImpl)} is triggered.
   *
   * @param activityInstance the {@link ActivityInstanceImpl} that is started.
   * @return true if the activityInstance should be started, false otherwise
   */
  boolean starting(ActivityInstanceImpl activityInstance);

  /**
   * Called whenever an activity is ended on a workflow instance. It will be called before
   * {@link #transitioning(ActivityInstanceImpl, TransitionImpl, ActivityInstanceImpl)} is triggered.
   *
   * @param activityInstance the {@link ActivityInstanceImpl} that is ended.
   */
  void ended(ActivityInstanceImpl activityInstance);

  /**
   * Called whenever an workflow instance is ended.
   *
   * @param activityInstance the {@link ActivityInstanceImpl} that is ended.
   */
  void ended(WorkflowInstanceImpl workflowInstance);
  

  /**
   * Called whenever a transition is taken. It will be called after {@link #starting(ActivityInstanceImpl)} and before
   * {@link #ended(ActivityInstanceImpl)}.
   *
   * @param from the {@link ActivityInstanceImpl} that triggered this transition.
   * @param transition the actual transition that is taken.
   * @param to the {@link ActivityInstanceImpl} that will be started next based on the transition.
   * @return true if the activityInstance should be started, false otherwise
   */
  boolean transitioning(ActivityInstanceImpl from, TransitionImpl transition, ActivityInstanceImpl to);
  
  /** called when the workflow instance is being inserted or updated.
   * The updates that are about to be flushed are available in workflowInstance.updates */
  void flush(WorkflowInstanceImpl workflowInstance);

  /** called after the workflow instance has been unlocked. */
  void unlocked(WorkflowInstanceImpl workflowInstance);

  /** called when the workflow instance is being inserted or updated.
   * The updates that are about to be flushed are available in workflowInstance.updates */
  void insert(WorkflowInstanceImpl workflowInstance);
}
