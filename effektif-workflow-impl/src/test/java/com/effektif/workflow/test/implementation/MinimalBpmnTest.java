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

import com.effektif.workflow.api.activities.EndEvent;
import com.effektif.workflow.api.activities.StartEvent;
import com.effektif.workflow.api.activities.UserTask;
import com.effektif.workflow.api.workflow.Activity;
import com.effektif.workflow.api.workflow.Workflow;
import com.effektif.workflow.impl.activity.ActivityTypeService;
import com.effektif.workflow.impl.bpmn.BpmnReader;
import com.effektif.workflow.impl.bpmn.BpmnWriter;
import com.effektif.workflow.impl.memory.TestConfiguration;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import junit.framework.TestCase;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

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
 */
public class MinimalBpmnTest extends TestCase {

  private static ActivityTypeService activityTypeService;
  private static ObjectMapper objectMapper;
  private static File testResources;

  static {
    String dir = MinimalBpmnTest.class.getProtectionDomain().getCodeSource().getLocation().toString();
    dir = dir.substring(5);
    testResources = new File(dir);
  }

  @Override
  public void setUp() throws Exception {
    if (activityTypeService == null || objectMapper == null) {
      TestConfiguration testConfiguration = new TestConfiguration();
      testConfiguration.getWorkflowEngine(); // to ensure initialization of the object mapper
      activityTypeService = testConfiguration.get(ActivityTypeService.class);
      objectMapper = testConfiguration.get(ObjectMapper.class);
    }
  }

  @Test
  public void testMinimalBpmn() throws IOException {
    File bpmn = new File(testResources, "bpmn/MinimalProcess.bpmn.xml");
    byte[] encoded = Files.readAllBytes(Paths.get(bpmn.getPath()));
    String bpmnXmlString = new String(encoded, StandardCharsets.UTF_8);
    BpmnReader reader = new BpmnReader(activityTypeService);
    Workflow workflow = reader.readBpmnDocument(new StringReader(bpmnXmlString));

    checkProcessModel(workflow);
    checkStartEvent(findActivity(workflow, StartEvent.class, "theStart"));
    checkUserTask(findActivity(workflow, UserTask.class, "approveRequest"));
    checkEndEvent(findActivity(workflow, EndEvent.class, "theEnd"));

    // Inspect the XML output for correctness. TODO Automate this check.
    BpmnWriter writer = new BpmnWriter(activityTypeService);
    System.out.println("--- GENERATED BPMN " + " ------------------------------------------ ");
    System.out.println(BpmnWriter.writeBpmnDocumentString(workflow, activityTypeService));
  }

  private void checkProcessModel(Workflow workflow) {
    assertEquals("Workflow should have the right source ID", "vacationRequest", workflow.getSourceWorkflowId());
    assertEquals("Workflow should have the right name", "Vacation request", workflow.getName());
  }

  private void checkStartEvent(StartEvent startEvent) {
    assertNotNull("StartEvent should exist", startEvent);
  }

  private void checkUserTask(UserTask task) {
    assertNotNull("UserTask should exist", task);
  }

  private void checkEndEvent(EndEvent endEvent) {
    assertNotNull("EndEvent should exist", endEvent);
  }

  /**
   * Returns the workflow activity with the given ID, with the specified type.
   * Returns null if the ID isn’t found or the activity has the wrong type.
   */
  private <T extends Activity> T findActivity(Workflow workflow, Class<T> activityType, String activityId) {
    for (Activity activity : workflow.getActivities()) {
      System.out.println("activity is " + activity.getClass());
      if (activity.getClass().equals(activityType) && activity.getId().equals(activityId)) {
        return (T) activity;
      }
    }
    return null;
  }

}
