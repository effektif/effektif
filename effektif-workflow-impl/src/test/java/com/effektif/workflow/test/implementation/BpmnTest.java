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
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import junit.framework.TestCase;

import org.junit.Test;

import com.effektif.workflow.api.Configuration;
import com.effektif.workflow.api.activities.EndEvent;
import com.effektif.workflow.api.activities.StartEvent;
import com.effektif.workflow.api.activities.UserTask;
import com.effektif.workflow.api.workflow.Workflow;
import com.effektif.workflow.impl.activity.ActivityTypeService;
import com.effektif.workflow.impl.bpmn.BpmnReader;
import com.effektif.workflow.impl.bpmn.BpmnWriter;
import com.effektif.workflow.impl.memory.TestConfiguration;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

/**
 * @author Tom Baeyens
 */
public class BpmnTest extends TestCase {

  private static Configuration configuration;

  @Override
  public void setUp() throws Exception {
    if (configuration == null) {
      configuration = new TestConfiguration();
    }
  }

  @Test
  public void testBpmnParsingAndSerialization() throws Exception {
    String dir = BpmnTest.class.getProtectionDomain().getCodeSource().getLocation().toString();
    dir = dir.substring(5);
    scan(new File(dir));
  }

  private void scan(File directory) throws Exception {
    configuration.getWorkflowEngine(); // to ensure initialization of the object mapper
    ActivityTypeService activityTypeService = configuration.get(ActivityTypeService.class);
    ObjectMapper objectMapper = configuration.get(ObjectMapper.class);
    
    try {
      for (File file : directory.listFiles()) {
        // System.err.println("scanning "+file.getCanonicalPath());
        if (file.isFile() && file.getName().endsWith(".bpmn.xml")) {
          byte[] encoded = Files.readAllBytes(Paths.get(file.getPath()));
          String bpmnXmlString = new String(encoded, StandardCharsets.UTF_8);
          System.err.println("=== SOURCE " + file.getPath()+" ========================================== ");
          System.err.println(bpmnXmlString);
          BpmnReader bpmnReader = new BpmnReader(configuration);
          Workflow workflow = bpmnReader.readBpmnDocument(new StringReader(bpmnXmlString));
          
          System.err.println("--- JSON " + file.getPath()+" ------------------------------------------ ");
          ObjectWriter writer = objectMapper.writerWithDefaultPrettyPrinter();
          String workflowJsonString = writer.writeValueAsString(workflow);
          System.err.println(workflowJsonString);
          workflow = objectMapper.readValue(workflowJsonString, Workflow.class);
          
          System.err.println("--- BPMN " + file.getPath()+" ------------------------------------------ ");
          System.err.println(BpmnWriter.writeBpmnDocumentString(workflow, configuration));
          System.err.println();
          System.err.println();
        } else if (file.isDirectory()) {
          scan(file);
        }
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  
  
  @Test
  public void testBpmnSerializationFromNewWorkflow() throws Exception {
    TestConfiguration testConfiguration = new TestConfiguration();
    testConfiguration.getWorkflowEngine(); // to ensure initialization of the object mapper
    ActivityTypeService activityTypeService = testConfiguration.get(ActivityTypeService.class);

    Workflow workflow = new Workflow()
      .activity("s", new StartEvent()
        .transitionTo("t"))
      .activity("t", new UserTask()
        .transitionTo("e"))
      .activity("e", new EndEvent());

    System.err.println(BpmnWriter.writeBpmnDocumentString(workflow, configuration));
  }
}
