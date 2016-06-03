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

import com.effektif.workflow.api.bpmn.BpmnElement;
import com.effektif.workflow.api.bpmn.BpmnWriter;
import com.effektif.workflow.api.bpmn.XmlElement;
import com.effektif.workflow.api.model.Id;
import com.effektif.workflow.api.model.RelativeTime;
import com.effektif.workflow.api.types.DataType;
import com.effektif.workflow.api.workflow.*;
import com.effektif.workflow.api.workflow.diagram.*;
import com.effektif.workflow.impl.json.Mappings;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.util.*;
import java.util.stream.Collectors;

import static com.effektif.workflow.impl.bpmn.Bpmn.*;


/**
 * @author Tom Baeyens
 */
public class BpmnWriterImpl implements BpmnWriter {

  private static final double EXPORT_MARGIN = 50d;

  public static DateTimeFormatter DATE_FORMAT = ISODateTimeFormat.dateTime();

  protected BpmnMappings bpmnMappings;
  protected String bpmnPrefix;
  protected String bpmnDiagramPrefix;
  protected String effektifPrefix;
  /** how much the diagram needs to be transposed in the x direction to end up in the top left corner */
  protected double xOffset;
  /** how much the diagram needs to be transposed in the y direction to end up in the top left corner */
  protected double yOffset;

  /** stack of the current scopes */
  protected Stack<Scope> scopeStack = new Stack();
  /** current scope (==scopeStack.peek()) */
  protected Scope scope;
  
  /** stack of the current xml elements */
  protected Stack<XmlElement> xmlStack = new Stack();
  /** current xml element */
  protected XmlElement xml;
  
  public BpmnWriterImpl(BpmnMappings bpmnMappings) {
    this.bpmnMappings = bpmnMappings;
  }

  protected void startElementBpmn(String localpart, Object source) {
    startElementBpmn(localpart, source, null);
  }

  protected void startElementBpmn(String localpart, Object source, Integer index) {
    if (source==null) {
      startElementBpmn(localpart, index);
    } else if (source instanceof XmlElement) {
      XmlElement sourceElement = (XmlElement) source;
      sourceElement.setElementParents();
      if (xml!=null) {
        xml.addElement(sourceElement, index);
      }
      sourceElement.setName(BPMN_URI, localpart);
      startElement(sourceElement);
    } else {
      throw new RuntimeException("Unknown BPMN source: "+source);
    }
  }

  @Override
  public void startElementBpmn(String localPart) {
    startElementBpmn(localPart, null);
  }

  @Override
  public void startElementBpmn(String localPart, Integer index) {
    XmlElement newXmlElement = null;
    if (xml!=null) {
      newXmlElement = xml.createElement(BPMN_URI, localPart, index);
    } else {
      newXmlElement = new XmlElement();
      newXmlElement.setName(BPMN_URI, localPart);
    }
    startElement(newXmlElement);
  }

  @Override
  public void startElementBpmnDiagram(String localPart) {
    startElement(xml.createElement(BPMN_DI_URI, localPart));
  }

  @Override
  public void startElementEffektif(String localPart) {
    startElementEffektif(localPart, null);
  }
  
  @Override
  public void startElementEffektif(Class modelClass) {
    BpmnTypeMapping bpmnTypeMapping = bpmnMappings.getBpmnTypeMapping(modelClass);
    String localPart = bpmnTypeMapping.getBpmnElementName();
    startElementEffektif(localPart, null);
  }

  @Override
  public void startElementEffektif(String localPart, Integer index) {
    startElement(xml.createElement(EFFEKTIF_URI, localPart, index));
  }

  public void startOrGetElement(String namespaceUri, String localPart, Integer index) {
    startElement(xml.getOrCreateChildElement(namespaceUri, localPart, index));
  }

  protected void startElement(XmlElement nestedXml) {
    if (xml!=null) {
      xmlStack.push(xml);
    }
    xml = nestedXml;
  }
  
  @Override
  public void endElement() {
    xml = xmlStack.pop();
  }

  @Override
  public void startExtensionElements() {
    // Set the extensionElements insertion index to first or second child element.
    boolean xmlHasDocumentation = xml.getElement(BPMN_URI, "documentation") != null;
    int extensionElementsIndex = xmlHasDocumentation ? 1 : 0;

    // start or get is used as extensionElements might be added
    // multiple times by different levels in the class hierarchy
    // eg: a call activity might add stuff and it s super class activity might
    //     also add extensionElements
    startOrGetElement(BPMN_URI, "extensionElements", extensionElementsIndex);
  }

