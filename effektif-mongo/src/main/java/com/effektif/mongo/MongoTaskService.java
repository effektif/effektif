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
import com.effektif.workflow.api.acl.Authorizations;
import com.effektif.workflow.api.query.OrderBy;
import com.effektif.workflow.api.query.OrderDirection;
import com.effektif.workflow.api.ref.UserReference;
import com.effektif.workflow.api.task.Task;
import com.effektif.workflow.api.task.TaskQuery;
import com.effektif.workflow.api.task.TaskService;
import com.effektif.workflow.impl.WorkflowEngineImpl;
import com.effektif.workflow.impl.configuration.Brewable;
import com.effektif.workflow.impl.configuration.Brewery;
import com.effektif.workflow.impl.json.JsonService;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;


public class MongoTaskService implements TaskService, Brewable {
  
  public static final Logger log = WorkflowEngineImpl.log;
  
  protected JsonService jsonService;
  protected MongoCollection tasksCollection;
  
  public interface FieldsTask {
    String _ID = "_id";
    String NAME = "name";
    String ORGANIZATION_ID = "organizationId";
  }

  @Override
  public void brew(Brewery brewery) {
    MongoDb mongoDb = brewery.get(MongoDb.class);
    MongoConfiguration mongoConfiguration = brewery.get(MongoConfiguration.class);
    this.tasksCollection = mongoDb.createCollection(mongoConfiguration.getTasksCollectionName());
    this.jsonService = brewery.get(JsonService.class);
  }

  @Override
  public void insertTask(Task task) {
    BasicDBObject dbTask = taskToMongo(task);
    tasksCollection.insert("insert-task", dbTask);
  }

  @Override
  public void assignTask(String taskId, UserReference assignee) {
//    BasicDBObject query = new MongoQuery()
//      .access();
  }

  @Override
  public List<Task> findTasks(TaskQuery query) {
    List<Task> tasks = new ArrayList<>();
    DBCursor cursor = createTaskDbCursor(query);
    while (cursor.hasNext()) {
      BasicDBObject dbTask = (BasicDBObject) cursor.next();
      Task task = mongoToTask(dbTask);
      tasks.add(task);
    }
    return tasks;
  }

  @Override
  public void deleteTasks(TaskQuery query) {
    BasicDBObject dbQuery = createTaskDbQuery(query);
    tasksCollection.remove("delete-tasks", dbQuery);
  }

  public BasicDBObject taskToMongo(Task task) {
    Map<String,Object> jsonWorkflow = jsonService.objectToJsonMap(task);
    BasicDBObject dbWorkflow = new BasicDBObject(); 
    jsonWorkflow.remove("id");
    jsonWorkflow.remove(FieldsTask.ORGANIZATION_ID);
    dbWorkflow.putAll(jsonWorkflow);
    writeId(dbWorkflow, FieldsWorkflow._ID, task.getId());
    writeIdOpt(dbWorkflow, FieldsWorkflow.ORGANIZATION_ID, task.getOrganizationId());
    return dbWorkflow;
  }

  public Task mongoToTask(BasicDBObject dbTask) {
    ObjectId taskId = (ObjectId) dbTask.remove(FieldsWorkflow._ID);
    ObjectId organizationId = (ObjectId) dbTask.remove(FieldsWorkflow.ORGANIZATION_ID);
    Task task = jsonService.jsonMapToObject(dbTask, Task.class);
    if (taskId!=null) {
      task.setId(taskId.toString());
    }
    if (organizationId!=null) {
      task.setOrganizationId(organizationId.toString());
    }
    return task;
  }
  
  public String generateTaskId() {
    return new ObjectId().toString();
  }

  public DBCursor createTaskDbCursor(TaskQuery query) {
    BasicDBObject dbQuery = createTaskDbQuery(query);
    DBCursor dbCursor = tasksCollection.find("find-tasks", dbQuery);
    if (query.getLimit()!=null) {
      dbCursor.limit(query.getLimit());
    }
    if (query.getOrderBy()!=null) {
      dbCursor.sort(writeOrderBy(query.getOrderBy()));
    }
    return dbCursor;
  }

  protected BasicDBObject createTaskDbQuery(TaskQuery query) {
    BasicDBObject dbQuery = new BasicDBObject();
    if (query.getTaskId()!=null) {
      dbQuery.append(FieldsTask._ID, new ObjectId(query.getTaskId()));
    }
// TODO change to MongoQuery
//  if (MongoHelper.hasOrganizationId(authorization)) {
//    dbQuery.append(FieldsWorkflow.ORGANIZATION_ID, authorization.getOrganizationId());
//  }
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
