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

import java.util.List;

import org.joda.time.LocalDateTime;
import org.junit.Test;

import com.effektif.workflow.api.activities.UserTask;
import com.effektif.workflow.api.model.RelativeTime;
import com.effektif.workflow.api.task.Task;
import com.effektif.workflow.api.task.TaskQuery;
import com.effektif.workflow.api.workflow.Workflow;
import com.effektif.workflow.impl.job.JobQuery;
import com.effektif.workflow.impl.util.Time;
import com.effektif.workflow.test.JobWorkflowTest;


/**
 * @author Tom Baeyens
 */
public class UserTaskTimersTest extends JobWorkflowTest {
  
  @Test
  public void testTaskEscalation() throws Exception {
    Workflow workflow = new Workflow()
      .activity("1", new UserTask()
        .name("t")
        .assigneeId("johndoe")
        .escalate(RelativeTime.minutes(5))
        .escalateToUserId("joesmoe"));
    
    deploy(workflow);
    
    start(workflow);
    
    Time.now = new LocalDateTime().plusHours(1);

    assertEquals("johndoe", getTasks().get(0).getAssigneeId().getInternal());

    checkWorkflowInstanceJobs();
    
    assertEquals("joesmoe", getTasks().get(0).getAssigneeId().getInternal());

    assertEquals(0, jobStore.findJobs(new JobQuery()).size());
    assertEquals(1, jobStore.findArchivedJobs(new JobQuery()).size());
  }

  public List<Task> getTasks() {
    return taskService.findTasks(new TaskQuery());
  }

}
