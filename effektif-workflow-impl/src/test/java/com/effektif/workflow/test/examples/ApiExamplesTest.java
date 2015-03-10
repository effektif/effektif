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
package com.effektif.workflow.test.examples;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import com.effektif.workflow.api.Configuration;
import com.effektif.workflow.api.WorkflowEngine;
import com.effektif.workflow.api.activities.EmailTask;
import com.effektif.workflow.api.activities.UserTask;
import com.effektif.workflow.api.model.TriggerInstance;
import com.effektif.workflow.api.task.Task;
import com.effektif.workflow.api.task.TaskQuery;
import com.effektif.workflow.api.task.TaskService;
import com.effektif.workflow.api.workflow.Workflow;
import com.effektif.workflow.api.workflowinstance.WorkflowInstance;
import com.effektif.workflow.impl.bpmn.BpmnWriter;
import com.effektif.workflow.impl.email.EmailServiceImpl;
import com.effektif.workflow.impl.json.JsonService;
import com.effektif.workflow.impl.memory.MemoryConfiguration;


/**
 * @author Tom Baeyens
 */
public class ApiExamplesTest {

  @Test
  public void testApiExample() {
    // Create the default (in-memory) workflow engine
    Configuration configuration = new MemoryConfiguration();
    WorkflowEngine workflowEngine = configuration.getWorkflowEngine();
    TaskService taskService = configuration.getTaskService();
    
    // Create a workflow
    Workflow workflow = new Workflow()
      .sourceWorkflowId("Release")
      .activity("Move open issues", new UserTask()
        .assigneeId("johndoe")
        .transitionToNext())
      .activity("Check continuous integration", new UserTask()
        .transitionToNext())
      .activity("Notify community", new EmailTask()
        .to("releases@example.com")
        .subject("New version released")
        .bodyText("Enjoy!"));
    
    // Deploy the workflow to the engine
    String workflowId = workflowEngine
      .deployWorkflow(workflow)
      .checkNoErrorsAndNoWarnings()
      .getWorkflowId();

    // Start a new workflow instance
    WorkflowInstance workflowInstance = workflowEngine
      .start(new TriggerInstance()
        .workflowId(workflowId));
    
    List<Task> tasks = taskService.findTasks(new TaskQuery()
      .open()
      .taskAssigneeId("johndoe"));
    
    Task task = tasks.get(0);
    assertEquals("Move open issues", task.getName());
    assertEquals(1, tasks.size());
    
    taskService.completeTask(task.getId());
    
    System.err.println(configuration.get(JsonService.class).objectToJsonStringPretty(workflow));
    
    System.err.println(BpmnWriter.writeBpmnDocumentString(workflow, configuration));
  }
  
  @Test
  public void testEmailServerConfiguration() {
    Configuration configuration = new MemoryConfiguration();
    configuration.get(EmailServiceImpl.class)
      // by default, localhost and port 25 are configured
      .host("smtp.gmail.com") // overwrite the default server
      .ssl() // also sets the port to the default ssl port 465 
      .tls() // also sets the port to the default tls port 587
      .connectionTimeoutSeconds(34523523l)
      .authenticate("youraccount@gmail.com", "***");
  }

}
