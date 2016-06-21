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

import static com.effektif.workflow.impl.bpmn.Bpmn.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.Stack;
import java.util.stream.Collectors;

import com.effektif.workflow.api.bpmn.XmlNamespaces;
import com.effektif.workflow.api.condition.SingleBindingCondition;
import com.effektif.workflow.api.workflow.*;
import com.effektif.workflow.impl.workflow.boundary.BoundaryEventTimer;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.effektif.workflow.api.bpmn.BpmnReadable;
import com.effektif.workflow.api.bpmn.BpmnReader;
import com.effektif.workflow.api.bpmn.XmlElement;
import com.effektif.workflow.api.condition.Condition;
import com.effektif.workflow.api.model.Id;
import com.effektif.workflow.api.model.RelativeTime;
import com.effektif.workflow.api.types.DataType;
import com.effektif.workflow.api.workflow.diagram.Bounds;
import com.effektif.workflow.api.workflow.diagram.Diagram;
import com.effektif.workflow.api.workflow.diagram.Edge;
import com.effektif.workflow.api.workflow.diagram.Node;
import com.effektif.workflow.api.workflow.diagram.Point;
import com.effektif.workflow.impl.exceptions.BadRequestException;
import com.effektif.workflow.impl.json.JsonObjectReader;
import com.effektif.workflow.impl.json.JsonStreamMapper;
import com.effektif.workflow.impl.json.JsonTypeMapper;
import com.effektif.workflow.impl.json.PolymorphicMapping;
import com.effektif.workflow.impl.json.TypeMapping;
import com.effektif.workflow.impl.json.types.LocalDateTimeStreamMapper;

/**
 * This implementation of the BPMN reader is based on reading single values from XML elements and attributes into
 * single-valued (mostly primitive) types. Complex types and arbitrary Java beans are not supported
 *
 * To support complex types in the future, a preferable alternative to implementing an bean mapping framework will be to
 * leverage the existing JSON mapping implementation, which supports nested structures, and read complex objects from
 * JSON embedded in CDATA sections in the BPMN. For example, something like:
 *
 * <pre>
 *   <e:input key="user">
 *     <e:binding type="json"><![CDATA[
 *       { type: "user", id: 42, name: "Joe Bloggs" }
 *     ]]></e:binding>
 *   </e:input>
 * </pre>
 *
 * TODO Refactor to make reading use a more consistent API than the current read methods:
 * a mix between model class readBpmn methods, and read* methods in this class with inconsistent parameter lists.
 *
 * @author Tom Baeyens
 */
public class BpmnReaderImpl implements BpmnReader {

  private static final Logger log = LoggerFactory.getLogger(BpmnReaderImpl.class);

  /** global mappings */
  protected BpmnMappings bpmnMappings;

  /** stack of scopes */
  protected Stack<Scope> scopeStack = new Stack<Scope>();
  /** current scope */
  protected Scope scope;

  /** stack of xml elements */
  protected Stack<XmlElement> xmlStack = new Stack<XmlElement>();
  /** current xml element */
  protected XmlElement currentXml;
  protected Class<?> currentClass;

  protected JsonStreamMapper jsonStreamMapper;

  public BpmnReaderImpl(BpmnMappings bpmnMappings, JsonStreamMapper jsonStreamMapper) {
    this.bpmnMappings = bpmnMappings;
    this.jsonStreamMapper = jsonStreamMapper;
  }

  /**
   * The BPMN <code>definitions</code> element includes the document’s XML namespace declarations.
   * Ideally, namespaces should be read in a stack, so that each element can add new namespaces.
   * The addPrefixes() should then be refactored to pushPrefixes and popPrefixes.
   * The current implementation assumes that all namespaces are defined in the root element.
   */
  protected AbstractWorkflow readDefinitions(XmlElement definitionsXml) {
    AbstractWorkflow workflow = null;

    if (definitionsXml.elements != null) {
      Iterator<XmlElement> iterator = definitionsXml.elements.iterator();
      while (iterator.hasNext()) {
        XmlElement definitionElement = iterator.next();
        boolean processAlreadyParsed = workflow != null;
        if (definitionElement.is(BPMN_URI, "process") && !processAlreadyParsed) {
          iterator.remove();
          workflow = readWorkflow(definitionElement);
        }
      }
    }

    if (workflow == null) {
      workflow = new ExecutableWorkflow();
    }

    readDiagram(workflow, definitionsXml);
    definitionsXml.cleanEmptyElements();
    workflow.property(KEY_DEFINITIONS, definitionsXml);

    return workflow;
  }