  @Override
  public void endExtensionElements() {
    endElement();
    xml.removeEmptyElement(BPMN_URI, "extensionElements");
  }
  
  public void startScope(Scope nestedScope) {
    if (this.scope!=null) {
      scopeStack.push(this.scope);
    }
    this.scope = nestedScope;
  }
  
  public void endScope() {
    this.scope = scopeStack.pop();
  }

  protected void initializeNamespacePrefixes() {
    if (xml.namespaces != null) {
      bpmnPrefix = xml.namespaces.getPrefix(BPMN_URI);
      effektifPrefix = xml.namespaces.getPrefix(EFFEKTIF_URI);
    }

    // Add any missing namespaces with their default prefixes.
    if (bpmnPrefix == null) {
      bpmnPrefix = "";
      xml.addNamespace(BPMN_URI, bpmnPrefix);
    }
    if (bpmnDiagramPrefix == null) {
      bpmnDiagramPrefix = "bpmndi";
      xml.addNamespace(BPMN_DI_URI, bpmnDiagramPrefix);
      xml.addNamespace(OMG_DC_URI, "omgdc");
      xml.addNamespace(OMG_DI_URI, "omgdi");
    }
    if (effektifPrefix == null) {
      effektifPrefix = "e";
      xml.addNamespace(EFFEKTIF_URI, effektifPrefix);
    }
  }
  
  protected XmlElement writeDefinitions(AbstractWorkflow workflow) {
    startElementBpmn("definitions", workflow.getProperty(KEY_DEFINITIONS));
    initializeNamespacePrefixes();

    // Add the BPMN targetNamespace, which has nothing to do with XML schema and just identifies the modelled process.
    if (xml.getAttribute(BPMN_URI, "targetNamespace") == null) {
      xml.addAttribute(BPMN_URI, "targetNamespace", EFFEKTIF_URI);
    }

    writeWorkflow(workflow);
    return xml;
  }

  protected void writeWorkflow(AbstractWorkflow workflow) {
    startScope(workflow);
    // Add the ‘process’ element as the first child element of the ‘definitions’ element.
    startElementBpmn("process", workflow.getBpmn(), 0);

    if (ExecutableWorkflow.class.isAssignableFrom(workflow.getClass())) {
      ExecutableWorkflow executableWorkflow = (ExecutableWorkflow) workflow;
      if (executableWorkflow.getSourceWorkflowId() == null && workflow.getId() != null) {
        executableWorkflow.setSourceWorkflowId(workflow.getId().getInternal());
      }
    }

    // Output documentation, workflow BPMN (extension elements) and scope (activities/transitions) in that order, as
    // required by the BPMN schema.
    writeDocumentation(workflow.getDescription());
    workflow.writeBpmn(this);
    writeScope();
    endElement();
    fixDiagramDuplicateIds(workflow);
    writeDiagram(workflow);
  }

    /**
     * Temporary #2932 fix - ensures that the export doesn’t generate XML that contains duplicate IDs (not well-formed).
     * The fix is to preprend duplicate shape IDs
     */
  private void fixDiagramDuplicateIds(AbstractWorkflow workflow) {
    Diagram diagram = workflow.getDiagram();
    if (diagram != null) {
      // Replace shape IDs that duplicate activity IDs.
      Set<String> activityIds = workflow.getActivities() == null ? Collections.emptySet() :
        workflow.getActivities().stream().map(activity -> activity.getId()).collect(Collectors.toSet());

      if (diagram.canvas.hasChildren()) {
        diagram.canvas.children.stream()
          .filter(shape -> activityIds.contains(shape.id))
          .forEach(shape -> shape.id("shape-" + shape.id));
      }

      if (diagram.edges != null) {
        diagram.edges.stream()
          .filter(edge -> activityIds.contains(edge.fromId))
          .forEach(edge -> edge.fromId("shape-" + edge.fromId));

        diagram.edges.stream()
          .filter(edge -> activityIds.contains(edge.toId))
          .forEach(edge -> edge.toId("shape-" + edge.toId));

        // Replace edge IDs that duplicate transition IDs.
        Set<String> transitionIds = workflow.getTransitions() == null ? Collections.emptySet() :
          workflow.getTransitions().stream().map(transition -> transition.getId()).collect(Collectors.toSet());
        diagram.edges.stream()
          .filter(edge -> transitionIds.contains(edge.id))
          .forEach(edge -> edge.id("edge-" + edge.id));
      }
    }
  }

