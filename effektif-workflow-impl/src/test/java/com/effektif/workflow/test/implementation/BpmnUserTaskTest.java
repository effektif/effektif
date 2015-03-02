package com.effektif.workflow.test.implementation;

import java.io.IOException;

import com.effektif.workflow.api.activities.UserTask;
import com.effektif.workflow.api.form.Form;
import com.effektif.workflow.api.types.TextType;
import com.effektif.workflow.api.workflow.Workflow;
import com.effektif.workflow.impl.bpmn.BpmnWriter;
import org.junit.Test;

/**
 *
 *
 * @author Peter Hilton
 */
public class BpmnUserTaskTest extends BpmnTestCase {

  @Test
  public void testUserTask() throws IOException {
    Workflow workflow = readWorkflow("bpmn/EffektifUserTask.bpmn.xml");

    UserTask task = findActivity(workflow, UserTask.class, "performRelease");
    assertNotNull("UserTask should exist", task);
    assertEquals("UserTask name", "Perform release", task.getName());
    assertEquals("UserTask description", "Release a new version of the software", task.getDescription());
    assertEquals("UserTask task name", "Release version {{version}}", task.getTaskName());
    assertEquals("UserTask assignee", "42", task.getAssigneeId().getValue().getId());
    assertEquals("UserTask candidates size", 2, task.getCandidateIds().size());
    assertEquals("UserTask candidate 2 ID", "43", task.getCandidateIds().get(1).getValue().getId());
    assertEquals("UserTask candidate group ID", "44", task.getCandidateGroupIds().get(0).getValue().getId());
    assertNotNull("UserTask form", task.getForm());
    checkForm(task.getForm());

    // Check XML generated from model…
    String generatedBpmnDocument = BpmnWriter.writeBpmnDocumentString(workflow, configuration);

    // Inspect the XML output for correctness.
    System.out.println("--- GENERATED BPMN ------------------------------------------ ");
    System.out.println(generatedBpmnDocument);
    System.out.println("------------------------------------------------------------- ");

    // Validate generated XML.
    // TODO Automate the XML correctness check with assertions on the parsed XML.
    validateBpmnXml(generatedBpmnDocument);
  }

  private void checkForm(Form form) {
    assertEquals("Form description", "Provide release information.", form.getDescription());
    assertEquals("Form field count", 3, form.getFields().size());
    assertEquals("Form field 1 key", "v", form.getFields().get(0).getKey());
    assertEquals("Form field 1 label", "Version", form.getFields().get(0).getName());
    assertTrue("Form field 1 readonly", form.getFields().get(0).getReadOnly());
    assertTrue("Form field 2 required", form.getFields().get(1).getRequired());
    assertFalse("Form field 3 readonly", form.getFields().get(2).getReadOnly());
    assertFalse("Form field 3 required", form.getFields().get(2).getRequired());
    assertEquals("Form field 3 type", TextType.class, form.getFields().get(2).getType().getClass());
  }}
