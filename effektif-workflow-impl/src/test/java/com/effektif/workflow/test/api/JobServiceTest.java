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

import java.util.ArrayList;
import java.util.List;

import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.effektif.workflow.api.json.TypeName;
import com.effektif.workflow.impl.job.AbstractJobType;
import com.effektif.workflow.impl.job.Job;
import com.effektif.workflow.impl.job.JobController;
import com.effektif.workflow.impl.job.JobExecution;
import com.effektif.workflow.impl.util.Time;
import com.effektif.workflow.test.JobWorkflowTest;


/**
 * @author Tom Baeyens
 */
public class JobServiceTest extends JobWorkflowTest {
  
  public static final Logger log = LoggerFactory.getLogger(JobServiceTest.class);

  @TypeName("tst")
  public static class TestJob extends AbstractJobType {
    static List<JobExecution> jobExecutions;
    static boolean throwException;
    @Override
    public void execute(JobController jobController) {
      jobExecutions.add((JobExecution)jobController);
      if (throwException) {
        jobController.log("oops");
        throw new RuntimeException("oops");
      } else {
        jobController.log(":ok_hand:");
      }
    }
  }
  
  @Before
  public void before() {
    TestJob.jobExecutions = new ArrayList<>();
    TestJob.throwException = false;
    Time.now = null;
  }

//  @Test
//  public void testWorkfowJobOK() throws Exception {
//    // quickest way to get a processInstanceId
//    Workflow workflow = new Workflow()
//      .activity("t", new UserTask());
//    deploy(workflow);
//    String workflowInstanceId = start(workflow).getId();
//    
//    jobService.scheduleJob(new Job()
//      .jobType(new TestJob())
//      .dueDate(Time.now())
//      .processInstanceId(workflowInstanceId));
//    
//    assertEquals(0, TestJob.jobExecutions.size());
//    checkOtherJobs(); 
//    assertEquals(0, TestJob.jobExecutions.size());
//    checkProcessJobs(); // only this one should execute the job
//    assertEquals(1, TestJob.jobExecutions.size());
//    checkOtherJobs();
//    assertEquals(1, TestJob.jobExecutions.size());
//    checkProcessJobs();
//    assertEquals(1, TestJob.jobExecutions.size());
//    
//    JobExecution jobExecution = TestJob.jobExecutions.get(0);
//    assertNull(jobExecution.error);
//  }

//  @Test
//  public void testRealProcessJobOK() throws Exception {
//    // quickest way to get a processInstanceId
//    WorkflowBuilder w = processEngine.newWorkflow();
//    w.newActivity("t", new UserTask())
//     .newTimer(new TestJob())
//     ;
//    String workflowId = w.deploy();
//    String workflowInstanceId = processEngine.newStart()
//      .workflowId(workflowId)
//      .startWorkflowInstance()
//      .getId();
//    
//    jobService.newJob(new TestJob())
//      .dueDate(Time.now())
//      .processInstanceId(workflowInstanceId)
//      .save();
//    
//    assertEquals(0, TestJob.jobExecutions.size());
//    checkOtherJobs(); 
//    assertEquals(0, TestJob.jobExecutions.size());
//    checkProcessJobs(); // only this one should execute the job
//    assertEquals(1, TestJob.jobExecutions.size());
//    checkOtherJobs();
//    assertEquals(1, TestJob.jobExecutions.size());
//    checkProcessJobs();
//    assertEquals(1, TestJob.jobExecutions.size());
//    
//    JobExecution jobExecution = TestJob.jobExecutions.get(0);
//    assertNull(jobExecution.error);
//  }

  @Test
  public void testJobOK() throws Exception {
    jobService.saveJob(new Job()
      .jobType(new TestJob())
      .dueDate(Time.now()));
    
    checkWorkflowInstanceJobs();
    assertEquals(0, TestJob.jobExecutions.size());
    checkJobs();  // only this one should execute the job
    assertEquals(1, TestJob.jobExecutions.size());
    checkJobs();
    assertEquals(1, TestJob.jobExecutions.size());
    checkWorkflowInstanceJobs();
    assertEquals(1, TestJob.jobExecutions.size());

    JobExecution jobExecution = TestJob.jobExecutions.get(0);
    assertNull(jobExecution.error);
  }

  @Test
  public void testJobFailAndRecover() throws Exception {
    jobService.saveJob(new Job()
      .jobType(new TestJob())
      .dueDate(Time.now()));
    
    TestJob.throwException = true;
    
    assertEquals(0, TestJob.jobExecutions.size());
    checkJobs();
    assertEquals(1, TestJob.jobExecutions.size());
    
    assertTrue(TestJob.jobExecutions.get(0).error);
    assertNull(TestJob.jobExecutions.get(0).job.done);

    // the default retry reschedule is 3 seconds
    Time.now = new LocalDateTime().plusMinutes(10);
    TestJob.throwException = false;
    
    checkJobs();
    assertEquals(2, TestJob.jobExecutions.size());
    
    assertNull(TestJob.jobExecutions.get(1).error);
    assertNotNull(TestJob.jobExecutions.get(1).job.done);
  }
  
  @Test
  public void testJobFailTillDead() throws Exception {
    jobService.saveJob(new Job()
      .jobType(new TestJob())
      .dueDate(Time.now()));
    
    TestJob.throwException = true;
    
    checkJobs();
    assertEquals(1, TestJob.jobExecutions.size());
    JobExecution jobExecution = TestJob.jobExecutions.get(0);
    assertTrue(jobExecution.error);
    assertNull(jobExecution.job.done);
    assertNull(jobExecution.job.dead);

    // the first retry rescheduled in 3 seconds
    Time.now = new LocalDateTime().plusSeconds(4);

    checkJobs();
    assertEquals(2, TestJob.jobExecutions.size());
    jobExecution = TestJob.jobExecutions.get(1);
    assertTrue(jobExecution.error);
    assertNull(jobExecution.job.done);
    assertNull(jobExecution.job.dead);

    // the second retry rescheduled in an hour seconds
    Time.now = new LocalDateTime().plusHours(2);

    checkJobs();
    assertEquals(3, TestJob.jobExecutions.size());
    jobExecution = TestJob.jobExecutions.get(2);
    assertTrue(jobExecution.error);
    assertNull(jobExecution.job.done);
    assertNull(jobExecution.job.dead);

    // the second retry rescheduled in an hour seconds
    Time.now = new LocalDateTime().plusDays(2);

    checkJobs();
    assertEquals(4, TestJob.jobExecutions.size());
    jobExecution = TestJob.jobExecutions.get(3);
    assertTrue(jobExecution.error);
    
    // But now the job should be dead 
    assertNotNull(jobExecution.job.done);
    assertTrue(jobExecution.job.dead);
  }

  @Test
  public void testUniqueJob() throws Exception {
    jobService.saveJob(new Job()
      .key("uniqueid")
      .jobType(new TestJob())
      .dueDate(Time.now()));

    jobService.saveJob(new Job()
      .key("uniqueid")
      .jobType(new TestJob())
      .dueDate(Time.now()));

    checkJobs();
    assertEquals(1, TestJob.jobExecutions.size());
  }
}
