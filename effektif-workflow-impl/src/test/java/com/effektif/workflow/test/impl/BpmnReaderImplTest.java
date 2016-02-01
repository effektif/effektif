package com.effektif.workflow.test.impl;

import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.HashMap;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.effektif.workflow.api.bpmn.XmlElement;
import com.effektif.workflow.impl.bpmn.Bpmn;
import com.effektif.workflow.impl.bpmn.BpmnMappings;
import com.effektif.workflow.impl.bpmn.BpmnReaderImpl;
import com.effektif.workflow.impl.json.DefaultJsonStreamMapper;
import com.effektif.workflow.impl.json.JsonStreamMapper;

/**
 * Tests {@link BpmnReaderImpl#cleanEmptyElements(XmlElement)}
 */
public class BpmnReaderImplTest {

  private static BpmnReaderImpl bpmnReader;
  private XmlElement root;

  @BeforeClass
  public static void initialize() {
    JsonStreamMapper jsonStreamMapper = new DefaultJsonStreamMapper();
    bpmnReader = new BpmnReaderImpl(new BpmnMappings(jsonStreamMapper.getMappings()), jsonStreamMapper);
  }

  @Before
  public void createRootElement() {
    root = new XmlElement();
    root.setName(Bpmn.BPMN_URI, "process");
  }

  @Test
  public void testCleanEmptyAttributes() {
    root.attributes = new HashMap<>();
    bpmnReader.cleanEmptyElements(root);
    assertNull(root.attributes);
  }

  @Test
  public void testCleanEmptyElements() {
    root.elements = new ArrayList<>();
    bpmnReader.cleanEmptyElements(root);
    assertNull(root.elements);
  }

  @Test
  public void testCleanEmptyChild() {
    XmlElement childElement = new XmlElement();
    childElement.setName(Bpmn.BPMN_URI, "startEvent");
    childElement.attributes = new HashMap<>();
    childElement.elements = new ArrayList<>();

    root.addElement(childElement);
    bpmnReader.cleanEmptyElements(root);

    assertNull(childElement.attributes);
    assertNull(childElement.elements);
  }

  @Test
  public void testCleanEmptyExtensionElements() {
    root.elements = new ArrayList<>();
    root.getOrCreateChildElement(Bpmn.BPMN_URI, "extensionElements", 0);
    bpmnReader.cleanEmptyElements(root);
    assertNull(root.elements);
  }
}
