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

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.effektif.workflow.api.activities.ReceiveTask;
import com.effektif.workflow.api.workflow.ExecutableWorkflow;
import com.effektif.workflow.api.workflow.Transition;
import com.effektif.workflow.api.workflowinstance.WorkflowInstance;
import com.effektif.workflow.test.WorkflowTest;


/**
 * @author Tom Baeyens
 */
public class SequentialExecutionTest extends WorkflowTest {
  
  @Test
  public void testSequentialExecution() {
    ExecutableWorkflow workflow = new ExecutableWorkflow()
      .activity("one", new ReceiveTask()
        .transitionTo("two"))
      .activity("two", new ReceiveTask()
        .transitionTo("three"))
      .activity("three", new ReceiveTask());
    
    deploy(workflow);
    
    WorkflowInstance workflowInstance = start(workflow);
    
    assertOpen(workflowInstance, "one");
    
    String oneId = getActivityInstanceId(workflowInstance, "one");
    
    workflowInstance = sendMessage(workflowInstance, oneId);

    assertOpen(workflowInstance, "two");
    
    String twoId = getActivityInstanceId(workflowInstance, "two");
    
    workflowInstance = sendMessage(workflowInstance, twoId);

    assertOpen(workflowInstance, "three");
    
    String threeId = getActivityInstanceId(workflowInstance, "three");

    workflowInstance = sendMessage(workflowInstance, threeId);

    assertTrue(workflowInstance.isEnded());
  }

  @Test
  public void testDanglingTransition() {
    ExecutableWorkflow workflow = new ExecutableWorkflow()
      .activity("one", new ReceiveTask())
      .transition(new Transition().fromId("one"));
    
    deploy(workflow);
    
    WorkflowInstance workflowInstance = start(workflow);
    
    assertOpen(workflowInstance, "one");
    
    String oneId = getActivityInstanceId(workflowInstance, "one");
    
    workflowInstance = sendMessage(workflowInstance, oneId);

    assertTrue(workflowInstance.isEnded());
  }
}
