package com.effektif.workflow.test.serialization;

import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.TreeSet;

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
    root.children = new ArrayList<>();
    root.cleanEmptyElements();
    assertNull(root.children);
  }

  @Test
  public void testCleanEmptyChild() {
    XmlElement childElement = new XmlElement();
    childElement.setName(Bpmn.BPMN_URI, "startEvent");
    childElement.attributes = new HashMap<>();
    childElement.children = new ArrayList<>();

    root.addChild(childElement);
    root.cleanEmptyElements();

    assertNull(childElement.attributes);
    assertNull(childElement.children);
  }
}
