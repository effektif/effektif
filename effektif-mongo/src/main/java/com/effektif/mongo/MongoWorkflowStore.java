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

import static com.effektif.mongo.MongoDb._ID;
import static com.effektif.mongo.WorkflowFields.*;

import com.effektif.workflow.api.Configuration;
import com.effektif.workflow.api.model.WorkflowId;
import com.effektif.workflow.api.query.OrderBy;
import com.effektif.workflow.api.query.OrderDirection;
import com.effektif.workflow.api.query.WorkflowQuery;
import com.effektif.workflow.api.workflow.AbstractWorkflow;
import com.effektif.workflow.api.workflow.ExecutableWorkflow;
import com.effektif.workflow.impl.WorkflowEngineImpl;
import com.effektif.workflow.impl.WorkflowStore;
import com.effektif.workflow.impl.activity.ActivityTypeService;
import com.effektif.workflow.impl.configuration.Brewable;
import com.effektif.workflow.impl.configuration.Brewery;
import com.effektif.workflow.impl.data.DataTypeService;
import com.effektif.workflow.impl.util.Exceptions;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import org.bson.types.ObjectId;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;


public class MongoWorkflowStore implements WorkflowStore, Brewable {
  
  public static final Logger log = MongoDb.log;
  
  protected WorkflowEngineImpl workflowEngine;
  protected DataTypeService dataTypeService;
  protected MongoCollection workflowsCollection;
  protected ActivityTypeService activityTypeService;
  protected Configuration configuration;
  protected MongoObjectMapper mongoMapper;

  @Override
  public void brew(Brewery brewery) {
    MongoDb mongoDb = brewery.get(MongoDb.class);
    MongoConfiguration mongoConfiguration = brewery.get(MongoConfiguration.class);
    this.workflowsCollection = mongoDb.createCollection(mongoConfiguration.getWorkflowsCollectionName());
    this.configuration = brewery.get(Configuration.class);
    this.workflowEngine = brewery.get(WorkflowEngineImpl.class);
    this.activityTypeService = brewery.get(ActivityTypeService.class);
    this.mongoMapper = brewery.get(MongoObjectMapper.class);
  }

  public BasicDBObject workflowApiToMongo(AbstractWorkflow workflow) {
    return (BasicDBObject) mongoMapper.write(workflow);
  }

  public <T extends AbstractWorkflow> T mongoToWorkflowApi(BasicDBObject dbWorkflow, Class<T> workflowClass) {
    return mongoMapper.read(dbWorkflow, workflowClass);
  }
  
  @Override
  public WorkflowId generateWorkflowId() {
    return new WorkflowId(new ObjectId().toString());
  }

  @Override
  public void insertWorkflow(ExecutableWorkflow workflow) {
    BasicDBObject dbWorkflow = workflowApiToMongo(workflow);
    workflowsCollection.insert("insert-workflow", dbWorkflow);
  }

  @Override
  public List<ExecutableWorkflow> findWorkflows(WorkflowQuery query) {
    if (query==null) {
      query = new WorkflowQuery();
    }
    List<ExecutableWorkflow> workflows = new ArrayList<>();
    DBCursor cursor = createWorkflowDbCursor(query);
    while (cursor.hasNext()) {
      BasicDBObject dbWorkflow = (BasicDBObject) cursor.next();
      ExecutableWorkflow workflow = mongoToWorkflowApi(dbWorkflow, ExecutableWorkflow.class);
      workflows.add(workflow);
    }
    return workflows;
  }

  @Override
  public ExecutableWorkflow loadWorkflowById(WorkflowId workflowId) {
    List<ExecutableWorkflow> workflows = findWorkflows(new WorkflowQuery()
      .workflowId(workflowId));
    return !workflows.isEmpty() ? workflows.get(0) : null;
  }

  @Override
  public void deleteWorkflows(WorkflowQuery query) {
    BasicDBObject dbQuery = createDbQuery(query);
    workflowsCollection.remove("delete-workflows", dbQuery);
  }
  
  @Override
  public void deleteAllWorkflows() {
    workflowsCollection.remove("delete-workflows-unchecked", new BasicDBObject(), false);
  }

  @Override
  public WorkflowId findLatestWorkflowIdBySource(String sourceWorkflowId) {
    Exceptions.checkNotNullParameter(sourceWorkflowId, "sourceWorkflowId");
    Query query = new Query()
      .equal(SOURCE_WORKFLOW_ID, sourceWorkflowId)
      .orderDesc(CREATE_TIME)
      .page(0,  1);
    Fields fields = new Fields()
      .include(_ID);
    BasicDBObject dbWorkflow = workflowsCollection.findOne("find-latest-workflow", query.get(), fields.get(), query.orderBy);
    return dbWorkflow!=null ? new WorkflowId(dbWorkflow.get(_ID).toString()) : null;
  }

  public DBCursor createWorkflowDbCursor(WorkflowQuery query) {
    BasicDBObject dbQuery = createDbQuery(query);
    DBCursor dbCursor = workflowsCollection.find("find-workflows", dbQuery);
    if (query.getLimit()!=null) {
      dbCursor.limit(query.getLimit());
    }
    if (query.getOrderBy()!=null) {
      dbCursor.sort(writeOrderBy(query.getOrderBy()));
    }
    return dbCursor;
  }

  protected BasicDBObject createDbQuery(WorkflowQuery query) {
    BasicDBObject dbQuery = new BasicDBObject();
    if (query.getWorkflowId()!=null) {
      dbQuery.append(_ID, new ObjectId(query.getWorkflowId().getInternal()));
    }
    if (query.getWorkflowSource()!=null) {
      dbQuery.append(SOURCE_WORKFLOW_ID, query.getWorkflowSource());
    }
    return dbQuery;
  }
  
  public DBObject writeOrderBy(List<OrderBy> orderBy) {
    BasicDBObject dbOrderBy = new BasicDBObject();
    for (OrderBy element: orderBy) {
      String dbField = getDbField(element.getField());
      int dbDirection = (element.getDirection()==OrderDirection.asc ? 1 : -1);
      dbOrderBy.append(dbField, dbDirection);
    }
    return dbOrderBy;
  }
  
  private String getDbField(String field) {
    if (WorkflowQuery.FIELD_CREATE_TIME.equals(field)) {
      return CREATE_TIME;
    }
    throw new RuntimeException("Unknown field "+field);
  }

  public MongoCollection getWorkflowsCollection() {
    return workflowsCollection;
  }
}
