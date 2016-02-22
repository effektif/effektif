package com.effektif.workflow.test.serialization;

import static org.junit.Assert.assertEquals;

import java.net.URISyntaxException;

import org.junit.Before;
import org.junit.Test;

import com.effektif.workflow.api.bpmn.XmlNamespaces;

public class XmlNamespacesTest {

  public static final String BPMN_URI = "http://www.omg.org/spec/BPMN/20100524/MODEL";
  public static final String EFFEKTIF_URI = "http://effektif.com/bpmn20";

  private XmlNamespaces namespaces;

  @Before
  public void createNamespaces() {
    namespaces = new XmlNamespaces();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testDefaultNamespaceError() throws URISyntaxException {
    namespaces.add(null, BPMN_URI);
    namespaces.add(null, EFFEKTIF_URI);
  }

  @Test
  public void testPrefixLookup() throws URISyntaxException {
    namespaces.add(null, BPMN_URI);
    namespaces.add("e", EFFEKTIF_URI);
    assertEquals("", namespaces.getPrefix(BPMN_URI));
    assertEquals("e", namespaces.getPrefix(EFFEKTIF_URI));
  }
}
