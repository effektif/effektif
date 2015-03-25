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
package com.effektif.workflow.test;

import org.junit.Before;

import com.effektif.workflow.impl.job.JobService;
import com.effektif.workflow.impl.job.JobServiceImpl;
import com.effektif.workflow.impl.job.JobStore;
import com.effektif.workflow.impl.util.Time;


/**
 * @author Tom Baeyens
 */
public class JobWorkflowTest extends WorkflowTest {

  protected JobService jobService;
  protected JobStore jobStore;
  
  @Before
  public void initialize() {
    this.jobService = configuration.get(JobService.class);
    // this prevents the job service from starting any threads
    ((JobServiceImpl)jobService).isRunning = true;
    this.jobStore = configuration.get(JobStore.class);

    Time.now = null;
  }
  
  @Before
  public void before() {
    Time.now = null;
  }

  public void checkWorkflowInstanceJobs() {
    ((JobServiceImpl)jobService).checkWorkflowInstanceJobs();
  }

  public void checkJobs() {
    ((JobServiceImpl)jobService).checkJobs();
  }
}
