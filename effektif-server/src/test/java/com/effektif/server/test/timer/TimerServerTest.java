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
import com.effektif.workflow.api.activities.ReceiveTask;
import com.effektif.workflow.api.query.WorkflowInstanceQuery;
import com.effektif.workflow.api.workflow.ExecutableWorkflow;
import com.effektif.workflow.api.workflowinstance.WorkflowInstance;
import com.effektif.workflow.impl.bpmn.BpmnMapper;
import com.effektif.workflow.impl.workflow.boundary.BoundaryEventTimer;
import com.effektif.workflow.impl.workflowinstance.WorkflowInstanceImpl;
import com.effektif.workflow.test.WorkflowTest;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;


/**
 * @author Tom Baeyens
 */
public class TimerServerTest extends WorkflowTest {

  @Override
  public void initializeWorkflowEngine() {

    try {
      String mongoUri = "mongodb://localhost:27017/effektif-test";
      MongoClientURI clientUri;
      MongoClient mongoClient;

      if (mongoUri != null && !"".equals(mongoUri)) {
        clientUri = new MongoClientURI(mongoUri);

        mongoClient = new MongoClient(clientUri);

        cachedConfiguration = new MongoConfiguration()
            .databaseName(clientUri.getDatabase())
            .mongoClient(mongoClient);
      }
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    super.initializeWorkflowEngine();
//    deleteWorkflowEngineContents();

  }

  @Override
  public void after() {
//    super.after();
  }

//  @Test
  public void testTimer() {
    ExecutableWorkflow workflow = new ExecutableWorkflow()
      .activity("r", new ReceiveTask()
        .timer(new BoundaryEventTimer()
          .dueDateExpression("PT1H5M")));

    deploy(workflow);

    WorkflowInstance workflowInstance = start(workflow);
  }


//  @Test
  public void testReadBpmn () {

    String fileName = "workflows/Task_boundary.bpmn.xml";
    MongoConfiguration config;

    try {

      ExecutableWorkflow workflow = readFlowFromFile(fileName);
      workflow.setId(null);
      deploy(workflow);

      WorkflowInstance workflowInstance = start(workflow);

      List<WorkflowInstance> workflowInstances = workflowEngine.findWorkflowInstances(new WorkflowInstanceQuery().workflowInstanceId(workflowInstance.getId()));

      WorkflowInstance wfi2;
      if (workflowInstances.size() > 0) {
        wfi2 = workflowInstances.get(0);
      }

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
