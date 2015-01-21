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
package com.effektif.workflow.test.examples;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.effektif.workflow.api.WorkflowEngine;
import com.effektif.workflow.api.activities.NoneTask;
import com.effektif.workflow.api.workflow.Workflow;
import com.effektif.workflow.api.workflowinstance.WorkflowInstance;
import com.effektif.workflow.impl.memory.MemoryWorkflowEngineConfiguration;


public class ApiExamplesTest {

  @Test
  public void testApiExample() {
    // Create the default (in-memory) workflow engine
    WorkflowEngine workflowEngine = new MemoryWorkflowEngineConfiguration()
      .initialize()
      .getWorkflowEngine();
    
    // Create a workflow
    Workflow workflow = new Workflow()
      .activity(new NoneTask("a")
        .transitionTo("b"))
      .activity(new NoneTask("b"));
    
    // Deploy the workflow to the engine
    workflow = workflowEngine.deployWorkflow(workflow);
    
    // Start a new workflow instance
    WorkflowInstance workflowInstance = workflowEngine.startWorkflowInstance(workflow);
    
    assertTrue(workflowInstance.isEnded());
  }
}
