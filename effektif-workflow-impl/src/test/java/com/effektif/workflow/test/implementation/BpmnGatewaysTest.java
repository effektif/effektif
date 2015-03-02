package com.effektif.workflow.test.implementation;

import java.io.IOException;

import com.effektif.workflow.api.activities.ExclusiveGateway;
import com.effektif.workflow.api.activities.ParallelGateway;
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
  public void testExclusiveGatewayFork() {
    ExclusiveGateway gateway = findActivity(workflow, ExclusiveGateway.class, "testingFork");
    assertNotNull("ExclusiveGateway fork should exist", gateway);
    assertEquals("ExclusiveGateway name", "Integration release?", gateway.getName());
  }

  @Test
  public void testExclusiveGatewayJoin() {
    ExclusiveGateway gateway = findActivity(workflow, ExclusiveGateway.class, "testingJoin");
    assertNotNull("ExclusiveGateway join should exist", gateway);
    assertEquals("ExclusiveGateway name", "Testing complete", gateway.getName());
  }

  @Test
  public void testParallelGatewayFork() {
    ParallelGateway gateway = findActivity(workflow, ParallelGateway.class, "notificationFork");
    assertNotNull("ParallelGateway fork should exist", gateway);
    assertEquals("ParallelGateway name", "start notifications", gateway.getName());
  }

  @Test
  public void testParallelGatewayJoin() {
    ParallelGateway gateway = findActivity(workflow, ParallelGateway.class, "notificationJoin");
    assertNotNull("ParallelGateway join should exist", gateway);
    assertEquals("ParallelGateway name", "notifications complete", gateway.getName());
  }

  /**
   * Check XML generated from model: validates the generated XML. Inspect the XML output for correctness manually.
   * TODO Automate the XML correctness check with assertions on the parsed XML.
   */
  @Test
  public void testGeneratedBpmn() throws IOException {
    String generatedBpmnDocument = BpmnWriter.writeBpmnDocumentString(workflow, configuration);
    printBpmnXml(generatedBpmnDocument);
    validateBpmnXml(generatedBpmnDocument);
  }
}
