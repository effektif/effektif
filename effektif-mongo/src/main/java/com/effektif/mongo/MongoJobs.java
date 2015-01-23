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

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.effektif.workflow.impl.configuration.Brewery;
import com.effektif.workflow.impl.job.Job;
import com.effektif.workflow.impl.job.JobExecution;
import com.effektif.workflow.impl.job.JobQueryImpl;
import com.effektif.workflow.impl.job.JobType;
import com.effektif.workflow.impl.json.JsonService;
import com.effektif.workflow.impl.util.Time;
import com.effektif.workflow.impl.workflowinstance.LockImpl;
import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.WriteConcern;


public class MongoJobs extends MongoCollection {

  protected MongoConfiguration.JobFields fields;
  protected WriteConcern writeConcernJobs;
  protected String lockOwner;
  protected JsonService jsonService;

  public MongoJobs() {
  }

  public MongoJobs(Brewery brewery) {
  }

  public void saveJob(Job job) {
    BasicDBObject dbJob = writeJob(job);
    if (job.key!=null) {
      BasicDBObject query = new BasicDBObject(fields.key, job.key);
      update(query, dbJob, true, false, writeConcernJobs);
    } else {
      save(dbJob, writeConcernJobs);
    }
  }
  
  public Iterator<String> getProcessInstanceIdsToLockForJobs() {
    DBObject query = buildJobQuery(true).get();
    DBObject retrieveFields = new BasicDBObject(fields.workflowInstanceId, true);
    DBCursor jobsDueHavingProcessInstance = find(query, retrieveFields);
    List<String> processInstanceIds = new ArrayList<>();
    while (jobsDueHavingProcessInstance.hasNext()) {
      DBObject partialJob = jobsDueHavingProcessInstance.next();
      Object processInstanceId = partialJob.get(fields.workflowInstanceId);
      processInstanceIds.add(processInstanceId.toString());
    }
    return processInstanceIds.iterator();
  }

  protected BasicDBObjectBuilder buildJobQuery(boolean mustHaveProcessInstance) {
    Date now = Time.now().toDate();
    return BasicDBObjectBuilder.start()
      .append("$or", new DBObject[]{
        new BasicDBObject(fields.duedate, new BasicDBObject("$exists", false)),
        new BasicDBObject(fields.duedate, new BasicDBObject("$lte", now))
      })
      .push(fields.done).append("$exists", false).pop()
      .push(fields.workflowInstanceId).append("$exists", mustHaveProcessInstance).pop();
  }

  public Job lockJob(boolean mustHaveProcessInstance) {
    DBObject query = buildJobQuery(mustHaveProcessInstance)
      .push(fields.lock).append("$exists", false).pop()
      .get();
    DBObject dbLock = BasicDBObjectBuilder.start()
      .append(fields.time, Time.now().toDate())
      .append(fields.owner, lockOwner)
      .get();
    DBObject update = BasicDBObjectBuilder.start()
      .push("$set").append(fields.lock, dbLock).pop()
      .get();
    BasicDBObject dbJob = findAndModify(query, update);
    if (dbJob!=null) {
      return readJob(dbJob);
    }
    return null;
  }

  public Job readJob(BasicDBObject dbJob) {
    Job job = new Job();
    job.id = readId(dbJob, fields._id);
    job.key = readString(dbJob, fields.key);
    job.duedate = readTime(dbJob, fields.duedate);
    job.dead = readBoolean(dbJob, fields.dead);
    job.done = readTime(dbJob, fields.done);
    job.retries = readLong(dbJob, fields.retries);
    job.retryDelay = readLong(dbJob, fields.retryDelay);
    job.organizationId = readId(dbJob, fields.organizationId);
    job.processId = readId(dbJob, fields.processId);
    job.taskId = readId(dbJob, fields.taskId);
    job.processDefinitionId = readId(dbJob, fields.workflowId);
    job.workflowInstanceId = readId(dbJob, fields.workflowInstanceId);
    job.activityInstanceId = readId(dbJob, fields.activityInstanceId);
    readExecutions(job, readList(dbJob, fields.executions));
    readLock(job, readBasicDBObject(dbJob, fields.lock));
    Map<String,Object> dbJobType = readObjectMap(dbJob, fields.jobType);
    job.jobType = jsonService.jsonMapToObject(dbJobType, JobType.class);
    return job;
  }
  
