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
package com.effektif.workflow.impl.bpmn;

import java.io.StringWriter;
import java.io.Writer;
import java.util.List;
import java.util.Map;

import com.effektif.workflow.api.activities.StartEvent;
import com.effektif.workflow.api.workflow.Activity;
import com.effektif.workflow.api.workflow.Scope;
import com.effektif.workflow.api.workflow.Workflow;
import com.effektif.workflow.impl.bpmn.xml.XmlElement;
import com.effektif.workflow.impl.bpmn.xml.XmlWriter;
import com.fasterxml.jackson.databind.ObjectMapper;


public class BpmnWriter extends Bpmn {
  
  protected Writer writer;
  protected String bpmnPrefix;
  protected String effektifPrefix;
  
  public BpmnWriter(Writer writer) {
    this.writer = writer;
  }

  public static String toBpmnString(Workflow workflow) {
    StringWriter stringWriter = new StringWriter();
    BpmnWriter bpmnWriter = new BpmnWriter(stringWriter);
    XmlElement bpmnDefinitions = bpmnWriter.writeDefinitions(workflow);
    return XmlWriter.toString(bpmnDefinitions);
  }

  protected XmlElement writeDefinitions(Workflow workflow) {
    XmlElement definitionsElement = null;

    Object definitionsValue = workflow.getProperty(KEY_DEFINITIONS);
    
    if (definitionsValue==null) {
      definitionsElement = new XmlElement();
      
    } else if (definitionsValue instanceof XmlElement) {
      definitionsElement = (XmlElement) definitionsValue;
      
    } else if (definitionsValue instanceof Map) {
      // In case the workflow was serialized, the properties are detyped
      // because jackson can't figure out what type a specific property value has
      ObjectMapper objectMapper = new ObjectMapper();
      definitionsElement = objectMapper.convertValue(definitionsValue, XmlElement.class);
    }

    for (String prefix: definitionsElement.namespaces.keySet()) {
      String uri = definitionsElement.namespaces.get(prefix);
      if (BPMN_URI.equals(uri)) {
        bpmnPrefix = prefix;
      } else if (EFFEKTIF_URI.equals(uri)) {
        effektifPrefix = prefix;
      }
    }
    
    if (bpmnPrefix==null) {
      bpmnPrefix = "";
      definitionsElement.addNamespace(bpmnPrefix, BPMN_URI);
    } if (effektifPrefix==null) {
      effektifPrefix = "effektif";
      definitionsElement.addNamespace(effektifPrefix, EFFEKTIF_URI);
    }
    
    definitionsElement.name = getBpmnQName("definitions"); 
    
    XmlElement processElement = writeWorkflow(workflow);
    // let's add the process we write as the first process element inside the definitions
    definitionsElement.addElementFirst(processElement);
    
    return definitionsElement;
  }
  
  protected XmlElement writeWorkflow(Workflow workflow) {
    XmlElement processElement = getXmlElement(workflow.getProperty(KEY_BPMN), "process");
    writeScope(workflow, processElement);
    return processElement;
  }

  protected void writeScope(Scope scope, XmlElement scopeElement) {
    List<Activity> activities = scope.getActivities();
    if (activities!=null) {
      // We loop backwards and then add each activity as the first
      // This way all the parsed activities will be serialized first 
      // before the unknown elements and the parsed elements will 
      // appear in the order as they were parsed. 
      for (int i=activities.size()-1; i>=0; i--) {
        Activity activity = activities.get(i);
        if (activity instanceof StartEvent) {
          scopeElement.addElementFirst(writeStartElement((StartEvent)activity));
        }
      }
    }
  }

  protected XmlElement writeStartElement(StartEvent startEvent) {
    XmlElement startEventElement = getXmlElement(startEvent.getProperty(KEY_BPMN), "startEvent");
    startEventElement.addAttribute(getBpmnQName("id"), startEvent.getId());
    return startEventElement;
  }

  protected XmlElement getXmlElement(Object source) {
    return getXmlElement(source, null);
  }

  protected XmlElement getXmlElement(Object source, String localPart) {
    XmlElement xmlElement = null;
    if (source==null) {
      xmlElement = new XmlElement();
      
    } else if (source instanceof XmlElement) {
      xmlElement = (XmlElement) source;
      
    } else if (source instanceof Map) {
      // In case the workflow was serialized, the properties are detyped
      // because jackson can't figure out what type a specific property value has
      ObjectMapper objectMapper = new ObjectMapper();
      xmlElement = objectMapper.convertValue(source, XmlElement.class);
    }
    
    if (xmlElement!=null) {
      if (localPart!=null) { 
        xmlElement.name = getBpmnQName(localPart);
      }
      return xmlElement;
    }
    throw new RuntimeException("Unknown bpmn source: "+source);
  }

  protected String getBpmnQName(String localPart) {
    return bpmnPrefix==null || "".equals(bpmnPrefix) ? localPart : bpmnPrefix+":"+localPart;
  }
}
