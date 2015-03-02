package com.effektif.workflow.test.implementation;

import java.io.IOException;

import com.effektif.workflow.api.activities.EmailTask;
import com.effektif.workflow.api.activities.UserTask;
import com.effektif.workflow.api.workflow.Workflow;
import com.effektif.workflow.impl.bpmn.BpmnWriter;
import org.junit.Test;

/**
 * Test BPMN parsing and generation for basic {@link com.effektif.workflow.api.activities.EmailTask} properties.
 *
 * @author Peter Hilton
 */
public class BpmnEmailTaskTest extends BpmnTestCase {

  private Workflow workflow;
  private EmailTask task;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    workflow = readWorkflow("bpmn/EffektifEmailTask.bpmn.xml");
    task = findActivity(workflow, EmailTask.class, "announceRelease");
  }

  @Test
  public void testEmailTask() {
    assertNotNull("Task should exist", task);
    assertEquals("Task name", "Announce release", task.getName());
    assertEquals("Task description", "Announce the new software release.", task.getDescription());

    assertEquals("Task to addresses", 1, task.getToEmailAddresses().size());
    assertEquals("Task to addresses", "dev@example.org", task.getToEmailAddresses().get(0).getValue());
    assertEquals("Task to groups", 1, task.getToGroupIds().size());
    assertEquals("Task to groups", "42", task.getToGroupIds().get(0).getValue().getId());
    assertEquals("Task to users", 1, task.getToUserIds().size());
    assertEquals("Task to users", "43", task.getToUserIds().get(0).getValue().getId());

    assertEquals("Task CC addresses", 1, task.getCcEmailAddresses().size());
    assertEquals("Task CC addresses", "phb@example.org", task.getCcEmailAddresses().get(0).getValue());
    assertEquals("Task CC groups", 1, task.getCcGroupIds().size());
    assertEquals("Task CC groups", "44", task.getCcGroupIds().get(0).getValue().getId());
    assertEquals("Task CC users", 1, task.getCcUserIds().size());
    assertEquals("Task CC users", "45", task.getCcUserIds().get(0).getValue().getId());

    assertEquals("Task BCC addresses", 1, task.getBccEmailAddresses().size());
    assertEquals("Task BCC addresses", "archive@example.org", task.getBccEmailAddresses().get(0).getValue());
    assertEquals("Task BCC groups", 1, task.getBccGroupIds().size());
    assertEquals("Task BCC groups", "46", task.getBccGroupIds().get(0).getValue().getId());
    assertEquals("Task BCC users", 1, task.getBccUserIds().size());
    assertEquals("Task BCC users", "47", task.getBccUserIds().get(0).getValue().getId());

    assertEquals("Task subject", "Version {{version}} released", task.getSubject());
    assertEquals("Task text body", "Release deployed in production!", task.getBodyText());
    assertEquals("Task HTML body", "<p>Release deployed in <strong>production!</p>", task.getBodyHtml());
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
