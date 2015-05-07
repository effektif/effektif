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

import org.junit.Test;

import com.effektif.workflow.api.activities.ExclusiveGateway;
import com.effektif.workflow.api.activities.ReceiveTask;
import com.effektif.workflow.api.activities.StartEvent;
import com.effektif.workflow.api.condition.LessThan;
import com.effektif.workflow.api.model.TriggerInstance;
import com.effektif.workflow.api.types.NumberType;
import com.effektif.workflow.api.workflow.Transition;
import com.effektif.workflow.api.workflow.Workflow;
import com.effektif.workflow.api.workflowinstance.WorkflowInstance;
import com.effektif.workflow.test.WorkflowTest;


/**
 * @author Tom Baeyens
 */
public class ExclusiveGatewayTest extends WorkflowTest {
  
  @Test
  public void testExclusiveGateway() {
    Workflow workflow = new Workflow()
      .variable("v", new NumberType())
      .activity("start", new StartEvent()
        .transitionTo("?"))
      .activity("?", new ExclusiveGateway()
        .defaultTransitionId("default"))
      .transition(new Transition()
        .condition(new LessThan()
          .leftExpression("v")
          .rightValue(10))
        .from("?").to("t1"))
      .transition(new Transition()
        .condition(new LessThan()
          .leftExpression("v")
          .rightValue(100))
        .from("?").to("t2"))
      .transition(new Transition()
        .id("default")
        .from("?").to("t3"))
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
