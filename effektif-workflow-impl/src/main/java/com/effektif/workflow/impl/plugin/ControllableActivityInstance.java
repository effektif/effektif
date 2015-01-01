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
package com.effektif.workflow.impl.plugin;

import java.util.List;

import com.effektif.workflow.api.workflow.Transition;
import com.effektif.workflow.api.workflowinstance.ActivityInstance;
import com.effektif.workflow.impl.BindingImpl;
import com.effektif.workflow.impl.definition.ActivityImpl;


public interface ControllableActivityInstance {
  
  String getId();
  String getActivityId();
  String getWorkflowInstanceId();
  String getWorkflowId();

  Object getVariable(String variableId);
  void setVariableValue(String variableId, Object value);

  <T> T getValue(BindingImpl<T> binding);
  <T> List<T> getValue(List<BindingImpl<T>> bindings);
  
  Object getTransientContextObject(String key);

  /** ends this activity instance, takes outgoing transitions if there are any and if not, notifies the parent this execution path has ended. */
  void onwards();

  /** ends this activity instance and notifies the parent that this execution path has ended. */
  void end();

  /** ends this activity instance and optionally notifies the parent that this execution path has ended. */
  void end(boolean notifyParent);

  /** executes a nested activity instance for the given activity definition */
  void execute(ActivityImpl activity);

  void takeTransition(Transition transition);
  
  ServiceRegistry getServiceRegistry();
  
  void setJoining();
  boolean isJoining(ActivityInstance siblingActivityInstance);
  void removeJoining(ActivityInstance otherJoiningActivityInstance);
}
