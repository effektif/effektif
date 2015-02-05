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

import java.io.Reader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.effektif.workflow.api.activities.StartEvent;
import com.effektif.workflow.api.workflow.Scope;
import com.effektif.workflow.api.workflow.Workflow;
import com.effektif.workflow.impl.bpmn.xml.XmlElement;
import com.effektif.workflow.impl.bpmn.xml.XmlReader;


public class BpmnReader extends Bpmn {
  
  protected XmlElement xmlRoot;
  /** maps uri's to prefixes */
  protected Map<String,String> prefixes = new HashMap<>();
  
  public BpmnReader(XmlElement xmlDocument) {
    this.xmlRoot = xmlDocument;
  }

  public static Workflow readWorkflow(Reader reader) {
    XmlElement xmlRoot = XmlReader.parseXml(reader);
    BpmnReader bpmnParser = new BpmnReader(xmlRoot);
    return bpmnParser.readDefinitions(xmlRoot);
  }

  public Workflow readDefinitions(XmlElement definitionsXml) {
    Workflow workflow = null;
    
    addPrefixes(definitionsXml);
    
    if (definitionsXml.elements!=null) {
      Iterator<XmlElement> iterator = definitionsXml.elements.iterator();
      while (iterator.hasNext()) {
        XmlElement definitionElement = iterator.next();
        if (definitionElement.is(getQName(BPMN_URI, "process")) && workflow == null) {
          iterator.remove();
          workflow = new Workflow();
          readProcess(definitionElement, workflow);
        }
      }
    }
    
    if (workflow!=null) {
      workflow.property(KEY_DEFINITIONS, definitionsXml);
    }
    
    return workflow;
  }

  public Workflow readProcess(XmlElement processElement, Workflow workflow) {
    readScope(processElement, workflow);
    return workflow;
  }

  protected void readScope(XmlElement scopeElement, Scope scope) {
    scope.id(scopeElement.removeAttribute(getQName(BPMN_URI, "id")));
    Iterator<XmlElement> iterator = scopeElement.elements.iterator();
    while (iterator.hasNext()) {
      XmlElement childElement = iterator.next();
      if (childElement.is(getQName(BPMN_URI, "startEvent"))) {
        iterator.remove();
        scope.activity(readStartEvent(childElement));
      }
    }
    scopeElement.name = null;
    scope.property(KEY_BPMN, scopeElement);
  }

  protected StartEvent readStartEvent(XmlElement startElement) {
    StartEvent startEvent = new StartEvent();
    startEvent.id(startElement.removeAttribute(getQName(BPMN_URI, "id")));
    startElement.name = null;
    startEvent.property(KEY_BPMN, startElement);
    return startEvent;
  }

  public void addPrefixes(XmlElement xmlElement) {
    Map<String, String> namespaces = xmlElement.namespaces;
    if (namespaces!=null) {
      for (String prefix: namespaces.keySet()) {
        prefixes.put(namespaces.get(prefix), prefix);
      }
    }
  }

  protected String getQName(String namespaceUri, String localName) {
    String prefix = prefixes.get(namespaceUri);
    return "".equals(prefix) ? localName : prefix+":"+prefix;
  }
}
