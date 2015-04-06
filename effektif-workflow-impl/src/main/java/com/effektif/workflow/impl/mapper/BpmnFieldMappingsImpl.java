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
package com.effektif.workflow.impl.mapper;

import java.util.HashMap;
import java.util.Map;

import com.effektif.workflow.api.mapper.BpmnFieldMappings;
import com.effektif.workflow.impl.bpmn.Bpmn;


/**
 * @author Tom Baeyens
 */
public class BpmnFieldMappingsImpl implements BpmnFieldMappings {
  
  /** maps fieldNames to the correct namespace + local name */
  Map<String,BpmnFieldName> bpmnFieldNames = new HashMap<>();
  Map<String,String> extensionElements = new HashMap<>();
  Map<String,String> extensionCdatas = new HashMap<>();

  @Override
  public void mapToBpmn(String fieldName, String bpmnName) {
    bpmnFieldNames.put(fieldName, new BpmnFieldName(bpmnName, Bpmn.BPMN_URI));
  }

  @Override
  public void mapToEffektif(String fieldName, String effektifName) {
    bpmnFieldNames.put(fieldName, new BpmnFieldName(effektifName, Bpmn.EFFEKTIF_URI));
  }

  @Override
  public void mapToEffektif(String fieldName) {
    mapToEffektif(fieldName, fieldName);
  }

  public String readAttribute(BpmnReader bpmnReader, String fieldName) {
    BpmnFieldName bpmnFieldName = bpmnFieldNames.get(fieldName);
    if (bpmnFieldName==null) {
      return bpmnReader.readBpmnAttribute(fieldName);
    } else if (Bpmn.BPMN_URI==bpmnFieldName.namespaceUri) {
      return bpmnReader.readBpmnAttribute(fieldName);
    } else {
      return bpmnReader.readEffektifAttribute(fieldName);
    }
  }

  public void writeAttribute(BpmnWriter bpmnWriter, String fieldName, String value) {
    BpmnFieldName bpmnFieldName = bpmnFieldNames.get(fieldName);
    if (bpmnFieldName==null) {
      bpmnWriter.writeBpmnAttribute(fieldName, value);
    } else if (Bpmn.BPMN_URI==bpmnFieldName.namespaceUri) {
      bpmnWriter.writeBpmnAttribute(fieldName, value);
    } else {
      bpmnWriter.writeEffektifAttribute(fieldName, value);
    }
  }

  @Override
  public void mapToExtensionElement(String fieldName) {
    extensionElements.put(fieldName, fieldName);
  }

  @Override
  public void mapToExtensionElementCdata(String fieldName) {
    extensionCdatas.put(fieldName, fieldName);
  }
}
