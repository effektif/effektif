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

import com.effektif.workflow.api.activities.EmbeddedSubprocess;
import com.effektif.workflow.api.activities.EndEvent;
import com.effektif.workflow.api.activities.StartEvent;
import com.effektif.workflow.api.activities.UserTask;
import com.effektif.workflow.api.workflow.Workflow;
import com.effektif.workflow.api.workflowinstance.WorkflowInstance;
import com.effektif.workflow.test.WorkflowTest;


/**
 * @author Tom Baeyens
 */
public class EmbeddedSuprocessTest extends WorkflowTest {
  
  /**          +-------------+
   *           | sub         |
   * +-----+   | +--+   +--+ |   +---+
   * |start|-->| |w1|   |w2| |-->|end|
   * +-----+   | +--+   +--+ |   +---+
   *           +-------------+
   */ 
  @Test 
  public void testOne() {
    Workflow workflow = new Workflow()
      .activity(new StartEvent("start")
        .transitionTo("sub"))
      .activity(new EmbeddedSubprocess("sub")
        .activity(new UserTask("w1"))
        .activity(new UserTask("w2"))
        .transitionTo("end"))
      .activity(new EndEvent("end"));
  
    deploy(workflow);
    
    WorkflowInstance workflowInstance = start(workflow);

    assertOpen(workflowInstance, "sub", "w1", "w2");
    
    workflowInstance = endTask(workflowInstance, "w1");

    assertOpen(workflowInstance, "sub", "w2");

    workflowInstance = endTask(workflowInstance, "w2");
    
    assertTrue(workflowInstance.isEnded());
  }
}
