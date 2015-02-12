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

import java.util.Map;

import org.bson.types.ObjectId;
import org.slf4j.Logger;

import com.effektif.workflow.impl.WorkflowEngineImpl;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.WriteConcern;
import com.mongodb.WriteResult;


public class MongoCollection {
  
  public static final Logger log = WorkflowEngineImpl.log;
  
  public DBCollection dbCollection;
  public boolean isPretty;
  public WriteConcern defaultWriteConcern;
  public Map<String,WriteConcern> writeConcerns;

  public MongoCollection(DBCollection dbCollection, boolean isPretty) {
    this.dbCollection = dbCollection;
    this.isPretty = isPretty;
  }

  public WriteResult insert(String description, BasicDBObject o) {
    if (log.isDebugEnabled())  {
      log.debug("--"+description+"-> o="+toString(o));
    }
    WriteResult writeResult = dbCollection.insert(o, getWriteConcern(description));
    if (log.isDebugEnabled())  {
      log.debug("<-"+description+"-- "+writeResult);
    }
    return writeResult;
  }
  
  public WriteResult save(String description, BasicDBObject dbObject) {
    if (log.isDebugEnabled()) {
      log.debug("--"+dbCollection.getName()+"-> "+description+" "+toString(dbObject));
    }
    WriteResult writeResult = dbCollection.save(dbObject, getWriteConcern(description));
    if (log.isDebugEnabled()) {
      log.debug("<-"+dbCollection.getName()+"-- "+writeResult);
    }
    return writeResult;
  }

  public void updateById(String description, ObjectId id, DBObject dbObject) {
    update(description, new BasicDBObject("_id", id), dbObject, false, false);
  }

  public WriteResult update(String description, DBObject query, DBObject update) {
    return update(description, query, update, false, false);
  }

  public WriteResult update(String description, DBObject query, DBObject update, boolean upsert, boolean multi) {
    if (log.isDebugEnabled()) {
      log.debug("--"+dbCollection.getName()+"-> "+description+" q="+toString(query)+" u="+toString(update));
    }
    WriteResult writeResult = dbCollection.update(query, update, upsert, multi, getWriteConcern(description));
    if (log.isDebugEnabled()) {
      log.debug("<-"+dbCollection.getName()+"-- "+writeResult);
    }
    return writeResult;
  }

  public BasicDBObject findAndModify(String description, DBObject query, DBObject update) {
    return findAndModify(description, query, update, null);
  }

  public BasicDBObject findAndModify(String description, DBObject query, DBObject update, DBObject fields) {
    if (log.isDebugEnabled()) {
      log.debug("--"+dbCollection.getName()+"-> "+description+" q="+toString(query)+" u="+toString(update));
    }
    BasicDBObject dbObject = (BasicDBObject) dbCollection.findAndModify( query, fields, null, false, update, true, false );
    if (log.isDebugEnabled()) {
      log.debug("<-"+dbCollection.getName()+"-- "+(dbObject!=null ? toString(dbObject) : "null"));
    }
    return dbObject;
  }

  public BasicDBObject findOne(String description, DBObject query) {
    return findOne(description, query, null);
  }

  public BasicDBObject findOne(String description, DBObject query, DBObject fields) {
    if (log.isDebugEnabled()) {
      log.debug("--"+dbCollection.getName()+"-> "+description+" q="+toString(query));
    }
    BasicDBObject dbObject = (BasicDBObject) dbCollection.findOne(query, fields);
    if (log.isDebugEnabled()) {
      log.debug("<-"+dbCollection.getName()+"-- "+toString(dbObject));
    }
    return dbObject;
  }

  public DBCursor find(String description, DBObject query) {
    return find(description, query, null);
  }

  public DBCursor find(String description, DBObject query, DBObject fields) {
    if (log.isDebugEnabled()) {
      log.debug("--"+dbCollection.getName()+"-> "+description+" q="+toString(query)+(fields!=null ? " f="+toString(fields) :""));
    }
    return new LoggingCursor(this, dbCollection.find(query, fields));
  }
  
  public WriteResult remove(String description, DBObject query) {
    if (log.isDebugEnabled()) { 
      log.debug("--"+dbCollection.getName()+"-> "+description+" q="+toString(query));
    }
    WriteResult writeResult = dbCollection.remove(query);
    if (log.isDebugEnabled()) {
      log.debug("<-"+dbCollection.getName()+"-- "+writeResult);
    }
    return writeResult;
  }

  public String toString(Object o) {
    return isPretty ? PrettyPrinter.toJsonPrettyPrint(o) : o.toString();
  }

  public WriteConcern getWriteConcern(WriteConcern writeConcern) {
    return writeConcern!=null ? writeConcern : dbCollection.getWriteConcern();
  }

  public DBCollection getDbCollection() {
    return dbCollection;
  }
  
  public boolean isPretty() {
    return isPretty;
  }
  
  public WriteConcern getWriteConcern(String description) {
    if (writeConcerns==null) {
      return dbCollection.getWriteConcern();
    }
    WriteConcern writeConcern = writeConcerns.get(description);
    return writeConcern!=null ? writeConcern : dbCollection.getWriteConcern();
  }
}
