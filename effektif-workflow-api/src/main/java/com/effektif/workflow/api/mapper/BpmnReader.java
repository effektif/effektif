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


/**
 * @author Tom Baeyens
 */
public interface BpmnReader {

  List<XmlElement> readElementsBpmn(String localPart);
  List<XmlElement> readElementsEffektif(String localPart);
  void startElement(XmlElement xmlElement);
  void endElement();
  
  void startExtensionElements();
  void endExtensionElements();
  
  void readScope();

  /** Reads a binding like
   * e.g. <e:assignee value="42"/> or <e:assignee expression="v1.fullName"/>. */
  <T> Binding<T> readBinding(String elementName, Class<T> type);

  /** Returns a list of bindings like
   * e.g. <e:assignee value="42"/> or <e:assignee expression="v1.fullName"/>. */
  <T> List<Binding<T>> readBindings(String elementName, Class<T> type);
  
  /** Reads the given documentation string as a BPMN <code>documentation</code> element. */
  String readDocumentation();

  /** Reads a string field as an attribute on the current xml element in the BPMN namespace */
  String readStringAttributeBpmn(String localPart);
  
  /** Reads a string field as an attribute on the current xml element in the Effektif namespace */
  String readStringAttributeEffektif(String localPart);
  
  /** Reads an id as an attribute on the current xml element in the BPMN namespace */
  <T extends Id> T readIdAttributeBpmn(String localPart, Class<T> idType);
  
  /** Reads an id as an attribute on the current xml element in the Effektif namespace */
  <T extends Id> T readIdAttributeEffektif(String localPart, Class<T> idType);

  /** Reads a date field as an attribute on the current xml element in the Effektif namespace */
  LocalDateTime readDateAttributeEffektif(String localPart);

  /** Reads a string as content text in the current xml element */
  String readTextEffektif(String localPart);
  
  XmlElement getUnparsedXml();
}