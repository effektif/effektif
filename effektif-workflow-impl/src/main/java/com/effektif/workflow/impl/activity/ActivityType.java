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

import com.effektif.workflow.api.workflow.Activity;
import com.effektif.workflow.api.xml.XmlElement;
import com.effektif.workflow.impl.WorkflowParser;
import com.effektif.workflow.impl.bpmn.BpmnReader;
import com.effektif.workflow.impl.bpmn.BpmnWriter;
import com.effektif.workflow.impl.workflow.ActivityImpl;
import com.effektif.workflow.impl.workflow.MultiInstanceImpl;
import com.effektif.workflow.impl.workflowinstance.ActivityInstanceImpl;

/**
 * An activity is part of a workflow model that corresponds to a workflow task when executing the workflow.
 *
 * @see <a href="https://github.com/effektif/effektif/wiki/Activity-types">Activity types</a>
 */
public interface ActivityType<T extends Activity> extends Plugin {
  
  /** provides the data structure to the UI how this activity can be configured so that the UI can show a dialog */
  ActivityDescriptor getDescriptor();
  
  Class<?> getActivityApiClass();
  
  T getActivity();
  
  /** first checks if the activityXml element matches this type and if 
   * it matches, it returns the parsed API activity.
   * Returns null if the activityElement doesn't match 
   * @param bpmnReader */
  T readBpmn(XmlElement activityXml, BpmnReader bpmnReader);

  void writeBpmn(T activity, XmlElement activityXml, BpmnWriter bpmnWriter);

  /** called when the process is being validated or deployed.
   * Note that configuration values in the activityApi object could be the target java beans classes, 
   * or the detyped json representation (maps, lists, Strings, etc) if it's coming from json parsing. 
   * 
   * @param activityApi will only contain the activity-specific fields.  The other fields 
   *   may be cleaned if the object gets stored and retrieved from the db storage. */
  void parse(ActivityImpl activityImpl, T activityApi, WorkflowParser parser);

  boolean isFlushSkippable();

  boolean isAsync(ActivityInstanceImpl activityInstance);

  /** called when the execution flow arrives in this activity */
  void execute(ActivityInstanceImpl activityInstance);

  /** called when an external signal is invoked on this activity instance through the process engine api */
  void message(ActivityInstanceImpl activityInstance);

  boolean saveTransitionsTaken();

  MultiInstanceImpl getMultiInstance();

}
