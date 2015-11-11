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

import org.joda.time.LocalDateTime;

import com.effektif.workflow.api.Configuration;
import com.effektif.workflow.api.json.JsonIgnore;
import com.effektif.workflow.impl.util.Time;
import com.effektif.workflow.impl.workflowinstance.WorkflowInstanceImpl;


/**
 * @author Tom Baeyens
 */
public class JobExecution implements JobController {
  
  @JsonIgnore
  public Job job;
  @JsonIgnore
  public WorkflowInstanceImpl workflowInstance;
  public Boolean error;
  public String logs;
  public LocalDateTime time;
  public Long duration;
  public Configuration configuration;

  public JobExecution() {
  }

  public JobExecution(Job job, Configuration configuration) {
    this(job, configuration, null);
  }
  
  public JobExecution(Job job, Configuration configuration, WorkflowInstanceImpl workflowInstance) {
    this.job = job;
    this.workflowInstance = workflowInstance;
    this.time = Time.now();
    this.configuration = configuration;
  }

  public void rescheduleFromNow(int delayInMillis) {
    job.rescheduleFromNow(delayInMillis);
  }
  
  public void rescheduleFor(LocalDateTime dueDate) {
    job.rescheduleFor(dueDate);
  }
  
  public void log(String msg) {
    logs = (logs!=null ? logs : "") + msg + "\n";
  }

  public WorkflowInstanceImpl getWorkflowInstance() {
    return workflowInstance;
  }

  
  public Job getJob() {
    return job;
  }

  @Override
  public Configuration getConfiguration() {
    return configuration;
  }
  
  
}
