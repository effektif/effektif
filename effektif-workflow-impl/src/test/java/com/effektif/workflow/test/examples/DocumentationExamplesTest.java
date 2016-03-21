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
package com.effektif.workflow.test.examples;

import junit.framework.TestCase;

import org.junit.Test;

import com.effektif.workflow.api.Configuration;
import com.effektif.workflow.api.activities.SubProcess;
import com.effektif.workflow.api.activities.EmbeddedSubprocess;
import com.effektif.workflow.api.activities.EndEvent;
import com.effektif.workflow.api.activities.ExclusiveGateway;
import com.effektif.workflow.api.activities.HttpServiceTask;
import com.effektif.workflow.api.activities.JavaServiceTask;
import com.effektif.workflow.api.activities.NoneTask;
import com.effektif.workflow.api.activities.ParallelGateway;
import com.effektif.workflow.api.activities.ReceiveTask;
import com.effektif.workflow.api.activities.StartEvent;
import com.effektif.workflow.api.model.WorkflowId;
import com.effektif.workflow.api.workflow.Activity;
import com.effektif.workflow.api.workflow.ExecutableWorkflow;
import com.effektif.workflow.impl.json.JsonStreamMapper;
import com.effektif.workflow.impl.memory.TestConfiguration;

/**
 * Stub for a test of JSON and BPMN output, used to generate samples for documentation.
 *
 * @author Peter Hilton
 */
public class DocumentationExamplesTest extends TestCase {

  private static Configuration configuration;

  @Override
  public void setUp() throws Exception {
    if (configuration == null) {
      configuration = new TestConfiguration();
      configuration.get(JsonStreamMapper.class).pretty();
      configuration.start();
      configuration.getWorkflowEngine();
    }
  }

  @Test
  public void testCall() {
    SubProcess activity = new SubProcess()
      .id("runTests")
      .subWorkflowSourceId("Run tests")
      .subWorkflowId(new WorkflowId("releaseTests1"));
    activity.setSubWorkflowSourceId("releaseTests");
    print(activity);
  }

  @Test
  public void testEmbeddedSubprocess() {
    EmbeddedSubprocess activity = new EmbeddedSubprocess().id("phase1");
    print(activity);
  }

  @Test
  public void testEndEvent() {
    EndEvent activity = new EndEvent();
    activity.setId("releaseComplete");
    activity.setName("software released");
    activity.setDescription("Ends the process when the release is complete.");
    print(activity);
  }

  @Test
  public void testExclusiveGateway() {
    ExclusiveGateway activity = (ExclusiveGateway) new ExclusiveGateway()
      .id("ok?")
      .defaultTransitionId("proceed");
    print(activity);
  }

  @Test
  public void testHttpServiceTask() {
    HttpServiceTask activity = new HttpServiceTask().id("publishReleaseNotes");
    print(activity);
  }

  @Test
  public void testJavaServiceTask() {
    JavaServiceTask activity = new JavaServiceTask().id("profilePerformance");
    print(activity);
  }

  @Test
  public void testNoneTask() {
    NoneTask activity = new NoneTask().id("verifyRequirements");
    print(activity);
  }

  @Test
  public void testParallelGateway() {
    ParallelGateway activity = new ParallelGateway().id("fork");
    print(activity);
  }

  @Test
  public void testReceiveTask() {
    ReceiveTask activity = new ReceiveTask().id("buildComplete");
    print(activity);
  }

  @Test
  public void testStartEvent() {
    StartEvent activity = new StartEvent().id("codeComplete");
    activity.setName("code complete");
    activity.setDescription("Starts the process when the code is ready to release.");
    print(activity);
  }

  private void printJson(Object o) {
    System.out.println("--- " + o.getClass().getSimpleName() + "----------");
    JsonStreamMapper jsonMapper = configuration.get(JsonStreamMapper.class);
    System.out.println(jsonMapper.write(o));
  }

  private void print(Activity activity) {
    printJson(activity);

    ExecutableWorkflow workflow = new ExecutableWorkflow().activity(activity);
    // System.out.println(BpmnWriter.writeBpmnDocumentString(workflow, configuration));
  }
}
