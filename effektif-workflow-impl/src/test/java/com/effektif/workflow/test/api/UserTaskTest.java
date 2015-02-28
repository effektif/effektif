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
import com.effektif.workflow.api.task.Task;
import com.effektif.workflow.api.task.TaskQuery;
import com.effektif.workflow.api.workflow.Workflow;
import com.effektif.workflow.test.WorkflowTest;


/**
 * @author Tom Baeyens
 */
public class UserTaskTest extends WorkflowTest {

  @Test
  public void testTask() throws Exception {
    long duedateEarliest = new LocalDateTime().plusMinutes(5).toDate().getTime();
    
    Workflow workflow = new Workflow()
      .activity("1", new UserTask()
        .name("release")
        .assigneeUserId("johndoe")
        .candidateUserId("joesmoe")
        .candidateUserId("jackblack")
        .duedate(RelativeTime.minutes(5)));
    
    deploy(workflow);
    
    start(workflow);
    
    Task task = taskService.findTasks(new TaskQuery()).get(0);
    assertEquals("release", task.getName());
    assertEquals("johndoe", task.getAssigneeId().getId());
    assertEquals("joesmoe", task.getCandidateIds().get(0).getId());
    assertEquals("jackblack", task.getCandidateIds().get(1).getId());
    assertTrue(duedateEarliest<=task.getDuedate().toDate().getTime());
    long duedateLatest = new LocalDateTime().plusMinutes(5).toDate().getTime();
    assertTrue(task.getDuedate().toDate().getTime()<=duedateLatest);
  }
}
