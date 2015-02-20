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
package com.effektif.workflow.impl.bpmn;

import java.io.Reader;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.effektif.workflow.api.workflow.Activity;
import com.effektif.workflow.api.workflow.Scope;
import com.effektif.workflow.api.workflow.Transition;
import com.effektif.workflow.api.workflow.Workflow;
import com.effektif.workflow.api.xml.XmlElement;
import com.effektif.workflow.impl.activity.ActivityType;
import com.effektif.workflow.impl.activity.ActivityTypeService;
import com.effektif.workflow.impl.bpmn.xml.XmlReader;


/** Reads an BPMN XML document and parses it to a Workflow API model.
 * Not threadsafe, use one BpmnReader object per parse.
 * 
 * First the XML is parsed into a XmlElement, which represents the full 
 * DOM structure of the document.
 * 
 * Then the XmlElement structure is parsed.  All information that is 
 * parsed is removed from the XmlElement DOM structure.
 * 
 * The remaining XmlElement structure is the portion in the XML that 
 * was not parsed and not understood by us.  That portion is stored 
 * as part of the Workflow objects.
 * 
 * When writing these workflows back to XML, the unknown parts are 
 * merged back into the XML.
 **/
public class BpmnReader extends Bpmn {
  
  protected XmlElement xmlRoot;
  
  protected ActivityTypeService activityTypeService;
  
  /** maps uri's to prefixes. 
   * Ideally this should be done in a stack so that each element can add new namespaces.
   * The addPrefixes() should then be refactored to pushPrefixes and popPrefixes.
   * The current implementation assumes that all namespaces are defined in the root element */
  protected Map<String,String> prefixes = new HashMap<>();
  
  public BpmnReader(ActivityTypeService activityTypeService) {
    this.activityTypeService = activityTypeService;
  }

  public Workflow readBpmnDocument(Reader reader) {
    this.xmlRoot = XmlReader.parseXml(reader);
    return readDefinitions(xmlRoot);
  }

  protected Workflow readDefinitions(XmlElement definitionsXml) {
    Workflow workflow = null;
    
    // see #prefixes for more details about the limitations of namespaces
    addPrefixes(definitionsXml);
    
    if (definitionsXml.elements!=null) {
      Iterator<XmlElement> iterator = definitionsXml.elements.iterator();
      while (iterator.hasNext()) {
        XmlElement definitionElement = iterator.next();
        if (definitionElement.is(getQName(BPMN_URI, "process")) && workflow == null) {
          iterator.remove();
          workflow = readProcess(definitionElement);
        }
      }
    }
    
    if (workflow!=null) {
      workflow.property(KEY_DEFINITIONS, definitionsXml);
    }
    
    return workflow;
  }

  protected Workflow readProcess(XmlElement processXml) {
    Workflow workflow = new Workflow();
    workflow.sourceWorkflowId(readBpmnAttribute(processXml, "id"));
    workflow.setName(readBpmnAttribute(processXml, "name"));
    readScope(processXml, workflow);
    // TODO readTransitions
    setUnparsedBpmn(workflow, processXml);
    return workflow;
  }

  public String readBpmnAttribute(XmlElement xmlElement, String name) {
    return xmlElement.removeAttribute(getQName(BPMN_URI, name));
  }

  public void readScope(XmlElement scopeElement, Scope scope) {
    Collection<ActivityType> activityTypes = activityTypeService.getActivityTypes();
    Iterator<XmlElement> iterator = scopeElement.elements.iterator();
    while (iterator.hasNext()) {
      XmlElement scopeXmlElement = iterator.next();

      // if it's a sequenceFlow
      if (scopeElement.is(getQName(BPMN_URI, "sequenceFlow"))) {
        Transition transition = new Transition();
        // TODO parse the scopeElement into a transition
        
        scope.transition(transition);
        // remove the sequenceFlow as it is parsed in the model
        iterator.remove();
        
      } else {
        // check if the xml element can be parsed as one of the activity types
        Activity activity = null;
        Iterator<ActivityType> activityTypeIterator = activityTypes.iterator();
        while (activity==null && activityTypeIterator.hasNext()) {
          ActivityType activityType = activityTypeIterator.next();
          activity = activityType.readBpmn(scopeXmlElement, this);
        }
        if (activity!=null) {
          scope.activity(activity);
          setUnparsedBpmn(activity, scopeXmlElement);
          // remove the activity xml element as it is parsed in the model
          iterator.remove();
        }
      }
    }
  }

  protected void addPrefixes(XmlElement xmlElement) {
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

  protected void setUnparsedBpmn(Scope scope, XmlElement unparsedBpmn) {
    unparsedBpmn.name = null;
    scope.setBpmn(unparsedBpmn);
  }

  public boolean isLocalPart(XmlElement xmlElement, String localPart) {
    return xmlElement!=null 
            && xmlElement.name!=null 
            && xmlElement.name.endsWith(localPart);
  }
}
