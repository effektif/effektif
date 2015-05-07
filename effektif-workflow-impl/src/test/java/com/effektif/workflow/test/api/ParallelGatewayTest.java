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

import com.effektif.workflow.api.activities.EndEvent;
import com.effektif.workflow.api.activities.ExclusiveGateway;
import com.effektif.workflow.api.activities.JavaServiceTask;
import com.effektif.workflow.api.activities.ParallelGateway;
import com.effektif.workflow.api.activities.StartEvent;
import com.effektif.workflow.api.condition.LessThan;
import com.effektif.workflow.api.model.TriggerInstance;
import com.effektif.workflow.api.types.NumberType;
import com.effektif.workflow.api.workflow.Transition;
import com.effektif.workflow.api.workflow.Workflow;
import com.effektif.workflow.api.workflowinstance.WorkflowInstance;
import com.effektif.workflow.test.WorkflowTest;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests BPMN execution semantics for parallel executions using parallel gateways, including some edge cases.
 *
 * @author Tom Baeyens
 */
public class ParallelGatewayTest extends WorkflowTest {

  /**
   * Tests that two parallel tasks must complete before the first task after a parallel join is open.
   * <pre>
   *
   *  ◯─→<+>─→[t1]─→<+>─→[after]─→◯
   *      │          ↑
   *      └──→[t2]───┘
   *
   * </pre>
   */
  @Test
  public void testSimpleParallelGateway() {
    Workflow workflow = new Workflow()
      .activity("start", new StartEvent()
        .transitionTo("fork"))
      .activity("fork", new ParallelGateway()
        .transitionTo("t1")
        .transitionTo("t2"))
      .activity("t1", new JavaServiceTask()
        .transitionTo("join"))
      .activity("t2", new JavaServiceTask()
        .transitionTo("join"))
      .activity("join", new ParallelGateway()
        .transitionTo("afterJoinTask"))
      .activity("afterJoinTask", new JavaServiceTask()
        .transitionTo("end"))
      .activity("end", new StartEvent());

    deploy(workflow);
    WorkflowInstance workflowInstance = start(workflow);

    assertOpen(workflowInstance, "t1", "t2");

    workflowInstance = endTask(workflowInstance, "t1");
    assertOpen(workflowInstance, "t2");

    workflowInstance = endTask(workflowInstance, "t2");
    assertOpen(workflowInstance, "afterJoinTask");
  }

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
  public void testComplexParallelGateway() {

    Workflow workflow = new Workflow()
      .activity("start", new StartEvent()
        .transitionTo("f1"))
      .activity("f1", new ParallelGateway()
        .transitionTo("f2")
        .transitionTo("t3"))
      .activity("f2", new ParallelGateway().transitionTo("t1").transitionTo("t2"))
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

  /**
   * TODO Work out what this is supposed to be testing and finish the test
   * <pre>
   *
   *  [one]──→<+>──→[a]
   *    │      │
   *    ↓      └───→[b]
   *  [two]
   *
   * </pre>
   */
  @Test
  public void testCombineDefaultOutgoingParallelWithGatewayParallel() {
    Workflow workflow = new Workflow()
      .activity("one", new JavaServiceTask()
        .transitionTo("two")
        .transitionTo("fork"))
      .activity("two", new JavaServiceTask())
      .activity("fork", new ParallelGateway()
        .transitionTo("a")
        .transitionTo("b"))
      .activity("a", new JavaServiceTask())
      .activity("b", new JavaServiceTask());

    deploy(workflow);
    WorkflowInstance workflowInstance = start(workflow);
    assertOpen(workflowInstance, "one");
  }

  /**
   * Tests that a workflow can have both explicit and implicit parallel forking.
   * <pre>
   *
   *              +-->[t1]---+
   *              |          |
   *  [start]-+->[f]        [j]--+
   *          |   |          |   |
   *          |   +-->[t2]---+   |
   *          +------------------+
   *
   * </pre>
   * TODO Fix test, which fails since migration from the product.
   */
//  @Test
  public void testJoinInLoop() {
    Workflow workflow = new Workflow()
      .activity("start", new StartEvent()
        .transitionTo("fork"))
      .activity("fork", new ParallelGateway()
        .transitionTo("t1")
        .transitionTo("t2"))
      .activity("t1", new JavaServiceTask()
        .transitionTo("join"))
      .activity("t2", new JavaServiceTask()
        .transitionTo("join"))
      .activity("join", new ParallelGateway()
        .transitionTo("fork"));

    deploy(workflow);
    WorkflowInstance workflowInstance = start(workflow);

    assertOpen(workflowInstance, "t1", "t2");

    // First time around…

    workflowInstance = endTask(workflowInstance, "t1");
    assertOpen(workflowInstance, "t2");

    workflowInstance = endTask(workflowInstance, "t2");
    assertOpen(workflowInstance, "t1", "t2");

    // Second time around…

    workflowInstance = endTask(workflowInstance, "t1");
    assertOpen(workflowInstance, "t2");

    workflowInstance = endTask(workflowInstance, "t2");
    assertOpen(workflowInstance, "t1", "t2");
  }

  /**
   * Tests that the process instance finishes even though one of the flows has no target.
   * <pre>
   *
   *  ◯─→<+>─→[t1]─→◯
   *      │
   *      └──→
   *
   * </pre>
   */
  @Test
  public void testLooseEnd() {
    Workflow workflow = new Workflow()
      .activity("start", new StartEvent().transitionTo("fork"))
      .activity("fork", new ParallelGateway()
        .transitionTo("t1")
        .transitionTo((String) null))
      .activity("t1", new JavaServiceTask().transitionTo("end"))
      .activity("end", new StartEvent());

    deploy(workflow);
    WorkflowInstance workflowInstance = start(workflow);

    assertOpen(workflowInstance, "t1");

    workflowInstance = endTask(workflowInstance, "t1");
    assertTrue(workflowInstance.isEnded());
  }

  /**
   * Tests that the process instance finishes even though the gateway has no valid outgoing flow.
   * <pre>
   *
   *  ◯─→<+>─→
   *
   * </pre>
   */
  @Test
  public void testHasNoValidOutgoingFlow() {
    Workflow workflow = new Workflow()
      .activity("start", new StartEvent()
        .transitionTo("fork"))
      .activity("fork", new ParallelGateway().transitionTo((String) null));

    deploy(workflow);
    WorkflowInstance workflowInstance = start(workflow);

    assertTrue(workflowInstance.isEnded());
  }

  /**
   * Tests that an XOR following an AND works as expected.
   * <pre>
   *
   *                +--->[exclGw]-->[t1]--->[join1]---+
   *                |       |                 ^       |
   *                |       +------>[t2]------+       v
   *  [start]--->[fork]                            [join2]-->[end]
   *                +-------------->[t3]--------------^
   *
   * </pre>
   */
  @Test
  public void testAndXorCombination() {
    Workflow workflow = new Workflow()
      .variable("dealSize", new NumberType())
      .activity("start", new StartEvent()
        .transitionTo("fork"))
      .activity("fork", new ParallelGateway()
        .transitionTo("exclusiveGateway")
        .transitionTo("t3"))
      .activity("exclusiveGateway", new ExclusiveGateway()
        .transitionTo(new Transition()
          .id("tr1")
          .to("t1")
          .condition(new LessThan()
            .leftExpression("dealSize")
            .rightValue(1000L)))
        .transitionTo(new Transition()
          .id("tr2")
          .to("t2")
          .condition(new LessThan()
            .leftExpression("dealSize")
            .rightValue(5000L))))
      .activity("join1", new ExclusiveGateway()
        .transitionTo("join2"))
      .activity("join2", new ParallelGateway()
        .transitionTo("end"))
      .activity("t1", new JavaServiceTask()
        .transitionTo("join1"))
      .activity("t2", new JavaServiceTask()
        .transitionTo("join1"))
      .activity("t3", new JavaServiceTask().transitionTo("join2"))
      .activity("end", new EndEvent());

    deploy(workflow);
    TriggerInstance trigger = new TriggerInstance().workflowId(workflow.getId()).data("dealSize", 3000L);
    WorkflowInstance workflowInstance = workflowEngine.start(trigger);

    assertOpen(workflowInstance, "t2", "t3");

    workflowInstance = endTask(workflowInstance, "t2");
    assertOpen(workflowInstance, "t3");

    workflowInstance = endTask(workflowInstance, "t3");
    assertTrue(workflowInstance.isEnded());
  }

  /**
   * Tests that an XOR following an AND works as expected.
   * <pre>
   *
   *                +--->[exclGw]-->[t1]--->[join1]---+
   *                |       |                         |
   *                |       +------>                  v
   *  [start]--->[fork]                            [join2]-->[end]
   *                +-------------->[t3]--------------^
   *
   * </pre>
   * Would this be an improperly configured process?
   * Anyway, somehow this case has to be handled as well.
   */
  @Test
  public void testAndXorCombinationWithLooseEnd() {
    Workflow workflow = new Workflow()
      .variable("dealSize", new NumberType())
      .activity("start", new StartEvent().transitionTo("fork"))
      .activity("fork", new ParallelGateway()
        .transitionTo("exclusiveGateway")
        .transitionTo("t3"))
      .activity("exclusiveGateway", new ExclusiveGateway()
        .transitionTo(new Transition()
          .id("tr1")
          .to("t1")
          .condition(new LessThan()
            .leftExpression("dealSize")
            .rightValue(1000L)))
        .transitionTo(
          new Transition().id("tr2").to(null).condition(new LessThan().leftExpression("dealSize").rightValue(5000L))))
      .activity("join1", new ExclusiveGateway()
        .transitionTo("join2"))
      .activity("join2", new ParallelGateway()
        .transitionTo("end"))
      .activity("t1", new JavaServiceTask()
        .transitionTo("join1"))
      .activity("t3", new JavaServiceTask()
        .transitionTo("join2"))
      .activity("end", new EndEvent());

    deploy(workflow);
    TriggerInstance trigger = new TriggerInstance().workflowId(workflow.getId()).data("dealSize", 3000L);
    WorkflowInstance workflowInstance = workflowEngine.start(trigger);

    assertOpen(workflowInstance, "t3");
    assertFalse(workflowInstance.isEnded());

    workflowInstance = endTask(workflowInstance, "t3");
    assertTrue(workflowInstance.isEnded());
  }

  /**
   * Tests that an AND join is only finished if all incoming flows are satisfied.
   * <pre>
   *
   *               +---->[t1]-->[t2]---+
   *               |                   v
   *  [start]-->[fork]-->[t3]------->[join]-->[t4]-->[end]
   *
   * </pre>
   */
  @Test
  public void testMultipleStepsBeforeJoin() {
    Workflow workflow = new Workflow()
      .activity("start", new StartEvent()
        .transitionTo("fork"))
      .activity("fork", new ParallelGateway()
        .transitionTo("t1")
        .transitionTo("t3"))
      .activity("t1", new JavaServiceTask()
        .transitionTo("t2"))
      .activity("t2", new JavaServiceTask()
        .transitionTo("join"))
      .activity("t3", new JavaServiceTask()
        .transitionTo("join"))
      .activity("join", new ParallelGateway()
        .transitionTo("t4"))
      .activity("t4", new JavaServiceTask()
        .transitionTo("end"))
      .activity("end", new StartEvent());

    deploy(workflow);
    WorkflowInstance workflowInstance = start(workflow);

    assertOpen(workflowInstance, "t1", "t3");

    workflowInstance = endTask(workflowInstance, "t3");
    assertOpen(workflowInstance, "t1");

    workflowInstance = endTask(workflowInstance, "t1");
    assertOpen(workflowInstance, "t2");

    workflowInstance = endTask(workflowInstance, "t2");
    assertOpen(workflowInstance, "t4");

    workflowInstance = endTask(workflowInstance, "t4");
    assertTrue(workflowInstance.isEnded());
  }
}
