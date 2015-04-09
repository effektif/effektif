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

import com.effektif.workflow.api.activities.Call;
import com.effektif.workflow.api.activities.EmailTask;
import com.effektif.workflow.api.activities.EmbeddedSubprocess;
import com.effektif.workflow.api.activities.EndEvent;
import com.effektif.workflow.api.activities.ExclusiveGateway;
import com.effektif.workflow.api.activities.HttpServiceTask;
import com.effektif.workflow.api.activities.JavaServiceTask;
import com.effektif.workflow.api.activities.NoneTask;
import com.effektif.workflow.api.activities.ParallelGateway;
import com.effektif.workflow.api.activities.ScriptTask;
import com.effektif.workflow.api.activities.StartEvent;
import com.effektif.workflow.api.activities.UserTask;
import com.effektif.workflow.api.workflow.Transition;
import com.effektif.workflow.api.workflow.Workflow;
import com.effektif.workflow.impl.bpmn.BpmnWriter;

/**
 * Test BPMN parsing and generation for all activity types in one process.
 * Each activity type only has a shallow test here, more detailed tests being for other classes.
 * Initially, the generated XML is validated, but not checked against the input.
 *
 * @author Peter Hilton
 */
@Ignore
public class BpmnProcessTest extends BpmnTestCase {

  private Workflow workflow;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    workflow = readWorkflow("bpmn/EffektifProcess.bpmn.xml");
  }

  @Test
  public void testWholeProcess() throws IOException {
    // Check parsed model…
    checkProcessModel(workflow);
    checkStartEvent(findActivity(workflow, StartEvent.class, "theStart"));

    checkEmailTask(findActivity(workflow, EmailTask.class, "emailNotification"));
    checkHttpServiceTask(findActivity(workflow, HttpServiceTask.class, "publishVacation"));
    checkJavaServiceTask(findActivity(workflow, JavaServiceTask.class, "lookupAllowance"));
    checkNoneTask(findActivity(workflow, NoneTask.class, "evaluateCase"));
    checkScriptTask(findActivity(workflow, ScriptTask.class, "checkAllowance"));
    checkUserTask(findActivity(workflow, UserTask.class, "approveRequest"));
    checkTransition(findTransition(workflow, "approvalFork1"));

    checkExclusiveGateway(findActivity(workflow, ExclusiveGateway.class, "approvalFork"));
    checkParallelGateway(findActivity(workflow, ParallelGateway.class, "notificationFork"));

    checkCall(findActivity(workflow, Call.class, "investigateRequest"));
    checkEmbeddedSubprocess(findActivity(workflow, EmbeddedSubprocess.class, "increaseVacationAllowance"));

    checkEndEvent(findActivity(workflow, EndEvent.class, "theEnd"));

    // Check XML generated from model…
//    String generatedBpmnDocument = BpmnWriter.writeBpmnDocumentString(workflow, configuration);
//    printBpmnXml(generatedBpmnDocument);
//    validateBpmnXml(generatedBpmnDocument);
  }

  private void checkProcessModel(Workflow workflow) {
    assertEquals("Workflow should have the right source ID", "vacationRequest", workflow.getSourceWorkflowId());
    assertEquals("Workflow should have the right name", "Vacation request", workflow.getName());
  }

  private void checkCall(Call task) { assertNotNull("Call should exist", task); }

  private void checkEmailTask(EmailTask task) { assertNotNull("EmailTask should exist", task); }

  private void checkEmbeddedSubprocess(EmbeddedSubprocess activity) { assertNotNull("EmbeddedSubprocess should exist", activity); }

  private void checkEndEvent(EndEvent endEvent) {
    assertNotNull("EndEvent should exist", endEvent);
  }

  private void checkExclusiveGateway(ExclusiveGateway gateway) { assertNotNull("ExclusiveGateway should exist", gateway); }

  private void checkHttpServiceTask(HttpServiceTask task) { assertNotNull("HttpServiceTask should exist", task); }

  private void checkJavaServiceTask(JavaServiceTask task) { assertNotNull("JavaServiceTask should exist", task); }

  private void checkNoneTask(NoneTask task) { assertNotNull("NoneTask should exist", task); }

  private void checkParallelGateway(ParallelGateway gateway) { assertNotNull("ParallelGateway should exist", gateway); }

  private void checkScriptTask(ScriptTask task) { assertNotNull("ScriptTask should exist", task); }

  private void checkStartEvent(StartEvent startEvent) { assertNotNull("StartEvent should exist", startEvent); }

  private void checkTransition(Transition transition) {
    assertNotNull("Transition should exist", transition);
    assertEquals("Transition name", "Allowance available", transition.getName());
    assertEquals("Transition from", "approvalFork", transition.getFrom());
    assertEquals("Transition to", "approveRequest", transition.getTo());
  }

  private void checkUserTask(UserTask task) { assertNotNull("UserTask should exist", task); }


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
