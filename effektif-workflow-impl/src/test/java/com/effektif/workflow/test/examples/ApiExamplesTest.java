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

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import com.effektif.workflow.api.Configuration;
import com.effektif.workflow.api.WorkflowEngine;
import com.effektif.workflow.api.activities.EmailTask;
import com.effektif.workflow.api.activities.UserTask;
import com.effektif.workflow.api.model.Start;
import com.effektif.workflow.api.task.Task;
import com.effektif.workflow.api.task.TaskQuery;
import com.effektif.workflow.api.task.TaskService;
import com.effektif.workflow.api.workflow.Workflow;
import com.effektif.workflow.api.workflowinstance.WorkflowInstance;
import com.effektif.workflow.impl.memory.MemoryConfiguration;


public class ApiExamplesTest {

  @Test
  public void testApiExample() {
    // Create the default (in-memory) workflow engine
    Configuration configuration = new MemoryConfiguration();
    WorkflowEngine workflowEngine = configuration.getWorkflowEngine();
    TaskService taskService = configuration.getTaskService();
    
    // Create a workflow
    Workflow workflow = new Workflow()
      .source("Release")
      .activity("Move open issues", new UserTask()
        .transitionToNext())
      .activity("Check continuous integration", new UserTask()
        .transitionToNext())
      .activity("Notify community", new EmailTask()
        .to("releases@example.com")
        .subject("New version released")
        .bodyText("Enjoy!"));
    
    // Deploy the workflow to the engine
    workflowEngine
      .deployWorkflow(workflow)
      .checkNoErrorsAndNoWarnings();
    
    // Start a new workflow instance
    WorkflowInstance workflowInstance = workflowEngine.startWorkflowInstance(new Start().workflowId(workflow.getId()));
    
    List<Task> tasks = taskService.findTasks(new TaskQuery());
    assertEquals("Move open issues", tasks.get(0).getName());
    assertEquals(1, tasks.size());
  }
}
