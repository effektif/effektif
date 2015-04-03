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
package com.effektif.workflow.impl.workflow;

import com.effektif.workflow.api.mapper.XmlElement;
import com.effektif.workflow.impl.bpmn.BpmnReader;
import com.effektif.workflow.impl.bpmn.BpmnWriter;

/**
 * Interface for BPMN model elements other than activities, using the same interface methods for reading and writing
 * BPMN as {@link com.effektif.workflow.impl.activity.ActivityType}.
 *
 * @author Peter Hilton
 */
public interface BpmnModel<T> {

  /** first checks if the activityXml element matches this type and if
   * it matches, it returns the parsed API activity.
   * Returns null if the activityElement doesn't match
   * @param bpmnReader */
  T readBpmn(XmlElement activityXml, BpmnReader bpmnReader);

  void writeBpmn(T activity, XmlElement activityXml, BpmnWriter bpmnWriter);
}
