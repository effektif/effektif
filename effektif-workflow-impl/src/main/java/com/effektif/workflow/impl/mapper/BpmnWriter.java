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

import static com.effektif.workflow.impl.bpmn.Bpmn.*;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import org.joda.time.LocalDateTime;

import com.effektif.workflow.api.Configuration;
import com.effektif.workflow.api.mapper.Writable;
import com.effektif.workflow.api.mapper.Writer;
import com.effektif.workflow.api.mapper.XmlElement;
import com.effektif.workflow.api.model.Id;
import com.effektif.workflow.api.types.Type;
import com.effektif.workflow.api.workflow.Activity;
import com.effektif.workflow.api.workflow.Binding;
import com.effektif.workflow.api.workflow.Scope;
import com.effektif.workflow.api.workflow.Workflow;
import com.effektif.workflow.impl.activity.ActivityTypeService;
import com.effektif.workflow.impl.bpmn.ServiceTaskType;
import com.effektif.workflow.impl.bpmn.xml.XmlWriter;
import com.effektif.workflow.impl.data.DataType;
import com.effektif.workflow.impl.data.DataTypeService;


/**
 * @author Tom Baeyens
 */
public class BpmnWriter implements Writer {
  
  protected Mappings mappings;
  protected ActivityTypeService activityTypeService;
  protected String bpmnPrefix;
  protected String effektifPrefix;
  protected DataTypeService dataTypeService;
  protected XmlElement xmlElement;

  /** convenience method */
  public static String writeBpmnDocumentString(Workflow workflow, Configuration configuration) {
    BpmnWriter bpmnWriter = new BpmnWriter(configuration);
    XmlElement bpmnDefinitions = bpmnWriter.writeDefinitions(workflow);
    return XmlWriter.toString(bpmnDefinitions);
  }

  public BpmnWriter(Mappings mappings) {
    this.mappings = mappings;
  }

  public BpmnWriter(Configuration configuration) {
    activityTypeService = configuration.get(ActivityTypeService.class);
    dataTypeService = configuration.get(DataTypeService.class);
  }

  public void write(Workflow workflow, OutputStream out) {
    XmlElement bpmnDefinitions = writeDefinitions(workflow);
    XmlWriter xmlWriter = new XmlWriter(out, "UTF-8");
    xmlWriter.writeDocument(bpmnDefinitions);
    xmlWriter.flush();
  }

  public String write(Workflow workflow) {
    ByteArrayOutputStream stream = new ByteArrayOutputStream();
    write(workflow, stream);
    return stream.toString();
  }