  private void writeDiagram(AbstractWorkflow workflow) {
    Diagram diagram = workflow.getDiagram();
    if (diagram != null) {
      calculateOffsets(diagram);


      startElementBpmnDiagram("BPMNDiagram");
      writeIdAttributeBpmnDiagram("id", diagram.id);
      writeStringAttributeBpmnDiagram("name", workflow.getName());

      startElementBpmnDiagram("BPMNPlane");
      writeIdAttributeBpmnDiagram("bpmnElement", workflow.getId().getInternal());

      if (diagram.canvas.hasChildren()) {
        for (Node shape : diagram.canvas.children) {
          startElementBpmnDiagram("BPMNShape");
          writeIdAttributeBpmnDiagram("id", shape.id);
          writeIdAttributeBpmnDiagram("bpmnElement", shape.elementId);
          if (shape.horizontal != null) {
            writeStringAttributeBpmnDiagram("isHorizontal", shape.horizontal ? "true" : "false");
          }
          if (shape.expanded != null) {
            writeStringAttributeBpmnDiagram("isExpanded", shape.expanded ? "true" : "false");
          }
          writeBpmnDiagramBounds(shape.bounds);
          endElement();
        }
      }

      if (diagram.hasEdges()) {
        for (Edge edge : diagram.edges) {
          startElementBpmnDiagram("BPMNEdge");
          writeIdAttributeBpmnDiagram("id", edge.id);
          writeIdAttributeBpmnDiagram("bpmnElement", edge.transitionId);
          writeBpmnDiagramEdgeDockers(edge.dockers);
          endElement();
        }
      }

      endElement();
      endElement();
    }
  }

  protected void calculateOffsets(Diagram diagram) {
    xOffset = Integer.MAX_VALUE;
    yOffset = Integer.MAX_VALUE;
    if (diagram!=null && diagram.canvas!=null && diagram.canvas.children!=null) {
      for (Node child: diagram.canvas.children) {
        scanOffset(child);
      }
    }
    if (diagram!=null && diagram.edges!=null) {
      for (Edge edge: diagram.edges) {
        scanOffset(edge);
      }
    }
    if (xOffset==Integer.MAX_VALUE) {
      xOffset = 0;
    } else {
      xOffset -= EXPORT_MARGIN;
    }
    if (yOffset==Integer.MAX_VALUE) {
      yOffset = 0;
    } else {
      yOffset -= EXPORT_MARGIN;
    }
  }

  protected void scanOffset(Node node) {
    if (node!=null
        && node.bounds!=null
        && node.bounds.upperLeft!=null) {
      Point upperLeft = node.bounds.upperLeft;
      xOffset = Math.min(xOffset, upperLeft.x);
      yOffset = Math.min(yOffset, upperLeft.y);
    }
    if (node.children!=null) {
      for (Node child: node.children) {
        scanOffset(child);
      }
    }
  }

  protected void scanOffset(Edge edge) {
    if (edge.dockers!=null) {
      for (Point docker: edge.dockers) {
        xOffset = Math.min(xOffset, docker.x);
        yOffset = Math.min(yOffset, docker.y);
      }
    }
  }


  /**
   * Writes a {@link Scope} as BPMN, which is implemented here instead of in {@link Scope#writeBpmn(BpmnWriter)}
   * because it requires access to {@link #bpmnMappings}.
   */
  public void writeScope() {
    writeActivities(scope.getActivities());
    writeTransitions(scope.getTransitions());
  }

  protected void writeActivities(List<Activity> activities) {
    if (activities!=null) {
      for (Activity activity : activities) {
        startScope(activity);
        BpmnTypeMapping bpmnTypeMapping = getBpmnTypeMapping(activity.getClass());
        startElementBpmn(bpmnTypeMapping.getBpmnElementName(), activity.getBpmn());
        Map<String, String> bpmnTypeAttributes = bpmnTypeMapping.getBpmnTypeAttributes();
        if (bpmnTypeAttributes!=null) {
          for (String attributeLocalPart: bpmnTypeAttributes.keySet()) {
            String value = bpmnTypeAttributes.get(attributeLocalPart);
            xml.addAttribute(EFFEKTIF_URI, attributeLocalPart, value);
          }
        }
        activity.writeBpmn(this);
        endElement();
        endScope();
      }
    }
  }

  private BpmnTypeMapping getBpmnTypeMapping(Class<? extends Activity> activityClass) {
    BpmnTypeMapping bpmnTypeMapping = bpmnMappings.getBpmnTypeMapping(activityClass);
    if (bpmnTypeMapping == null) {
      throw new RuntimeException("Register " + activityClass + " in class " + Mappings.class.getName() +
        " with method registerSubClass and ensure annotation " + BpmnElement.class + " is set");
    }
    return bpmnTypeMapping;
  }

