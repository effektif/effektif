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

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import junit.framework.TestCase;

import org.junit.Test;
import org.xml.sax.SAXException;

import com.effektif.workflow.api.Configuration;
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
import com.effektif.workflow.api.form.Form;
import com.effektif.workflow.api.types.TextType;
import com.effektif.workflow.api.workflow.Activity;
import com.effektif.workflow.api.workflow.Transition;
import com.effektif.workflow.api.workflow.Workflow;
import com.effektif.workflow.impl.activity.ActivityTypeService;
import com.effektif.workflow.impl.bpmn.BpmnReader;
import com.effektif.workflow.impl.bpmn.BpmnWriter;
import com.effektif.workflow.impl.memory.TestConfiguration;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Peter Hilton
 *
 * parseOneXmlFile
 * checkXxxModel
 * serializeTheXmlFile
 * checkXxxXml
 *
 * public checkStartEventModel(StartEvent startEvent) {
 * assertEquals("sstartid", startEvent.getId());
 *
 * public checkStartEventXml(XmlNode xmlDom) {
 * assertEquals("sstartid", xmlDom.getValue("id");
 *
 * - add each of the remaining activity types
 * - how to parse each sequenceFlow and add a Transition to the Workflow
 * - adding the unparsed BPMN to the output in the right place
 * - automate the check on the generated XML (checkXxxXml) *
 */
public class EffektifBpmnTest extends TestCase {

  private static Configuration configuration;
  private static ActivityTypeService activityTypeService;
  private static ObjectMapper objectMapper;
  private static File testResources;

  static {
    String dir = EffektifBpmnTest.class.getProtectionDomain().getCodeSource().getLocation().toString();
    dir = dir.substring(5);
    testResources = new File(dir);
  }

  @Override
  public void setUp() throws Exception {
    if (configuration == null) {
      configuration = new TestConfiguration();
      configuration.getWorkflowEngine(); // to ensure initialization of the object mapper
      activityTypeService = configuration.get(ActivityTypeService.class);
      objectMapper = configuration.get(ObjectMapper.class);
    }
  }

  @Test
  public void testMinimalBpmn() throws IOException {
    File bpmn = new File(testResources, "bpmn/EffektifProcess.bpmn.xml");
    byte[] encoded = Files.readAllBytes(Paths.get(bpmn.getPath()));
    String bpmnXmlString = new String(encoded, StandardCharsets.UTF_8);
    BpmnReader reader = new BpmnReader(configuration);
    Workflow workflow = reader.readBpmnDocument(new StringReader(bpmnXmlString));

    // Check parsed model…
    checkProcessModel(workflow);
    checkStartEvent(findActivity(workflow, StartEvent.class, "theStart"));

    checkEmailTask(findActivity(workflow, EmailTask.class, "emailNotification"));
    checkHttpServiceTask(findActivity(workflow, HttpServiceTask.class, "publishVacation"));
    checkJavaServiceTask(findActivity(workflow, JavaServiceTask.class, "lookupAllowance"));
    checkNoneTask(findActivity(workflow, NoneTask.class, "evaluateCase"));
    checkScriptTask(findActivity(workflow, ScriptTask.class, "checkAllowance"));
    checkTransition(findTransition(workflow, "approvalFork1"));
    checkUserTask(findActivity(workflow, UserTask.class, "approveRequest"));

    checkExclusiveGateway(findActivity(workflow, ExclusiveGateway.class, "approvalFork"));
    checkParallelGateway(findActivity(workflow, ParallelGateway.class, "notificationFork"));

    checkCall(findActivity(workflow, Call.class, "investigateRequest"));
    checkEmbeddedSubprocess(findActivity(workflow, EmbeddedSubprocess.class, "increaseVacationAllowance"));

    checkEndEvent(findActivity(workflow, EndEvent.class, "theEnd"));

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

  private void checkProcessModel(Workflow workflow) {
    assertEquals("Workflow should have the right source ID", "vacationRequest", workflow.getSourceWorkflowId());
    assertEquals("Workflow should have the right name", "Vacation request", workflow.getName());
  }

  private void checkCall(Call task) {
    assertNotNull("Call should exist", task);
  }

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

  private void checkUserTask(UserTask task) {
    assertNotNull("UserTask should exist", task);
    assertEquals("UserTask name", "Approve vacation request", task.getName());
    assertEquals("UserTask assignee", "42", task.getAssigneeId().getValue().getId());
    assertEquals("UserTask candidates size", 2, task.getCandidateIds().size());
    assertEquals("UserTask candidate 2 ID", "43", task.getCandidateIds().get(1).getValue().getId());
    assertEquals("UserTask candidate group ID", "44", task.getCandidateGroupIds().get(0).getValue().getId());
    assertNotNull("UserTask form", task.getForm());
    checkForm(task.getForm());
  }

  private void checkForm(Form form) {
    assertEquals("Form description", "Approve or reject the vacation request.", form.getDescription());
    assertEquals("Form field count", 3, form.getFields().size());
    assertEquals("Form field 1 key", "c", form.getFields().get(0).getKey());
    assertEquals("Form field 1 label", "Candidate name", form.getFields().get(0).getName());
    assertTrue("Form field 1 readonly", form.getFields().get(0).getReadOnly());
    assertTrue("Form field 2 required", form.getFields().get(1).getRequired());
    assertFalse("Form field 3 readonly", form.getFields().get(2).getReadOnly());
    assertFalse("Form field 3 required", form.getFields().get(2).getRequired());
    assertEquals("Form field 3 type", TextType.class, form.getFields().get(2).getType().getClass());
  }

  /**
   * Returns the workflow activity with the given ID, with the specified type.
   * Returns null if the ID isn’t found or the activity has the wrong type.
   */
  private <T extends Activity> T findActivity(Workflow workflow, Class<T> activityType, String activityId) {
    for (Activity activity : workflow.getActivities()) {
      if (activity.getClass().equals(activityType) && activity.getId().equals(activityId)) {
        return (T) activity;
      }
    }
    return null;
  }

  /**
   * Returns the workflow transition with the given ID, or null if the ID isn’t found.
   */
  private Transition findTransition(Workflow workflow, String transitionId) {
    List<Transition> transitions = workflow.getTransitions();
    if (transitions != null) {
      for (Transition transition : transitions) {
        if (transition.getId() != null && transition.getId().equals(transitionId)) {
          return transition;
        }
      }
    }
    return null;
  }

  /**
   * Performs XML schema validation on the generated XML using the BPMN 2.0 schema.
   */
  private void validateBpmnXml(String bpmnDocument) throws IOException {
    File schemaFile = new File(testResources, "bpmn/xsd/BPMN20.xsd");
    Source xml = new StreamSource(new StringReader(bpmnDocument));
    SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
    try {
      Schema schema = schemaFactory.newSchema(schemaFile);
      Validator validator = schema.newValidator();
      validator.validate(xml);
    } catch (SAXException e) {
      fail("BPMN XML validation error: " + e.getMessage());
    }
  }
}
