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
package com.effektif.workflow.impl.memory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.effektif.workflow.api.model.WorkflowInstanceId;
import com.effektif.workflow.impl.job.Job;
import com.effektif.workflow.impl.job.JobQuery;
import com.effektif.workflow.impl.job.JobStore;


/**
 * @author Tom Baeyens
 */
public class MemoryJobStore implements JobStore {
  
  protected Map<String,Job> jobs = new LinkedHashMap<>();
  protected List<Job> archivedJobs = new ArrayList<>();

  @Override
  public synchronized Job lockNextJob() {
    for (Job job: jobs.values()) {
      if (job.isDue() && !job.isDone() && job.getWorkflowInstanceId()==null) {
        jobs.remove(job.id);
        return job;
      }
    }
    return null;
  }

  @Override
  public synchronized void saveJob(Job job) {
    if (job.key!=null) {
      for (Job existingJob: jobs.values()) {
        if (job.key.equals(existingJob.key)) {
          job.id = existingJob.id;
        }
      }
    }
    if (job.id==null) {
      job.id = UUID.randomUUID().toString();
    }
    jobs.put(job.id, job);
  }

  protected List<Job> findJobs(Collection<Job> jobs, JobQuery query) {
    List<Job> result = new ArrayList<>();
    for (Job job: jobs) {
      if (query.meetsCriteria(job)) {
        result.add(job);
      }
    }
    return result;
  }

  @Override
  public void deleteAllJobs() {
    jobs = new LinkedHashMap<>();
  }

  @Override
  public void deleteJobById(String jobId) {
    jobs.remove(jobId);
  }

  @Override
  public void saveArchivedJob(Job job) {
    archivedJobs.add(job);
  }

  @Override
  public void deleteAllArchivedJobs() {
    archivedJobs = new ArrayList<>();
  }

  @Override
  public List<Job> findAllJobs() {
    return new ArrayList<>(jobs.values());
  }

  @Override
  public void deleteJobByScope(WorkflowInstanceId workflowInstanceId, String activityInstanceId) {
    for (Job job: new ArrayList<>(archivedJobs)) {
      if (workflowInstanceId.equals(job.getWorkflowInstanceId())
          && (activityInstanceId==null || activityInstanceId.equals(activityInstanceId))) {
        archivedJobs.remove(job);
      }
    }
  }
}