  protected XmlElement writeDefinitions(Workflow workflow) {
    XmlElement definitionsElement = getXmlElement(workflow.getProperty(KEY_DEFINITIONS));
    
    if (definitionsElement.namespaces!=null) {
      for (String prefix : definitionsElement.namespaces.keySet()) {
        String uri = definitionsElement.namespaces.get(prefix);
        if (BPMN_URI.equals(uri)) {
          bpmnPrefix = prefix;
        } else if (EFFEKTIF_URI.equals(uri)) {
          effektifPrefix = prefix;
        }
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
    xmlElement = getXmlElement(workflow.getBpmn());
    setBpmnName("process");
    if (workflow.getSourceWorkflowId()==null && workflow.getId()!=null) {
      workflow.setSourceWorkflowId(workflow.getId().getInternal());
    }
    workflow.writeFields(this);
    return xmlElement;
  }

  public void setBpmnAttribute(String name, String value) {
    if (value!=null) {
      xmlElement.addAttribute(getBpmnQName(name), value);
    }
  }

  protected void writeActivities(Scope scope, XmlElement scopeElement) {
    List<Activity> activities = scope.getActivities();
    if (activities!=null) {
      // We loop backwards and then add each activity as the first
      // This way all the parsed activities will be serialized first
      // before the unknown elements and the parsed elements will
      // appear in the order as they were parsed.
      for (int i=activities.size()-1; i>=0; i--) {
        Activity activity = activities.get(i);
        activity.writeFields(this);
        
//        ActivityType<Activity> activityType = activityTypeService.getActivityType(activity.getClass());
//        XmlElement activityXml = getXmlElement(activity.getBpmn());
//        writeBpmnAttribute(activityXml, "id", activity.getId());
//        writeBpmnAttribute(activityXml, "name", activity.getName());
//        writeDocumentation(activityXml, activity.getDescription());
//        activityType.writeBpmn(activity, activityXml, this);
//        scopeElement.addElementFirst(activityXml);
      }
    }
  }

//  private void writeTransitions(Workflow workflow, XmlElement processElement) {
//    List<Transition> transitions = workflow.getTransitions();
//    if (transitions != null) {
//      for (Transition transition : transitions) {
//        XmlElement transitionXml = getXmlElement(transition.getBpmn());
//        new TransitionImpl().writeBpmn(transition, transitionXml, this);
//        processElement.addElement(transitionXml);
//      }
//    }
//  }

  protected XmlElement getXmlElement(Object source) {
    if (source==null) {
      return new XmlElement();
    } else if (source instanceof XmlElement) {
      return (XmlElement) source;
    }
    throw new RuntimeException("Unknown BPMN source: "+source);
  }

  public String getBpmnQName(String localPart) {
    return bpmnPrefix==null || "".equals(bpmnPrefix) ? localPart : bpmnPrefix+":"+localPart;
  }

  public String getEffektifQName(String localPart) {
    return effektifPrefix==null || "".equals(effektifPrefix) ? localPart : effektifPrefix+":"+localPart;
  }

  public void setBpmnName(String localPart) {
    xmlElement.name = getBpmnQName(localPart);
  }

  /**
   * Writes the given service task type as an <code>effektif:type</code> attribute.
   */
  public void writeEffektifType(XmlElement xml, ServiceTaskType type) {
    xml.addAttribute(getEffektifQName("type"), type.value());
  }

  /**
   * Writes binding values as extension elements with the given local name and attribute name,
   * e.g. <effektif:assignee userId="42"/>.
   * TODO other Binding fields, e.g. variableId
   */
  public void writeBinding(XmlElement xml, String elementName, Binding<?> binding, Type type) {
    if (binding==null) {
      return;
    }
    DataType dataType = dataTypeService.createDataType(type);
    if (binding.getValue() != null) {
      XmlElement extensionElements = xml.findOrAddChildElement(getBpmnQName("extensionElements"));
      XmlElement bindingXml = new XmlElement(getEffektifQName(elementName));
      dataType.writeValue(bindingXml, binding.getValue());
      extensionElements.addElement(bindingXml);
    }
    else {
      // TODO other binding fields
    }
  }
  
  /**
   * Writes binding values as extension elements with the given local name and attribute name,
   * e.g. <effektif:assignee userId="42"/>.
   */
  public void writeBindings(XmlElement xml, String elementName, List<Binding> bindings, Type type) {
    if (bindings==null) {
      return;
    }
    for (Binding nestedBinding : bindings) {
      writeBinding(xml, elementName, nestedBinding, type);
    }
  }

  /**
   * Writes the given documentation string as a BPMN <code>documentation</code> element.
   */
  public void writeDocumentation(XmlElement xml, String documentation) {
    if (documentation != null && !documentation.isEmpty()) {
      XmlElement newElement = new XmlElement(getBpmnQName("documentation"));
      newElement.addText(documentation);
      xml.addElementFirst(newElement);
    }
  }

  /**
   * Writes an extension element with the string value as an attribute.
   */
  public void writeStringValue(XmlElement xml, String elementName, String value) {
    if (value != null && !value.isEmpty()) {
      XmlElement newElement = new XmlElement(getEffektifQName(elementName));
      newElement.addAttribute("value", value);
      XmlElement extensionElements = xml.findOrAddChildElement(getBpmnQName("extensionElements"));
      extensionElements.addElement(newElement);
    }
  }

  /**
   * Writes an extension element with the string value as text content.
   */
  public void writeStringValueAsText(XmlElement xml, String elementName, String value) {
    if (value != null && !value.isEmpty()) {
      XmlElement newElement = new XmlElement(getEffektifQName(elementName));
      newElement.addText(value);
      XmlElement extensionElements = xml.findOrAddChildElement(getBpmnQName("extensionElements"));
      extensionElements.addElement(newElement);
    }
  }

  /**
   * Writes an extension element with the string value as text content.
   */
  public void writeStringValueAsCData(XmlElement xml, String elementName, String value) {
    if (value != null && !value.isEmpty()) {
      writeStringValueAsText(xml, elementName, "<![CDATA[" + value + "]]>");
    }
  }

  /**
   * Writes extension elements with the string mappings as attribute values.
   */
  public void writeStringMappings(XmlElement xml, String elementName, String keyAttribute, String valueAttribute, Map<String, String> mappings) {
    if (mappings != null && !mappings.isEmpty()) {
      XmlElement extensionElements = xml.findOrAddChildElement(getBpmnQName("extensionElements"));
      for (String mappingKey : mappings.keySet()) {
        XmlElement newElement = new XmlElement(getEffektifQName(elementName));
        newElement.addAttribute(keyAttribute, mappingKey);
        newElement.addAttribute(valueAttribute, mappings.get(mappingKey));
        extensionElements.addElement(newElement);
      }
    }
  }



  @Override
  public void writeString(String fieldName, String value) {
    if (value!=null) {
      setBpmnAttribute(fieldName, value);
    }
  }

  @Override
  public void writeBoolean(String fieldName, Boolean value) {
    if (value!=null) {
      setBpmnAttribute(fieldName, value.toString());
    }
  }

  @Override
  public void writeNumber(String fieldName, Number value) {
    if (value!=null) {
      setBpmnAttribute(fieldName, value.toString());
    }
  }

  @Override
  public void writeObject(String fieldName, Writable o) {
  }

  @Override
  public void writeId(Id id) {
    writeId("id", id);
  }

  @Override
  public void writeId(String fieldName, Id id) {
    if (id!=null) {
      writeString(fieldName, id.getInternal());
    }
  }

  @Override
  public void writeDate(String fieldName, LocalDateTime value) {
  }

  @Override
  public void writeList(String fieldName, List< ? extends Object> elements) {
    if (elements==null) {
      return;
    }
    if ("activities".equals(fieldName)) {
      XmlElement parent = xmlElement;
      for (int i=elements.size()-1; i>=0; i--) {
        Activity activity = (Activity) elements.get(i);
        xmlElement = getXmlElement(activity.getBpmn());
        BpmnTypeMapping bpmnTypeMapping = mappings.getBpmnTypeMapping(activity.getClass());
        setBpmnName(bpmnTypeMapping.getBpmnElementName());
        Map<String, String> bpmnTypeAttributes = bpmnTypeMapping.getBpmnTypeAttributes();
        if (bpmnTypeAttributes!=null) {
          for (String attribute: bpmnTypeAttributes.keySet()) {
            String value = bpmnTypeAttributes.get(attribute);
            xmlElement.addAttribute(getEffektifQName(attribute), value);
          }
        }
        activity.writeFields(this);
        parent.addElement(xmlElement);
      }
      xmlElement = parent;
    }
  }

  @Override
  public void writeFields(Map<String, ? extends Object> fieldValues) {
  }

  @Override
  public void writeMap(String fieldName, Map<String, ? extends Object> map) {
  }

}
