package com.effektif.workflow.test.implementation;

import java.io.IOException;

import com.effektif.workflow.api.activities.EndEvent;
import com.effektif.workflow.api.activities.StartEvent;
import com.effektif.workflow.api.workflow.Workflow;
import com.effektif.workflow.impl.bpmn.BpmnWriter;
import org.junit.Test;

/**
 * Test BPMN parsing and generation for basic gateways.
 *
 * @author Peter Hilton
 */
public class BpmnGatewaysTest extends BpmnTestCase {

  private Workflow workflow;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    workflow = readWorkflow("bpmn/EffektifGateways.bpmn.xml");
  }

  @Test
  public void testParallelGatewayFork() throws IOException {
    StartEvent event = findActivity(workflow, StartEvent.class, "notificationFork");
    assertNotNull("ParallelGateway fork should exist", event);
    assertEquals("ParallelGateway name", "code complete", event.getName());
  }

  @Test
  public void testParallelGatewayJoin() throws IOException {
    EndEvent event = findActivity(workflow, EndEvent.class, "notificationJoin");
    assertNotNull("ParallelGateway join should exist", event);
    assertEquals("ParallelGateway name", "software released", event.getName());
  }

  /**
   * Check XML generated from model: validates the generated XML. Inspect the XML output for correctness manually.
   * TODO Automate the XML correctness check with assertions on the parsed XML.
   */
  @Test
  public void testGeneratedBpmn() throws IOException {
    String generatedBpmnDocument = BpmnWriter.writeBpmnDocumentString(workflow, configuration);

    System.out.println("--- GENERATED BPMN ------------------------------------------ ");
    System.out.println(generatedBpmnDocument);
    System.out.println("------------------------------------------------------------- ");

    validateBpmnXml(generatedBpmnDocument);
  }
}
