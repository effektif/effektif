package com.effektif.workflow.test.implementation;

import java.io.IOException;

import com.effektif.workflow.api.activities.EndEvent;
import com.effektif.workflow.api.activities.StartEvent;
import com.effektif.workflow.api.activities.UserTask;
import com.effektif.workflow.api.form.Form;
import com.effektif.workflow.api.types.TextType;
import com.effektif.workflow.api.workflow.Workflow;
import com.effektif.workflow.impl.bpmn.BpmnWriter;
import org.junit.Test;

/**
 * Test BPMN parsing and generation for basic {@link com.effektif.workflow.api.activities.UserTask} properties.
 *
 * @author Peter Hilton
 */
public class BpmnEventsTest extends BpmnTestCase {

  private Workflow workflow;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    workflow = readWorkflow("bpmn/EffektifEvents.bpmn.xml");
  }

  @Test
  public void testStartEvent() throws IOException {
    StartEvent event = findActivity(workflow, StartEvent.class, "theStart");
    assertNotNull("StartEvent should exist", event);
    assertEquals("StartEvent name", "code complete", event.getName());
    assertEquals("StartEvent description", "Starts the process when the code is ready to release.", event.getDescription());
//    assertEquals("StartEvent outgoing transitions", 1, event.getOutgoingTransitions().size());
  }

  @Test
  public void testEndEvent() throws IOException {
    EndEvent event = findActivity(workflow, EndEvent.class, "theEnd");
    assertNotNull("EndEvent should exist", event);
    assertEquals("EndEvent name", "software released", event.getName());
    assertEquals("EndEvent description", "Ends the process when the release is complete.", event.getDescription());
    assertNull("EndEvent outgoing transitions", event.getOutgoingTransitions());
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