  protected AbstractWorkflow readWorkflow(XmlElement processXml) {
    AbstractWorkflow workflow = new ExecutableWorkflow();
    this.currentXml = processXml;
    this.scope = workflow;
    workflow.readBpmn(this);

    attachTimers(workflow);

    readLanes(workflow);
    removeDanglingTransitions(workflow);
    setUnparsedBpmn(workflow, processXml);
    workflow.cleanUnparsedBpmn();
    return workflow;
  }

  protected void attachTimers(AbstractWorkflow workflow) {

    List<Timer> timers = workflow.getTimers();

    if (timers != null && timers.size() > 0) {

      Iterator<Timer> timerIterator = timers.iterator();
      while (timerIterator.hasNext()) {
        Timer timer = timerIterator.next();

        // todo: make generic
        if (timer instanceof BoundaryEventTimer) {
          BoundaryEvent boundaryEvent = ((BoundaryEventTimer) timer).boundaryEvent;

          if (boundaryEvent != null) {
            Activity act = workflow.findActivity(boundaryEvent.getFromId());
            if (act != null) act.timer(timer);
            timerIterator.remove();
          }
        }
      }
    }
  }

  protected void readLanes(AbstractWorkflow workflow) {
    // Not supported.
  }

  public void readScope() {
    if (currentXml.elements!=null) {
      Iterator<XmlElement> iterator = currentXml.elements.iterator();
      while (iterator.hasNext()) {
        XmlElement scopeElement = iterator.next();
        startElement(scopeElement);

        if (scopeElement.is(BPMN_URI, "extensionElements")) {
          scope.setProperties(readSimpleProperties());
        } else if (scopeElement.is(BPMN_URI, "sequenceFlow")) {
          Transition transition = new Transition();
          transition.readBpmn(this);
          scope.transition(transition);
          // Remove the sequenceFlow as it has been parsed in the model.
          iterator.remove();
        } else if (scopeElement.is(BPMN_URI, "boundaryEvent")) {

//          <bpmn:boundaryEvent id="BoundaryEvent_1ymyt09" attachedToRef="Task_02wgtff">
//            <bpmn:outgoing>SequenceFlow_0se37xg</bpmn:outgoing>
//            <bpmn:timerEventDefinition>
//              <bpmn:timeDuration>PT5M</bpmn:timeDuration>
//            </bpmn:timerEventDefinition>
//          </bpmn:boundaryEvent>
//          <bpmn:sequenceFlow id="SequenceFlow_0se37xg" sourceRef="BoundaryEvent_1ymyt09" targetRef="Task_13koiv2" />

          startElement(scopeElement);
          BoundaryEvent boundaryEvent = new BoundaryEvent();
          boundaryEvent.readBpmn(this);

          for (XmlElement xmlElement : currentXml.getElements()) {
            BpmnTypeMapping typeMapping = bpmnMappings.getBpmnTypeMapping(xmlElement, this);

            if (typeMapping != null) {
              BoundaryEventTimer timer = new BoundaryEventTimer();

              startElement(xmlElement);
              timer.readBpmn(this);
              timer.boundaryEvent = boundaryEvent;
              endElement();

              scope.timer(timer);
            }
          }

          iterator.remove();
          endElement();
        } else {
          BpmnTypeMapping bpmnTypeMapping = getBpmnTypeMapping();
          if (bpmnTypeMapping != null) {
            // Check whether the BPMN type mapping is to an Activity or Timer, etc.
            Object bpmnElement = bpmnTypeMapping.instantiate();
            if (bpmnElement instanceof Activity) {
              Activity activity = (Activity) bpmnElement;
              // read the fields
              activity.readBpmn(this);
              scope.activity(activity);
              setUnparsedBpmn(activity, currentXml);
              activity.cleanUnparsedBpmn();
              // Remove the activity XML element as it has been parsed in the model.
              iterator.remove();
            }
          }
        }

        endElement();
      }
      currentXml.removeEmptyElement(BPMN_URI, "extensionElements");
    }
  }

