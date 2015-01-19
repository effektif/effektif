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
package com.effektif.workflow.test.api;

import static org.junit.Assert.*;

import org.junit.Test;

import com.effektif.workflow.api.activities.Call;
import com.effektif.workflow.api.activities.UserTask;
import com.effektif.workflow.api.command.MessageCommand;
import com.effektif.workflow.api.command.StartCommand;
import com.effektif.workflow.api.query.WorkflowInstanceQuery;
import com.effektif.workflow.api.type.TextType;
import com.effektif.workflow.api.workflow.Workflow;
import com.effektif.workflow.api.workflowinstance.ActivityInstance;
import com.effektif.workflow.api.workflowinstance.WorkflowInstance;
import com.effektif.workflow.impl.task.Task;
import com.effektif.workflow.impl.task.TaskQuery;
import com.effektif.workflow.test.WorkflowTest;


public class CallTest extends WorkflowTest {
  
  @Test
  public void testCallActivity() {
    Workflow subWorkflow = new Workflow()
      .activity(new UserTask("subtask"));
    
    subWorkflow = deploy(subWorkflow);
    
    Workflow superWorkflow = new Workflow()
      .activity(new Call("call")
        .subWorkflowId(subWorkflow.getId()));

    superWorkflow = deploy(superWorkflow);

    WorkflowInstance superInstance = start(superWorkflow);

    ActivityInstance callActivityInstance = superInstance.findOpenActivityInstance("call");
    assertNotNull(callActivityInstance.getCalledWorkflowInstanceId());
    
    WorkflowInstance subInstance = workflowEngine.findWorkflowInstances(new WorkflowInstanceQuery()
        .workflowInstanceId(callActivityInstance.getCalledWorkflowInstanceId()))
      .get(0);
    
    assertNotNull(subInstance);
    
    ActivityInstance subtaskInstance = subInstance.findOpenActivityInstance("subtask");
    
    subInstance = workflowEngine.sendMessage(new MessageCommand()
      .workflowInstanceId(subInstance.getId())
      .activityInstanceId(subtaskInstance.getId()));
    
    assertTrue(subInstance.isEnded());

    superInstance = workflowEngine.findWorkflowInstances(new WorkflowInstanceQuery()
        .workflowInstanceId(superInstance.getId()))
      .get(0);
    assertTrue(superInstance.isEnded());
  }

  @Test
  public void testCallActivityInputMappingVariable() {
    Workflow subWorkflow = new Workflow()
      .variable("performer", new TextType())
      .activity("subtask", new UserTask()
        .candidateIdVariableId("performer")
      );
    
    subWorkflow = deploy(subWorkflow);
    
    Workflow superWorkflow = new Workflow()
      .variable("assignee", new TextType())
      .activity(new Call("call")
        .inputMappingVariable("assignee", "performer")
        .subWorkflowId(subWorkflow.getId()));
    
    superWorkflow = deploy(superWorkflow);
    
    workflowEngine.startWorkflowInstance(new StartCommand()
      .workflowId(superWorkflow.getId())
      .variableValue("assignee", "johndoe")
    );
    
    Task task = taskService.findTasks(new TaskQuery()).get(0);
    assertEquals("johndoe", task.getAssigneeId());
  }

  @Test
  public void testCallActivityInputMappingValue() {
    Workflow subWorkflow = new Workflow()
      .variable("performer", new TextType())
      .activity("subtask", new UserTask()
        .candidateIdVariableId("performer")
      );
    
    subWorkflow = deploy(subWorkflow);
    
    Workflow superWorkflow = new Workflow()
      .activity(new Call("call")
        .inputMappingValue("johndoe", "performer")
        .subWorkflowId(subWorkflow.getId()));
    
    superWorkflow = deploy(superWorkflow);
    
    workflowEngine.startWorkflowInstance(new StartCommand()
      .workflowId(superWorkflow.getId())
      .variableValue("assignee", "johndoe")
    );
    
    Task task = taskService.findTasks(new TaskQuery()).get(0);
    assertEquals("johndoe", task.getAssigneeId());
  }
}
