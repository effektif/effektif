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

import com.effektif.workflow.api.activities.EndEvent;
import com.effektif.workflow.api.activities.StartEvent;
import com.effektif.workflow.api.workflow.Workflow;

/**
 * Test BPMN parsing and generation for basic {@link com.effektif.workflow.api.activities.UserTask} properties.
 *
 * @author Peter Hilton
 */
@Ignore
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
//    String generatedBpmnDocument = BpmnWriter.writeBpmnDocumentString(workflow, configuration);
//    printBpmnXml(generatedBpmnDocument);
//    validateBpmnXml(generatedBpmnDocument);
  }
}