  /**
   * Check if the XML element can be parsed as one of the activity types.
   */
  protected BpmnTypeMapping getBpmnTypeMapping() {
    return bpmnMappings.getBpmnTypeMapping(currentXml, this);
  }

  protected void setUnparsedBpmn(Scope scope, XmlElement unparsedBpmn) {
    unparsedBpmn.name = null;
    scope.setBpmn(unparsedBpmn);
  }

  @Override
  public List<XmlElement> readElementsBpmn(String localPart) {
    if (currentXml==null) {
      return Collections.EMPTY_LIST;
    }
    return currentXml.removeElements(BPMN_URI, localPart);
  }

  @Override
  public List<XmlElement> readElementsEffektif(Class modelClass) {
    BpmnTypeMapping bpmnTypeMapping = bpmnMappings.getBpmnTypeMapping(modelClass);
    String localPart = bpmnTypeMapping.getBpmnElementName();
    return readElementsEffektif(localPart);
  }

  @Override
  public List<XmlElement> readElementsEffektif(String localPart) {
    if (currentXml==null) {
      return Collections.EMPTY_LIST;
    }
    return currentXml.removeElements(EFFEKTIF_URI, localPart);
  }

  @Override
  public XmlElement readElementEffektif(String localPart) {
    if (currentXml==null) {
      return null;
    }
    List<XmlElement> xmlElements = currentXml.removeElements(EFFEKTIF_URI, localPart);
    return !xmlElements.isEmpty() ? xmlElements.get(0) : null;
  }


  @Override
  public void startElement(XmlElement xmlElement) {
    if (currentXml!=null) {
      xmlStack.push(currentXml);
    }
    currentXml = xmlElement;
  }

  @Override
  public void endElement() {
    currentXml = xmlStack.empty() ? null : xmlStack.pop();
  }

  public void startScope(Scope scope) {
    if (this.scope!=null) {
      scopeStack.push(this.scope);
    }
    this.scope = scope;
  }

  public void endScope() {
    this.scope = scopeStack.pop();
  }

  @Override
  public void startExtensionElements() {
    XmlElement extensionsXmlElement = currentXml.getElement(BPMN_URI, "extensionElements");
    startElement(extensionsXmlElement);
  }

  @Override
  public void endExtensionElements() {
    endElement();
    currentXml.removeEmptyElement(BPMN_URI, "extensionElements");
  }

  @Override
  public Boolean readBooleanAttributeEffektif(String localPart) {
    if (currentXml==null) {
      return null;
    }
    String booleanStringValue = currentXml.removeAttribute(BPMN_URI, localPart);
    if (booleanStringValue==null) {
      return null;
    }
    return Boolean.valueOf(booleanStringValue);
  }

  @Override
  public String readStringAttributeBpmn(String localPart) {
    if (currentXml==null) {
      return null;
    }
    return currentXml.removeAttribute(BPMN_URI, localPart);
  }

  @Override
  public String readStringAttributeEffektif(String localPart) {
    if (currentXml==null) {
      return null;
    }
    return currentXml.removeAttribute(EFFEKTIF_URI, localPart);
  }

  @Override
  public <T extends Id> T readIdAttributeBpmn(String localPart, Class<T> idType) {
    if (currentXml==null) {
      return null;
    }
    return toId(readStringAttributeBpmn(localPart), idType);
  }

  @Override
  public <T extends Id> T readIdAttributeEffektif(String localPart, Class<T> idType) {
    if (currentXml==null) {
      return null;
    }
    return toId(readStringAttributeEffektif(localPart), idType);
  }
  
