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

import java.util.List;

import org.junit.Test;

import com.effektif.workflow.api.activities.Call;
import com.effektif.workflow.api.activities.JavaServiceTask;
import com.effektif.workflow.api.activities.NoneTask;
import com.effektif.workflow.api.deprecated.activities.UserTask;
import com.effektif.workflow.api.deprecated.model.UserId;
import com.effektif.workflow.api.deprecated.task.Task;
import com.effektif.workflow.api.deprecated.task.TaskQuery;
import com.effektif.workflow.api.deprecated.types.UserIdType;
import com.effektif.workflow.api.model.Message;
import com.effektif.workflow.api.model.TriggerInstance;
import com.effektif.workflow.api.query.WorkflowInstanceQuery;
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
      .activity("subtask", new JavaServiceTask());
    
    deploy(subWorkflow);
    
    Workflow superWorkflow = new Workflow()
      .activity("call", new Call()
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
  public void testTwoCallActivitiesInSequence() {
    Workflow subWorkflow = new Workflow()
      .activity("auto", new NoneTask());
    
    deploy(subWorkflow);
    
    Workflow superWorkflow = new Workflow()
      .activity("call1", new Call()
        .subWorkflowId(subWorkflow.getId())
        .transitionToNext())
      .activity("call2", new Call()
        .subWorkflowId(subWorkflow.getId()));

    deploy(superWorkflow);

    WorkflowInstance superInstance = start(superWorkflow);
    assertTrue(superInstance.isEnded());

    List<WorkflowInstance> workflowInstances = workflowEngine.findWorkflowInstances(new WorkflowInstanceQuery());
    for (WorkflowInstance workflowInstance: workflowInstances) {
      assertTrue(workflowInstance.isEnded());
    }
    assertEquals(3, workflowInstances.size());
  }

  @Test
  public void testTwoCallActivitiesInparallel() {
    Workflow subWorkflow = new Workflow()
      .activity("auto", new NoneTask());
    
    deploy(subWorkflow);
    
    Workflow superWorkflow = new Workflow()
      .activity("call1", new Call()
        .subWorkflowId(subWorkflow.getId()))
      .activity("call2", new Call()
        .subWorkflowId(subWorkflow.getId()));

    deploy(superWorkflow);

    WorkflowInstance superInstance = start(superWorkflow);
    assertTrue(superInstance.isEnded());

    List<WorkflowInstance> workflowInstances = workflowEngine.findWorkflowInstances(new WorkflowInstanceQuery());
    for (WorkflowInstance workflowInstance: workflowInstances) {
      assertTrue(workflowInstance.isEnded());
    }
    assertEquals(3, workflowInstances.size());
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
        .inputValue("performer", new UserId("552ce4fdc2e610a6a3dedb83"))
        .subWorkflowId(subWorkflow.getId()));
    
    deploy(superWorkflow);
    
    start(superWorkflow);
    
    Task task = taskService.findTasks(new TaskQuery()).get(0);
    assertEquals("552ce4fdc2e610a6a3dedb83", task.getAssigneeId().getInternal());
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
      .activity("call", new Call()
        .inputExpression("performer", "guineapig")
        .subWorkflowId(subWorkflow.getId()));
    
    deploy(superWorkflow);
    
    workflowEngine.start(new TriggerInstance()
      .workflowId(superWorkflow.getId())
      .data("guineapig", new UserId("552ce4fdc2e610a6a3dedb83"))
    );

    Task task = taskService.findTasks(new TaskQuery()).get(0);
    assertEquals("552ce4fdc2e610a6a3dedb83", task.getAssigneeId().getInternal());
  }

}
