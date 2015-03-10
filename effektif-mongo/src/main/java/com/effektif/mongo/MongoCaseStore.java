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

import static com.effektif.mongo.MongoHelper.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bson.types.ObjectId;
import org.slf4j.Logger;

import com.effektif.mongo.MongoWorkflowStore.FieldsWorkflow;
import com.effektif.workflow.api.acl.Access;
import com.effektif.workflow.api.query.OrderBy;
import com.effektif.workflow.api.query.OrderDirection;
import com.effektif.workflow.api.task.Case;
import com.effektif.workflow.api.task.CaseId;
import com.effektif.workflow.api.task.CaseQuery;
import com.effektif.workflow.impl.CaseStore;
import com.effektif.workflow.impl.WorkflowEngineImpl;
import com.effektif.workflow.impl.configuration.Brewable;
import com.effektif.workflow.impl.configuration.Brewery;
import com.effektif.workflow.impl.json.JsonService;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;


public class MongoCaseStore implements CaseStore, Brewable {
  
  public static final Logger log = WorkflowEngineImpl.log;
  
  protected JsonService jsonService;
  protected MongoCollection casesCollection;
  
  public interface FieldsCase {
    String _ID = "_id";
    String NAME = "name";
    String ORGANIZATION_ID = "organizationId";
    String LAST_UPDATED = "lastUpdated";
  }

  @Override
  public void brew(Brewery brewery) {
    MongoDb mongoDb = brewery.get(MongoDb.class);
    MongoConfiguration mongoConfiguration = brewery.get(MongoConfiguration.class);
    this.casesCollection = mongoDb.createCollection(mongoConfiguration.getCasesCollectionName());
    this.jsonService = brewery.get(JsonService.class);
  }

  @Override
  public CaseId generateCaseId() {
    return new CaseId(new ObjectId().toString());
  }

  @Override
  public void insertCase(Case caze) {
    BasicDBObject dbCase = caseToMongo(caze);
    casesCollection.insert("insert-case", dbCase);
  }

//  @Override
//  public Task addTask(String caseId, Case caze) {
//    String subtaskId = caze.getId();
//    BasicDBObject query = new MongoQuery()
//      ._id(parentId)
//      .access(Access.EDIT)
//      .get();
//    BasicDBObject update = new MongoUpdate()
//      .push(FieldsCase.SUBTASK_IDS, subtaskId)
//      .set(FieldsCase.LAST_UPDATED, Time.now().toDate())
//      .get();
//    BasicDBObject dbTask = casesCollection.findAndModify("add-subtask", query, update);
//    return mongoToTask(dbTask);
//  }
  
  @Override
  public List<Case> findCases(CaseQuery query) {
    List<Case> cases = new ArrayList<>();
    BasicDBObject dbQuery = createDbQuery(query, Access.VIEW).get();
    DBCursor dbCursor = casesCollection.find("find-cases", dbQuery);
    if (query.getLimit()!=null) {
      dbCursor.limit(query.getLimit());
    }
    if (query.getOrderBy()!=null) {
      dbCursor.sort(writeOrderBy(query.getOrderBy()));
    }
    while (dbCursor.hasNext()) {
      BasicDBObject dbCase = (BasicDBObject) dbCursor.next();
      Case task = mongoToCase(dbCase);
      cases.add(task);
    }
    return cases;
  }

  @Override
  public void deleteCases(CaseQuery query) {
    BasicDBObject dbQuery = createDbQuery(query, Access.EDIT).get();
    casesCollection.remove("delete-cases", dbQuery);
  }

  public BasicDBObject caseToMongo(Case caze) {
    Map<String,Object> jsonWorkflow = jsonService.objectToJsonMap(caze);
    BasicDBObject dbWorkflow = new BasicDBObject(); 
    jsonWorkflow.remove("id");
    jsonWorkflow.remove(FieldsCase.ORGANIZATION_ID);
    dbWorkflow.putAll(jsonWorkflow);
    writeId(dbWorkflow, FieldsWorkflow._ID, caze.getId());
    writeIdOpt(dbWorkflow, FieldsWorkflow.ORGANIZATION_ID, caze.getOrganizationId());
    return dbWorkflow;
  }

  public Case mongoToCase(BasicDBObject dbTask) {
    ObjectId caseId = (ObjectId) dbTask.remove(FieldsCase._ID);
    ObjectId organizationId = (ObjectId) dbTask.remove(FieldsCase.ORGANIZATION_ID);
    Case caze = jsonService.jsonMapToObject(dbTask, Case.class);
    if (caseId!=null) {
      caze.setId(new CaseId(caseId.toString()));
    }
    if (organizationId!=null) {
      caze.setOrganizationId(organizationId.toString());
    }
    return caze;
  }
  
  /** builds the query and ensures VIEW access */
  protected MongoQuery createDbQuery(CaseQuery query, String... accessActions) {
    if (query==null) {
      query = new CaseQuery();
    }
    MongoQuery dbQuery = new MongoQuery();
    if (accessActions!=null) {
      dbQuery.access(accessActions);
    }
    if (query.getCaseId()!=null) {
      dbQuery.id(query.getCaseId());
    }
    return dbQuery;
  }

  public DBObject writeOrderBy(List<OrderBy> orderBy) {
    BasicDBObject dbOrderBy = new BasicDBObject();
    for (OrderBy element: orderBy) {
      String dbField = element.getField();
      int dbDirection = (element.getDirection()==OrderDirection.asc ? 1 : -1);
      dbOrderBy.append(dbField, dbDirection);
    }
    return dbOrderBy;
  }
}
