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

import com.effektif.workflow.api.workflow.Activity;
import com.effektif.workflow.impl.tooling.ConfigurationPanel;
import com.effektif.workflow.impl.workflow.ActivityImpl;
import com.effektif.workflow.impl.workflow.WorkflowParse;
import com.effektif.workflow.impl.workflowinstance.ActivityInstanceImpl;


public interface ActivityType extends Plugin {
  
  /** provides the data structure to the UI how this activity can be configured so that the UI can show a dialog */
  ConfigurationPanel getConfigurationPanel();
  
  Class<?> getApiClass();

  /** called when the process is being validated or deployed.
   * Note that configuration values in the activityApi object could be the target java beans classes, 
   * or the detyped json representation (maps, lists, Strings, etc) if it's coming from json parsing. */
  void parse(ActivityImpl activityImpl, Activity activityApi, WorkflowParse validator);
  
  boolean isAsync(ActivityInstanceImpl activityInstance);

  /** called when the execution flow arrives in this activity */
  void execute(ActivityInstanceImpl activityInstance);

  /** called when an external signal is invoked on this activity instance through the process engine api */
  void message(ActivityInstanceImpl activityInstance);

  /** called when a nested activity instance is ended */
  void ended(ActivityInstanceImpl activityInstance, ActivityInstanceImpl nestedEndedActivityInstance);
}
