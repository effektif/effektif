/*
 * Copyright (c) 2013, Effektif GmbH.  All rights reserved.
 */

package com.effektif.workflow.impl.job;

import org.joda.time.LocalDateTime;

import com.effektif.workflow.impl.util.Time;
import com.effektif.workflow.impl.workflowinstance.WorkflowInstanceImpl;
import com.fasterxml.jackson.annotation.JsonIgnore;


public class JobExecution implements JobController {
  
  @JsonIgnore
  public Job job;
  @JsonIgnore
  public WorkflowInstanceImpl processInstance;
  public Boolean error;
  public String logs;
  public LocalDateTime time;
  public Long duration;

  public JobExecution() {
  }

  public JobExecution(Job job, WorkflowInstanceImpl processInstance) {
    this.job = job;
    this.processInstance = processInstance;
    this.time = Time.now();
  }

  public void rescheduleFromNow(int delayInMillis) {
    job.rescheduleFromNow(delayInMillis);
  }
  
  public void rescheduleFor(LocalDateTime duedate) {
    job.rescheduleFor(duedate);
  }
  
  public void log(String msg) {
    logs = (logs!=null ? logs : "") + msg + "\n";
  }

  public WorkflowInstanceImpl getProcessInstance() {
    return processInstance;
  }
}
