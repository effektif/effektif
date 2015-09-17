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

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.effektif.workflow.api.activities.ReceiveTask;
import com.effektif.workflow.api.workflow.ExecutableWorkflow;
import com.effektif.workflow.api.workflow.Transition;
import com.effektif.workflow.test.WorkflowTest;

/**
 * @author Tom Baeyens
 */
public class PropertiesTest extends WorkflowTest {

  @Test
  public void testProperties() {
    ExecutableWorkflow workflow = new ExecutableWorkflow()
      .activity("start", new ReceiveTask()
        .property("a", "A"))
      .transition(new Transition()
        .fromId("a")
        .property("b",  "B"))
      .property("c", "C");
  
    deploy(workflow);
    
    workflow = workflowEngine.findWorkflows(null).get(0);
    assertEquals("C", workflow.getProperty("c"));
    assertEquals("A", workflow.getActivities().get(0).getProperty("a"));
    assertEquals("B", workflow.getTransitions().get(0).getProperty("b"));
  }
}
