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

import static com.effektif.mongo.MongoHelper.readBasicDBObject;
import static com.effektif.mongo.MongoHelper.readBoolean;
import static com.effektif.mongo.MongoHelper.readId;
import static com.effektif.mongo.MongoHelper.readList;
import static com.effektif.mongo.MongoHelper.readLong;
import static com.effektif.mongo.MongoHelper.readObjectMap;
import static com.effektif.mongo.MongoHelper.readString;
import static com.effektif.mongo.MongoHelper.readTime;
import static com.effektif.mongo.MongoHelper.writeBooleanOpt;
import static com.effektif.mongo.MongoHelper.writeIdOpt;
import static com.effektif.mongo.MongoHelper.writeLongOpt;
import static com.effektif.mongo.MongoHelper.writeObjectOpt;
import static com.effektif.mongo.MongoHelper.writeStringOpt;
import static com.effektif.mongo.MongoHelper.writeTimeOpt;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bson.types.ObjectId;

import com.effektif.workflow.api.model.RequestContext;
import com.effektif.workflow.impl.configuration.Brewable;
import com.effektif.workflow.impl.configuration.Brewery;
import com.effektif.workflow.impl.job.Job;
import com.effektif.workflow.impl.job.JobExecution;
import com.effektif.workflow.impl.job.JobQuery;
import com.effektif.workflow.impl.job.JobStore;
import com.effektif.workflow.impl.job.JobType;
import com.effektif.workflow.impl.json.JsonService;
import com.effektif.workflow.impl.util.Time;
import com.effektif.workflow.impl.workflowinstance.LockImpl;
import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;


public class MongoJobStore implements JobStore, Brewable {
  
  interface JobFields {
    public String _id = "_id";
    public String key = "key";
    public String duedate = "duedate";
    public String lock = "lock";
    public String executions= "executions";
    public String retries = "retries";
    public String retryDelay = "retryDelay";
    public String done = "done";
    public String dead = "dead";
    public String organizationId = "organizationId";
    public String processId = "processId";
    public String workflowId = "workflowId";
    public String workflowInstanceId = "workflowInstanceId";
    public String lockWorkflowInstance = "lockWorkflowInstance";
    public String activityInstanceId = "activityInstanceId";
    public String taskId = "taskId";
    public String error = "error";
    public String logs = "logs";
    public String time = "time";
    public String duration = "duration";
    public String owner = "owner";
    public String jobType = "jobType";
  }

  protected JsonService jsonService;
  protected String lockOwner;
  protected MongoCollection jobsCollection;
  protected MongoCollection archivedJobsCollection;
  
  @Override
  public void brew(Brewery brewery) {
    MongoDb mongoDb = brewery.get(MongoDb.class);
    MongoConfiguration mongoConfiguration = brewery.get(MongoConfiguration.class);
    this.jobsCollection = mongoDb.createCollection(mongoConfiguration.getJobsCollectionName());
    this.archivedJobsCollection = mongoDb.createCollection(mongoConfiguration.getJobsArchivedCollectionName());
  }
  
  public void saveJob(Job job) {
    BasicDBObject dbJob = writeJob(job);
    if (job.key!=null) {
      BasicDBObject query = new BasicDBObject(JobFields.key, job.key);
      jobsCollection.update("insert-job-with-key", query, dbJob, true, false);
    } else {
      jobsCollection.save("save-job", dbJob);
    }
  }
  
  public Iterator<String> getWorkflowInstanceIdsToLockForJobs() {
    DBObject query = buildLockNextJobQuery()
      .push(JobFields.workflowInstanceId).append("$exists", true).pop()
      .get();
    filterOrganization(query, JobFields.organizationId);
    DBObject retrieveFields = new BasicDBObject(JobFields.workflowInstanceId, true);
    DBCursor jobsDueHavingProcessInstance = jobsCollection.find("jobs-having-process-instance", query, retrieveFields);
    List<String> processInstanceIds = new ArrayList<>();
    while (jobsDueHavingProcessInstance.hasNext()) {
      DBObject partialJob = jobsDueHavingProcessInstance.next();
      Object processInstanceId = partialJob.get(JobFields.workflowInstanceId);
      processInstanceIds.add(processInstanceId.toString());
    }
    return processInstanceIds.iterator();
  }

