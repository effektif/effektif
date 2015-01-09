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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.effektif.impl.WorkflowEngineImpl;
import com.effektif.impl.json.JsonService;
import com.effektif.impl.memory.MemoryTaskService;
import com.effektif.workflow.api.WorkflowEngineConfiguration;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;



public class MongoWorkflowEngine extends WorkflowEngineImpl {
  
  protected DB db;

  public MongoWorkflowEngine() {
    this(new MongoWorkflowEngineConfiguration());
  }
  
  public MongoWorkflowEngine(MongoWorkflowEngineConfiguration configuration) {
    super(configuration);
  }
  
  @Override
  protected void initializeStorageServices(WorkflowEngineConfiguration cfg) {
    MongoWorkflowEngineConfiguration configuration = (MongoWorkflowEngineConfiguration) cfg;
    JsonService jsonService = serviceRegistry.getService(JsonService.class);
    
    MongoClient mongoClient = new MongoClient(
            configuration.getServerAddresses(), 
            configuration.getCredentials(), 
            configuration.getOptionBuilder().build());
    
    this.db = mongoClient.getDB(configuration.getDatabaseName());
    configuration.registerService(db);

    boolean isPretty = configuration.isPretty();

//    MongoWorkflowStore processDefinitions = new MongoWorkflowStore(serviceRegistry);
//    processDefinitions.dbCollection = db.getCollection(configuration.getWorkflowsCollectionName());
//    processDefinitions.isPretty = isPretty;
//    processDefinitions.fields = configuration.getProcessDefinitionFields();
//    processDefinitions.writeConcernInsertProcessDefinition = processDefinitions.getWriteConcern(configuration.getWriteConcernInsertProcessDefinition());
//    configuration.registerService(processDefinitions);

    MongoWorkflowInstanceStore workflowInstances = new MongoWorkflowInstanceStore(serviceRegistry);
    workflowInstances.dbCollection = db.getCollection(configuration.workflowInstancesCollectionName);
    workflowInstances.processEngine = this;
    workflowInstances.isPretty = isPretty;
    workflowInstances.fields = configuration.getProcessInstanceFields();
    workflowInstances.writeConcernStoreProcessInstance = workflowInstances.getWriteConcern(configuration.getWriteConcernInsertWorkflowInstance());
    workflowInstances.writeConcernFlushUpdates = workflowInstances.getWriteConcern(configuration.getWriteConcernFlushUpdates());
    configuration.registerService(workflowInstances);

    // TODO
    // MongoTaskService mongoTaskService = new MongoTaskService();
    configuration.registerService(new MemoryTaskService());
    
    MongoJobs mongoJobs = new MongoJobs(serviceRegistry);
    DBCollection jobsDbCollection = db.getCollection(configuration.getJobsCollectionName());
    mongoJobs.dbCollection = jobsDbCollection;
    mongoJobs.isPretty = configuration.isPretty();
    mongoJobs.fields = configuration.getJobFields();
    mongoJobs.writeConcernJobs = mongoJobs.getWriteConcern(configuration.getWriteConcernJobs());
    mongoJobs.lockOwner = configuration.getId();
    mongoJobs.jsonService = jsonService;
    configuration.registerService(mongoJobs);

    MongoJobService mongoJobService = new MongoJobService(serviceRegistry);
    mongoJobService.jobs = mongoJobs;
    configuration.registerService(mongoJobService);
  }
  
  public DB getDb() {
    return db;
  }
  
  public void setDb(DB db) {
    this.db = db;
  }
}