  @Override
  public Integer readIntegerAttributeEffektif(String localPart) {
    if (currentXml==null) {
      return null;
    }
    String valueString = readStringAttributeEffektif(localPart);
    try {
      return new Integer(valueString);
    } catch (NumberFormatException e) {
      return null;
    }
  }
  
  @Override
  public <T> Binding<T> readBinding(Class modelClass, Class<T> type) {
    BpmnTypeMapping bpmnTypeMapping = bpmnMappings.getBpmnTypeMapping(modelClass);
    String localPart = bpmnTypeMapping.getBpmnElementName();
    if (currentXml != null && currentXml.getName().equals(localPart)) {
      // in some cases the respective element which contains the binding information was already started
      return readBindingFromCurrentElement();
    }
    return readBinding(localPart, type);
  }

  /** Returns a binding from the first extension element with the given name. */
  @Override
  public <T> Binding<T> readBinding(String localPart, Class<T> type) {
    if (currentXml==null) {
      return null;
    }
    List<Binding<T>> bindings = readBindings(localPart);
    if (bindings.isEmpty()) {
      return new Binding<T>();
    } else {
      return bindings.get(0);
    }
  }

  /** Returns a list of bindings from the extension elements with the given name. */
  @Override
  public <T> List<Binding<T>> readBindings(String localPart) {
    if (currentXml==null) {
      return null;
    }
    List<Binding<T>> bindings = new ArrayList<>();
    for (XmlElement element: currentXml.removeElements(EFFEKTIF_URI, localPart)) {
      Binding binding = new Binding();
      String value = element.getAttribute(EFFEKTIF_URI, "value");
      String typeName = element.getAttribute(EFFEKTIF_URI, "type");
      startElement(element);
      XmlElement metadataElement = readElementEffektif("metadata");
      Map<String, Object> metadata = null;
      if (metadataElement != null) {
        startElement(metadataElement);
        metadata = readSimpleProperties();
        endElement();
      }
      endElement();
      DataType type = convertType(typeName);
      binding.setValue(parseText(value, (Class<Object>) type.getValueType()));
      binding.setExpression(element.getAttribute(EFFEKTIF_URI, "expression"));
      binding.setMetadata(metadata);
      bindings.add(binding);
    }
    return bindings;
  }

  private <T> Binding<T> readBindingFromCurrentElement() {
    if (currentXml != null) {
      Binding binding = new Binding();
      String value = currentXml.getAttribute(EFFEKTIF_URI, "value");
      String typeName = currentXml.getAttribute(EFFEKTIF_URI, "type");
      XmlElement metadataElement = readElementEffektif("metadata");
      Map<String, Object> metadata = null;
      if (metadataElement != null) {
        startElement(metadataElement);
        metadata = readSimpleProperties();
        endElement();
      }
      DataType type = convertType(typeName);
      binding.setValue(parseText(value, (Class<Object>) type.getValueType()));
      binding.setExpression(currentXml.getAttribute(EFFEKTIF_URI, "expression"));
      binding.setMetadata(metadata);
      return binding;
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  protected <T> T parseText(String value, Class<T> type) {
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
      return (T) toId(value, (Class<Id>) type);
    }
    if (type==LocalDateTime.class) {
      return (T) LocalDateTimeStreamMapper.PARSER.parseLocalDateTime(value);
    }
    if (type==Number.class) {
      return (T) Double.valueOf(value);
    }

    // Use a registered JSON type mapper to parse the value.
    JsonObjectReader jsonReader = new JsonObjectReader(bpmnMappings);
    JsonTypeMapper typeMapper = bpmnMappings.getTypeMapper(type);
    return (T) typeMapper.read(value, jsonReader);
  }

  /**
   * Returns an ID type instance, constructed from the given JSON string ID.
   */
  private static final Class< ? >[] ID_CONSTRUCTOR_PARAMETERS = new Class< ? >[] { String.class };
  public static <T extends Id> T toId(Object jsonId, Class<T> idType) {
    if (jsonId==null) {
      return null;
    }
    try {
      jsonId = jsonId.toString();
      Constructor<T> c = idType.getDeclaredConstructor(ID_CONSTRUCTOR_PARAMETERS);
      return (T) c.newInstance(new Object[] { jsonId });
    } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException
            | InvocationTargetException e) {
      throw new RuntimeException(e);
    }
  }

