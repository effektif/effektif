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
package com.effektif.workflow.impl.activity;

import java.util.Map;

import com.effektif.workflow.api.model.Message;
import com.effektif.workflow.api.workflow.Activity;
import com.effektif.workflow.impl.WorkflowParser;
import com.effektif.workflow.impl.workflow.ActivityImpl;
import com.effektif.workflow.impl.workflow.InputParameterImpl;
import com.effektif.workflow.impl.workflow.MultiInstanceImpl;
import com.effektif.workflow.impl.workflowinstance.ActivityInstanceImpl;

/**
 * An activity is part of a workflow model that corresponds to a workflow task when executing the workflow.
 *
 * @see <a href="https://github.com/effektif/effektif/wiki/Activity-types">Activity types</a>
 */
public interface ActivityType<T extends Activity> {
  
  /** provides the data structure to the UI how this activity can be configured so that the UI can show a dialog */
  ActivityDescriptor getDescriptor();
  
  Class<?> getActivityApiClass();
  
  T getActivity();
  
  /** called when the process is being validated or deployed.
   * Note that configuration values in the activityApi object could be the target java beans classes, 
   * or the detyped json representation (maps, lists, Strings, etc) if it's coming from json parsing. 
   * 
   * @param activityApi will only contain the activity-specific fields.  The other fields 
   *   may be cleaned if the object gets stored and retrieved from the db storage. */
  void parse(ActivityImpl activityImpl, T activity, WorkflowParser parser);

  boolean isFlushSkippable();

  boolean isAsync(ActivityInstanceImpl activityInstance);

  /** called when the execution flow arrives in this activity */
  void execute(ActivityInstanceImpl activityInstance);

  /** called when an external signal is invoked on this activity instance through the process engine api 
   * @param message TODO*/
  void message(ActivityInstanceImpl activityInstance, Message message);

  boolean saveTransitionsTaken();

  MultiInstanceImpl getMultiInstance();
  
  Map<String,InputParameterImpl> getInputs();
  Map<String,String> getOutputs();
}
