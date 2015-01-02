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

import com.effektif.workflow.impl.definition.ActivityImpl;
import com.effektif.workflow.impl.definition.WorkflowValidator;
import com.effektif.workflow.impl.instance.ActivityInstanceImpl;


public interface ActivityType<T> extends Plugin {
  
  Class<T> getConfigurationClass();
  
  /** called when the process is being validated or deployed. */
  void validate(ActivityImpl activity, T configuration, WorkflowValidator validator);
  
  boolean isAsync(ActivityInstanceImpl activityInstance);

  /** called when the execution flow arrives in this activity */
  void execute(ActivityInstanceImpl activityInstance);

  /** called when an external signal is invoked on this activity instance through the process engine api */
  void message(ActivityInstanceImpl activityInstance);

  /** called when a nested activity instance is ended */
  void ended(ActivityInstanceImpl activityInstance, ActivityInstanceImpl nestedEndedActivityInstance);
}
