/* Copyright (c) 2014, Effektif GmbH.
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
 * limitations under the License. */
package com.effektif.workflow.test.execution;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.effektif.workflow.api.activities.EndEvent;
import com.effektif.workflow.api.activities.ParallelGateway;
import com.effektif.workflow.api.activities.StartEvent;
import com.effektif.workflow.api.activities.UserTask;
import com.effektif.workflow.api.workflow.Workflow;
import com.effektif.workflow.api.workflowinstance.WorkflowInstance;
import com.effektif.workflow.test.WorkflowTest;

/**
 * @author Walter White
 */
public class ParallelGatewayTest extends WorkflowTest {
  
  @Test
  public void testParallelGateway() {
    /*                +-->[t1]------+
                      |             |
                 +-->[f2]           |
                 |    |             | 
     [start]-->[f1]   +-->[t2]-+   [j1]-->[end]
                 |             |    |
                 |            [j2]--+
                 |             |
                 +-->[t3]------+ 
    */

    Workflow workflow = new Workflow()
      .activity(new StartEvent("start")
        .transitionTo("f1"))
      .activity(new ParallelGateway("f1")
        .transitionTo("f2")
        .transitionTo("t3"))
      .activity(new ParallelGateway("f2")
        .transitionTo("t1")
        .transitionTo("t2"))
      .activity(new UserTask("t1")
        .transitionTo("j1"))
      .activity(new UserTask("t2")
        .transitionTo("j2"))
      .activity(new UserTask("t3")
        .transitionTo("j2"))
      .activity(new ParallelGateway("j1")
        .transitionTo("end"))
      .activity(new ParallelGateway("j2")
        .transitionTo("j1"))
      .activity(new EndEvent("end"));
        
    workflow = deploy(workflow);
    
    WorkflowInstance workflowInstance = start(workflow);

    assertOpen(workflowInstance, "t1", "t2", "t3");

    workflowInstance = endTask(workflowInstance, "t1");

    assertOpen(workflowInstance, "t2", "t3");

    workflowInstance = endTask(workflowInstance, "t2");

    assertOpen(workflowInstance, "t3");

    workflowInstance = endTask(workflowInstance, "t3");

    assertOpen(workflowInstance);

    assertTrue(workflowInstance.isEnded());
  }
}
