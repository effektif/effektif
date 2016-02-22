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
package com.effektif.workflow.impl.job;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

import com.effektif.workflow.impl.configuration.Startable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.effektif.workflow.api.Configuration;
import com.effektif.workflow.impl.ExecutorService;
import com.effektif.workflow.impl.WorkflowInstanceStore;
import com.effektif.workflow.impl.configuration.Brewable;
import com.effektif.workflow.impl.configuration.Brewery;
import com.effektif.workflow.impl.util.Time;
import com.effektif.workflow.impl.workflowinstance.WorkflowInstanceImpl;


/**
 * @author Tom Baeyens
 */
public class JobServiceImpl implements JobService, Brewable, Startable {
  
  private static final Logger log = LoggerFactory.getLogger(JobServiceImpl.class);
  
  protected Configuration configuration;
  protected JobStore jobStore;
  protected WorkflowInstanceStore workflowInstanceStore;
  protected ExecutorService executor;

  // configuration 
  public long checkInterval = 30 * 1000; // 30 seconds
  public int maxJobExecutions = 5;

  // runtime state
  public boolean isRunning = false;
  public Timer timer = null;
  public Timer checkOtherJobsTimer = null;
  public JobServiceListener listener = null;

  private static JobServiceImpl jobServiceImpl = null;

  @Override
  public void brew(Brewery brewery) {
    this.configuration = brewery.get(Configuration.class);
    this.workflowInstanceStore = brewery.get(WorkflowInstanceStore.class);
    this.executor = brewery.get(ExecutorService.class);
    this.jobStore = brewery.get(JobStore.class);
  }

  public JobServiceImpl () {
    jobServiceImpl = this;
  }

  public void setListener(JobServiceListener listener) {
    this.listener = listener;
  }

  public synchronized void startup() {
    if (!isRunning) {
      if (executor==null) {
        throw new RuntimeException("No executor configured in JobExecutor");
      }
      
      timer = new Timer("Job executor timer");

      keepDoing(new Runnable() {
        @Override
        public void run() {
          checkWorkflowInstanceJobs();
        }
      }, 100, checkInterval);

      keepDoing(new Runnable() {
        @Override
        public void run() {
          checkJobs();
        }
      }, 500, checkInterval);

      isRunning = true;
    }
  }
  
  /** Repeatedly executes the given doable until this job executor is shutdown.
   * It uses the process engine executor service to execute the doable.
   * We use a single timer object.  As each timer uses it's own thread, we ensure 
   * that the job executor only uses 1 thread for triggering all the recurring 
   * method invocations.  We delegate the doables to the executor, so we're sure they 
   * won't take long. */
  public void keepDoing(final Runnable doable, long delay, long period) {
    timer.schedule(new TimerTask(){
      @Override
      public void run() {
        executor.execute(doable);
      }
    }, delay, period);
  }

  public void shutdown() {
    timer.cancel();
    isRunning = false;
  }

  public boolean isRunning() {
    return isRunning;
  }

  public void checkWorkflowInstanceJobs() {
    boolean keepGoing = true;
    while (isRunning && keepGoing) {
      WorkflowInstanceImpl lockedProcessInstance = workflowInstanceStore.lockWorkflowInstanceWithJobsDue();
      if (lockedProcessInstance!=null) {
        executor.execute(new ExecuteWorkflowInstanceJobs(lockedProcessInstance));
      } else {
        keepGoing = false;
      }
    }
  }

  @Override
  public void start(Brewery brewery) {
    log.info("Starting workflowInstance timers.");
    this.startup();
  }

  public static void stop() {
    log.info("Stop called, stopping timers...");
    if (jobServiceImpl != null) {
      jobServiceImpl.shutdown();
    }
  }

  class ExecuteWorkflowInstanceJobs implements Runnable {
    JobServiceImpl jobService;
    WorkflowInstanceImpl workflowInstance;
    public ExecuteWorkflowInstanceJobs(WorkflowInstanceImpl workflowInstance) {
      this.workflowInstance = workflowInstance;
    }
    @Override
    public void run() {
      log.debug("Executing jobs for workflow instance "+workflowInstance.id);
      Job[] jobsArray = new Job[workflowInstance.jobs.size()];

      for (int i = 0; i < workflowInstance.jobs.size(); i++) {
        jobsArray[i] = workflowInstance.jobs.get(i);
      }

      for (int i = 0; i < jobsArray.length; i++) {
        Job job = jobsArray[i];
        if(job.isDue()) {
          log.debug("Jos is due, workflowInstanceId is: " + job.getWorkflowInstanceId() + ", jobId: " + job.getId());
          executeJob(new JobExecution(job, configuration, workflowInstance));

          if(job.isDone() | job.isDead()) {
            workflowInstance.removeJob(job);
            jobStore.saveArchivedJob(job);
          }

          if(i < jobsArray.length - 1) {
            workflowInstanceStore.flush(workflowInstance);
          }
        }
      }
      workflowInstanceStore.flushAndUnlock(workflowInstance);
    }
  }

  public void checkJobs() {
    boolean keepGoing = true;
    while (isRunning && keepGoing) {
      Job job = jobStore.lockNextJob();
      if (job != null) {
        if (job.jobType!=null) {
          executor.execute(new ExecuteJob(job));
        } else {
          shutdown();
          keepGoing = false;
        }
      } else {
        keepGoing = false;
      }
    }
  }
  
  class ExecuteJob implements Runnable {
    Job job;
    public ExecuteJob(Job job) {
      this.job = job;
    }
    @Override
    public void run() {
      executeJob(new JobExecution(job, configuration));
      if (job.isDone()||job.isDead()) {
        jobStore.deleteJobById(job.id);
        jobStore.saveArchivedJob(job);
      } else {
        jobStore.saveJob(job);
      }
    }
  }
  
  public void executeJob(JobExecution jobExecution) {
    Job job = jobExecution.job; 
    log.debug("Executing job "+job.id);
    job.dueDate = null;
    job.lock = null;
    JobType jobType = job.jobType;
    try {
      try {
        jobType.execute(jobExecution);
        if (job.dueDate ==null) { // if reschedule() was not called...
          job.done = Time.now();
          if (listener!=null) {
            listener.notifyJobDone(jobExecution);
          }
        } else {
          if (listener!=null) {
            listener.notifyJobRescheduled(jobExecution);
          }
        }
        
      } catch (Throwable exception) {
        log.error("Job failed: "+exception.getMessage(), exception);
        StringWriter stackTraceCollector = new StringWriter();
        exception.printStackTrace(new PrintWriter(stackTraceCollector));
        jobExecution.log(stackTraceCollector.toString());
        jobExecution.error = true;
        if (job.retries==null) {
          job.retries = (long) jobType.getMaxRetries();
        } 
        if (job.retries>0) {
          job.retries--;
          long retry = jobType.getMaxRetries()-job.retries;
          int delayInSeconds = jobType.getRetryDelayInSeconds(retry);
          jobExecution.rescheduleFromNow(delayInSeconds*1000);
          if (listener!=null) {
            listener.notifyJobRetry(jobExecution);
          }
        } else {
          // ALARM !  Manual intervention required
          job.done = Time.now();
          job.dead = true;
          if (listener!=null) {
            listener.notifyJobFailure(jobExecution);
          }
        }
      }
    } finally {
      if (job.executions==null) {
        job.executions = new LinkedList<>();
      }
      job.executions.add(jobExecution);
      if (job.executions.size()>maxJobExecutions) {
        job.executions.remove(0);
      }
    }
  }

  @Override
  public void saveJob(Job job) {
    jobStore.saveJob(job);
  }
}