  /** Returns the contents of the BPMN <code>documentation</code> element. */
  @Override
  public String readDocumentation() {
    if (currentXml==null) {
      return null;
    }
    XmlElement documentationElement = currentXml.removeElement(BPMN_URI, "documentation");
    if (documentationElement!=null) {
      return documentationElement.getText();
    }
    return null;
  }

  @Override
  public Trigger readTriggerEffektif() {
    try {
      PolymorphicMapping triggerMapping = bpmnMappings.getPolymorphicMapping(Trigger.class);
      TypeMapping triggerSubclassMapping = triggerMapping.getTypeMapping(this);
      Trigger type = (Trigger) triggerSubclassMapping.instantiate();
      type.readBpmn(this);
      return type;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public <T extends BpmnReadable> T readPolymorphicEffektif(XmlElement xmlElement, Class<T> type) {
    try {
      startElement(xmlElement);
      PolymorphicMapping polymorphicMapping = bpmnMappings.getPolymorphicMapping(type);
      TypeMapping subclassMapping = polymorphicMapping.getTypeMapping(this);
      T object = (T) subclassMapping.instantiate();
      object.readBpmn(this);
      endElement();
      return object;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public DataType readTypeAttributeEffektif() {
    return readTypeAttributeEffektif("type");
  }

  private DataType readTypeAttributeEffektif(String attributeName) {
    String typeName = readStringAttributeEffektif(attributeName);
    return convertType(typeName);
  }

  @Override
  public LocalDateTime readDateAttributeEffektif(String attributeName) {
    String dateStringName = readStringAttributeEffektif(attributeName);
    return dateStringName!=null ? LocalDateTimeStreamMapper.PARSER.parseLocalDateTime(dateStringName) : null;
  }

  private DataType convertType(String typeName) {
    if (typeName == null) {
      typeName = "text";
    }
    PolymorphicMapping dataTypeMapping = bpmnMappings.getPolymorphicMapping(DataType.class);
    DataType type = (DataType) dataTypeMapping.getTypeMapping(typeName).instantiate();
    type.readBpmn(this);
    return type;
  }

  @Override
  public DataType readTypeElementEffektif() {
    XmlElement typeElement = readElementEffektif("type");
    DataType type = null;
    if (typeElement!=null) {
      startElement(typeElement);
      type = readTypeAttributeEffektif("name");
      endElement();
    }
    return type;
  }

  @Override
  public RelativeTime readRelativeTimeEffektif(String localPart) {
    RelativeTime relativeTime = null;
    XmlElement element = currentXml != null ? currentXml.removeElement(EFFEKTIF_URI, localPart) : null;
    if (element != null) {
      startElement(element);
      relativeTime = RelativeTime.readBpmnPolymorphic(this);
      endElement();
    }
    return relativeTime;
  }

  @Override
  public LocalDateTime readDateValue(String localPart) {
    XmlElement element = currentXml != null ? currentXml.removeElement(EFFEKTIF_URI, localPart) : null;
    if (element != null) {
      String value = element.getAttribute(EFFEKTIF_URI, "value");
      if (value != null) {
        return LocalDateTimeStreamMapper.PARSER.parseLocalDateTime(value);
      }
    }
    return null;
  }

  /**
   * Reads nested property elements: currently only String, Boolean, Integer and Double properties are supported.
   */
  @Override
  public Map<String,Object> readSimpleProperties() {
    Map<String,Object> properties = new HashMap<>();
    for (XmlElement element : readElementsEffektif("property")) {
      startElement(element);
      String key = readStringAttributeEffektif("key");
      String value = readStringAttributeEffektif("value");
      String type = readStringAttributeEffektif("type");

      if (key != null && value != null && type != null) {
        try {
          if (String.class.getName().equals(type)) {
            properties.put(key, value.toString());
          }
          else if (Boolean.class.getName().equals(type)) {
            properties.put(key, Boolean.valueOf(value));
          }
          else if (Integer.class.getName().equals(type)) {
            properties.put(key, Integer.valueOf(value));
          }
          else if (Double.class.getName().equals(type)) {
            properties.put(key, Double.valueOf(value));
          }
          else {
            log.warn(String.format("Unsupported property type ‘%s’ for property %s=%s", type, key, value));
          }
        } catch (NumberFormatException e) {
          log.warn(String.format("Unsupported value format for type ‘%s’ for property %s=%s", type, key, value));
        }
      }

      endElement();
    }
    return properties;
  }

  @Override
  public String readStringValue(String localPart) {
    XmlElement element = currentXml != null ? currentXml.removeElement(EFFEKTIF_URI, localPart) : null;
    if (element != null) {
      return element.getAttribute(EFFEKTIF_URI, "value");
    }
    return null;
  }

  @Override
  public String readTextBpmn(String localPart) {
    return readText(BPMN_URI, localPart);
  }

  @Override
  public String readTextEffektif(String localPart) {
    return readText(EFFEKTIF_URI, localPart);
  }

  private String readText(String namespaceUri, String localPart) {
    XmlElement textElement = currentXml!=null ? currentXml.removeElement(namespaceUri, localPart) : null;
    if (textElement!=null) {
      return textElement.getText();
    }
    return null;
  }

  @Override
  public XmlElement getUnparsedXml() {
    return currentXml;
  }

  public Condition readCondition() {
    List<Condition> conditions = readConditions();
    if (conditions.size() > 0) {
      return conditions.get(0);
    }
    return null;
  }

  /**
   * Returns a list of {@link Condition} instances by using this reader to read BPMN for all of the condition types.
   */
  @Override
  public List<Condition> readConditions() {
    List<Condition> conditions = new ArrayList<>();

    SortedSet<Class<?>> bpmnClasses = bpmnMappings.getBpmnClasses();

    for (Class bpmnClass : bpmnClasses) {
      if (Condition.class.isAssignableFrom(bpmnClass)) {
        for (XmlElement xmlElement : readElementsEffektif(bpmnClass)) {
          startElement(xmlElement);
          try {
            Condition condition = (Condition) bpmnClass.newInstance();
            condition.readBpmn(this);
            if (!condition.isEmpty()) {
              conditions.add(condition);
            }
          } catch (Exception e) {
            throw new RuntimeException("Could not read condition type " + bpmnClass.getName());
          }
          endElement();
        }

      }
    }
    return conditions;
  }

  /**
   * Removes transitions to or from a missing activity, probably due to the activity not being imported.
   */
  private void removeDanglingTransitions(AbstractWorkflow workflow) {
    if (workflow.getTransitions() == null || workflow.getTransitions().isEmpty()) {
      return;
    }

    Set<String> activityIds = new HashSet<>();
    for (Activity activity : workflow.getActivities()) {
      activityIds.add(activity.getId());

      // Transitions from Boundary event timers should be included as well
      // todo: make generic
      List<Timer> activityTimers = activity.getTimers();

      if (activityTimers != null) {
        for (Timer timer : activityTimers) {
          if (timer instanceof BoundaryEventTimer) {
            BoundaryEvent boundaryEvent = ((BoundaryEventTimer) timer).boundaryEvent;

            activityIds.add(boundaryEvent.getBoundaryId());
            activityIds.addAll(boundaryEvent.getToTransitionIds());
          }
        }
      }
    }

    ListIterator<Transition> transitionIterator = workflow.getTransitions().listIterator();
    while(transitionIterator.hasNext()){
      Transition transition = transitionIterator.next();
      if (!activityIds.contains(transition.getFromId()) || !activityIds.contains(transition.getToId())) {
        transitionIterator.remove();
      }
    }
  }

  /**
   * Reads the workflow name, description and diagram from BPMN.
   */
  private void readDiagram(AbstractWorkflow workflow, XmlElement definitionsXml) {
    if (definitionsXml==null) {
      return;
    }

    for (XmlElement diagramElement: definitionsXml.removeElements(BPMN_DI_URI, "BPMNDiagram")) {
      startElement(diagramElement);
      if (currentXml==null) {
        return;
      }
      workflow.setName(currentXml.removeAttribute(BPMN_DI_URI, "name"));
      if (workflow.getDescription() == null) {
        workflow.setDescription(currentXml.removeAttribute(BPMN_DI_URI, "documentation"));
      }

      Diagram diagram = new Diagram();
      for (XmlElement planeElement: diagramElement.removeElements(BPMN_DI_URI, "BPMNPlane")) {
        List<Node> shapes = readShapes(planeElement);
        diagram.addNodes(shapes);
        if (workflow.getTransitions() != null) {
          diagram.edges(readEdges(shapes, workflow.getTransitions(), planeElement));
        }
      }

      // Reference the process we’re importing from the diagram, setting it directly because the BPMNPlane/@elementId
      // may refer to multiple processes via definitions/collaboration and its nested participants.
      if (workflow.getId() != null) {
        diagram.canvas.elementId = workflow.getId().getInternal();
      }

      workflow.setDiagram(diagram);
      removeOrphanedDiagramElements(workflow, definitionsXml);

      endElement();
    }
  }

  /**
   * Removes diagram shapes that don’t correspond to an imported workflow activity, such as those not supported.
   */
  private void removeOrphanedDiagramElements(AbstractWorkflow workflow, XmlElement definitionsXml) {
    Diagram diagram = workflow.getDiagram();
    if (diagram == null || !diagram.hasChildren()) {
      return;
    }

    // Collect valid participant (pool) IDs.
    Set<String> participantIds = findParticipantIds(definitionsXml);
    participantIds.forEach(id -> log.debug("POOL = " + id));

    // Collect valid activity IDs and variable IDs, which lane IDs are mapped to.

    Set<String> activityIds = workflow.getActivities() == null ? new HashSet<>() :
      workflow.getActivities().stream().map(activity -> activity.getId()).collect(Collectors.toSet());

    Set<String> variableIds = workflow.getVariables() == null ? new HashSet<>() :
      workflow.getVariables().stream().map(variable -> variable.getId()).collect(Collectors.toSet());

    // Remove orphaned shapes/nodes.
    Set<String> shapeIds = new HashSet<>();
    if (diagram.hasChildren()) {
      Iterator<Node> shapeIterator = diagram.canvas.children.iterator();
      while (shapeIterator.hasNext()) {
        Node shape = shapeIterator.next();
        // Keep shapes for lanes by checking against variable IDs, since lanes are the only shapes mapped to variables.
        boolean poolShape = participantIds.contains(shape.elementId);
        boolean laneShape = activityIds.contains(shape.elementId) || variableIds.contains(shape.elementId);
        if (!poolShape && !laneShape) {
          shapeIterator.remove();
        }
      }

      // Collect valid shape IDs.
      for (Node shape : diagram.canvas.children) {
        shapeIds.add(shape.id);
      }
    }

    // Remove orphaned edges.
    if (diagram.hasEdges()) {
      Iterator<Edge> edgeIterator = diagram.edges.iterator();
      while (edgeIterator.hasNext()) {
        Edge edge = edgeIterator.next();
        boolean transitionDefined = workflow.findTransition(edge.transitionId) != null;
        boolean edgeValid = shapeIds.contains(edge.fromId) && shapeIds.contains(edge.toId) && transitionDefined;
        if (!edgeValid) {
          edgeIterator.remove();
        }
      }
    }
  }

  private Set<String> findParticipantIds(XmlElement definitions) {
    Set<String> ids = definitions == null ? new HashSet<>() :
      definitions.elements.stream()
        .filter(element -> element.name.equals("collaboration"))
        .flatMap(collaboration -> collaboration.elements.stream())
        .filter(element -> element.name.equals("participant"))
        .map(participant -> participant.getAttribute(BPMN_URI, "id"))
        .collect(Collectors.toSet());
    return ids;
  }

  private List<Node> readShapes(XmlElement planeElement) {
    List<Node> nodes = new ArrayList<>();

    for (XmlElement shapeElement: planeElement.removeElements(BPMN_DI_URI, "BPMNShape")) {
      startElement(shapeElement);

      String id = currentXml.removeAttribute(BPMN_DI_URI, "id");
      String elementId = currentXml.removeAttribute(BPMN_DI_URI, "bpmnElement");

      Node node = new Node()
        .id(id)
        .elementId(elementId);

      // Read the optional BPMN attribute that indicates lane orientation.
      String horizontal = currentXml.removeAttribute(BPMN_DI_URI, "isHorizontal");
      if (horizontal != null) {
        node.horizontal(horizontal.equals("true"));
      }
      String expanded = currentXml.removeAttribute(BPMN_DI_URI, "isExpanded");
      if (expanded != null) {
        node.expanded(expanded.equals("true"));
      }

      for (XmlElement boundsElement: shapeElement.removeElements(OMG_DC_URI, "Bounds")) {
        startElement(boundsElement);

        double x = Double.valueOf(currentXml.removeAttribute(OMG_DC_URI, "x"));
        double y = Double.valueOf(currentXml.removeAttribute(OMG_DC_URI, "y"));
        double width = Double.valueOf(currentXml.removeAttribute(OMG_DC_URI, "width"));
        double height = Double.valueOf(currentXml.removeAttribute(OMG_DC_URI, "height"));
        node.bounds(new Bounds(new Point(x, y), width, height));

        nodes.add(node);

        endElement();
      }

      endElement();
    }

    return nodes;
  }

  /**
   * Returns a list of edges read from sequenceFlow element transitions, and BPMNEdge coordinates.
   */
  private List<Edge> readEdges(List<Node> shapes, List<Transition> transitions, XmlElement planeElement) {

    Map<String, Edge> edgesBySequenceFlowId = readEdgesBySequenceFlowId(planeElement);

    // Map shape activity IDs to shape IDs, which are needed for edge from/to IDs.
    Map<String,String> nodeIdByActivityId = new HashMap<>();
    for (Node shape : shapes) {
      nodeIdByActivityId.put(shape.elementId, shape.id);
    }

    // Add node IDs from the previously-parsed workflow transitions and diagram nodes.
    List<Edge> edges = new ArrayList<>();
    for (Transition transition : transitions) {
      String sequenceFlowId = transition.getId();
      Edge edge = edgesBySequenceFlowId.get(sequenceFlowId);
      if (edge==null) {
        BadRequestException.checkNotNull(edge, "No edge for sequenceFlow " + sequenceFlowId);
      }
      edge.fromId(nodeIdByActivityId.get(transition.getFromId()));
      edge.toId(nodeIdByActivityId.get(transition.getToId()));
      edges.add(edge);
    }
    return edges;
  }

  private Map<String, Edge> readEdgesBySequenceFlowId(XmlElement planeElement) {
    Map<String, Edge> edges = new HashMap<>();
    for (XmlElement edgeElement: planeElement.removeElements(BPMN_DI_URI, "BPMNEdge")) {
      startElement(edgeElement);
      List<Point> edgeWaypoints = new ArrayList<>();
      for (XmlElement pointElement: edgeElement.removeElements(OMG_DI_URI, "waypoint")) {
        startElement(pointElement);
        double x = Double.valueOf(currentXml.removeAttribute(OMG_DI_URI, "x"));
        double y = Double.valueOf(currentXml.removeAttribute(OMG_DI_URI, "y"));
        edgeWaypoints.add(new Point(x, y));
        endElement();
      }

      String id = currentXml.removeAttribute(BPMN_DI_URI, "id");
      String sequenceFlowId = currentXml.removeAttribute(BPMN_DI_URI, "bpmnElement");
      Edge edge = new Edge().id(id).transitionId(sequenceFlowId).dockers(edgeWaypoints);

      edges.put(sequenceFlowId, edge);
      endElement();
    }
    return edges;
  }
}
