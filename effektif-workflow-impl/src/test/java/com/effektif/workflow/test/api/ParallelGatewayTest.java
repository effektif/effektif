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

import com.effektif.workflow.api.activities.EndEvent;
import com.effektif.workflow.api.activities.ExclusiveGateway;
import com.effektif.workflow.api.activities.NoneTask;
import com.effektif.workflow.api.activities.ParallelGateway;
import com.effektif.workflow.api.activities.ReceiveTask;
import com.effektif.workflow.api.activities.StartEvent;
import com.effektif.workflow.api.condition.Equals;
import com.effektif.workflow.api.condition.LessThan;
import com.effektif.workflow.api.model.TriggerInstance;
import com.effektif.workflow.api.model.VariableValues;
import com.effektif.workflow.api.types.BooleanType;
import com.effektif.workflow.api.types.NumberType;
import com.effektif.workflow.api.workflow.ExecutableWorkflow;
import com.effektif.workflow.api.workflow.Transition;
import com.effektif.workflow.api.workflowinstance.WorkflowInstance;
import com.effektif.workflow.test.WorkflowTest;

/**
 * Tests BPMN execution semantics for parallel executions using parallel gateways.
 *
 * @author Tom Baeyens
 */
public class ParallelGatewayTest extends WorkflowTest {

  /**
   * Tests that two parallel tasks must complete before the first task after a parallel join is open.
   * <pre>
   *
   *  ◯─→<+>─→[t1]─→<+>─→[after]
   *      │          ↑
   *      └──→[t2]───┘
   *
   * </pre>
   */
  @Test
  public void testParallelTasks() {
    ExecutableWorkflow workflow = new ExecutableWorkflow()
      .activity("start", new StartEvent()
        .transitionTo("fork"))
      .activity("fork", new ParallelGateway().transitionTo("t1").transitionTo("t2"))
      .activity("t1", new ReceiveTask()
        .transitionTo("join"))
      .activity("t2", new ReceiveTask()
        .transitionTo("join"))
      .activity("join", new ParallelGateway()
        .transitionTo("afterJoinTask"))
      .activity("afterJoinTask", new ReceiveTask());

    deploy(workflow);
    WorkflowInstance workflowInstance = start(workflow);

    assertOpen(workflowInstance, "t1", "t2");

    workflowInstance = endTask(workflowInstance, "t1");
    assertOpen(workflowInstance, "t2");

    workflowInstance = endTask(workflowInstance, "t2");
    assertOpen(workflowInstance, "afterJoinTask");
  }

  /**
   * Tests that the process ends after two parallel automatic tasks.
   * <pre>
   *
   *  ◯─→<+>─→[t1]─→<+>─→◯
   *      │          ↑
   *      └──→[t2]───┘
   *
   * </pre>
   */
  @Test
  public void testAutomaticTasks() {
    ExecutableWorkflow workflow = new ExecutableWorkflow()
      .activity("start", new StartEvent().transitionTo("fork"))
      .activity("fork", new ParallelGateway()
        .transitionTo("t1")
        .transitionTo("t2"))
      .activity("t1", new NoneTask()
        .transitionTo("join"))
      .activity("t2", new NoneTask()
        .transitionTo("join"))
      .activity("join", new ParallelGateway()
        .transitionTo("end"))
      .activity("end", new EndEvent());

    deploy(workflow);
    WorkflowInstance workflowInstance = start(workflow);
    assertTrue(workflowInstance.isEnded());
  }

  /**
   * Tests that the process ends after two parallel automatic tasks.
   * <pre>
   *
   *  ◯─→<+>─→[t1]─→[t3]─→[t4]─→<+>─→◯
   *      │                      ↑
   *      └──→[t2]───────────────┘
   *
   * </pre>
   */
  @Test
  public void testMixedTasks() {
    // @formatter:off
    ExecutableWorkflow workflow = new ExecutableWorkflow()
      .activity("start", new StartEvent()
        .transitionTo("fork"))
      .activity("fork", new ParallelGateway()
        .transitionTo("t1")
        .transitionTo("t2"))
      .activity("t1", new NoneTask()
        .transitionTo("t3"))
      .activity("t3", new ReceiveTask()
        .transitionTo("t4"))
      .activity("t4", new NoneTask()
        .transitionTo("join"))
      .activity("t2", new ReceiveTask()
        .transitionTo("join"))
      .activity("join", new ParallelGateway()
        .transitionTo("end"))
      .activity("end", new EndEvent());
    // @formatter:on

    deploy(workflow);

    WorkflowInstance workflowInstance = start(workflow);
    assertOpen(workflowInstance, "t2", "t3");

    workflowInstance = endTask(workflowInstance, "t2");
    workflowInstance = endTask(workflowInstance, "t3");
    assertTrue(workflowInstance.isEnded());
  }

