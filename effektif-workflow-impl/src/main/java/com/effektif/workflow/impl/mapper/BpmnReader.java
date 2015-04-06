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

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.joda.time.format.DateTimeFormatter;

import com.effektif.workflow.api.Configuration;
import com.effektif.workflow.api.form.Form;
import com.effektif.workflow.api.form.FormField;
import com.effektif.workflow.api.mapper.Readable;
import com.effektif.workflow.api.mapper.Reader;
import com.effektif.workflow.api.mapper.XmlElement;
import com.effektif.workflow.api.model.Id;
import com.effektif.workflow.api.types.TextType;
import com.effektif.workflow.api.types.Type;
import com.effektif.workflow.api.workflow.Activity;
import com.effektif.workflow.api.workflow.Binding;
import com.effektif.workflow.api.workflow.Scope;
import com.effektif.workflow.api.workflow.Transition;
import com.effektif.workflow.api.workflow.Workflow;
import com.effektif.workflow.impl.activity.ActivityType;
import com.effektif.workflow.impl.activity.ActivityTypeService;
import com.effektif.workflow.impl.bpmn.Bpmn;
import com.effektif.workflow.impl.bpmn.ServiceTaskType;
import com.effektif.workflow.impl.bpmn.xml.XmlReader;
import com.effektif.workflow.impl.data.DataType;
import com.effektif.workflow.impl.data.DataTypeService;


/**
 * @author Tom Baeyens
 */
public class BpmnReader implements Reader {

  public static DateTimeFormatter DATE_FORMAT = AbstractReader.DATE_FORMAT;

  /** global mappings */
  protected Mappings mappings;
  
  /** current bpmnMappings */
  protected BpmnFieldMappingsImpl bpmnFieldMappings; 

  protected XmlElement xmlRoot;
  
  protected ActivityTypeService activityTypeService;
  
  /** current xml element */
  protected XmlElement xml; 

  /** maps uri's to prefixes.
   * Ideally this should be done in a stack so that each element can add new namespaces.
   * The addPrefixes() should then be refactored to pushPrefixes and popPrefixes.
   * The current implementation assumes that all namespaces are defined in the root element */
  protected Map<String,String> prefixes = new HashMap<>();

  private DataTypeService dataTypeService;


  public BpmnReader(Mappings mappings) {
    this.mappings = mappings;
  }

  @Override
  public <T extends Id> T readId(Class<T> idType) {
    return AbstractReader.createId(readBpmnAttribute("id"), idType);
  }

  @Override
  public <T extends Id> T readId(String fieldName, Class<T> idType) {
    return null;
  }

  @Override
  public <T extends Readable> List<T> readList(String fieldName, Class<T> type) {
    return null;
  }

  @Override
  public <T extends Readable> T readObject(String fieldName, Class<T> type) {
    return null;
  }

  @Override
  public <T> Map<String, T> readMap(String fieldName, Class<T> valueType) {
    return null;
  }

  @Override
  public String readString(String fieldName) {
    // TODO add default description to documentation mapping
    if ("description".equals(fieldName)) {
      return readBpmnDocumentation(xml);
    }
    if (bpmnFieldMappings!=null) {
      return bpmnFieldMappings.readAttribute(this, fieldName);
    }
    return readBpmnAttribute(fieldName); 
  }
  
  public BpmnReader(Configuration configuration) {
    activityTypeService = configuration.get(ActivityTypeService.class);
    dataTypeService = configuration.get(DataTypeService.class);
  }

  public Workflow toWorkflow(String bpmnString) {
    return toWorkflow(new StringReader(bpmnString));
  }

