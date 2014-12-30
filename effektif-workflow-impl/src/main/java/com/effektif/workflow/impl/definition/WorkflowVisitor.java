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
package com.effektif.workflow.impl.definition;



/**
 * @author Walter White
 */
public interface WorkflowVisitor {

  /** invoked only for process definitions */
  void startWorkflow(WorkflowImpl processDefinition);

  /** invoked only for process definitions */
  void endWorkflow(WorkflowImpl processDefinition);

  /** invoked only for process definitions and activity definitions */
  void startActivityDefinition(ActivityImpl activityDefinition, int index);

  /** invoked only for process definitions and activity definitions */
  void endActivityDefinition(ActivityImpl activityDefinition, int index);

  /** visit variable definitions */
  void variableDefinition(VariableImpl variableDefinition, int index);

  /** visit transition definitions */
  void transitionDefinition(TransitionImpl transitionDefinition, int index);

}
