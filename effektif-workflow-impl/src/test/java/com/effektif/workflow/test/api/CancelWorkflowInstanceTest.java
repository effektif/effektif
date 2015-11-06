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

import static org.junit.Assert.*;

import org.junit.Test;

import com.effektif.workflow.api.activities.ReceiveTask;
import com.effektif.workflow.api.workflow.ExecutableWorkflow;
import com.effektif.workflow.api.workflowinstance.ActivityInstance;
import com.effektif.workflow.api.workflowinstance.WorkflowInstance;
import com.effektif.workflow.test.WorkflowTest;


/**
 * @author Tom Baeyens
 */
public class CancelWorkflowInstanceTest extends WorkflowTest {
  
  @Test
  public void testWorkflowInstanceCancellation() {
    ExecutableWorkflow workflow = new ExecutableWorkflow()
      .activity("one", new ReceiveTask());
    
    deploy(workflow);
    
    WorkflowInstance workflowInstance = start(workflow);
    
    workflowInstance = workflowEngine.cancel(workflowInstance.getId());
    
    assertTrue(workflowInstance.isEnded());
    assertNotNull(workflowInstance.getEnd());
    assertEquals(WorkflowInstance.ENDSTATE_CANCELED, workflowInstance.getEndState());
    
    ActivityInstance activityInstance = workflowInstance.getActivityInstances().get(0);
    assertTrue(activityInstance.isEnded());
    assertNotNull(activityInstance.getEnd());
    assertEquals(WorkflowInstance.ENDSTATE_CANCELED, activityInstance.getEndState());
  }
}
