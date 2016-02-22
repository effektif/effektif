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

import com.effektif.workflow.api.model.Id;
import com.effektif.workflow.api.model.RelativeTime;
import com.effektif.workflow.api.types.DataType;
import com.effektif.workflow.api.workflow.Binding;
import org.joda.time.LocalDateTime;

import java.util.List;
import java.util.Map;


/**
 * An abstraction that allows a {@link BpmnWritable} to write its internal data to any BPMN writer.
 * 
 * The goal of this interface is to make it easy to implement BPMN (de)serialization
 * by offering an API that is similar to the JSON abstractions.
 * 
 * @author Tom Baeyens
 */
public interface BpmnWriter {

  void startElementBpmn(String localPart);
  void startElementBpmn(String localPart, Integer index);
  void startElementBpmnDiagram(String localPart);
  void startElementEffektif(String localPart);
  void startElementEffektif(String localPart, Integer index);
  void startElementEffektif(Class modelClass);
  void endElement();
  
  /**
   * Writes the given documentation string as a BPMN <code>documentation</code> element. The BPMN 2.0 specification
   * requires this to be the first child element.
   */
  void writeDocumentation(String documentation);

  /**
   * Starts a BPMN extensionElements tag as a child of the current element. The BPMN 2.0 specification requires that the
   * optional extensionElements must be the first child element, or immediately after the optional documentation element.
   */
  void startExtensionElements();
  void endExtensionElements();
  
  void writeScope();

  /** Writes a binding using the model class’ defined BPMN element name. */
  <T> void writeBinding(Class modelClass, Binding<T> binding);

  /** Writes a binding like
   * e.g. <e:assignee value="42"/> or <e:assignee expression="v1.fullName"/>. */
  <T> void writeBinding(String localPart, Binding<T> binding);

  /** Writes a binding with a key, e.g. <e:input key="a" value="42"/>. */
  <T> void writeBinding(String localPart, Binding<T> binding, String key);

  /** Writes a list of bindings like
   * e.g. <e:assignee value="42"/> or <e:assignee expression="v1.fullName"/>. */
  <T> void writeBindings(String fieldName, List<Binding<T>> bindings);
  
  /** Writes a string field as an attribute on the current xml element in the BPMN namespace */
  void writeStringAttributeBpmn(String localPart, Object value);

  /** Writes a string field as an attribute on the current xml element in the BPMN namespace */
  void writeIdAttributeBpmnDiagram(String localPart, Object value);

  /** Writes a string field as an attribute on the current xml element in the BPMN namespace */
  void writeStringAttributeBpmnDiagram(String localPart, Object value);

  /** Writes a string field as an attribute on the current xml element in the Effektif namespace */
  void writeStringAttributeEffektif(String localPart, Object value);

  /** Writes an id field as an attribute on the current xml element in the BPMN namespace */
  void writeIdAttributeBpmn(String localPart, Id value);

  /** Writes an id field as an attribute on the current xml element in the BPMN namespace */
  void writeIdAttributeBpmn(String localPart, String value);

  /** Writes an id field as an attribute on the current xml element in the Effektif namespace */
  void writeIdAttributeEffektif(String localPart, Id value);

  /** Writes an element in the Effektif namespace with the value as content text, wrapped in a CDATA section. */
  void writeCDataTextEffektif(String localPart, String value);

  /** write a date field as an attribute on the current xml element in the BPMN namespace */
  void writeDateAttributeBpmn(String localPart, LocalDateTime value);

  /** write a date field as an attribute on the current xml element in the Effektif namespace */
  void writeDateAttributeEffektif(String localPart, LocalDateTime value);

  /** Writes a {@link RelativeTime} as an element in the Effektif namespace. */
  void writeRelativeTimeEffektif(String localPart, RelativeTime value);

  /** Writes an integer value as an attribute in the Effektif namespace. */
  void writeIntegerAttributeEffektif(String localPart, Integer value);

  /** Writes a boolean value as an attribute in the Effektif namespace. */
  void writeBooleanAttributeEffektif(String localPart, Boolean value);

  /** Serialises properties with simple Java types as String values. */
  void writeSimpleProperties(Map<String,Object> properties);

  /** Writes an element in the Effektif namespace with the value as a text attribute. */
  void writeStringValue(String localPart, String attributeName, Object value);

  /** Writes text as the body of the current element. */
  void writeText(String value);

  /** Writes an element in the BPMN namespace with the value as content text.
   * If necessary, the value will be wrapped in a CDATA section. */
  void writeTextElementBpmn(String localPart, Object value);

  /** Writes an element in the Effektif namespace with the value as content text. */
  void writeTextElementEffektif(String localPart, Object value);

  /** Writes a <code>type</code> attribute whose value is a data type name. */
  void writeTypeAttribute(Object o);

  /** Writes a element in the Effektif namespace with the given data type as an attribute. */
  void writeTypeElement(DataType type);
}