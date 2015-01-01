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
import com.effektif.workflow.api.workflowinstance.ActivityInstance;
import com.effektif.workflow.impl.definition.ActivityImpl;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;


public interface ActivityType<T> extends Plugin {
  
  /** called when the process is being deployed. 
   * @param activity 
   * @param activity */
  void validate(ActivityImpl activity, T apiActivity, Validator validator);
  
  boolean isAsync(ActivityInstance activityInstance);

  /** called when the activity instance is started */
  void start(ControllableActivityInstance activityInstance);

  /** called when an external signal is invoked on this activity instance through the process engine api */
  void message(ControllableActivityInstance activityInstance);

  /** called when a nested activity instance is ended */
  void ended(ControllableActivityInstance activityInstance, ActivityInstance nestedEndedActivityInstance);
}
