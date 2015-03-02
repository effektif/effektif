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

import com.effektif.workflow.impl.json.JsonService;
import org.junit.Test;

import com.effektif.workflow.api.activities.Call;
import com.effektif.workflow.api.activities.UserTask;
import com.effektif.workflow.api.model.Message;
import com.effektif.workflow.api.model.TriggerInstance;
import com.effektif.workflow.api.model.UserId;
import com.effektif.workflow.api.query.WorkflowInstanceQuery;
import com.effektif.workflow.api.task.Task;
import com.effektif.workflow.api.task.TaskQuery;
import com.effektif.workflow.api.types.UserIdType;
import com.effektif.workflow.api.workflow.Workflow;
import com.effektif.workflow.api.workflowinstance.ActivityInstance;
import com.effektif.workflow.api.workflowinstance.WorkflowInstance;
import com.effektif.workflow.test.WorkflowTest;


/**
 * @author Tom Baeyens
 */
public class CallTest extends WorkflowTest {
  
  @Test
  public void testCallActivity() {
    Workflow subWorkflow = new Workflow()
      .activity(new UserTask("subtask"));
    
    deploy(subWorkflow);
    
    Workflow superWorkflow = new Workflow()
      .activity(new Call("call")
        .subWorkflowId(subWorkflow.getId()));

    deploy(superWorkflow);

    WorkflowInstance superInstance = start(superWorkflow);

    ActivityInstance callActivityInstance = superInstance.findOpenActivityInstance("call");
    assertNotNull(callActivityInstance.getCalledWorkflowInstanceId());
    
    WorkflowInstance subInstance = workflowEngine.findWorkflowInstances(new WorkflowInstanceQuery()
        .workflowInstanceId(callActivityInstance.getCalledWorkflowInstanceId()))
      .get(0);
    
    assertNotNull(subInstance);
    
    ActivityInstance subtaskInstance = subInstance.findOpenActivityInstance("subtask");
    
    subInstance = workflowEngine.send(new Message()
      .workflowInstanceId(subInstance.getId())
      .activityInstanceId(subtaskInstance.getId()));
    
    assertTrue(subInstance.isEnded());

    superInstance = workflowEngine.findWorkflowInstances(new WorkflowInstanceQuery()
        .workflowInstanceId(superInstance.getId()))
      .get(0);
    assertTrue(superInstance.isEnded());
  }

  @Test
  public void testCallActivityInputValue() {
    Workflow subWorkflow = new Workflow()
      .variable("performer", new UserIdType())
      .activity("subtask", new UserTask()
        .assigneeExpression("performer")
      );
    
    deploy(subWorkflow);
    
    Workflow superWorkflow = new Workflow()
      .activity("call", new Call()
        .inputValue("performer", new UserId("johndoe"))
        .subWorkflowId(subWorkflow.getId()));
    
    deploy(superWorkflow);
    
    start(superWorkflow);
    
    Task task = taskService.findTasks(new TaskQuery()).get(0);
    assertEquals("johndoe", task.getAssigneeId().getId());
  }

  @Test
  public void testCallActivityInputBindingVariable() {
    Workflow subWorkflow = new Workflow()
      .variable("performer", new UserIdType())
      .activity("subtask", new UserTask()
        .assigneeExpression("performer")
      );
    
    deploy(subWorkflow);
    
    Workflow superWorkflow = new Workflow()
      .variable("guineapig", new UserIdType())
      .activity(new Call("call")
        .inputExpression("performer", "guineapig")
        .subWorkflowId(subWorkflow.getId()));
    
    deploy(superWorkflow);
    
    workflowEngine.start(new TriggerInstance()
      .workflowId(superWorkflow.getId())
      .data("guineapig", new UserId("johndoe"))
    );

    Task task = taskService.findTasks(new TaskQuery()).get(0);
    assertEquals("johndoe", task.getAssigneeId().getId());
  }

}
