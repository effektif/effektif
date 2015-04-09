/*
 * Copyright 2014 Effektif GmbH.
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
 * limitations under the License.
 */
package com.effektif.workflow.test.implementation;

import java.io.IOException;

import org.junit.Ignore;
import org.junit.Test;

import com.effektif.workflow.api.activities.UserTask;
import com.effektif.workflow.api.form.Form;
import com.effektif.workflow.api.types.TextType;
import com.effektif.workflow.api.workflow.Workflow;
import com.effektif.workflow.impl.bpmn.BpmnWriter;

/**
 * Test BPMN parsing and generation for basic {@link com.effektif.workflow.api.activities.UserTask} properties.
 *
 * @author Peter Hilton
 */
@Ignore
public class BpmnUserTaskTest extends BpmnTestCase {

  private Workflow workflow;
  private UserTask task;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    workflow = readWorkflow("bpmn/EffektifUserTask.bpmn.xml");
    task = findActivity(workflow, UserTask.class, "performRelease");
  }

  @Test
  public void testUserTask() {
    assertNotNull("Task should exist", task);
    assertEquals("Task name", "Perform release", task.getName());
    assertEquals("Task description", "Release a new version of the software", task.getDescription());
    assertEquals("Task task name", "Release version {{version}}", task.getTaskName());
    assertEquals("Task assignee", "42", task.getAssigneeId().getValue().getInternal());
    assertEquals("Task candidates size", 2, task.getCandidateIds().size());
    assertEquals("Task candidate 2 ID", "43", task.getCandidateIds().get(1).getValue().getInternal());
    assertEquals("Task candidate group ID", "44", task.getCandidateGroupIds().get(0).getValue().getInternal());
    assertNotNull("Task form", task.getForm());
  }

  @Test
  public void testForm() {
    Form form = task.getForm();
    assertEquals("Form description", "Provide release information.", form.getDescription());
    assertEquals("Form field count", 3, form.getFields().size());
    assertEquals("Form field 1 key", "v", form.getFields().get(0).getId());
    assertEquals("Form field 1 label", "Version", form.getFields().get(0).getName());
    assertTrue("Form field 1 readonly", form.getFields().get(0).getReadOnly());
    assertTrue("Form field 2 required", form.getFields().get(1).getRequired());
//    assertFalse("Form field 3 readonly", form.getFields().get(2).getReadOnly());
//    assertFalse("Form field 3 required", form.getFields().get(2).getRequired());
    assertEquals("Form field 3 type", TextType.class, form.getFields().get(2).getType().getClass());
  }

  /**
   * Check XML generated from model: validates the generated XML. Inspect the XML output for correctness manually.
   * TODO Automate the XML correctness check with assertions on the parsed XML.
   */
  @Test
  public void testGeneratedBpmn() throws IOException {
//    String generatedBpmnDocument = BpmnWriter.writeBpmnDocumentString(workflow, configuration);
//
//    System.out.println("--- GENERATED BPMN ------------------------------------------ ");
//    System.out.println(generatedBpmnDocument);
//    System.out.println("------------------------------------------------------------- ");
//
//    validateBpmnXml(generatedBpmnDocument);
  }

}
