/*
 * Copyright (c) 2013, Effektif GmbH.  All rights reserved.
 */

package com.effektif.workflow.impl.job;

import com.effektif.workflow.impl.workflowinstance.WorkflowInstanceImpl;
import com.fasterxml.jackson.annotation.JsonIgnore;


/**
 * @author Tom Baeyens
 */
public class JobExecution implements JobController {
  
  @JsonIgnore
  public Job job;
  @JsonIgnore
  public WorkflowInstanceImpl processInstance;
  public Boolean error;
  public String logs;
  public Long time;
  public Long duration;

  public JobExecution() {
  }

  public JobExecution(Job job, WorkflowInstanceImpl processInstance) {
    this.job = job;
    this.processInstance = processInstance;
    this.time = System.currentTimeMillis();
  }

  public void rescheduleFromNow(Long delayInMillis) {
    job.rescheduleFromNow(delayInMillis);
  }
  
  public void rescheduleFor(Long duedate) {
    job.rescheduleFor(duedate);
  }
  
  public void log(String msg) {
    logs = (logs!=null ? logs : "") + msg + "\n";
  }

  public WorkflowInstanceImpl getProcessInstance() {
    return processInstance;
  }
}
