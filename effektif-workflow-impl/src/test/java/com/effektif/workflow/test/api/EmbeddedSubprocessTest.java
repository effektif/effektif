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

import com.effektif.workflow.api.activities.EmbeddedSubprocess;
import com.effektif.workflow.api.activities.EndEvent;
import com.effektif.workflow.api.activities.ReceiveTask;
import com.effektif.workflow.api.activities.StartEvent;
import com.effektif.workflow.api.workflow.ExecutableWorkflow;
import com.effektif.workflow.api.workflowinstance.WorkflowInstance;
import com.effektif.workflow.test.WorkflowTest;


/**
 * @author Tom Baeyens
 */
public class EmbeddedSubprocessTest extends WorkflowTest {
  
  /**          +-------------+
   *           | sub         |
   * +-----+   | +--+   +--+ |   +---+
   * |start|-->| |w1|   |w2| |-->|end|
   * +-----+   | +--+   +--+ |   +---+
   *           +-------------+
   */ 
  @Test 
  public void testSubprocess() {
    ExecutableWorkflow workflow = new ExecutableWorkflow()
      .activity("start", new StartEvent()
        .transitionTo("sub"))
      .activity("sub", new EmbeddedSubprocess()
        .activity("w1", new ReceiveTask())
        .activity("w2", new ReceiveTask())
        .transitionTo("end"))
      .activity("end", new EndEvent());
  
    deploy(workflow);

    WorkflowInstance workflowInstance = start(workflow);
    assertOpen(workflowInstance, "sub", "w1", "w2");
    
    workflowInstance = endTask(workflowInstance, "w1");
    assertOpen(workflowInstance, "sub", "w2");

    workflowInstance = endTask(workflowInstance, "w2");
    assertTrue(workflowInstance.isEnded());
  }

  /**
   * Tests a nested subprocess.
   * <pre>
   *
   *           ┌───────────────────────┐
   *           │ subprocess1           │
   *           │                       │
   *           │       ┌─────────────┐ │
   *  [start]──┤       │ subprocess2 │ ├─→[t3]─>[end]
   *           │ [s1]─→┤             │ │
   *           │       │    [s2]     │ │
   *           │       └─────────────┘ │
   *           └───────────────────────┘
   *
   * </pre>
   */
  @Test
  public void testNestedSubprocess() {
    // @formatter:off
    ExecutableWorkflow workflow = new ExecutableWorkflow()
      .activity("start", new StartEvent()
        .transitionTo("subprocess1"))
      .activity("subprocess1", new EmbeddedSubprocess()
        .activity("s1", new ReceiveTask()
          .transitionTo("subprocess2"))
        .activity("subprocess2", new EmbeddedSubprocess()
          .activity("s2", new ReceiveTask()))
        .transitionTo("t3"))
      .activity("t3", new ReceiveTask()
        .transitionTo("end"))
      .activity("end", new EndEvent());
    // @formatter:on

    deploy(workflow);

    WorkflowInstance workflowInstance = start(workflow);
    assertOpen(workflowInstance, "subprocess1", "s1");

    workflowInstance = endTask(workflowInstance, "s1");
    assertOpen(workflowInstance, "subprocess1", "subprocess2", "s2");

    workflowInstance = endTask(workflowInstance, "s2");
    assertOpen(workflowInstance, "t3");
    assertFalse(workflowInstance.isEnded());
  }
}