  public void readExecutions(Job job, List<BasicDBObject> dbExecutions) {
    if (dbExecutions!=null && !dbExecutions.isEmpty()) {
      job.executions = new LinkedList<>();
      for (BasicDBObject dbJobExecution: dbExecutions) {
        JobExecution jobExecution = new JobExecution();
        jobExecution.error = readBoolean(dbJobExecution, fields.error);
        jobExecution.logs = readString(dbJobExecution, fields.logs);
        jobExecution.time = readTime(dbJobExecution, fields.time);
        jobExecution.duration = readLong(dbJobExecution, fields.duration);
        job.executions.add(jobExecution);
      }
    }
  }

  public void readLock(Job job, BasicDBObject dbLock) {
    if (dbLock!=null) {
      job.lock = new LockImpl();
      job.lock.time = readTime(dbLock, fields.time);
      job.lock.owner = readString(dbLock, fields.owner);
    }
  }

  public BasicDBObject writeJob(Job job) {
    BasicDBObject dbJob = new BasicDBObject();
    writeIdOpt(dbJob, fields._id, job.id);
    writeStringOpt(dbJob, fields.key, job.key);
    writeTimeOpt(dbJob, fields.duedate, job.duedate);
    writeBooleanOpt(dbJob, fields.dead, job.dead);
    writeTimeOpt(dbJob, fields.done, job.done);
    writeLongOpt(dbJob, fields.retries, job.retries);
    writeLongOpt(dbJob, fields.retryDelay, job.retryDelay);
    writeIdOpt(dbJob, fields.organizationId, job.organizationId);
    writeIdOpt(dbJob, fields.processId, job.processId);
    writeIdOpt(dbJob, fields.activityInstanceId, job.activityInstanceId);
    writeIdOpt(dbJob, fields.workflowInstanceId, job.workflowInstanceId);
    writeIdOpt(dbJob, fields.workflowId, job.processDefinitionId);
    writeIdOpt(dbJob, fields.taskId, job.taskId);
    writeExecutions(dbJob, job.executions);
    writeLock(dbJob, job.lock);
    
    Object dbJobType = jsonService.objectToJsonMap(job.jobType);
    writeObjectOpt(dbJob, fields.jobType, dbJobType);
    
    return dbJob;
  }

  public void writeExecutions(BasicDBObject dbJob, LinkedList<JobExecution> jobExecutions) {
    if (jobExecutions!=null && !jobExecutions.isEmpty()) {
      List<BasicDBObject> dbExecutions = new ArrayList<>();
      for (JobExecution jobExecution: jobExecutions) {
        BasicDBObject dbJobExecution = new BasicDBObject();
        writeBooleanOpt(dbJobExecution, fields.error, jobExecution.error);
        writeStringOpt(dbJobExecution, fields.logs, jobExecution.logs);
        writeTimeOpt(dbJobExecution, fields.time, jobExecution.time);
        writeLongOpt(dbJobExecution, fields.duration, jobExecution.duration);
        dbExecutions.add(dbJobExecution);
      }
      dbJob.put(fields.executions, dbExecutions);
    }
  }

  public void writeLock(BasicDBObject dbJob, LockImpl lock) {
    if (lock!=null) {
      BasicDBObject dbLock = new BasicDBObject();
      writeTimeOpt(dbLock, fields.time, lock.time);
      writeStringOpt(dbLock, fields.owner, lock.owner);
      dbJob.put(fields.lock, dbLock);
    }
  }

  public void deleteJob(String jobId) {
    throw new RuntimeException("TODO");
  }

  public List<Job> findJobs(JobQueryImpl jobQuery) {
    List<Job> jobs = new ArrayList<Job>();
    BasicDBObject query = buildQuery(jobQuery);
    DBCursor jobCursor = find(query);
    while (jobCursor.hasNext()) {
      BasicDBObject dbJob = (BasicDBObject) jobCursor.next();
      Job job = readJob(dbJob);
      jobs.add(job);
    }
    return jobs;
  }

  public BasicDBObject buildQuery(JobQueryImpl jobQuery) {
    return null;
  }
}
