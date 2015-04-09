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
package com.effektif.workflow.api.mapper;

import java.util.List;

import org.joda.time.LocalDateTime;

import com.effektif.workflow.api.model.Id;
import com.effektif.workflow.api.workflow.Binding;


/** an abstraction that allows {@link BpmnWritable}s to write 
 * their internal data to any BPMN writer.
 * 
 * The goal is to make it easy to implement BPMN (de)serialization 
 * by offering an API that is similar to the JSON abstractions.
 * 
 * @author Tom Baeyens
 */
public interface BpmnWriter {

  void startElementBpmn(String localPart);
  void startElementBpmn(String localPart, Integer index);
  void startElementEffektif(String localPart);
  void startElementEffektif(String localPart, Integer index);
  void endElement();
  
  void startExtensionElements();
  void endExtensionElements();
  
  void writeScope();

  /** Writes a binding like
   * e.g. <e:assignee value="42"/> or <e:assignee expression="v1.fullName"/>. */
  <T> void writeBinding(String localPart, Binding<T> binding);
  
  /** Writes a list of bindings like
   * e.g. <e:assignee value="42"/> or <e:assignee expression="v1.fullName"/>. */
  <T> void writeBindings(String fieldName, List<Binding<T>> bindings);
  
  /** Writes the given documentation string as a BPMN <code>documentation</code> element. */
  void writeDocumentation(String documentation);

  /** write a string field as an attribute on the current xml element in the BPMN namespace */
  void writeStringAttributeBpmn(String localPart, String value);
  
  /** write a string field as an attribute on the current xml element in the Effektif namespace */
  void writeStringAttributeEffektif(String localPart, String value);
  
  /** write an id field as an attribute on the current xml element in the BPMN namespace */
  void writeIdAttributeBpmn(String localPart, Id value);
  
  /** write an id field as an attribute on the current xml element in the Effektif namespace */
  void writeIdAttributeEffektif(String localPart, Id value);

  /** write a date field as an attribute on the current xml element in the BPMN namespace */
  void writeDateAttributeBpmn(String localPart, LocalDateTime value);
  
  /** write a date field as an attribute on the current xml element in the Effektif namespace */
  void writeDateAttributeEffektif(String localPart, LocalDateTime value);

  /** write an element in the BPMN namespace with the value as content text.  if necessary, the value will be wrapped 
   * automatic in a CDATA section */
  void writeTextBpmn(String localPart, String value);

  /** write an element in the Effektif namespace with the value as content text.  if necessary, the value will be wrapped 
   * automatic in a CDATA section */
  void writeTextEffektif(String localPart, String value);
}