  /**
   * Tests that the process only ends after all parallel automatic tasks are complete.
   * <pre>
   *
   *      ┌──→[t1]───┐
   *      │          ↓
   *  ◯─→<+>─→[t2]─→<+>─→◯
   *      │          ↑
   *      └──→[t3]───┘
   *
   * </pre>
   */
  @Test
  public void testMultiplePaths() {
    // @formatter:off
    ExecutableWorkflow workflow = new ExecutableWorkflow()
      .activity("start", new StartEvent()
        .transitionTo("fork"))
      .activity("fork", new ParallelGateway()
        .transitionTo("t1")
        .transitionTo("t2")
        .transitionTo("t3"))
      .activity("t1", new ReceiveTask()
        .transitionTo("join"))
      .activity("t2", new ReceiveTask()
        .transitionTo("join"))
      .activity("t3", new ReceiveTask()
        .transitionTo("join"))
      .activity("join", new ParallelGateway()
        .transitionTo("end"))
      .activity("end", new EndEvent());
    // @formatter:on

    deploy(workflow);

    WorkflowInstance workflowInstance = start(workflow);
    assertFalse(workflowInstance.isEnded());

    workflowInstance = endTask(workflowInstance, "t1");
    assertFalse(workflowInstance.isEnded());

    workflowInstance = endTask(workflowInstance, "t2");
    assertFalse(workflowInstance.isEnded());

    workflowInstance = endTask(workflowInstance, "t3");
    assertTrue(workflowInstance.isEnded());
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

    ExecutableWorkflow workflow = new ExecutableWorkflow()
      .activity("start", new StartEvent()
        .transitionTo("f1"))
      .activity("f1", new ParallelGateway()
        .transitionTo("f2")
        .transitionTo("t3"))
      .activity("f2", new ParallelGateway().transitionTo("t1").transitionTo("t2"))
      .activity("t1", new ReceiveTask().transitionTo("j1"))
      .activity("t2", new ReceiveTask()
        .transitionTo("j2"))
      .activity("t3", new ReceiveTask()
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
  public void testComplexParallelGatewayAutomaticTasks() {

    ExecutableWorkflow workflow = new ExecutableWorkflow()
      .activity("start", new StartEvent()
        .transitionTo("f1"))
      .activity("f1", new ParallelGateway()
        .transitionTo("f2")
        .transitionTo("t3"))
      .activity("f2", new ParallelGateway()
        .transitionTo("t1")
        .transitionTo("t2"))
      .activity("t1", new NoneTask()
        .transitionTo("j1"))
      .activity("t2", new NoneTask()
        .transitionTo("j2"))
      .activity("t3", new NoneTask()
        .transitionTo("j2"))
      .activity("j1", new ParallelGateway()
        .transitionTo("end"))
      .activity("j2", new ParallelGateway()
        .transitionTo("j1"))
      .activity("end", new EndEvent());

    deploy(workflow);
    WorkflowInstance workflowInstance = start(workflow);
    assertTrue(workflowInstance.isEnded());
  }

  @Test
  public void testParallelGatewayOnwardsOnOtherPathEnd() {
    /* [t1]-------+
                  |
                 <+>-->[t3]
                  |
       [t2]--<x>--+
              |
              o   
       
       When starting this workflow, t1 and t2 are created.
       We complete t1 which puts 1 joining token in the + parallel gateway.
       We then complete t2 and make sure that the exclusive gateway 
       takes the transition to the end event.
       The fact that there is nothing more to do should fire the + parallel gateway.
    */

    ExecutableWorkflow workflow = new ExecutableWorkflow()
      .activity("t1", new ReceiveTask()
        .transitionTo("+"))
      .activity("t2", new ReceiveTask()
        .transitionTo("x"))
      .activity("x", new ExclusiveGateway()
        .defaultTransitionId("default") // let's set the transition to 'o' as the default one
        .transitionTo(new Transition()
          .id("default").toId("o"))
        .transitionTo("+")) // this transition will never be taken
      .activity("o", new EndEvent())
      .activity("+", new ParallelGateway()
        .transitionTo("t3"))
      .activity("t3", new ReceiveTask());
        
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
    ExecutableWorkflow workflow = new ExecutableWorkflow()
      .activity("one", new ReceiveTask()
        .transitionTo("two")
        .transitionTo("fork"))
      .activity("two", new ReceiveTask())
      .activity("fork", new ParallelGateway()
        .transitionTo("a")
        .transitionTo("b"))
      .activity("a", new ReceiveTask())
      .activity("b", new ReceiveTask());

    deploy(workflow);
    WorkflowInstance workflowInstance = start(workflow);
    assertOpen(workflowInstance, "one");

    workflowInstance = endTask(workflowInstance, "one");
    assertOpen(workflowInstance, "two", "a", "b");

    workflowInstance = endTask(workflowInstance, "a");
    assertOpen(workflowInstance, "two", "b");

    workflowInstance = endTask(workflowInstance, "two");
    assertOpen(workflowInstance, "b");
    assertFalse(workflowInstance.isEnded());

    workflowInstance = endTask(workflowInstance, "b");
    assertTrue(workflowInstance.isEnded());
  }

  /**
   * Tests that parallel forks and joins work with a loop.
   * <pre>
   *
   *              +-->[t1]---+
   *              |          |
   *  [start]-+->[f]        [j]--+
   *          ^   |          |   |
   *          |   +-->[t2]---+   |
   *          +------------------+
   *
   * </pre>
   * TODO Fix test, which fails since migration from the product.
   */
  @Test
  public void testJoinInLoop() {
    ExecutableWorkflow workflow = new ExecutableWorkflow()
      .activity("start", new StartEvent()
        .transitionTo("fork"))
      .activity("fork", new ParallelGateway().transitionTo("t1").transitionTo("t2"))
      .activity("t1", new ReceiveTask()
        .transitionTo("join"))
      .activity("t2", new ReceiveTask()
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
    ExecutableWorkflow workflow = new ExecutableWorkflow()
      .activity("start", new StartEvent().transitionTo("fork"))
      .activity("fork", new ParallelGateway()
        .transitionTo("t1")
        .transitionTo((String) null))
      .activity("t1", new ReceiveTask().transitionTo("end"))
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
    ExecutableWorkflow workflow = new ExecutableWorkflow()
      .activity("start", new StartEvent().transitionTo("fork"))
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
    ExecutableWorkflow workflow = new ExecutableWorkflow()
      .variable("dealSize", new NumberType())
      .activity("start", new StartEvent().transitionTo("fork"))
      .activity("fork", new ParallelGateway()
        .transitionTo("exclusiveGateway")
        .transitionTo("t3"))
      .activity("exclusiveGateway", new ExclusiveGateway()
        .transitionTo(new Transition()
          .id("tr1")
          .toId("t1")
          .condition(new LessThan()
            .leftExpression("dealSize")
            .rightValue(1000L)))
        .transitionTo(
          new Transition().id("tr2").toId("t2").condition(new LessThan().leftExpression("dealSize").rightValue(5000L))))
      .activity("join1", new ExclusiveGateway()
        .transitionTo("join2"))
      .activity("join2", new ParallelGateway()
        .transitionTo("end"))
      .activity("t1", new ReceiveTask()
        .transitionTo("join1"))
      .activity("t2", new ReceiveTask()
        .transitionTo("join1"))
      .activity("t3", new ReceiveTask().transitionTo("join2"))
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
    ExecutableWorkflow workflow = new ExecutableWorkflow()
      .variable("dealSize", new NumberType())
      .activity("start", new StartEvent().transitionTo("fork"))
      .activity("fork", new ParallelGateway()
        .transitionTo("exclusiveGateway")
        .transitionTo("t3"))
      .activity("exclusiveGateway", new ExclusiveGateway()
        .transitionTo(new Transition()
          .id("tr1")
          .toId("t1")
          .condition(new LessThan()
            .leftExpression("dealSize")
            .rightValue(1000L)))
        .transitionTo(
          new Transition().id("tr2").toId(null).condition(new LessThan().leftExpression("dealSize").rightValue(5000L))))
      .activity("join1", new ExclusiveGateway()
        .transitionTo("join2"))
      .activity("join2", new ParallelGateway()
        .transitionTo("end"))
      .activity("t1", new ReceiveTask()
        .transitionTo("join1"))
      .activity("t3", new ReceiveTask()
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
   *               +-->[t1]-->[t2]--+
   *               |                v
   *  [start]-->[fork]--->[t3]--->[join]-->[t4]-->[end]
   *               |                ^
   *               +----->[t5]------+
   *
   * </pre>
   */
  @Test
  public void testMultipleStepsBeforeJoin() {
    ExecutableWorkflow workflow = new ExecutableWorkflow()
      .activity("start", new StartEvent().transitionTo("fork"))
      .activity("fork", new ParallelGateway().transitionTo("t1").transitionTo("t3").transitionTo("t5"))
      .activity("t1", new ReceiveTask()
        .transitionTo("t2"))
      .activity("t2", new ReceiveTask()
        .transitionTo("join"))
      .activity("t3", new ReceiveTask()
        .transitionTo("join"))
      .activity("t5", new NoneTask()
        .transitionTo("join"))
      .activity("join", new ParallelGateway()
        .transitionTo("t4"))
      .activity("t4", new ReceiveTask().transitionTo("end"))
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

  /**
   * Tests that a parallel gateway inside an exclusive gateway works.
   * The loop condition checks a variable that is changed the second time, so the loop only repeats once.
   * {@code
   *
   *             ┌──→[t1]───┐
   *             │          │
   *  ◯─→<eg1>─→<+>─→[t2]──<+>─→[t3]─→<eg2>─→◯
   *       ↑                            │
   *       └────────────────────────────┘
   * }
   * TODO Resolve the exclusive gateway issue and make the test pass; see ExclusiveGatewayTest.testSimpleCondition
   */
  @Test
  public void testParallelGatewayInsideExclusiveGateway() {
    // @formatter:off
    ExecutableWorkflow workflow = new ExecutableWorkflow()
      .variable("repeatRequired", new BooleanType())
      .activity("start", new StartEvent()
        .transitionTo("eg1"))
      .activity("eg1", new ExclusiveGateway()
        .transitionTo(new Transition().id("default").toId("fork"))
        .defaultTransitionId("default"))
      .activity("fork", new ParallelGateway()
        .transitionTo("t1")
        .transitionTo("t2"))
      .activity("t1", new NoneTask()
        .transitionTo("join"))
      .activity("t2", new NoneTask()
        .transitionTo("join"))
      .activity("join", new ParallelGateway()
        .transitionTo("t3"))
      .activity("t3", new ReceiveTask()
        .transitionTo("eg2"))
      .activity("eg2", new ExclusiveGateway()
        .transitionTo(new Transition().toId("end")
          .condition(new Equals().leftExpression("repeatRequired").rightValue(Boolean.FALSE)))
        .transitionTo(new Transition().toId("fork")
          .condition(new Equals().leftExpression("repeatRequired").rightValue(Boolean.TRUE))))
      .activity("end", new StartEvent());
    // @formatter:on

    deploy(workflow);

    WorkflowInstance workflowInstance = start(createTriggerInstance(workflow)
            .data("repeatRequired", Boolean.TRUE));

    // TODO assertion fails because workflow has ended with ended activity instances start and eg1 only
    // TODO … so figure out why it doesn't wait at the receive task.
    assertOpen(workflowInstance, "t3");
    workflowInstance = endTask(workflowInstance, "t3");

    assertOpen(workflowInstance, "t3");

    VariableValues variableValues = new VariableValues();
    variableValues.setValue("repeatRequired", Boolean.FALSE);
    workflowEngine.setVariableValues(workflowInstance.getId(), variableValues);
    workflowInstance = endTask(workflowInstance, "t3");
    assertTrue(workflowInstance.isEnded());
  }

  /**
   * Tests that the process ends after one parallel flow results in an end event (after t2),
   * which means that only one flow arrives at the join (after t1), but the engine should continue because
   * there are no remaining executions.
   * <pre>
   *
   *  (start)→<fork>─-→[t1]─→<join>─→(end)
   *      │              ↑
   *      └─→<condition>─┘
   *            │
   *            └─→[t2]─→(parallelEnd)
   *
   * </pre>
   * TODO Instead of ending, the process gets into an infinite loop, repeating the flow from 'join' to 'end'.
   */
  @Test
  public void testParallelFlowEndEvent() {
    // @formatter:off
    ExecutableWorkflow workflow = new ExecutableWorkflow()
      .activity("start", new StartEvent()
        .transitionTo("fork"))
      .activity("fork", new ParallelGateway()
        .transitionTo("t1")
        .transitionTo("condition"))
      .activity("t1", new NoneTask()
        .transitionTo("join"))
      .activity("condition", new ExclusiveGateway()
        .transitionTo("join")
        .transitionTo(new Transition().id("default").toId("t2"))
        .defaultTransitionId("default"))
      .activity("t2", new NoneTask()
        .transitionTo("parallelEnd"))
      .activity("parallelEnd", new EndEvent())
      .activity("join", new ParallelGateway()
        .transitionTo("end"))
      .activity("end", new EndEvent());
    // @formatter:on

    deploy(workflow);
    WorkflowInstance workflowInstance = start(workflow);
    assertTrue(workflowInstance.isEnded());
  }
}
