package com.effektif.workflow.test.serialization;

import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;

import com.effektif.workflow.api.bpmn.XmlElement;
import com.effektif.workflow.impl.bpmn.Bpmn;

public class XmlElementTest {

  private XmlElement root;

  @Before
  public void createRootElement() {
    root = new XmlElement();
    root.setName(Bpmn.BPMN_URI, "process");
  }

  @Test
  public void testCleanEmptyAttributes() {
    root.attributes = new HashMap<>();
    root.cleanEmptyElements();
    assertNull(root.attributes);
  }

  @Test
  public void testCleanEmptyElements() {
    root.elements = new ArrayList<>();
    root.cleanEmptyElements();
    assertNull(root.elements);
  }

  @Test
  public void testCleanEmptyChild() {
    XmlElement childElement = new XmlElement();
    childElement.setName(Bpmn.BPMN_URI, "startEvent");
    childElement.attributes = new HashMap<>();
    childElement.elements = new ArrayList<>();

    root.addElement(childElement);
    root.cleanEmptyElements();

    assertNull(childElement.attributes);
    assertNull(childElement.elements);
  }
}
