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
package com.effektif.mongo;

import com.effektif.workflow.impl.DefaultConfiguration;
import com.effektif.workflow.impl.util.Lists;
import com.mongodb.*;
import com.mongodb.gridfs.GridFS;

import java.util.ArrayList;
import java.util.List;

/**
 * @see <a href="https://github.com/effektif/effektif/wiki/Workflow-engine-types#mongodb-workflow-engine">MongoDB workflow engine</a>
 */
public class MongoConfiguration extends DefaultConfiguration {

  public static List<ServerAddress> DEFAULT_SERVER_ADDRESSES = Lists.of(createServerAddress("localhost", null));
  
  protected List<ServerAddress> serverAddresses;
  protected String databaseName = "effektif";
  protected String fileDatabaseName = "effektif-files";
  protected List<MongoCredential> credentials;
  protected String workflowsCollectionName = "workflows";
  protected String workflowInstancesCollectionName = "workflowInstances";
  protected String jobsCollectionName = "jobs";
  protected String jobsArchivedCollectionName = "jobsArchived";
  protected String typeCollectionName = "types";
  protected boolean isPretty;
  protected MongoClientOptions.Builder optionBuilder = new MongoClientOptions.Builder();
  protected boolean storeWorkflowIdsAsStrings = false;

  public MongoConfiguration() {
    brewery.ingredient(this);
    brewery.supplier(new MongoClientSupplier(), MongoClient.class);
    brewery.supplier(new MongoDbSupplier(), DB.class);
    brewery.supplier(new MongoGridFSSupplier(), GridFS.class);
    brewery.supplier(new MongoObjectMapperSupplier(), MongoObjectMapper.class);
    brewery.ingredient(new MongoDb());
    brewery.ingredient(new MongoWorkflowStore());
    brewery.ingredient(new MongoWorkflowInstanceStore());
    brewery.ingredient(new MongoJobStore());
    brewery.ingredient(new MongoObjectMappingsBuilder());
  }
  
  public MongoConfiguration db(DB db) {
    brewery.ingredient(db);
    return this;
  }
  
  public MongoConfiguration mongoClient(MongoClient mongoClient) {
    brewery.ingredient(mongoClient);
    return this;
  }
  
  public MongoConfiguration server(String host) {
    if (serverAddresses==null) {
      serverAddresses = new ArrayList<>();
    }
    serverAddresses.add(createServerAddress(host, null));
    return this;
  }

  public MongoConfiguration server(String host, int port) {
    if (serverAddresses==null) {
      serverAddresses = new ArrayList<>();
    }
    serverAddresses.add(createServerAddress(host, port));
    return this;
  }

  protected static ServerAddress createServerAddress(String host, Integer port) {
    try {
      if (port!=null) {
        return new ServerAddress(host, port);
      }
      return new ServerAddress(host);
    } catch (IllegalArgumentException | MongoException e) {
      throw new RuntimeException(e);
    }
  }
  
  public List<ServerAddress> getServerAddresses() {
    return serverAddresses!=null ? serverAddresses : DEFAULT_SERVER_ADDRESSES;
  }

  public MongoConfiguration authentication(String username, String password, String database) {
    if (credentials==null) {
      credentials = new ArrayList<>();
    }
    credentials.add(MongoCredential.createCredential(username, database, password.toCharArray()));
    return this;
  }

  public MongoConfiguration workflowInstancesCollectionName(String processInstancesCollectionName) {
    this.workflowInstancesCollectionName = processInstancesCollectionName;
    return this;
  }

  public MongoConfiguration workflowsCollectionName(String workflowsCollectionName) {
    this.workflowsCollectionName = workflowsCollectionName;
    return this;
  }

  public MongoConfiguration jobsCollectionName(String jobsCollectionName) {
    this.jobsCollectionName = jobsCollectionName;
    return this;
  }
  
  public MongoConfiguration prettyPrint() {
    this.isPretty = true;
    return this;
  }
  
  public MongoConfiguration storeWorkflowIdsAsStrings() {
    this.storeWorkflowIdsAsStrings = true;
    return this;
  }
  
  public MongoConfiguration jobsArchivedCollectionName(String jobsArchivedCollectionName) {
    this.jobsArchivedCollectionName = jobsArchivedCollectionName;
    return this;
  }

  @Override
  public MongoConfiguration synchronous() {
    super.synchronous();
    return this;
  }

  public MongoConfiguration databaseName(String databaseName) {
    this.databaseName = databaseName;
    return this;
  }

  // getters and setters //////////////////////////////////////////////////
  
  public void setServerAddresses(List<ServerAddress> serverAddresses) {
    this.serverAddresses = serverAddresses;
  }
  
  public String getDatabaseName() {
    return databaseName;
  }
  
  public void setDatabaseName(String databaseName) {
    this.databaseName = databaseName;
  }
  
  public List<MongoCredential> getCredentials() {
    return credentials;
  }
  
  public void setCredentials(List<MongoCredential> credentials) {
    this.credentials = credentials;
  }
  
  public String getWorkflowInstancesCollectionName() {
    return workflowInstancesCollectionName;
  }
  
  public void setWorkflowInstancesCollectionName(String workflowInstancesCollectionName) {
    this.workflowInstancesCollectionName = workflowInstancesCollectionName;
  }
 
  public String getWorkflowsCollectionName() {
    return workflowsCollectionName;
  }

  public void setWorkflowsCollectionName(String workflowsCollectionName) {
    this.workflowsCollectionName = workflowsCollectionName;
  }

  public String getJobsCollectionName() {
    return jobsCollectionName;
  }
  
  public void setJobsCollectionName(String jobsCollectionName) {
    this.jobsCollectionName = jobsCollectionName;
  }
  
  public String getJobsArchivedCollectionName() {
    return this.jobsArchivedCollectionName;
  }
  public void setJobsArchivedCollectionName(String jobsArchivedCollectionName) {
    this.jobsArchivedCollectionName = jobsArchivedCollectionName;
  }
  
  public String getTypeCollectionName() {
    return typeCollectionName;
  }
  
  public void setTypeCollectionName(String typeCollectionName) {
    this.typeCollectionName = typeCollectionName;
  }

  public boolean isPretty() {
    return isPretty;
  }
  
  public void setPretty(boolean isPretty) {
    this.isPretty = isPretty;
  }

  public boolean getStoreWorkflowIdsAsString() {
    return this.storeWorkflowIdsAsStrings;
  }
  
  public void setStoreWorkflowIdsAsString(boolean storeWorkflowIdsAsStrings) {
    this.storeWorkflowIdsAsStrings = storeWorkflowIdsAsStrings;
  }
  
  public void setOptionBuilder(MongoClientOptions.Builder optionBuilder) {
    this.optionBuilder = optionBuilder;
  }
  
  public String getFileDatabaseName() {
    return fileDatabaseName;
  }

  public void setFileDatabaseName(String fileDatabaseName) {
    this.fileDatabaseName = fileDatabaseName;
  }

  public MongoClientOptions.Builder getOptionBuilder() {
    return optionBuilder;
  }

  @Override
  public MongoConfiguration ingredient(Object ingredient) {
    super.ingredient(ingredient);
    return this;
  }

}