  @Override
  public Job lockNextJob() {
    DBObject query = buildLockNextJobQuery()
      .push(JobFields.workflowInstanceId).append("$exists", false).pop()
      .get();
    filterOrganization(query, JobFields.organizationId);
    return lockNextJob(query);
  }

  public Job lockNextJob(DBObject query) {
    DBObject dbLock = BasicDBObjectBuilder.start()
      .append(JobFields.time, Time.now().toDate())
      .append(JobFields.owner, lockOwner)
      .get();
    DBObject update = BasicDBObjectBuilder.start()
      .push("$set").append(JobFields.lock, dbLock).pop()
      .get();
    BasicDBObject dbJob = jobsCollection.findAndModify("lock-next-job", query, update);
    if (dbJob!=null) {
      return readJob(dbJob);
    }
    return null;
  }

  protected BasicDBObjectBuilder buildLockNextJobQuery() {
    Date now = Time.now().toDate();
    return BasicDBObjectBuilder.start()
      .append("$or", new DBObject[]{
        new BasicDBObject(JobFields.duedate, new BasicDBObject("$exists", false)),
        new BasicDBObject(JobFields.duedate, new BasicDBObject("$lte", now))
      })
      .push(JobFields.done).append("$exists", false).pop();
  }

  public Job readJob(BasicDBObject dbJob) {
    Job job = new Job();
    job.id = readId(dbJob, JobFields._id);
    job.key = readString(dbJob, JobFields.key);
    job.duedate = readTime(dbJob, JobFields.duedate);
    job.dead = readBoolean(dbJob, JobFields.dead);
    job.done = readTime(dbJob, JobFields.done);
    job.retries = readLong(dbJob, JobFields.retries);
    job.retryDelay = readLong(dbJob, JobFields.retryDelay);
    job.organizationId = readId(dbJob, JobFields.organizationId);
    job.processId = readId(dbJob, JobFields.processId);
    job.taskId = readId(dbJob, JobFields.taskId);
    job.processDefinitionId = readId(dbJob, JobFields.workflowId);
    job.workflowInstanceId = readId(dbJob, JobFields.workflowInstanceId);
    job.activityInstanceId = readId(dbJob, JobFields.activityInstanceId);
    readExecutions(job, readList(dbJob, JobFields.executions));
    readLock(job, readBasicDBObject(dbJob, JobFields.lock));
    Map<String,Object> dbJobType = readObjectMap(dbJob, JobFields.jobType);
    job.jobType = jsonService.jsonMapToObject(dbJobType, JobType.class);
    return job;
  }
  
  public void readExecutions(Job job, List<BasicDBObject> dbExecutions) {
    if (dbExecutions!=null && !dbExecutions.isEmpty()) {
      job.executions = new LinkedList<>();
      for (BasicDBObject dbJobExecution: dbExecutions) {
        JobExecution jobExecution = new JobExecution();
        jobExecution.error = readBoolean(dbJobExecution, JobFields.error);
        jobExecution.logs = readString(dbJobExecution, JobFields.logs);
        jobExecution.time = readTime(dbJobExecution, JobFields.time);
        jobExecution.duration = readLong(dbJobExecution, JobFields.duration);
        job.executions.add(jobExecution);
      }
    }
  }

  public void readLock(Job job, BasicDBObject dbLock) {
    if (dbLock!=null) {
      job.lock = new LockImpl();
      job.lock.time = readTime(dbLock, JobFields.time);
      job.lock.owner = readString(dbLock, JobFields.owner);
    }
  }

