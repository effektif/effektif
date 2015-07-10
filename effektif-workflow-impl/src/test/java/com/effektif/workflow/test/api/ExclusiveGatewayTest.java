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
import com.effektif.workflow.api.activities.ReceiveTask;
import com.effektif.workflow.api.activities.StartEvent;
import com.effektif.workflow.api.condition.IsFalse;
import com.effektif.workflow.api.condition.IsTrue;
import com.effektif.workflow.api.condition.LessThan;
import com.effektif.workflow.api.model.TriggerInstance;
import com.effektif.workflow.api.types.BooleanType;
import com.effektif.workflow.api.types.NumberType;
import com.effektif.workflow.api.workflow.ExecutableWorkflow;
import com.effektif.workflow.api.workflow.Transition;
import com.effektif.workflow.api.workflowinstance.WorkflowInstance;
import com.effektif.workflow.test.WorkflowTest;

/**
 * @author Tom Baeyens
 */
public class ExclusiveGatewayTest extends WorkflowTest {

  /**
   * Tests that the process takes the single outgoing flow 
   * leaving an exclusive gateway if it has no condition specified.
   * 
   * The specification says to throw an exception.  Effektif 
   * interprets this by taking the single, non-condition transition 
   * if there is one.  
   * <pre>
   *
   *  ◯─→<X>─→[t1]─→◯
   *
   * </pre>
   */
  @Test
  public void testSingleOutgoingFlow() {
    // @formatter:off
    ExecutableWorkflow workflow = new ExecutableWorkflow()
      .activity("start", new StartEvent()
        .transitionTo("gateway"))
      .activity("gateway", new ExclusiveGateway()
        .transitionTo("wait"))
      .activity("wait", new ReceiveTask()
        .transitionTo("end"))
      .activity("end", new EndEvent());
    // @formatter:on

    deploy(workflow);
    WorkflowInstance workflowInstance = start(workflow);
    assertOpen(workflowInstance, "wait");
  }

  /**
   * Tests that the process continues on an exclusive gateway that only has a default flow.
   * <pre>
   *
   *  ◯─→<X>-/─→[t1]─→◯
   *
   * </pre>
   */
  @Test
  public void testSingleOutgoingFlowDefault() {
    // @formatter:off
    ExecutableWorkflow workflow = new ExecutableWorkflow()
      .activity("start", new StartEvent()
        .transitionTo("gateway"))
      .activity("gateway", new ExclusiveGateway()
        .transitionTo(new Transition().id("default").toId("wait"))
        .defaultTransitionId("default"))
      .activity("wait", new ReceiveTask()
        .transitionTo("end"))
      .activity("end", new EndEvent());
    // @formatter:on

    deploy(workflow);
    WorkflowInstance workflowInstance = start(workflow);
    assertOpen(workflowInstance, "wait");
    assertFalse(workflowInstance.isEnded());
  }

  /**
   * Tests that the process continues from an exclusive gateway with a default transition.
   * <pre>
   *
   *  ◯─→<X>──→◯
   *      │
   *      └→[wait]
   * </pre>
   */
  @Test
  public void testDefaultTransition() {
    // @formatter:off
    ExecutableWorkflow workflow = new ExecutableWorkflow()
      .activity("start", new StartEvent()
        .transitionTo("gateway"))
      .activity("gateway", new ExclusiveGateway()
        .defaultTransitionId("wait")
        .transitionTo("end")
        .transitionTo(new Transition().id("wait").toId("receive")))
      .activity("receive", new ReceiveTask())
      .activity("end", new EndEvent());
    // @formatter:on

    deploy(workflow);
    WorkflowInstance workflowInstance = start(workflow);
    assertOpen(workflowInstance, "receive");
  }

  /**
   * Tests an exclusive gateway with a numeric condition.
   * <pre>
   *
   *  ◯─→<X>──→◯
   *      │
   *      └→[wait]
   * </pre>
   */
  @Test
  public void testSimpleCondition() {
    // @formatter:off
    ExecutableWorkflow workflow = new ExecutableWorkflow()
      .variable("waitingRequired", new BooleanType())
      .activity("start", new StartEvent()
        .transitionTo("gateway"))
      .activity("gateway", new ExclusiveGateway()
        .transitionTo(new Transition().id("continue").toId("end")
          .condition(new IsFalse().leftExpression("waitingRequired")))
        .transitionTo(new Transition().id("wait").toId("receive")
          .condition(new IsTrue().leftExpression("waitingRequired"))))
      .activity("receive", new ReceiveTask())
      .activity("end", new EndEvent());
    // @formatter:on

    deploy(workflow);

    WorkflowInstance endingWorkflow = workflowEngine.start(
      new TriggerInstance().workflowId(workflow.getId()).data("waitingRequired", Boolean.FALSE));
    assertTrue(endingWorkflow.isEnded());

    WorkflowInstance waitingWorkflow = workflowEngine.start(
      new TriggerInstance().workflowId(workflow.getId()).data("waitingRequired", Boolean.TRUE));
    assertOpen(waitingWorkflow, "receive");
  }

  @Test
  public void testExclusiveGateway() {
    ExecutableWorkflow workflow = new ExecutableWorkflow()
      .variable("v", new NumberType())
      .activity("start", new StartEvent()
        .transitionTo("?"))
      .activity("?", new ExclusiveGateway()
        .defaultTransitionId("default"))
      .transition(new Transition()
        .condition(new LessThan()
          .leftExpression("v")
          .rightValue(10))
        .fromId("?").toId("t1"))
      .transition(new Transition()
        .condition(new LessThan()
          .leftExpression("v")
          .rightValue(100))
        .fromId("?").toId("t2"))
      .transition(new Transition()
        .id("default")
        .fromId("?").toId("t3"))
      .activity("t1", new ReceiveTask())
      .activity("t2", new ReceiveTask())
      .activity("t3", new ReceiveTask());
    
    deploy(workflow);

    WorkflowInstance workflowInstance = workflowEngine.start(new TriggerInstance()
      .workflowId(workflow.getId())
      .data("v", 5));
    
    assertOpen(workflowInstance, "t1");

    workflowInstance = workflowEngine.start(new TriggerInstance()
      .workflowId(workflow.getId())
      .data("v", 50));

    assertOpen(workflowInstance, "t2");

    workflowInstance = workflowEngine.start(new TriggerInstance()
      .workflowId(workflow.getId())
      .data("v", 500));

    assertOpen(workflowInstance, "t3");
  }
}
