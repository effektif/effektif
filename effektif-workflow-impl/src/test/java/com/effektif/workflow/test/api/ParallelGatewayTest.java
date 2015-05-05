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

import com.effektif.workflow.api.activities.EndEvent;
import com.effektif.workflow.api.activities.ExclusiveGateway;
import com.effektif.workflow.api.activities.JavaServiceTask;
import com.effektif.workflow.api.activities.ParallelGateway;
import com.effektif.workflow.api.activities.StartEvent;
import com.effektif.workflow.api.workflow.Transition;
import com.effektif.workflow.api.workflow.Workflow;
import com.effektif.workflow.api.workflowinstance.WorkflowInstance;
import com.effektif.workflow.test.WorkflowTest;

/**
 * @author Walter White
 */
/**
 * @author Tom Baeyens
 */
public class ParallelGatewayTest extends WorkflowTest {

  /*
                    +-->[t1]------+
                    |             |
               +-->[f2]           |
               |    |             |
   [start]-->[f1]   +-->[t2]-+   [j1]-->[end]
               |             |    |
               |            [j2]--+
               |             |
               +-->[t3]------+
  */
  @Test
  public void testParallelGateway() {

    Workflow workflow = new Workflow()
      .activity("start", new StartEvent()
        .transitionTo("f1"))
      .activity("f1", new ParallelGateway()
        .transitionTo("f2")
        .transitionTo("t3"))
      .activity("f2", new ParallelGateway()
        .transitionTo("t1")
        .transitionTo("t2"))
      .activity("t1", new JavaServiceTask()
        .transitionTo("j1"))
      .activity("t2", new JavaServiceTask()
        .transitionTo("j2"))
      .activity("t3", new JavaServiceTask()
        .transitionTo("j2"))
      .activity("j1", new ParallelGateway()
        .transitionTo("end"))
      .activity("j2", new ParallelGateway()
        .transitionTo("j1"))
      .activity("end", new EndEvent());
        
    deploy(workflow);
    
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

  @Test
  public void testParallelGatewayOnwardsOnOtherPathEnd() {
    /* [t1]-------+
                  |
                 [+]-->[t3]
                  |
       [t2]--[x]--+
              |
              o   
       
       When starting this workflow, t1 and t2 are created.
       We complete t1 which puts 1 joining token in the + parallel gateway.
       We then complete t2 and make sure that the exclusive gateway 
       takes the transition to the end event.
       The fact that there is nothing more to do should fire the + parallel gateway. 
    */

    Workflow workflow = new Workflow()
      .activity("t1", new JavaServiceTask()
        .transitionTo("+"))
      .activity("t2", new JavaServiceTask()
        .transitionTo("x"))
      .activity("x", new ExclusiveGateway()
        .defaultTransitionId("default") // let's set the transition to 'o' as the default one
        .transitionTo(new Transition()
          .id("default").to("o"))
        .transitionTo("+")) // this transition will never be taken
      .activity("o", new EndEvent())
      .activity("+", new ParallelGateway()
        .transitionTo("t3"))
      .activity("t3", new JavaServiceTask());
        
    deploy(workflow);
    
    WorkflowInstance workflowInstance = start(workflow);

    assertOpen(workflowInstance, "t1", "t2");

    workflowInstance = endTask(workflowInstance, "t1");

    assertOpen(workflowInstance, "t2");

    workflowInstance = endTask(workflowInstance, "t2");

    assertOpen(workflowInstance, "t3");
  }
}
