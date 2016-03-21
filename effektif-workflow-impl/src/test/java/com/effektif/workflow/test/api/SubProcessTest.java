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
package com.effektif.workflow.test.api;

import com.effektif.workflow.api.activities.NoneTask;
import com.effektif.workflow.api.activities.ReceiveTask;
import com.effektif.workflow.api.activities.SubProcess;
import com.effektif.workflow.api.model.Message;
import com.effektif.workflow.api.model.TriggerInstance;
import com.effektif.workflow.api.query.WorkflowInstanceQuery;
import com.effektif.workflow.api.types.TextType;
import com.effektif.workflow.api.workflow.ExecutableWorkflow;
import com.effektif.workflow.api.workflowinstance.ActivityInstance;
import com.effektif.workflow.api.workflowinstance.WorkflowInstance;
import com.effektif.workflow.test.WorkflowTest;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;


/**
 * @author Tom Baeyens
 */
public class SubProcessTest extends WorkflowTest {
  
  @Test
  public void testCallActivity() {
    ExecutableWorkflow subWorkflow = new ExecutableWorkflow()
      .activity("subtask", new ReceiveTask());
    
    deploy(subWorkflow);
    
    ExecutableWorkflow superWorkflow = new ExecutableWorkflow()
      .activity("call", new SubProcess()
        .subWorkflowId(subWorkflow.getId()));

    deploy(superWorkflow);

    WorkflowInstance superInstance = start(superWorkflow);

    ActivityInstance callActivityInstance = superInstance.findOpenActivityInstance("call");
    assertNotNull(callActivityInstance.getCalledWorkflowInstanceId());
    
    WorkflowInstance subInstance = workflowEngine.findWorkflowInstances(new WorkflowInstanceQuery()
        .workflowInstanceId(callActivityInstance.getCalledWorkflowInstanceId()))
      .get(0);
    
    assertNotNull(subInstance);
    
    ActivityInstance subtaskInstance = subInstance.findOpenActivityInstance("subtask");
    
    subInstance = workflowEngine.send(new Message()
      .workflowInstanceId(subInstance.getId())
      .activityInstanceId(subtaskInstance.getId()));
    
    assertTrue(subInstance.isEnded());

    superInstance = workflowEngine.findWorkflowInstances(new WorkflowInstanceQuery()
        .workflowInstanceId(superInstance.getId()))
      .get(0);
    assertTrue(superInstance.isEnded());
  }

  @Test
  public void testTwoCallActivitiesInSequence() {
    ExecutableWorkflow subWorkflow = new ExecutableWorkflow()
      .activity("auto", new NoneTask());
    
    deploy(subWorkflow);
    
    ExecutableWorkflow superWorkflow = new ExecutableWorkflow()
      .activity("call1", new SubProcess()
        .subWorkflowId(subWorkflow.getId())
        .transitionToNext())
      .activity("call2", new SubProcess()
        .subWorkflowId(subWorkflow.getId()));

    deploy(superWorkflow);

    WorkflowInstance superInstance = start(superWorkflow);
    assertTrue(superInstance.isEnded());

    List<WorkflowInstance> workflowInstances = workflowEngine.findWorkflowInstances(new WorkflowInstanceQuery());
    for (WorkflowInstance workflowInstance: workflowInstances) {
      assertTrue(workflowInstance.isEnded());
    }
    assertEquals(3, workflowInstances.size());
  }

  @Test
  public void testTwoCallActivitiesInParallel() {
    ExecutableWorkflow subWorkflow = new ExecutableWorkflow()
      .activity("auto", new NoneTask());
    
    deploy(subWorkflow);
    
    ExecutableWorkflow superWorkflow = new ExecutableWorkflow()
      .activity("call1", new SubProcess()
        .subWorkflowId(subWorkflow.getId()))
      .activity("call2", new SubProcess()
        .subWorkflowId(subWorkflow.getId()));

    deploy(superWorkflow);

    WorkflowInstance superInstance = start(superWorkflow);
    assertTrue(superInstance.isEnded());

    List<WorkflowInstance> workflowInstances = workflowEngine.findWorkflowInstances(new WorkflowInstanceQuery());
    for (WorkflowInstance workflowInstance: workflowInstances) {
      assertTrue(workflowInstance.isEnded());
    }
    assertEquals(3, workflowInstances.size());
  }

  @Test
  public void testCallActivityInputValue() {
    ExecutableWorkflow subWorkflow = new ExecutableWorkflow()
      .variable("performer", TextType.INSTANCE)
      .activity("message", msgExpression("performer"));
    
    deploy(subWorkflow);
    
    ExecutableWorkflow superWorkflow = new ExecutableWorkflow()
      .activity("call", new SubProcess()
        .inputValue("performer", "walter")
        .subWorkflowId(subWorkflow.getId()));
    
    deploy(superWorkflow);
    
    start(superWorkflow);
    
    assertEquals("walter", getMessage(0));
  }

  @Test
  public void testCallActivityInputBindingVariable() {
    ExecutableWorkflow subWorkflow = new ExecutableWorkflow()
      .variable("performer", TextType.INSTANCE)
      .activity("subtask", msgExpression("performer"));
    
    deploy(subWorkflow);
    
    ExecutableWorkflow superWorkflow = new ExecutableWorkflow()
      .variable("guineapig", TextType.INSTANCE)
      .activity("call", new SubProcess()
        .inputExpression("performer", "guineapig")
        .subWorkflowId(subWorkflow.getId()));
    
    deploy(superWorkflow);
    
    workflowEngine.start(new TriggerInstance()
      .workflowId(superWorkflow.getId())
      .data("guineapig", "walter")
    );

    assertEquals("walter", getMessage(0));
  }


  @Test
  public void testNestedSubprocesses() {
    ExecutableWorkflow innerWorkflow = new ExecutableWorkflow()
        .activity("inner", msgValue("inner"));
    deploy(innerWorkflow);

    ExecutableWorkflow intermediateWorkflow = new ExecutableWorkflow()
        .activity("intermediate before inner", msgValue("intermediate before inner").transitionToNext())
        .activity("call-inner", new SubProcess()
            .subWorkflowId(innerWorkflow.getId())
            .transitionToNext())
        .activity("intermediate after inner", msgValue("intermediate after inner"));
    deploy(intermediateWorkflow);

    ExecutableWorkflow outerWorkflow = new ExecutableWorkflow()
        .activity("outer before intermediate", msgValue("outer before intermediate").transitionToNext())
        .activity("call-intermediate", new SubProcess()
            .subWorkflowId(intermediateWorkflow.getId())
            .transitionToNext())
        .activity("outer after intermediate", msgValue("outer after intermediate"));
    deploy(outerWorkflow);

    workflowEngine.start(new TriggerInstance()
        .workflowId(outerWorkflow.getId())
    );

    List<String> expectedMessages = new ArrayList<>();
    expectedMessages.add("outer before intermediate");
    expectedMessages.add("intermediate before inner");
    expectedMessages.add("inner");
    expectedMessages.add("intermediate after inner");
    expectedMessages.add("outer after intermediate");
    assertEquals(expectedMessages, getMessages());
  }

}
