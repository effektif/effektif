/*
 * Copyright (c) 2013, Effektif GmbH.  All rights reserved.
 */

package com.effektif.workflow.impl.job;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.joda.time.LocalDateTime;
import org.joda.time.Seconds;

import com.effektif.workflow.impl.ExecutorService;
import com.effektif.workflow.impl.WorkflowEngineConfiguration;
import com.effektif.workflow.impl.WorkflowEngineImpl;
import com.effektif.workflow.impl.WorkflowInstanceStore;
import com.effektif.workflow.impl.plugin.Initializable;
import com.effektif.workflow.impl.plugin.ServiceRegistry;
import com.effektif.workflow.impl.workflowinstance.WorkflowInstanceImpl;


/**
 * @author Tom Baeyens
 */
public abstract class JobServiceImpl implements JobService, Initializable<WorkflowEngineConfiguration> {
  
  // private static final Logger log = LoggerFactory.getLogger(JobServiceImpl.class);
  
  public WorkflowEngineImpl processEngine;
  public ExecutorService executor = null;

  // configuration 
  public long checkInterval = 30 * 1000; // 30 seconds
  public int maxJobExecutions = 5;

  // runtime state
  public boolean isRunning = false;
  public Timer timer = null;
  public Timer checkOtherJobsTimer = null;
  public JobServiceListener listener = null;
  
  public JobServiceImpl() {
  }

  @Override
  public void initialize(ServiceRegistry serviceRegistry, WorkflowEngineConfiguration configuration) {
    this.processEngine = serviceRegistry.getService(WorkflowEngineImpl.class);
    this.executor = serviceRegistry.getService(ExecutorService.class);
  }

  public Job newJob(JobType jobType) {
    return new Job(this, jobType);
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
          checkProcessJobs();
        }
      }, 100, checkInterval);

      keepDoing(new Runnable() {
        @Override
        public void run() {
          checkOtherJobs();
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

  public void checkProcessJobs() {
    WorkflowInstanceStore workflowInstanceStore = processEngine.getWorkflowInstanceStore();
    Iterator<String> processInstanceIds = getWorkflowInstanceIdsToLockForJobs();
    while (isRunning 
           && processInstanceIds!=null 
           && processInstanceIds.hasNext()) {
      String workflowInstanceId = processInstanceIds.next();
      WorkflowInstanceImpl lockedProcessInstance = workflowInstanceStore.lockWorkflowInstance(workflowInstanceId, null, null);
      boolean keepGoing = true;
      while (isRunning && keepGoing) {
        Job job = lockNextWorkflowJob(workflowInstanceId);
        if (job != null) {
          executor.execute(new ExecuteJob(job, lockedProcessInstance));
        } else {
          keepGoing = false;
        }
      }
      workflowInstanceStore.flushAndUnlock(lockedProcessInstance);
    }
  }
  
  public void checkOtherJobs() {
    boolean keepGoing = true;
    while (isRunning && keepGoing) {
      Job job = lockNextOtherJob();
      if (job != null) {
        executor.execute(new ExecuteJob(job));
      } else {
        keepGoing = false;
      }
    }
  }
  
  class ExecuteJob implements Runnable {
    JobServiceImpl jobService;
    Job job;
    WorkflowInstanceImpl processInstance;
    public ExecuteJob(Job job) {
      this.job = job;
    }
    public ExecuteJob(Job job, WorkflowInstanceImpl processInstance) {
      this.job = job;
      this.processInstance = processInstance;
    }
    @Override
    public void run() {
      executeJob(job, processInstance);
    }
  }
  
  public void executeJob(Job job, WorkflowInstanceImpl processInstance) {
    JobExecution jobExecution = new JobExecution(job, processInstance);
    job.duedate = null;
    job.lock = null;
    JobType jobType = job.jobType;
    try {
      try {
        jobType.execute(jobExecution);
        if (job.duedate==null) { // if reschedule() was not called...
          job.done = new LocalDateTime();
          if (listener!=null) {
            listener.notifyJobDone(jobExecution);
          }
        } else {
          if (listener!=null) {
            listener.notifyJobRescheduled(jobExecution);
          }
        }
        
      } catch (Throwable exception) {
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
          jobExecution.rescheduleFromNow(Seconds.seconds((int)delayInSeconds));
          if (listener!=null) {
            listener.notifyJobRetry(jobExecution);
          }
        } else {
          // ALARM !  Manual intervention required
          job.done = new LocalDateTime();
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
      saveJob(job);
    }
  }

  @Override
  public JobQuery newJobQuery() {
    return new JobQueryImpl(this);
  }

  public abstract List<Job> findJobs(JobQueryImpl jobQuery);
  
  /** returns the ids of process instance that have jobs requiring
   * a process instance lock. When a job requires a process instance lock, 
   * it has to specify {@link Job#workflowInstanceId} and set {@link Job#lockWorkflowInstance} to true.
   * This method is allowed to return null. */
  public abstract Iterator<String> getWorkflowInstanceIdsToLockForJobs();

  /** locks a job having the given processInstanceId and retrieves it from the store */
  public abstract Job lockNextWorkflowJob(String processInstanceId);

  /** locks a job not having a {@link Job#lockWorkflowInstance} specified  
   * and retrieves it from the store */
  public abstract Job lockNextOtherJob();

  public abstract void saveJob(Job job);
}
