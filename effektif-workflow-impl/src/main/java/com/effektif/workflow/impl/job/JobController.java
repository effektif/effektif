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
import com.effektif.workflow.impl.workflowinstance.WorkflowInstanceImpl;


/** 
 * Provides access to relevant job information and 
 * methods to rescheduling the job to JobType implementations.
 * Retry in case of exceptions is handled by the JobService itself. 
 * 
 * @author Tom Baeyens
 */
public interface JobController {
  
  Configuration getConfiguration();
  
  /** allows job types to get the locked workflow instance */
  WorkflowInstanceImpl getWorkflowInstance();
  
  Job getJob();

  /** can be used if the job wants to repeat this job on a later date.
   * JobType impls should NOT perform their own retry, the JobService takes care of that. */
  void rescheduleFromNow(int delayInMillis);
  
  /** can be used if the job wants to repeat this job on a later date.
   * JobType impls should NOT perform their own retry, the JobService takes care of that. */
  void rescheduleFor(LocalDateTime dueDate);
  
  void log(String msg);
}
