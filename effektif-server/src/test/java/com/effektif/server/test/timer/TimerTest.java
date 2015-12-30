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
package com.effektif.server.test.timer;

import com.effektif.mongo.MongoConfiguration;
import com.effektif.workflow.api.workflow.ExecutableWorkflow;
import com.effektif.workflow.api.workflowinstance.WorkflowInstance;
import com.effektif.workflow.impl.bpmn.BpmnMapper;
import com.effektif.workflow.test.WorkflowTest;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;


/**
 * @author Tom Baeyens
 */
public class TimerTest extends WorkflowTest {
//
//  @Test
//  public void testTimer() {
//    ExecutableWorkflow workflow = new ExecutableWorkflow()
//      .activity("r", new ReceiveTask()
//        .timer(new BoundaryEventTimer()
//          .dueDateExpression("0 minutes")));
//
//    deploy(workflow);
//
//    // WorkflowInstance workflowInstance = start(workflow);
//  }
  
  @Test
  public void testReadBpmn () {

    String fileName = "workflows/Task_boundary.bpmn.xml";
    MongoConfiguration config;

    try {

      ExecutableWorkflow workflow = readFlowFromFile(fileName);
      workflow.setId(null);
      deploy(workflow);

      WorkflowInstance workflowInstance = start(workflow);

      System.out.println("");

    } catch (Exception e) {
      e.printStackTrace();
    }

  }

  public ExecutableWorkflow readFlowFromFile(String fileName) throws IOException {
    URL fileUrl = TimerTest.class.getClassLoader().getResource(fileName);

    String content = readFile(fileUrl.getPath(), StandardCharsets.UTF_8);

    BpmnMapper mapper = BpmnMapper.createBpmnMapperForTest();

    return (ExecutableWorkflow) mapper.readFromString(content);
  }

  public static String readFile(String path, Charset encoding)
      throws IOException {
    byte[] encoded = Files.readAllBytes(Paths.get(path));
    return new String(encoded, encoding);
  }
}
