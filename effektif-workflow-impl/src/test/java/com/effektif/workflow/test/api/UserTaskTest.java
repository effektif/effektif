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

import org.joda.time.LocalDateTime;
import org.junit.Test;

import com.effektif.workflow.api.activities.UserTask;
import com.effektif.workflow.api.model.RelativeTime;
import com.effektif.workflow.api.model.TaskId;
import com.effektif.workflow.api.model.UserId;
import com.effektif.workflow.api.task.Task;
import com.effektif.workflow.api.task.TaskQuery;
import com.effektif.workflow.api.types.UserIdType;
import com.effektif.workflow.api.workflow.Workflow;
import com.effektif.workflow.test.WorkflowTest;


/**
 * @author Tom Baeyens
 */
public class UserTaskTest extends WorkflowTest {

  @Test
  public void testTask() throws Exception {
    long dueDateEarliest = new LocalDateTime().plusMinutes(5).toDate().getTime();
    
    Workflow workflow = new Workflow()
      .activity("1", new UserTask()
        .name("release")
        .assigneeId("552ce4fdc2e610a6a3dedb84")
        .candidateId("552ce4fdc2e610a6a3dedb85")
        .candidateId("552ce4fdc2e610a6a3dedb86")
        .duedate(RelativeTime.minutes(5)));
    
    deploy(workflow);
    
    start(workflow);
    
    Task task = taskService.findTasks(new TaskQuery()).get(0);
    assertEquals("release", task.getName());
    assertEquals("552ce4fdc2e610a6a3dedb84", task.getAssigneeId().getInternal());
    assertEquals("552ce4fdc2e610a6a3dedb85", task.getCandidateIds().get(0).getInternal());
    assertEquals("552ce4fdc2e610a6a3dedb86", task.getCandidateIds().get(1).getInternal());
    assertTrue(dueDateEarliest<=task.getDuedate().toDate().getTime());
    long dueDateLatest = new LocalDateTime().plusMinutes(5).toDate().getTime();
    assertTrue(task.getDuedate().toDate().getTime()<=dueDateLatest);
  }

  @Test
  public void testTaskRole() throws Exception {
    Workflow workflow = new Workflow()
      .variable("manager", new UserIdType())
      .activity("1", new UserTask()
        .assigneeExpression("manager")
        .transitionToNext())
      .activity("2", new UserTask()
        .assigneeExpression("manager"));
    
    deploy(workflow);
    
    start(workflow);
    
    Task task = taskService.findTasks(new TaskQuery()).get(0);
    
    TaskId taskId = task.getId();
    taskService.assignTask(taskId, new UserId("joesmoe"));
    taskService.completeTask(taskId);

    task = taskService.findTasks(new TaskQuery().open()).get(0);
    assertEquals("2", task.getName());
    assertEquals(new UserId("joesmoe"), task.getAssigneeId());
  }

  @Test
  public void testTaskRoleAutoAssign() throws Exception {
    Workflow workflow = new Workflow()
      .variable("manager", new UserIdType()
        .candidateId("552ce4fdc2e610a6a3dedb84"))
      .activity("1", new UserTask()
        .name("release")
        .assigneeExpression("manager"));

    deploy(workflow);
    
    start(workflow);
    
    Task task = taskService.findTasks(new TaskQuery()).get(0);
    // TODO
    // assertEquals(new UserId("552ce4fdc2e610a6a3dedb84"), task.getCandidateIds().get(0));
  }

  @Test
  public void testTaskQuery() throws Exception {
    Workflow workflow = new Workflow()
      .activity("1", new UserTask())
      .activity("2", new UserTask())
      .activity("3", new UserTask());
    
    deploy(workflow);
    
    start(workflow);
    
    assertOpenTaskNames(new TaskQuery(), "1", "2", "3");
    assertOpenTaskNames(new TaskQuery().open(), "1", "2", "3");
    assertOpenTaskNames(new TaskQuery().completed());

    Task task1 = taskService.findTasks(new TaskQuery().taskName("1")).get(0);
    taskService.completeTask(task1.getId());

    assertOpenTaskNames(new TaskQuery(), "1", "2", "3");
    assertOpenTaskNames(new TaskQuery().open(), "2", "3");
    assertOpenTaskNames(new TaskQuery().completed(), "1");
  }
}
