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


/** allows to specify a mapping if the field names are different from 
 * the attribute names in the bpmn namespace.  
 * 
 * @author Tom Baeyens
 */
public interface BpmnFieldMappings {
  
  /** maps the fieldName to the bpmnName in the BPMN namespace */
  void mapToBpmn(String fieldName, String bpmnName);

  /** maps the fieldName to the effektifName in the Effektif namespace */
  void mapToEffektif(String fieldName, String effektifName);

  /** maps the fieldName to the Effektif namespace */
  void mapToEffektif(String fieldName);

  void mapToExtensionElement(String fieldName);

  void mapToExtensionElementCdata(String fieldName);

}
