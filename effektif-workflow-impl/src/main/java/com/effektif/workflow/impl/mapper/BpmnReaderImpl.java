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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormatter;

import com.effektif.workflow.api.mapper.BpmnReader;
import com.effektif.workflow.api.mapper.XmlElement;
import com.effektif.workflow.api.model.Id;
import com.effektif.workflow.api.workflow.Activity;
import com.effektif.workflow.api.workflow.Binding;
import com.effektif.workflow.api.workflow.Scope;
import com.effektif.workflow.api.workflow.Transition;
import com.effektif.workflow.api.workflow.Workflow;
import com.effektif.workflow.impl.bpmn.xml.XmlReader;
import com.effektif.workflow.impl.data.DataTypeService;


/**
 * @author Tom Baeyens
 */
public class BpmnReaderImpl implements BpmnReader {

  public static DateTimeFormatter DATE_FORMAT = AbstractReader.DATE_FORMAT;

  /** global mappings */
  protected Mappings mappings;
  protected XmlElement xmlRoot;

  /** stack of scopes */ 
  protected Stack<Scope> scopeStack = new Stack<Scope>();
  /** current scope */ 
  protected Scope scope;
  
  /** stack of xml elements */ 
  protected Stack<XmlElement> xmlStack = new Stack<XmlElement>();
  /** current xml element */ 
  protected XmlElement xml; 
  
  protected DataTypeService dataTypeService;

  /** maps uri's to prefixes.
   * Ideally this should be done in a stack so that each element can add new namespaces.
   * The addPrefixes() should then be refactored to pushPrefixes and popPrefixes.
   * The current implementation assumes that all namespaces are defined in the root element */
  protected Map<String,String> prefixes = new HashMap<>();

  public BpmnReaderImpl(Mappings mappings) {
    this.mappings = mappings;
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
    initializeNamespacePrefixes(definitionsXml);

    if (definitionsXml.elements != null) {
      Iterator<XmlElement> iterator = definitionsXml.elements.iterator();
      while (iterator.hasNext()) {
        XmlElement definitionElement = iterator.next();
        if (definitionElement.is(getQName(BPMN_URI, "process")) && workflow == null) {
          iterator.remove();
          workflow = readWorkflow(definitionElement);
        }
      }
    }

    if (workflow != null) {
      workflow.property(KEY_DEFINITIONS, definitionsXml);
    }

    return workflow;
  }
  
  protected void initializeNamespacePrefixes(XmlElement xmlElement) {
    Map<String, String> namespaces = xmlElement.namespaces;
    if (namespaces != null) {
      for (String prefix : namespaces.keySet()) {
        prefixes.put(namespaces.get(prefix), prefix);
      }
    }
  }

  protected Workflow readWorkflow(XmlElement processXml) {
    Workflow workflow = new Workflow();
    this.xml = processXml;
    this.scope = workflow;
    workflow.readBpmn(this);
    setUnparsedBpmn(workflow, processXml);
    return workflow;
  }
  
  public void readScope() {
    if (xml.elements!=null) {
      Iterator<XmlElement> iterator = xml.elements.iterator();
      while (iterator.hasNext()) {
        XmlElement scopeElement = iterator.next();
        startElement(scopeElement);
        // Check if the XML element can be parsed as a sequenceFlow.
        if (scopeElement.is(getQName(BPMN_URI, "sequenceFlow"))) {
          Transition transition = new Transition();
          transition.readBpmn(this);
          scope.transition(transition);
          // Remove the sequenceFlow as it has been parsed in the model.
          iterator.remove();

        } else {
          // Check if the XML element can be parsed as one of the activity types.
          BpmnTypeMapping bpmnTypeMapping = mappings.getBpmnTypeMapping(xml, this);
          if (bpmnTypeMapping != null) {
            Activity activity = (Activity) bpmnTypeMapping.instantiate();
            // read the fields
            activity.readBpmn(this);
            scope.activity(activity);
            setUnparsedBpmn(activity, xml);
            // Remove the activity XML element as it has been parsed in the model.
            iterator.remove();
          }
        }

        endElement();
      }
    }
  }
  
  protected void setUnparsedBpmn(Scope scope, XmlElement unparsedBpmn) {
    unparsedBpmn.name = null;
    scope.setBpmn(unparsedBpmn);
  }
  
  @Override
  public List<XmlElement> readElementsBpmn(String localPart) {
    return readElements(getQNameBpmn(localPart));
  }
  
  @Override
  public List<XmlElement> readElementsEffektif(String localPart) {
    return readElements(getQNameEffektif(localPart));
  }
  
