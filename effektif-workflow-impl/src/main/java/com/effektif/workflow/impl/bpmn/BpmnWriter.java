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

import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import com.effektif.workflow.api.ref.UserId;
import com.effektif.workflow.api.workflow.*;
import com.effektif.workflow.api.xml.XmlElement;
import com.effektif.workflow.impl.activity.ActivityType;
import com.effektif.workflow.impl.activity.ActivityTypeService;
import com.effektif.workflow.impl.bpmn.xml.XmlWriter;
import com.effektif.workflow.impl.workflow.TransitionImpl;
import com.fasterxml.jackson.databind.ObjectMapper;


/** Parses BPMN XML from
 * {@link XmlElement our own jsonnable xml dom structure}
 * to {@link Workflow}.
 * Not thread safe, use one BpmnReader object per serialization. */
public class BpmnWriter extends Bpmn {
  
  protected ActivityTypeService activityTypeService;
  protected String bpmnPrefix;
  protected String effektifPrefix;
  
  /** convenience method */
  public static String writeBpmnDocumentString(Workflow workflow, ActivityTypeService activityTypeService) {
    BpmnWriter bpmnWriter = new BpmnWriter(activityTypeService);
    XmlElement bpmnDefinitions = bpmnWriter.writeDefinitions(workflow);
    return XmlWriter.toString(bpmnDefinitions);
  }

  public BpmnWriter(ActivityTypeService activityTypeService) {
    this.activityTypeService = activityTypeService;
  }

  public void writeBpmnDocument(Workflow workflow, OutputStream out) {
    XmlElement bpmnDefinitions = writeDefinitions(workflow);
    XmlWriter xmlWriter = new XmlWriter(out, "UTF-8");
    xmlWriter.writeDocument(bpmnDefinitions);
    xmlWriter.flush();
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
    XmlElement processElement = getXmlElement(workflow.getBpmn());
    setBpmnName(processElement, "process");
    String sourceWorkflowId = (String) workflow.getSourceWorkflowId();
    if (sourceWorkflowId==null) {
      sourceWorkflowId = workflow.getId();
    }
    writeBpmnAttribute(processElement, "id", sourceWorkflowId);
    writeBpmnAttribute(processElement, "name", workflow.getName());
    writeActivities(workflow, processElement);
    writeTransitions(workflow, processElement);
    return processElement;
  }

  public void writeBpmnAttribute(XmlElement xmlElement, String name, String value) {
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
        ActivityType<Activity> activityType = activityTypeService.getActivityType(activity.getClass());
        XmlElement activityXml = getXmlElement(activity.getBpmn());
        activityType.writeBpmn(activity, activityXml, this);
        scopeElement.addElementFirst(activityXml);
      }
    }
  }

  private void writeTransitions(Workflow workflow, XmlElement processElement) {
    List<Transition> transitions = workflow.getTransitions();
    if (transitions != null) {
      for (Transition transition : transitions) {
        XmlElement transitionXml = getXmlElement(transition.getBpmn());
        new TransitionImpl().writeBpmn(transition, transitionXml, this);
        processElement.addElement(transitionXml);
      }
    }
  }

  protected XmlElement getXmlElement(Object source) {
    if (source==null) {
      return new XmlElement();
      
    } else if (source instanceof XmlElement) {
      return (XmlElement) source;
      
    } else if (source instanceof Map) {
      // In case the workflow was serialized, the properties are detyped
      // because jackson can't figure out what type a specific property value has
      ObjectMapper objectMapper = new ObjectMapper();
      return objectMapper.convertValue(source, XmlElement.class);
    }

    throw new RuntimeException("Unknown BPMN source: "+source);
  }

  public String getBpmnQName(String localPart) {
    return bpmnPrefix==null || "".equals(bpmnPrefix) ? localPart : bpmnPrefix+":"+localPart;
  }

  public String getEffektifQName(String localPart) {
    return effektifPrefix==null || "".equals(effektifPrefix) ? localPart : effektifPrefix+":"+localPart;
  }

  public void setBpmnName(XmlElement xmlElement, String localPart) {
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
   */
  public void writeBinding(XmlElement xml, String elementName, String attributeName, Binding<?> binding) {
    if (binding==null) {
      return;
    }
    XmlElement extensionElements = xml.findOrAddChildElement(getBpmnQName("extensionElements"));
    if (binding.getValue() != null) {
      XmlElement bindingXml = new XmlElement(getEffektifQName(elementName));
      bindingXml.addAttribute(attributeName, binding.getValue().toString());
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
  public void writeBindings(XmlElement xml, String elementName, String attributeName, List<Binding<?>> bindings) {
    if (bindings==null) {
      return;
    }
    for (Binding<?> nestedBinding : bindings) {
      XmlElement bindingXml = new XmlElement(getEffektifQName(elementName));
      bindingXml.addAttribute(attributeName, nestedBinding.getValue().toString());
      xml.addElement(bindingXml);
    }
  }
}
