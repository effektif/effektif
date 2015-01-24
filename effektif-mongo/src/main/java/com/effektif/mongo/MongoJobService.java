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
package com.effektif.mongo;

import java.util.Iterator;
import java.util.List;

import com.effektif.workflow.impl.configuration.Brewery;
import com.effektif.workflow.impl.job.Job;
import com.effektif.workflow.impl.job.JobQueryImpl;
import com.effektif.workflow.impl.job.JobService;
import com.effektif.workflow.impl.job.JobServiceImpl;
import com.effektif.workflow.impl.job.JobType;



public class MongoJobService extends JobServiceImpl implements JobService {
  
  protected MongoJobs jobs;

  public MongoJobService() {
  }
  
  @Override
  public void brew(Brewery brewery) {
    super.brew(brewery);
    this.jobs = brewery.get(MongoJobs.class);
  }

  @Override
  public Job newJob(JobType jobType) {
    return new Job(this, jobType);
  }

  @Override
  public Iterator<String> getWorkflowInstanceIdsToLockForJobs() {
    return jobs.getProcessInstanceIdsToLockForJobs();
  }

  public Job lockNextWorkflowJob(String processInstanceId) {
    return jobs.lockJob(true);
  }

  @Override
  public Job lockNextOtherJob() {
    return jobs.lockJob(false);
  }

  @Override
  public void saveJob(Job job) {
    jobs.saveJob(job);
  }

  @Override
  public void deleteJob(String jobId) {
    jobs.deleteJob(jobId);
  }

  @Override
  public List<Job> findJobs(JobQueryImpl jobQuery) {
    return jobs.findJobs(jobQuery);
  }
}