  public List<XmlElement> readElements(String elementName) {
    List<XmlElement> elements = new ArrayList<>();
    if (xml.getElements()!=null) {
      Iterator<XmlElement> iterator = xml.getElements().iterator();
      while (iterator.hasNext()) {
        XmlElement xmlElement = iterator.next();
        if (elementName.equals(xmlElement.getName())) {
          iterator.remove();
          elements.add(xmlElement);
        }
      }
    }
    return elements;
  }
  
  @Override
  public void startElement(XmlElement xmlElement) {
    if (xml!=null) {
      xmlStack.push(xml);
    }
    xml = xmlElement;
  }
  
  @Override
  public void endElement() {
    xml = xmlStack.pop();
  }
  
  public void startScope(Scope scope) {
    if (this.scope!=null) {
      scopeStack.push(this.scope);
    }
    this.scope = scope;
  }
  
  public void endScpope() {
    this.scope = scopeStack.pop();
  }
  
  @Override
  public String getQNameBpmn(String localPart) {
    return getQName(BPMN_URI, localPart);
  }
  
  @Override
  public String getQNameEffektif(String localPart) {
    return getQName(EFFEKTIF_URI, localPart);
  }
  
  public String getQName(String namespaceUri, String localName) {
    String prefix = prefixes.get(namespaceUri);
    return "".equals(prefix) ? localName : prefix + ":" + localName;
  }
  
  @Override
  public void startExtensionElements() {
    List<XmlElement> elements = readElementsBpmn("extensionElements");
    startElement(!elements.isEmpty() ? elements.get(0) : null);
  }

  @Override
  public void endExtensionElements() {
    endElement();
  }
  
  @Override
  public String readStringAttributeBpmn(String localPart) {
    return xml.removeAttribute(getQNameBpmn(localPart));
  }

  @Override
  public String readStringAttributeEffektif(String localPart) {
    return xml.removeAttribute(getQNameEffektif(localPart));
  }

  @Override
  public <T extends Id> T readIdAttributeBpmn(String localPart, Class<T> idType) {
    return AbstractReader.createId(readStringAttributeBpmn(localPart), idType);
  }

  @Override
  public <T extends Id> T readIdAttributeEffektif(String localPart, Class<T> idType) {
    return AbstractReader.createId(readStringAttributeEffektif(localPart), idType);
  }

  /** Returns a binding from the first extension element with the given name. */
  @Override
  public <T> Binding<T> readBinding(String elementName, Class<T>  type) {
    List<Binding<T>> bindings = readBindings(elementName, type);
    if (bindings.isEmpty()) {
      return new Binding<T>();
    } else {
      return bindings.get(0);
    }
  }

  /** Returns a list of bindings from the extension elements with the given name. */
  @Override
  public <T> List<Binding<T>> readBindings(String elementName, Class<T> type) {
    List<Binding<T>> bindings = new ArrayList<>();
    if (xml.elements!=null) {
      Iterator<XmlElement> iterator = xml.elements.iterator();
      while (iterator.hasNext()) {
        XmlElement element = iterator.next();
        if (element.is(getQName(EFFEKTIF_URI, elementName))) {
          Binding<T> binding = new Binding<T>();
          String value = element.getAttribute(getQNameEffektif("value"));
          binding.setValue(readAttributeValue(value, type));
          iterator.remove();
        }
      }
    }
    return bindings;
  }
  
  protected <T> T readAttributeValue(String value, Class<T> type) {
    if (value==null) {
      return null;
    }
    if (type==String.class) {
      return (T) value;
    }
    if (type==Boolean.class) {
      return (T) Boolean.valueOf(value);
    }
    if (type==Double.class) {
      return (T) Double.valueOf(value);
    }
    if (type==Long.class) {
      return (T) Long.valueOf(value);
    }
    if (Id.class.isAssignableFrom(type)) {
      return (T) AbstractReader.createId(value, (Class<Id>)type);
    }
    if (type==LocalDateTime.class) {
      return (T) DATE_FORMAT.parseLocalDateTime(value);
    }
    throw new RuntimeException("Couldn't parse "+value+" ("+value.getClass().getName()+") as a "+type.getName());
  }


