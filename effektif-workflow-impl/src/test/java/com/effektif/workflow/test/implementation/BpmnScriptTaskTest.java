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

import org.junit.Test;

import com.effektif.workflow.api.activities.ScriptTask;
import com.effektif.workflow.api.workflow.Workflow;
import com.effektif.workflow.impl.bpmn.BpmnWriter;

/**
 * Test BPMN parsing and generation for basic {@link com.effektif.workflow.api.activities.ScriptTask} properties.
 *
 * @author Peter Hilton
 */
public class BpmnScriptTaskTest extends BpmnTestCase {

  private Workflow workflow;
  private ScriptTask task;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    workflow = readWorkflow("bpmn/EffektifScriptTask.bpmn.xml");
    task = findActivity(workflow, ScriptTask.class, "announceInChatRoom");
  }

  @Test
  public void testScriptTask() {
    assertNotNull("Task should exist", task);
    assertEquals("Task name", "Announce release in chat room", task.getName());
    assertEquals("Task description", "Announce the release in the developer chat room.", task.getDescription());

    assertNotNull("Script should exist", task.getScript());
    assertEquals("Script language", "javascript", task.getScript().getLanguage());
    assertEquals("Script", "console.log('TODO!');", task.getScript().getScript());

    assertNotNull("Script mappings", task.getScript().getMappings());
    assertTrue("Script mapping - date", task.getScript().getMappings().containsKey("date"));
    assertEquals("Script mapping - date", "releaseDate", task.getScript().getMappings().get("date"));
    assertTrue("Script mapping - version", task.getScript().getMappings().containsKey("version"));
    assertEquals("Script mapping - version", "versionNumber", task.getScript().getMappings().get("version"));
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
