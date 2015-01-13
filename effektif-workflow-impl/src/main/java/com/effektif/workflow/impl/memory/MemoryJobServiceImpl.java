/* Copyright 2014 Effektif GmbH.
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
package com.effektif.workflow.impl.memory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.effektif.workflow.impl.Time;
import com.effektif.workflow.impl.WorkflowEngineConfiguration;
import com.effektif.workflow.impl.job.Job;
import com.effektif.workflow.impl.job.JobQueryImpl;
import com.effektif.workflow.impl.job.JobServiceImpl;
import com.effektif.workflow.impl.plugin.ServiceRegistry;


public class MemoryJobServiceImpl extends JobServiceImpl {
  
  protected Set<String> workflowInstanceIds;
  protected LinkedList<Job> jobs;
  protected List<Job> jobsDone;
  protected Map<String,Job> jobsById;

  public MemoryJobServiceImpl() {
  }
  
  @Override
  public void initialize(ServiceRegistry serviceRegistry, WorkflowEngineConfiguration configuration) {
    super.initialize(serviceRegistry, configuration);
    this.workflowInstanceIds = new HashSet<>();
    this.jobs = new LinkedList<>();
    this.jobsDone = new ArrayList<>();
    this.jobsById = new HashMap<>();
  }

  @Override
  public synchronized Iterator<String> getWorkflowInstanceIdsToLockForJobs() {
    return workflowInstanceIds.iterator();
  }

  @Override
  public synchronized Job lockNextWorkflowJob(String workflowInstanceId) {
    return lockJob(true);
  }

  @Override
  public synchronized Job lockNextOtherJob() {
    return lockJob(false);
  }

  public synchronized Job lockJob(boolean mustHaveWorkflowInstance) {
    Iterator<Job> jobIterator = jobs.iterator();
    while (jobIterator.hasNext()) {
      Job job = jobIterator.next();
      if ( ( job.duedate==null || duedateHasPast(job) )
           && ( (mustHaveWorkflowInstance && job.workflowInstanceId!=null)
                || (!mustHaveWorkflowInstance && job.workflowInstanceId==null) )
         ) {
        jobIterator.remove();
        return job;
      }
    }
    return null;
  }

  public boolean duedateHasPast(Job job) {
    long nowMillis = Time.now();
    long duedateMillis = job.duedate;
    return (duedateMillis - nowMillis)<=0;
  }

  @Override
  public synchronized void saveJob(Job job) {
    if (job.done==null) {
      jobs.add(job);
      if (job.key!=null) {
        Job oldJob = jobsById.put(job.key, job);
        if (oldJob!=null) {
          jobs.remove(oldJob);
        }
      }
      if (job.workflowInstanceId != null) {
        workflowInstanceIds.add(job.workflowInstanceId);
      }
    } else {
      jobsDone.add(job);
    }
  }

  @Override
  public List<Job> findJobs(JobQueryImpl jobQuery) {
    return jobs;
  }

  @Override
  public void deleteJob(String jobId) {
  }
}