  public Workflow toWorkflow(java.io.Reader reader) {
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
          workflow = readWorkflow(definitionElement);
        }
      }
    }
    
    if (workflow!=null) {
      workflow.property(KEY_DEFINITIONS, definitionsXml);
    }
    
    return workflow;
  }

  protected Workflow readWorkflow(XmlElement processXml) {
    this.xml = processXml;
    Workflow workflow = new Workflow();
    workflow.readFields(this);
    readScope(processXml, workflow);
    setUnparsedBpmn(workflow, processXml);
    return workflow;
  }

  public String readBpmnAttribute(String name) {
    return xml.removeAttribute(getQName(BPMN_URI, name));
  }

  public String readEffektifAttribute(String name) {
    return xml.removeAttribute(getQName(EFFEKTIF_URI, name));
  }

  public String getBpmnAttribute(String name) {
    return xml.getAttribute(getQName(BPMN_URI, name));
  }

  public String getEffektifAttribute(String name) {
    return xml.getAttribute(getQName(EFFEKTIF_URI, name));
  }

  public void readScope(XmlElement scopeElement, Scope scope) {
    XmlElement parentXml = scopeElement;
    Iterator<XmlElement> iterator = scopeElement.elements.iterator();
    while (iterator.hasNext()) {
      this.xml = iterator.next();

      // Check if the XML element can be parsed as a sequenceFlow.
      if (xml.is(getQName(BPMN_URI, "sequenceFlow"))) {
        Transition transition = new Transition();
        bpmnFieldMappings = mappings.getBpmnNameMappings(Transition.class);
        transition.readFields(this);
        scope.transition(transition);
        // Remove the sequenceFlow as it has been parsed in the model.
        iterator.remove();
        
      } else {
        // Check if the XML element can be parsed as one of the activity types.
        BpmnTypeMapping bpmnTypeMapping = mappings.getBpmnTypeMapping(xml, this);
        if (bpmnTypeMapping!=null) {
          Activity activity = (Activity) bpmnTypeMapping.instantiate();
          // activate the activity's field mappings
          bpmnFieldMappings = bpmnTypeMapping.getBpmnFieldMappings();
          // read the fields
          activity.readFields(this);
          bpmnFieldMappings = null;
//          activity.setId(readBpmnAttribute(childElement, "id"));
//          activity.setName(readBpmnAttribute(childElement, "name"));
//          activity.setDescription(readBpmnDocumentation(childElement));
          scope.activity(activity);
          setUnparsedBpmn(activity, xml);
          // Remove the activity XML element as it has been parsed in the model.
          iterator.remove();
        }
      }
    }
    xml = parentXml;
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
  public <T> Binding<T> readBinding(Class<T> bindingType, Type dataType, XmlElement xml, String elementName) {
    List<Binding<T>> bindings = readBindings(bindingType, dataType, xml, elementName);
    if (bindings.isEmpty()) {
      return new Binding<T>();
    }
    else {
      return bindings.get(0);
    }
  }

  /**
   * Returns a list of bindings from the extension elements with the given name.
   */
  public <T> List<Binding<T>> readBindings(Class<T> bindingType, Type dataType, XmlElement xml, String elementName) {
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

  /**
   * Returns a form from the given XML element’s extension (child) elements.
   */
  public Form readForm(XmlElement xml) {
    Form form = new Form();
    XmlElement extensionElements = xml.findChildElement(getQName(BPMN_URI, "extensionElements"));
    if (extensionElements != null) {
      Iterator<XmlElement> extensions = extensionElements.elements.iterator();
      while (extensions.hasNext()) {
        XmlElement extension = extensions.next();

        if (extension.is(getQName(EFFEKTIF_URI, "form"))) {
          for (XmlElement formElement : extension.elements) {
            if (formElement.is(getQName(EFFEKTIF_URI, "description"))) {
              form.setDescription(formElement.text);
            }
            if (formElement.is(getQName(EFFEKTIF_URI, "field")) && formElement.attributes != null) {
              FormField field = new FormField();
              field.setId(formElement.attributes.get("id"));
              field.setName(formElement.attributes.get("name"));
              if ("true".equals(formElement.attributes.get("readonly"))) {
                field.readOnly();
              }
              if ("true".equals(formElement.attributes.get("required"))) {
                field.required();
              }

              // TODO Work out how to replace with DataType look-up
              if ("text".equals(formElement.attributes.get("type"))) {
                field.setType(TextType.INSTANCE);
              }

              form.field(field);
            }
          }
          // Remove the whole <code>effektif:form</code> element.
          extensions.remove();
        }
      }
    }
    return form;
  }

  /**
   * Returns a string value read from the extension element with the given name.
   * The value is either read from the element’s <code>value</code> attribute, or its text content.
   */
  public String readStringValue(XmlElement xml, String elementName) {
    XmlElement extensionElements = xml.findChildElement(getQName(BPMN_URI, "extensionElements"));

    if (extensionElements != null) {
      Iterator<XmlElement> extensions = extensionElements.elements.iterator();
      while (extensions.hasNext()) {
        XmlElement extension = extensions.next();

        if (extension.is(getQName(EFFEKTIF_URI, elementName))) {
          String value;
          if (extension.attributes != null && extension.attributes.containsKey("value")) {
            value = extension.attributes.get("value");
          }
          else {
            value = extension.text;
          }
          extensions.remove();
          return value;
        }
      }
    }
    return null;
  }

  /**
   * Returns the contents of the BPMN <code>documentation</code> element.
   */
  public String readBpmnDocumentation(XmlElement xml) {
    if (xml.elements != null) {
      Iterator<XmlElement> elements = xml.elements.iterator();
      while (elements.hasNext()) {
        XmlElement element = elements.next();
        if (element.is(getQName(BPMN_URI, "documentation"))) {
          elements.remove();
          return element.text;
        }
      }
    }
    return null;
  }
  public Map<String, String> readStringMappings(XmlElement xml, String elementName, String keyAttribute, String valueAttribute) {
    Map<String, String> mappings = new HashMap<>();
    XmlElement extensionElements = xml.findChildElement(getQName(BPMN_URI, "extensionElements"));

    if (extensionElements != null) {
      Iterator<XmlElement> extensions = extensionElements.elements.iterator();
      while (extensions.hasNext()) {
        XmlElement extension = extensions.next();

        if (extension.is(getQName(EFFEKTIF_URI, elementName))) {
          Map<String, String> attributes = extension.attributes;
          if (attributes != null && attributes.containsKey(keyAttribute) && attributes.containsKey(keyAttribute)) {
            mappings.put(attributes.get(keyAttribute), attributes.get(valueAttribute));
          }
          extensions.remove();
        }
      }
    }
    return mappings;
  }

  @Override
  public <T> Binding<T> readBinding(String fieldName, Class<T> type) {
    return null;
  }

  @Override
  public <T> List<Binding<T>> readBindings(String fieldName, Class<T> type) {
    return null;
  }
}