  public BasicDBObject writeJob(Job job) {
    BasicDBObject dbJob = new BasicDBObject();
    writeIdOpt(dbJob, JobFields._id, job.id);
    writeStringOpt(dbJob, JobFields.key, job.key);
    writeTimeOpt(dbJob, JobFields.duedate, job.duedate);
    writeBooleanOpt(dbJob, JobFields.dead, job.dead);
    writeTimeOpt(dbJob, JobFields.done, job.done);
    writeLongOpt(dbJob, JobFields.retries, job.retries);
    writeLongOpt(dbJob, JobFields.retryDelay, job.retryDelay);
    writeIdOpt(dbJob, JobFields.organizationId, job.organizationId);
    writeIdOpt(dbJob, JobFields.processId, job.processId);
    writeIdOpt(dbJob, JobFields.activityInstanceId, job.activityInstanceId);
    writeIdOpt(dbJob, JobFields.workflowInstanceId, job.workflowInstanceId);
    writeIdOpt(dbJob, JobFields.workflowId, job.processDefinitionId);
    writeIdOpt(dbJob, JobFields.taskId, job.taskId);
    writeExecutions(dbJob, job.executions);
    writeLock(dbJob, job.lock);
    
    Object dbJobType = jsonService.objectToJsonMap(job.jobType);
    writeObjectOpt(dbJob, JobFields.jobType, dbJobType);
    
    return dbJob;
  }

  public void writeExecutions(BasicDBObject dbJob, LinkedList<JobExecution> jobExecutions) {
    if (jobExecutions!=null && !jobExecutions.isEmpty()) {
      List<BasicDBObject> dbExecutions = new ArrayList<>();
      for (JobExecution jobExecution: jobExecutions) {
        BasicDBObject dbJobExecution = new BasicDBObject();
        writeBooleanOpt(dbJobExecution, JobFields.error, jobExecution.error);
        writeStringOpt(dbJobExecution, JobFields.logs, jobExecution.logs);
        writeTimeOpt(dbJobExecution, JobFields.time, jobExecution.time);
        writeLongOpt(dbJobExecution, JobFields.duration, jobExecution.duration);
        dbExecutions.add(dbJobExecution);
      }
      dbJob.put(JobFields.executions, dbExecutions);
    }
  }

  public void writeLock(BasicDBObject dbJob, LockImpl lock) {
    if (lock!=null) {
      BasicDBObject dbLock = new BasicDBObject();
      writeTimeOpt(dbLock, JobFields.time, lock.time);
      writeStringOpt(dbLock, JobFields.owner, lock.owner);
      dbJob.put(JobFields.lock, dbLock);
    }
  }

  @Override
  public void deleteJobs(JobQuery query) {
    jobsCollection.remove("delete-jobs", buildQuery(query));
  }

  public List<Job> findJobs(JobQuery jobQuery) {
    return findJobs(jobsCollection, jobQuery);
  }

  protected List<Job> findJobs(MongoCollection collection, JobQuery jobQuery) {
    List<Job> jobs = new ArrayList<Job>();
    BasicDBObject query = buildQuery(jobQuery);
    DBCursor jobCursor = collection.find("find-jobs", query);
    while (jobCursor.hasNext()) {
      BasicDBObject dbJob = (BasicDBObject) jobCursor.next();
      Job job = readJob(dbJob);
      jobs.add(job);
    }
    return jobs;
  }

  public BasicDBObject buildQuery(JobQuery jobQuery) {
    BasicDBObject dbQuery = new BasicDBObject();
    filterOrganization(dbQuery, JobFields.organizationId);
    if (jobQuery.getJobId()!=null) {
      dbQuery.append(JobFields._id, new ObjectId(jobQuery.getJobId()));
    }
    return dbQuery;
  }

  protected void filterOrganization(DBObject dbQuery, String fieldName) {
    RequestContext requestContext = RequestContext.current();
    if (requestContext!=null) {
      dbQuery.put(fieldName, requestContext.getOrganizationId());
    }
  }

  @Override
  public void deleteJobById(String jobId) {
    BasicDBObject dbQuery = buildQuery(new JobQuery().jobId(jobId));
    jobsCollection.remove("delete-job", dbQuery);
  }

  @Override
  public void saveArchivedJob(Job job) {
    BasicDBObject dbJob = writeJob(job);
    archivedJobsCollection.save("save-archived-job", dbJob);
  }

  @Override
  public List<Job> findArchivedJobs(JobQuery query) {
    return findJobs(archivedJobsCollection, query);
  }

  @Override
  public void deleteArchivedJobs(JobQuery query) {
    archivedJobsCollection.remove("delete-archived-jobs", buildQuery(query));
  }
}
