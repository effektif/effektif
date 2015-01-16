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

import org.junit.Test;

import com.effektif.workflow.api.activities.ExclusiveGateway;
import com.effektif.workflow.api.activities.StartEvent;
import com.effektif.workflow.api.activities.UserTask;
import com.effektif.workflow.api.command.StartCommand;
import com.effektif.workflow.api.type.NumberType;
import com.effektif.workflow.api.workflow.Transition;
import com.effektif.workflow.api.workflow.Workflow;
import com.effektif.workflow.api.workflowinstance.WorkflowInstance;
import com.effektif.workflow.test.WorkflowTest;


public class ExclusiveGatewayTest extends WorkflowTest {
  
  @Test
  public void testExclusiveGateway() {
    Workflow w = new Workflow()
      .variable("v", new NumberType())
      .activity("start", new StartEvent()
        .transitionTo("?"))
      .activity("?", new ExclusiveGateway()
        .defaultTransitionId("default"))
      .transition(new Transition()
        .condition("v < 10")
        .from("?").to("t1"))
      .transition(new Transition()
        .condition("v < 100")
        .from("?").to("t2"))
      .transition(new Transition("default")
        .from("?").to("t3"))
      .activity(new UserTask("t1"))
      .activity(new UserTask("t2"))
      .activity(new UserTask("t3"));
    
    w = deploy(w);

    WorkflowInstance workflowInstance = workflowEngine.startWorkflowInstance(new StartCommand()
      .workflowId(w.getId())
      .variableValue("v", 5));
    
    assertOpen(workflowInstance, "t1");

    workflowInstance = workflowEngine.startWorkflowInstance(new StartCommand()
      .workflowId(w.getId())
      .variableValue("v", 50));

    assertOpen(workflowInstance, "t2");

    workflowInstance = workflowEngine.startWorkflowInstance(new StartCommand()
      .workflowId(w.getId())
      .variableValue("v", 500));

    assertOpen(workflowInstance, "t3");
  }
}
