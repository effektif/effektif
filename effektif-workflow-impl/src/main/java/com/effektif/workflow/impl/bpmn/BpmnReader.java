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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.effektif.workflow.api.Configuration;
import com.effektif.workflow.api.types.Type;
import com.effektif.workflow.api.workflow.Activity;
import com.effektif.workflow.api.workflow.Binding;
import com.effektif.workflow.api.workflow.Scope;
import com.effektif.workflow.api.workflow.Transition;
import com.effektif.workflow.api.workflow.Workflow;
import com.effektif.workflow.api.xml.XmlElement;
import com.effektif.workflow.impl.activity.ActivityType;
import com.effektif.workflow.impl.activity.ActivityTypeService;
import com.effektif.workflow.impl.bpmn.xml.XmlReader;
import com.effektif.workflow.impl.data.DataType;
import com.effektif.workflow.impl.data.DataTypeService;
import com.effektif.workflow.impl.workflow.TransitionImpl;


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

  private DataTypeService dataTypeService;

  public BpmnReader(Configuration configuration) {
    activityTypeService = configuration.get(ActivityTypeService.class);
    dataTypeService = configuration.get(DataTypeService.class);
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
      XmlElement childElement = iterator.next();

      // Check if the XML element can be parsed as a sequenceFlow.
      if (childElement.is(getQName(BPMN_URI, "sequenceFlow"))) {
        Transition transition = new TransitionImpl().readBpmn(childElement, this);
        scope.transition(transition);
        // Remove the sequenceFlow as it has been parsed in the model.
        iterator.remove();
        
      } else {
        // Check if the XML element can be parsed as one of the activity types.
        Activity activity = null;
        Iterator<ActivityType> activityTypeIterator = activityTypes.iterator();
        while (activity==null && activityTypeIterator.hasNext()) {
          ActivityType activityType = activityTypeIterator.next();
          activity = activityType.readBpmn(childElement, this);
        }
        if (activity!=null) {
          activity.setName(readBpmnAttribute(childElement, "name"));
          scope.activity(activity);
          setUnparsedBpmn(activity, childElement);
          // Remove the activity XML element as it has been parsed in the model.
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

  /**
   * Returns true iff the given XML element’s <code>effektif:type</code> attribute value is the given Effektif type.
   */
  public boolean hasServiceTaskType(XmlElement xml, ServiceTaskType type) {
    if (type == null) {
      throw new IllegalArgumentException("type must not be null");
    }
    String typeAttributeValue = xml.attributes.get(getQName(Bpmn.EFFEKTIF_URI, "type"));
    return type.hasValue(typeAttributeValue);
  }

  protected String getQName(String namespaceUri, String localName) {
    String prefix = prefixes.get(namespaceUri);
    return "".equals(prefix) ? localName : prefix+":"+localName;
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

  /**
   * Returns a binding from the first extension element with the given name.
   */
  public <T> Binding<T> readFirstBinding(Class<T> bindingType, Type dataType, XmlElement xml, String elementName) {
    List<Binding<T>> bindings = readBindings(bindingType, dataType, xml, elementName);
    if (bindings.isEmpty()) {
      return new Binding<T>();
    }
    else {
      return bindings.get(0);
    }
  }

  /**
   * Returns a list-based binding from the extension elements with the given name.
   */
  public <T> Binding<T> readListBinding(Class<T> bindingType, Type dataType, XmlElement xml, String elementName) {
    Binding<T> binding = new Binding<T>();
    binding.setBindings(readBindings(bindingType, dataType, xml, elementName));
    return binding;
  }

  /**
   * Returns a list of bindings from the extension elements with the given name.
   */
  private <T> List<Binding<T>> readBindings(Class<T> bindingType, Type dataType, XmlElement xml, String elementName) {
    List<Binding<T>> results = new ArrayList<>();
    XmlElement extensionElements = xml.findChildElement(getQName(BPMN_URI, "extensionElements"));
    if (extensionElements != null) {
      Iterator<XmlElement> extensions = extensionElements.elements.iterator();
      while (extensions.hasNext()) {
        XmlElement extension = extensions.next();

        if (extension.is(getQName(EFFEKTIF_URI, elementName))) {
          DataType type = dataTypeService.createDataType(dataType);
          Binding binding = type.readValue(extension);
          if (binding != null) {
            results.add(binding);
            extensions.remove();
          }
          // TODO other binding fields
        }
      }
    }
    return results;
  }

}
