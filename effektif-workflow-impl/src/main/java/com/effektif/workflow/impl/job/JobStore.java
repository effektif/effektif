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
package com.effektif.workflow.impl.job;

import java.util.Iterator;
import java.util.List;


public interface JobStore {

  void saveJob(Job job);
  
  List<Job> findJobs(JobQuery jobQuery);
  
  void deleteJobs(JobQuery query);

  /** returns the ids of process instance that have jobs requiring
   * a process instance lock. When a job requires a process instance lock, 
   * it has to specify {@link Job#workflowInstanceId} and set {@link Job#lockWorkflowInstance} to true.
   * This method is allowed to return null. */
  Iterator<String> getWorkflowInstanceIdsToLockForJobs();

  /** locks a job having the given processInstanceId and retrieves it from the store */
  Job lockNextWorkflowJob(String processInstanceId);

  /** locks a job not having a {@link Job#lockWorkflowInstance} specified  
   * and retrieves it from the store */
  Job lockNextOtherJob();

}
