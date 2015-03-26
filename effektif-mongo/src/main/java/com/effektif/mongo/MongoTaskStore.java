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
import com.effektif.workflow.api.model.TaskId;
import com.effektif.workflow.api.model.UserId;
import com.effektif.workflow.api.model.WorkflowId;
import com.effektif.workflow.api.model.WorkflowInstanceId;
import com.effektif.workflow.api.query.OrderBy;
import com.effektif.workflow.api.query.OrderDirection;
import com.effektif.workflow.api.task.Task;
import com.effektif.workflow.api.task.TaskQuery;
import com.effektif.workflow.impl.TaskStore;
import com.effektif.workflow.impl.configuration.Brewable;
import com.effektif.workflow.impl.configuration.Brewery;
import com.effektif.workflow.impl.json.JsonService;
import com.effektif.workflow.impl.util.Time;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;


public class MongoTaskStore implements TaskStore, Brewable {
  
  public static final Logger log = MongoDb.log;
  
  protected JsonService jsonService;
  protected MongoCollection tasksCollection;
  
  public interface FieldsTask {
    String _ID = "_id";
    String NAME = "name";
    String ORGANIZATION_ID = "organizationId";
    String ASSIGNEE_ID = "assigneeId";
    String SUBTASK_IDS = "subtaskIds";
    String LAST_UPDATED = "lastUpdated";
    String COMPLETED = "completed";
    String ACTIVITY_NOTIFY = "activityNotify";
    String HAS_WORKFLOW_FORM = "hasWorkflowForm";
    String WORKFLOW_ID = "workflowId";
    String WORKFLOW_INSTANCE_ID = "workflowInstanceId";
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
  public TaskId generateTaskId() {
    return new TaskId(new ObjectId().toString());
  }

  @Override
  public void insertTask(Task task) {
    task.setLastUpdated(Time.now());
    BasicDBObject dbTask = taskToMongo(task);
    tasksCollection.insert("insert-task", dbTask);
  }

  @Override
  public Task assignTask(TaskId taskId, UserId assignee) {
    BasicDBObject query = new MongoQuery()
      ._id(taskId.getInternal())
      .access(Access.EDIT)
      .get();
    BasicDBObject update = new MongoUpdate()
      .set(FieldsTask.ASSIGNEE_ID, assignee.getInternal())
      .set(FieldsTask.LAST_UPDATED, Time.now().toDate())
      .get();
    BasicDBObject dbTask = tasksCollection.findAndModify("assign-task", query, update);
    return mongoToTask(dbTask);
  }
  

  @Override
  public Task completeTask(TaskId taskId) {
    BasicDBObject query = new MongoQuery()
      ._id(taskId.getInternal())
      .access(Access.EDIT)
      .get();
    BasicDBObject update = new MongoUpdate()
      .set(FieldsTask.COMPLETED, true)
      .set(FieldsTask.LAST_UPDATED, Time.now().toDate())
      .unset(FieldsTask.ACTIVITY_NOTIFY)
      .unset(FieldsTask.HAS_WORKFLOW_FORM)
      .get();
    // this findAndModify returns the old version
    BasicDBObject dbTask = tasksCollection.findAndModify("complete-task", query, update, null, null, false, false, false);
    return mongoToTask(dbTask);
  }
  
  @Override
  public Task addSubtask(TaskId parentId, Task subtask) {
    TaskId subtaskId = subtask.getId();
    BasicDBObject query = new MongoQuery()
      ._id(parentId.getInternal())
      .access(Access.EDIT)
      .get();
    BasicDBObject update = new MongoUpdate()
      .push(FieldsTask.SUBTASK_IDS, new ObjectId(subtaskId.getInternal()))
      .set(FieldsTask.LAST_UPDATED, Time.now().toDate())
      .get();
    BasicDBObject dbTask = tasksCollection.findAndModify("add-subtask", query, update);
    return mongoToTask(dbTask);
  }
  
