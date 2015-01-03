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
import com.effektif.workflow.api.command.StartCommand;
import com.effektif.workflow.api.workflow.Workflow;
import com.effektif.workflow.api.workflowinstance.WorkflowInstance;
import com.effektif.workflow.impl.SynchronousExecutorService;
import com.effektif.workflow.impl.memory.MemoryWorkflowEngineConfiguration;


/**
 * @author Walter White
 */
public class ApiExamplesTest {

  @Test
  public void testApiExample() {
    // Create the default (in-memory) workflow engine
    WorkflowEngine workflowEngine = new MemoryWorkflowEngineConfiguration()
       // for test purposes it's best to avoid concurrency so 
       // the synchronous executor service is configured here
       .registerService(new SynchronousExecutorService())
       .buildWorkflowEngine();
    
    // Create a workflow
    Workflow workflow = new Workflow()
      .activity(new NoneTask()
        .id("a")
        .transitionTo("b"))
      .activity(new NoneTask()
        .id("b"));
    
    // Deploy the workflow to the engine
    String workflowId = workflowEngine.deployWorkflow(workflow)
      .checkNoErrorsAndNoWarnings()
      .getWorkflowId();

    // Start a new workflow instance
    StartCommand start = new StartCommand()
      .workflowId(workflowId);
    WorkflowInstance workflowInstance = workflowEngine.startWorkflowInstance(start);
    
    assertTrue(workflowInstance.isEnded());
  }
}