  protected void writeTransitions(List<Transition> transitions) {
    if (transitions!=null) {
      transitions.stream().filter(transition -> transition.valid()).forEach(transition -> {
        startElementBpmn("sequenceFlow", transition.getBpmn());
        transition.writeBpmn(this);
        endElement();
      });
    }
  }

  /** Writes binding values as extension elements with the given local name and attribute name,
   * e.g. <e:assignee value="42"/> or <e:assignee expression="v1.fullName"/>. */
  @Override
  public <T> void writeBinding(Class modelClass, Binding<T> binding) {
    BpmnTypeMapping bpmnTypeMapping = bpmnMappings.getBpmnTypeMapping(modelClass);
    String localPart = bpmnTypeMapping.getBpmnElementName();
    writeBinding(localPart, binding);
  }

  /** Writes binding values as extension elements with the given local name and attribute name,
   * e.g. <e:assignee value="42"/> or <e:assignee expression="v1.fullName"/>. */
  @Override
  public <T> void writeBinding(String localPart, Binding<T> binding) {
    writeBinding(localPart, binding, null);
  }

  /** Writes binding values as extension elements with the given local name and attribute name,
   * e.g. <e:assignee value="42"/> or <e:assignee expression="v1.fullName"/>. */
  @Override
  public <T> void writeBinding(String localPart, Binding<T> binding, String key) {
    if (binding!=null) {
      startElementEffektif(localPart);
      if (key != null) {
        writeStringAttributeEffektif("key", key);
      }
      T value = binding.getValue();
      if (value!=null) {
        writeStringAttributeEffektif("value", value);
        writeTypeAttribute(bpmnMappings.getTypeByValue(value));
      }
      if (binding.getExpression()!=null) {
        writeStringAttributeEffektif("expression", binding.getExpression());
      }
      if (binding.getMetadata() != null && !binding.getMetadata().isEmpty()) {
        startElementEffektif("metadata");
        writeSimpleProperties(binding.getMetadata());
        endElement();
      }
      endElement();
    }
  }
  
  @Override
  public <T> void writeBindings(String fieldName, List<Binding<T>> bindings) {
    if (bindings!=null) {
      for (Binding<T> binding: bindings) {
        writeBinding(fieldName, binding);
      }
    }
  }

  /** Writes the given documentation string as a BPMN <code>documentation</code> element. */
  @Override
  public void writeDocumentation(String documentation) {
    if (documentation != null && !documentation.isEmpty()) {
      // Set the insertion index to zero, because the BPMN spec requires this to be the first child element.
      startElementBpmn("documentation");
      xml.addText(documentation);
      endElement();
    }
  }

  /**
   * Serialises properties with simple Java types as String values.
   */
  @Override
  public void writeSimpleProperties(Map<String,Object> properties) {
    if (properties != null) {
      for (Map.Entry<String, Object> property : properties.entrySet()) {
        if (property.getValue() != null) {
          Class<?> type = property.getValue().getClass();
          boolean simpleType = type.isPrimitive() || type.getName().startsWith("java.lang.");
          if (simpleType) {
            startElementEffektif("property");
            writeStringAttributeEffektif("key", property.getKey());
            writeStringAttributeEffektif("value", property.getValue().toString());
            writeStringAttributeEffektif("type", property.getValue().getClass().getName());
            endElement();
          }
        }
      }
    }
  }

  @Override
  public void writeStringAttributeBpmn(String localPart, Object value) {
    if (value!=null) {
      xml.addAttribute(BPMN_URI, localPart, value);
    }
  }

  @Override
  public void writeIdAttributeBpmnDiagram(String localPart, Object id) {
    if (id != null) {
      xml.addAttribute(BPMN_DI_URI, localPart, normaliseXmlId(id.toString()));
    }
  }

  @Override
  public void writeStringAttributeBpmnDiagram(String localPart, Object value) {
    if (value!=null) {
      xml.addAttribute(BPMN_DI_URI, localPart, value);
    }
  }

  @Override
  public void writeStringAttributeEffektif(String localPart, Object value) {
    if (value!=null) {
      xml.addAttribute(EFFEKTIF_URI, localPart, value);
    }
  }

  @Override
  public void writeIdAttributeBpmn(String localPart, Id id) {
    if (id!=null) {
      writeIdAttributeBpmn(localPart, id.getInternal());
    }
  }