  @Override
  public List<Task> findTasks(TaskQuery query) {
    List<Task> tasks = new ArrayList<>();
    BasicDBObject dbQuery = createDbQuery(query, Access.VIEW).get();
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
    BasicDBObject dbQuery = createDbQuery(query, Access.EDIT).get();
    tasksCollection.remove("delete-tasks", dbQuery);
  }

  public BasicDBObject taskToMongo(Task task) {
    Map<String,Object> jsonTask = jsonService.objectToJsonMap(task);
    BasicDBObject dbTask = new BasicDBObject(); 
    jsonTask.remove("id");
    jsonTask.remove(FieldsTask.ORGANIZATION_ID);
    jsonTask.remove(FieldsTask.LAST_UPDATED);
    jsonTask.remove(FieldsTask.WORKFLOW_ID);
    jsonTask.remove(FieldsTask.WORKFLOW_INSTANCE_ID);
    dbTask.putAll(jsonTask);
    writeId(dbTask, FieldsWorkflow._ID, task.getId());
    writeIdOpt(dbTask, FieldsTask.ORGANIZATION_ID, task.getOrganizationId());
    writeIdOptNew(dbTask, FieldsTask.WORKFLOW_ID, task.getWorkflowId());
    writeIdOptNew(dbTask, FieldsTask.WORKFLOW_INSTANCE_ID, task.getWorkflowInstanceId());
    writeTimeOpt(dbTask, FieldsTask.LAST_UPDATED, task.getLastUpdated());
    List<String> subtaskIdStrings = (List<String>) dbTask.get(FieldsTask.SUBTASK_IDS);
    if (subtaskIdStrings!=null) {
      List<ObjectId> subtaskIdsInternal = new ArrayList<>();
      for (String subtaskIdString: subtaskIdStrings) {
        subtaskIdsInternal.add(new ObjectId(subtaskIdString));
      }
      dbTask.put(FieldsTask.SUBTASK_IDS, subtaskIdsInternal);
    }
    return dbTask;
  }

  public Task mongoToTask(BasicDBObject dbTask) {
    ObjectId taskId = (ObjectId) dbTask.remove(FieldsTask._ID);
    List<ObjectId> subtaskIdInternals = (List<ObjectId>) dbTask.remove(FieldsTask.SUBTASK_IDS);
    ObjectId organizationId = (ObjectId) dbTask.remove(FieldsTask.ORGANIZATION_ID);
    ObjectId workflowId = (ObjectId) dbTask.remove(FieldsTask.WORKFLOW_ID);
    ObjectId workflowInstanceId = (ObjectId) dbTask.remove(FieldsTask.WORKFLOW_INSTANCE_ID);
    Date lastUpdated = (Date) dbTask.remove(FieldsTask.LAST_UPDATED);
    Task task = jsonService.jsonMapToObject(dbTask, Task.class);
    if (taskId!=null) {
      task.setId(new TaskId(taskId.toString()));
    }
    if (subtaskIdInternals!=null) {
      List<TaskId> subtaskIds = new ArrayList<>(subtaskIdInternals.size());
      for (ObjectId subtaskId: subtaskIdInternals) {
        subtaskIds.add(new TaskId(subtaskId.toString()));
      }
      task.setSubtaskIds(subtaskIds);
    }
    if (organizationId!=null) {
      task.setOrganizationId(organizationId.toString());
    }
    if (workflowId!=null) {
      task.setWorkflowId(new WorkflowId(workflowId.toString()));
    }
    if (workflowInstanceId!=null) {
      task.setWorkflowInstanceId(new WorkflowInstanceId(workflowInstanceId.toString()));
    }
    if (lastUpdated!=null) {
      task.setLastUpdated(new LocalDateTime(lastUpdated));
    }
    
    return task;
  }
  
  /** builds the query and ensures VIEW access */
  protected MongoQuery createDbQuery(TaskQuery query, String... accessActions) {
    if (query==null) {
      query = new TaskQuery();
    }
    MongoQuery mongoQuery = new MongoQuery();
    if (accessActions!=null) {
      mongoQuery.access(accessActions);
    }
    if (query.getTaskId()!=null) {
      mongoQuery.equal(FieldsTask._ID, query.getTaskId().getInternal());
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
