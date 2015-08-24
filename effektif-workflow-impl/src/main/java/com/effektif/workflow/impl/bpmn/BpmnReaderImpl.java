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

import com.effektif.workflow.api.workflow.AbstractWorkflow;
import com.effektif.workflow.api.workflow.diagram.Bounds;
import com.effektif.workflow.api.workflow.diagram.Edge;
import com.effektif.workflow.api.workflow.diagram.Node;
import com.effektif.workflow.api.workflow.diagram.Point;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.effektif.workflow.api.bpmn.BpmnReader;
import com.effektif.workflow.api.bpmn.XmlElement;
import com.effektif.workflow.api.condition.Condition;
import com.effektif.workflow.api.model.Id;
import com.effektif.workflow.api.model.RelativeTime;
import com.effektif.workflow.api.types.DataType;
import com.effektif.workflow.api.workflow.Activity;
import com.effektif.workflow.api.workflow.Binding;
import com.effektif.workflow.api.workflow.ExecutableWorkflow;
import com.effektif.workflow.api.workflow.Scope;
import com.effektif.workflow.api.workflow.Transition;
import com.effektif.workflow.api.workflow.Trigger;
import com.effektif.workflow.api.workflow.diagram.Diagram;
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
  
  /** maps uri's to prefixes.
   * Ideally this should be done in a stack so that each element can add new namespaces.
   * The addPrefixes() should then be refactored to pushPrefixes and popPrefixes.
   * The current implementation assumes that all namespaces are defined in the root element */
  protected Map<String,String> prefixes = new HashMap<>();
  
  protected JsonStreamMapper jsonStreamMapper;

  public BpmnReaderImpl(BpmnMappings bpmnMappings, JsonStreamMapper jsonStreamMapper) {
    this.bpmnMappings = bpmnMappings;
    this.jsonStreamMapper = jsonStreamMapper;
  }
  
  protected AbstractWorkflow readDefinitions(XmlElement definitionsXml) {
    AbstractWorkflow workflow = null;

    // see #prefixes for more details about the limitations of namespaces
    initializeNamespacePrefixes(definitionsXml);

    if (definitionsXml.elements != null) {
      Iterator<XmlElement> iterator = definitionsXml.elements.iterator();
      while (iterator.hasNext()) {
        XmlElement definitionElement = iterator.next();
        if (definitionElement.is(BPMN_URI, "process") && workflow == null) {
          iterator.remove();
          workflow = readWorkflow(definitionElement);
        }
      }
    }

    if (workflow != null) {
      readDiagram(workflow, definitionsXml);
      // Clear the namespaces, because the dots in the URL keys break serialisation.
      definitionsXml.namespaces = null;
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

  protected AbstractWorkflow readWorkflow(XmlElement processXml) {
    AbstractWorkflow workflow = new ExecutableWorkflow();
    this.currentXml = processXml;
    this.scope = workflow;
    workflow.readBpmn(this);
    removeDanglingTransitions(workflow);
    setUnparsedBpmn(workflow, processXml);
    return workflow;
  }

  public void readScope() {
    if (currentXml.elements!=null) {
      Iterator<XmlElement> iterator = currentXml.elements.iterator();
      while (iterator.hasNext()) {
        XmlElement scopeElement = iterator.next();
        startElement(scopeElement);

        if (scopeElement.is(BPMN_URI, "sequenceFlow")) {
          Transition transition = new Transition();
          transition.readBpmn(this);
          scope.transition(transition);
          // Remove the sequenceFlow as it has been parsed in the model.
          iterator.remove();
        }
        else {
          // Check if the XML element can be parsed as one of the activity types.
          BpmnTypeMapping bpmnTypeMapping = bpmnMappings.getBpmnTypeMapping(currentXml, this);
          if (bpmnTypeMapping != null) {
            Activity activity = (Activity) bpmnTypeMapping.instantiate();
            // read the fields
            activity.readBpmn(this);
            scope.activity(activity);
            setUnparsedBpmn(activity, currentXml);
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
    currentXml = xmlStack.pop();
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
  
//  @Override
//  public String getQNameBpmn(String localPart) {
//    return getQName(BPMN_URI, localPart);
//  }
//  
//  @Override
//  public String getQNameEffektif(String localPart) {
//    return getQName(EFFEKTIF_URI, localPart);
//  }
//  
//  public String getQName(String namespaceUri, String localName) {
//    String prefix = prefixes.get(namespaceUri);
//    return "".equals(prefix) ? localName : prefix + ":" + localName;
//  }
  
  @Override
  public void startExtensionElements() {
    XmlElement extensionsXmlElement = currentXml.getElement(BPMN_URI, "extensionElements");
    startElement(extensionsXmlElement);
  }

  @Override
  public void endExtensionElements() {
    endElement();
  }
  
  @Override
  public Boolean readBooleanAttributeEffektif(String localPart) {
    if (currentXml==null) {
      return null;
    }
    return Boolean.valueOf(currentXml.removeAttribute(BPMN_URI, localPart));
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
  public <T> Binding<T> readBinding(Class modelClass, Class<T> type) {
    BpmnTypeMapping bpmnTypeMapping = bpmnMappings.getBpmnTypeMapping(modelClass);
    String localPart = bpmnTypeMapping.getBpmnElementName();
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
      DataType type = convertType(typeName);
      binding.setValue(parseText(value, (Class<Object>) type.getValueType()));
      binding.setExpression(element.getAttribute(EFFEKTIF_URI, "expression"));
      bindings.add(binding);
    }
    return bindings;
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



//  @Override
//  public AccessIdentity readAccessIdentity() {
//    try {
//      Class<AccessIdentity> identityClass = mappings.getConcreteClass(this, AccessIdentity.class);
//      AccessIdentity identity = identityClass.newInstance();
//      identity.readBpmn(this);
//      return identity;
//    } catch (Exception e) {
//      throw new RuntimeException(e);
//    }
//  }


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
  public DataType readTypeAttributeEffektif() {
    return readTypeAttributeEffektif("type");
  }

  private DataType readTypeAttributeEffektif(String attributeName) {
    String typeName = readStringAttributeEffektif(attributeName);
    return convertType(typeName);
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
    XmlElement element = currentXml != null ? currentXml.removeElement(EFFEKTIF_URI, localPart) : null;
    if (element != null) {
      String value = element.getAttribute(EFFEKTIF_URI, "after");
      if (value != null) {
        return RelativeTime.parse(value);
      }
    }
    return null;
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
        try {
          Condition condition = (Condition) bpmnClass.newInstance();
          condition.readBpmn(this);
          if (!condition.isEmpty()) {
            conditions.add(condition);
          }
        } catch (Exception e) {
          throw new RuntimeException("Could not read condition type " + bpmnClass.getName());
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

    // TODO use workflow.getTransitions()

    for (XmlElement diagramElement: definitionsXml.removeElements(BPMN_DI_URI, "BPMNDiagram")) {
      startElement(diagramElement);
      if (currentXml==null) {
        return;
      }
      workflow.setName(currentXml.removeAttribute(BPMN_DI_URI, "name"));
      workflow.setDescription(currentXml.removeAttribute(BPMN_DI_URI, "documentation"));

      Diagram diagram = new Diagram();
      for (XmlElement planeElement: diagramElement.removeElements(BPMN_DI_URI, "BPMNPlane")) {
        diagram.edges(readEdges(workflow.getTransitions(), planeElement));
        diagram.addNodes(readShapes(planeElement));
      }
      workflow.setDiagram(diagram);

      endElement();
    }
  }

  private List<Node> readShapes(XmlElement planeElement) {
    List<Node> nodes = new ArrayList<>();

    for (XmlElement shapeElement: planeElement.removeElements(BPMN_DI_URI, "BPMNShape")) {
      startElement(shapeElement);

      String id = currentXml.removeAttribute(BPMN_DI_URI, "id");
      String elementId = currentXml.removeAttribute(BPMN_DI_URI, "bpmnElement");
      Node node = new Node().id(id).elementId(elementId);

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
  private List<Edge> readEdges(List<Transition> transitions, XmlElement planeElement) {

    Map<String, Edge> edgesBySequenceFlowId = readEdgesBySequenceFlowId(planeElement);

    // Add activity IDs from the previously-parsed workflow transitions.
    List<Edge> edges = new ArrayList<>();
    for (Transition transition : transitions) {
      String sequenceFlowId = transition.getId();
      Edge edge = edgesBySequenceFlowId.get(sequenceFlowId)
        .fromId(transition.getFromId())
        .toId(transition.getToId());
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

  //  @Override
//  public AccessIdentity readAccessIdentity() {
//    return null;
//  }

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