  @Override
  public void writeIdAttributeBpmn(String localPart, String id) {
    if (id != null && !id.isEmpty()) {
      xml.addAttribute(BPMN_URI, localPart, normaliseXmlId(id));
    }
  }

  /**
   * Fixes an XML id attribute by prefixing it with an underscore if it doesn’t start with a letter character, as
   * required by the XML Schema definition of NCName http://www.w3.org/TR/xmlschema-2/#NCName
   */
  public static String normaliseXmlId(String id) {
    if (id == null) {
      return null;
    }
    boolean startsWithLetter = id.matches("^[_\\p{Ll}\\p{Lu}\\p{Lo}\\p{Lt}\\p{Nl}].*");
    return startsWithLetter ? id : "_" + id;
  }

  @Override
  public void writeIdAttributeEffektif(String localPart, Id value) {
    if (value!=null) {
      xml.addAttribute(EFFEKTIF_URI, localPart, value.getInternal());
    }
  }

  @Override
  public void writeCDataTextEffektif(String localPart, String value) {
    if (value != null) {
      xml.createElement(EFFEKTIF_URI, localPart).addCDataText(value);
    }
  }

  @Override
  public void writeDateAttributeBpmn(String localPart, LocalDateTime value) {
    if (value!=null) {
      xml.addAttribute(BPMN_URI, localPart, DATE_FORMAT.print(value));
    }
  }

  @Override
  public void writeDateAttributeEffektif(String localPart, LocalDateTime value) {
    if (value != null) {
      xml.addAttribute(EFFEKTIF_URI, localPart, DATE_FORMAT.print(value));
    }
  }

  @Override
  public void writeIntegerAttributeEffektif(String localPart, Integer value) {
    if (value != null) {
      xml.addAttribute(EFFEKTIF_URI, localPart, value.toString());
    }
  }

  @Override
  public void writeBooleanAttributeEffektif(String localPart, Boolean value) {
    if (value != null) {
      xml.addAttribute(EFFEKTIF_URI, localPart, value.toString());
    }
  }

  @Override
  public void writeRelativeTimeEffektif(String localPart, RelativeTime value) {
    if (value != null && value.valid()) {
      startElementEffektif(localPart);
      value.writeBpmn(this);
      endElement();
    }
  }

  @Override
  public void writeStringValue(String localPart, String attributeName, Object value) {
    if (value!=null) {
      xml.createElement(EFFEKTIF_URI, localPart).addAttribute(EFFEKTIF_URI, attributeName, value);
    }
  }

  @Override
  public void writeText(String value) {
    if (value != null) {
      xml.addText(value);
    }
  }

  @Override
  public void writeTextElementBpmn(String localPart, Object value) {
    writeTextElement(BPMN_URI, localPart, value);
  }

  @Override
  public void writeTextElementEffektif(String localPart, Object value) {
    writeTextElement(EFFEKTIF_URI, localPart, value);
  }

  protected void writeTextElement(String namespaceUri, String localPart, Object value) {
    if (value!=null) {
      xml.createElement(namespaceUri, localPart).addText(value);
    }
  }

  @Override
  public void writeTypeAttribute(Object o) {
    bpmnMappings.writeTypeAttribute(this, o, "type");
  }

  @Override
  public void writeTypeElement(DataType type) {
    if (type!=null) {
      startElementEffektif("type");
      bpmnMappings.writeTypeAttribute(this, type, "name");
      type.writeBpmn(this);
      endElement();
    }
  }

  private void writeBpmnDiagramBounds(Bounds bounds) {
    if (bounds != null && bounds.isValid()) {
      startElement(xml.createElement(OMG_DC_URI, "Bounds"));
      xml.addAttribute(OMG_DC_URI, "height", bounds.getHeight());
      xml.addAttribute(OMG_DC_URI, "width", bounds.getWidth());
      xml.addAttribute(OMG_DC_URI, "x", bounds.upperLeft.x-xOffset);
      xml.addAttribute(OMG_DC_URI, "y", bounds.upperLeft.y-yOffset);
      endElement();
    }
  }

  private void writeBpmnDiagramEdgeDockers(List<Point> dockers) {
    if (dockers != null) {
      for (Point waypoint : dockers) {
        startElement(xml.createElement(OMG_DI_URI, "waypoint"));
        xml.addAttribute(OMG_DI_URI, "x", waypoint.x-xOffset);
        xml.addAttribute(OMG_DI_URI, "y", waypoint.y-yOffset);
        endElement();
      }
    }
  }
}
