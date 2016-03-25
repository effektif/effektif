/* Copyright (c) 2014, Effektif GmbH.
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
package com.effektif.workflow.api.bpmn;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Used to specify the mapping between a BPMN element and the activity type on which the annotation is defined.
 * 
 * When multiple activity classes match the same BPMN element name, an activity can specify a BpmnTypeChildElement
 * to ensure each BPMN activity element is uniquely mapped to a Java class. The attribute value is a BPMN XML element
 * name that is a child element of the BPMN element specified by {@link BpmnElement}.
 * 
 * @author Tom Baeyens
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface BpmnTypeChildElement {

  String value();

  /**
   * Indicates whether the child XML element must be present for the mapping to match the XML
   */
  boolean required() default false;
}