  /** Returns the contents of the BPMN <code>documentation</code> element. */
  @Override
  public String readDocumentation() {
    if (xml.elements!=null) {
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
  
  @Override
  public LocalDateTime readDateAttributeEffektif(String localPart) {
    return readDate(readStringAttributeEffektif(localPart));
  }

  private LocalDateTime readDate(String readStringAttributeEffektif) {
    throw new RuntimeException("TODO");
  }

  @Override
  public String readTextEffektif(String localPart) {
    if (xml.elements!=null) {
      Iterator<XmlElement> iterator = xml.elements.iterator();
      while (iterator.hasNext()) {
        XmlElement element = iterator.next();
        if (element.is(getQName(EFFEKTIF_URI, localPart))) {
          iterator.remove();
          return element.getText();
        }
      }
    }
    return null;
  }

  @Override
  public XmlElement getUnparsedXml() {
    return xml;
  }

//  @Override
//  public <T extends Id> T readId(Class<T> idType) {
//    return AbstractReader.createId(readBpmnAttribute("id"), idType);
//  }
//
//  @Override
//  public <T extends Id> T readId(String fieldName, Class<T> idType) {
//    return null;
//  }
//
//  @Override
//  public <T extends JsonReadable> List<T> readList(String fieldName, Class<T> type) {
//    return null;
//  }
//
//  @Override
//  public <T extends JsonReadable> T readObject(String fieldName, Class<T> type) {
//    return null;
//  }
//
//  @Override
//  public <T> Map<String, T> readMap(String fieldName, Class<T> valueType) {
//    return null;
//  }
//
//  @Override
//  public String readString(String fieldName) {
//    if (bpmnFieldMappings!=null && bpmnFieldMappings.hasMapping(fieldName)) {
//      return bpmnFieldMappings.readAttribute(this, fieldName);
//    } else if ("id".equals(fieldName)) {
//      return readBpmnAttribute(fieldName);
//    } else if ("description".equals(fieldName)) {
//      return readDocumentation();
//    }
//    return readStringValue(fieldName); 
//  }
//  
//  public BpmnReaderImpl(Configuration configuration) {
//    activityTypeService = configuration.get(ActivityTypeService.class);
//    dataTypeService = configuration.get(DataTypeService.class);
//  }
//
//  public Workflow toWorkflow(String bpmnString) {
//    return toWorkflow(new StringReader(bpmnString));
//  }
//
//  public Workflow toWorkflow(java.io.Reader reader) {
//    this.xmlRoot = XmlReader.parseXml(reader);
//    return readDefinitions(xmlRoot);
//  }
//
//  public String readBpmnAttribute(String name) {
//    return xml.removeAttribute(getQName(BPMN_URI, name));
//  }
//
//  public String readEffektifAttribute(String name) {
//    return xml.removeAttribute(getQName(EFFEKTIF_URI, name));
//  }
//
//  public String getBpmnAttribute(String name) {
//    return xml.getAttribute(getQName(BPMN_URI, name));
//  }
//
//  public String getEffektifAttribute(String name) {
//    return xml.getAttribute(getQName(EFFEKTIF_URI, name));
//  }
//
//
//
//  /**
//   * Returns true iff the given XML element’s <code>effektif:type</code> attribute value is the given Effektif type.
//   */
//  public boolean hasServiceTaskType(XmlElement xml, ServiceTaskType type) {
//    if (type == null) {
//      throw new IllegalArgumentException("type must not be null");
//    }
//    String typeAttributeValue = xml.attributes.get(getQName(Bpmn.EFFEKTIF_URI, "type"));
//    return type.hasValue(typeAttributeValue);
//  }
//
//  protected String getQName(String namespaceUri, String localName) {
//    String prefix = prefixes.get(namespaceUri);
//    return "".equals(prefix) ? localName : prefix+":"+localName;
//  }
//
//  protected void setUnparsedBpmn(Scope scope, XmlElement unparsedBpmn) {
//    unparsedBpmn.name = null;
//    scope.setBpmn(unparsedBpmn);
//  }
//
//  public boolean isLocalPart(XmlElement xmlElement, String localPart) {
//    return xmlElement!=null 
//            && xmlElement.name!=null 
//            && xmlElement.name.endsWith(localPart);
//  }
//
//  /**
//   * Returns a form from the given XML element’s extension (child) elements.
//   */
//  public Form readForm(XmlElement xml) {
//    Form form = new Form();
//    XmlElement extensionElements = xml.findChildElement(getQName(BPMN_URI, "extensionElements"));
//    if (extensionElements != null) {
//      Iterator<XmlElement> extensions = extensionElements.elements.iterator();
//      while (extensions.hasNext()) {
//        XmlElement extension = extensions.next();
//
//        if (extension.is(getQName(EFFEKTIF_URI, "form"))) {
//          for (XmlElement formElement : extension.elements) {
//            if (formElement.is(getQName(EFFEKTIF_URI, "description"))) {
//              form.setDescription(formElement.text);
//            }
//            if (formElement.is(getQName(EFFEKTIF_URI, "field")) && formElement.attributes != null) {
//              FormField field = new FormField();
//              field.setId(formElement.attributes.get("id"));
//              field.setName(formElement.attributes.get("name"));
//              if ("true".equals(formElement.attributes.get("readonly"))) {
//                field.readOnly();
//              }
//              if ("true".equals(formElement.attributes.get("required"))) {
//                field.required();
//              }
//
//              // TODO Work out how to replace with DataType look-up
//              if ("text".equals(formElement.attributes.get("type"))) {
//                field.setType(TextType.INSTANCE);
//              }
//
//              form.field(field);
//            }
//          }
//          // Remove the whole <code>effektif:form</code> element.
//          extensions.remove();
//        }
//      }
//    }
//    return form;
//  }
//
//  /**
//   * Returns a string value read from the extension element with the given name.
//   * The value is either read from the element’s <code>value</code> attribute, or its text content.
//   */
//  public String readStringValue(String fieldName) {
//    XmlElement extensionElements = xml.findChildElement(getQName(BPMN_URI, "extensionElements"));
//    if (extensionElements != null) {
//      Iterator<XmlElement> extensions = extensionElements.elements.iterator();
//      while (extensions.hasNext()) {
//        XmlElement extension = extensions.next();
//        if (extension.is(getQName(EFFEKTIF_URI, fieldName))) {
//          String value;
//          if (extension.attributes != null && extension.attributes.containsKey("value")) {
//            value = extension.attributes.get("value");
//          }
//          else {
//            value = extension.text;
//          }
//          extensions.remove();
//          return value;
//        }
//      }
//    }
//    return null;
//  }
//
//  
//  public Map<String, String> readStringMappings(XmlElement xml, String elementName, String keyAttribute, String valueAttribute) {
//    Map<String, String> mappings = new HashMap<>();
//    XmlElement extensionElements = xml.findChildElement(getQName(BPMN_URI, "extensionElements"));
//
//    if (extensionElements != null) {
//      Iterator<XmlElement> extensions = extensionElements.elements.iterator();
//      while (extensions.hasNext()) {
//        XmlElement extension = extensions.next();
//
//        if (extension.is(getQName(EFFEKTIF_URI, elementName))) {
//          Map<String, String> attributes = extension.attributes;
//          if (attributes != null && attributes.containsKey(keyAttribute) && attributes.containsKey(keyAttribute)) {
//            mappings.put(attributes.get(keyAttribute), attributes.get(valueAttribute));
//          }
//          extensions.remove();
//        }
//      }
//    }
//    return mappings;
//  }
//
//  @Override
//  public <T> Binding<T> readBinding(String fieldName, Class<T> type) {
//    List<Binding<T>> bindings = readBindings(fieldName, type);
//    return bindings!=null ? bindings.get(0) : null;
//  }
//
//  @Override
//  public <T> List<Binding<T>> readBindings(String fieldName, Class<T> type) {
//    List<Binding<T>> bindings = null;
//    XmlElement extensionElements = xml.findChildElement(getQName(BPMN_URI, "extensionElements"));
//    if (extensionElements != null) {
//      Iterator<XmlElement> extensions = extensionElements.elements.iterator();
//      while (extensions.hasNext()) {
//        XmlElement extension = extensions.next();
//        if (extension.is(getQName(EFFEKTIF_URI, fieldName))) {
//          extensions.remove();
//          Binding<T> binding = new Binding<>();
//          String value = extension.getAttribute("value");
//          if (value!=null) {
//            T typedValue = (T) parseValue(value, type);
//            binding.setValue(typedValue);
//          }
//          String expression = extension.getAttribute("expression");
//          binding.setExpression(expression);
//          if (bindings==null) {
//            bindings = new ArrayList<>();
//          }
//          bindings.add(binding);
//        }
//      }
//    }
//    return bindings;
//  }
//  
//  public Object parseValue(String value, Class<?> type) {
//    if (String.class==type) {
//      return value;
//    }
//    if ((Id.class.isAssignableFrom(type))) {
//      try {
//        Constructor<?> c = type.getConstructor(new Class<?>[]{String.class});
//        return c.newInstance(new Object[]{value});
//      } catch (Exception e) {
//        throw new RuntimeException(e);
//      }
//    }
//    throw new RuntimeException("Don't know how to parse value "+value+" as a "+type.getName());
//  }
}
