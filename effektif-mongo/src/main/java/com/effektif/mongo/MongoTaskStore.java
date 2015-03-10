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
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.bson.types.ObjectId;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;

import com.effektif.mongo.MongoWorkflowStore.FieldsWorkflow;
import com.effektif.workflow.api.acl.Access;
import com.effektif.workflow.api.model.UserId;
import com.effektif.workflow.api.query.OrderBy;
import com.effektif.workflow.api.query.OrderDirection;
import com.effektif.workflow.api.task.Task;
import com.effektif.workflow.api.task.TaskQuery;
import com.effektif.workflow.impl.TaskStore;
import com.effektif.workflow.impl.WorkflowEngineImpl;
import com.effektif.workflow.impl.configuration.Brewable;
import com.effektif.workflow.impl.configuration.Brewery;
import com.effektif.workflow.impl.json.JsonService;
import com.effektif.workflow.impl.util.Time;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;


public class MongoTaskStore implements TaskStore, Brewable {
  
  public static final Logger log = WorkflowEngineImpl.log;
  
  protected JsonService jsonService;
  protected MongoCollection tasksCollection;
  
  public interface FieldsTask {
    String _ID = "_id";
    String NAME = "name";
    String ORGANIZATION_ID = "organizationId";
    String ASSIGNEE = "assignee";
    String SUBTASK_IDS = "subtaskIds";
    String LAST_UPDATED = "lastUpdated";
    String COMPLETED = "completed";
    String ACTIVITY_NOTIFY = "activityNotify";
  }

  public interface FieldsUserReference {
    String ID = "id";
  }

  @Override
  public void brew(Brewery brewery) {
    MongoDb mongoDb = brewery.get(MongoDb.class);
    MongoConfiguration mongoConfiguration = brewery.get(MongoConfiguration.class);
    this.tasksCollection = mongoDb.createCollection(mongoConfiguration.getTasksCollectionName());
    this.jsonService = brewery.get(JsonService.class);
  }

  @Override
  public String generateTaskId() {
    return new ObjectId().toString();
  }

  @Override
  public void insertTask(Task task) {
    task.setLastUpdated(Time.now());
    BasicDBObject dbTask = taskToMongo(task);
    tasksCollection.insert("insert-task", dbTask);
  }

  @Override
  public Task assignTask(String taskId, UserId assignee) {
    BasicDBObject query = new MongoQuery()
      ._id(taskId)
      .access(Access.EDIT)
      .get();
    BasicDBObject dbAssignee = new BasicDBObject(FieldsUserReference.ID, assignee.getId());
    BasicDBObject update = new MongoUpdate()
      .set(FieldsTask.ASSIGNEE, dbAssignee)
      .set(FieldsTask.LAST_UPDATED, Time.now().toDate())
      .get();
    BasicDBObject dbTask = tasksCollection.findAndModify("assign-task", query, update);
    return mongoToTask(dbTask);
  }
  

  @Override
  public Task completeTask(String taskId) {
    BasicDBObject query = new MongoQuery()
      ._id(taskId)
      .access(Access.EDIT)
      .get();
    BasicDBObject update = new MongoUpdate()
      .set(FieldsTask.COMPLETED, true)
      .set(FieldsTask.LAST_UPDATED, Time.now().toDate())
      .unset(FieldsTask.ACTIVITY_NOTIFY)
      .get();
    BasicDBObject dbTask = tasksCollection.findAndModify("complete-task", query, update, null, null, false, false, false);
    return mongoToTask(dbTask);
  }
  
  @Override
  public Task addSubtask(String parentId, Task subtask) {
    String subtaskId = subtask.getId();
    BasicDBObject query = new MongoQuery()
      ._id(parentId)
      .access(Access.EDIT)
      .get();
    BasicDBObject update = new MongoUpdate()
      .push(FieldsTask.SUBTASK_IDS, subtaskId)
      .set(FieldsTask.LAST_UPDATED, Time.now().toDate())
      .get();
    BasicDBObject dbTask = tasksCollection.findAndModify("add-subtask", query, update);
    return mongoToTask(dbTask);
  }
  
  @Override
  public List<Task> findTasks(TaskQuery query) {
    List<Task> tasks = new ArrayList<>();
    BasicDBObject dbQuery = createTaskQuery(query, Access.VIEW).get();
    DBCursor dbCursor = tasksCollection.find("find-tasks", dbQuery);
    if (query.getLimit()!=null) {
      dbCursor.limit(query.getLimit());
    }
    if (query.getOrderBy()!=null) {
      dbCursor.sort(writeOrderBy(query.getOrderBy()));
    }
    while (dbCursor.hasNext()) {
      BasicDBObject dbTask = (BasicDBObject) dbCursor.next();
      Task task = mongoToTask(dbTask);
      tasks.add(task);
    }
    return tasks;
  }

  @Override
  public void deleteTasks(TaskQuery query) {
    BasicDBObject dbQuery = createTaskQuery(query, Access.EDIT).get();
    tasksCollection.remove("delete-tasks", dbQuery);
  }

  public BasicDBObject taskToMongo(Task task) {
    Map<String,Object> jsonWorkflow = jsonService.objectToJsonMap(task);
    BasicDBObject dbWorkflow = new BasicDBObject(); 
    jsonWorkflow.remove("id");
    jsonWorkflow.remove(FieldsTask.ORGANIZATION_ID);
    jsonWorkflow.remove(FieldsTask.LAST_UPDATED);
    dbWorkflow.putAll(jsonWorkflow);
    writeId(dbWorkflow, FieldsWorkflow._ID, task.getId());
    writeIdOpt(dbWorkflow, FieldsWorkflow.ORGANIZATION_ID, task.getOrganizationId());
    writeTimeOpt(dbWorkflow, FieldsTask.LAST_UPDATED, task.getLastUpdated());
    return dbWorkflow;
  }

  public Task mongoToTask(BasicDBObject dbTask) {
    ObjectId taskId = (ObjectId) dbTask.remove(FieldsWorkflow._ID);
    ObjectId organizationId = (ObjectId) dbTask.remove(FieldsTask.ORGANIZATION_ID);
    Date lastUpdated = (Date) dbTask.remove(FieldsTask.LAST_UPDATED);
    Task task = jsonService.jsonMapToObject(dbTask, Task.class);
    if (taskId!=null) {
      task.setId(taskId.toString());
    }
    if (organizationId!=null) {
      task.setOrganizationId(organizationId.toString());
    }
    if (lastUpdated!=null) {
      task.setLastUpdated(new LocalDateTime(lastUpdated));
    }
    return task;
  }
  
  /** builds the query and ensures VIEW access */
  protected MongoQuery createTaskQuery(TaskQuery query, String... accessActions) {
    MongoQuery mongoQuery = new MongoQuery();
    if (accessActions!=null) {
      mongoQuery.access(accessActions);
    }
    if (query.getTaskId()!=null) {
      mongoQuery.equal(FieldsTask._ID, new ObjectId(query.getTaskId()));
    }
    if (query.getTaskName()!=null) {
      mongoQuery.equal(FieldsTask.NAME, Pattern.compile(query.getTaskName()));
    }
    if (query.getCompleted()!=null) {
      if (query.getCompleted()) {
        mongoQuery.equal(FieldsTask.COMPLETED, true);
      } else {
        mongoQuery.doesNotExist(FieldsTask.COMPLETED);
      }
    }
    return mongoQuery;
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
