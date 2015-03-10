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

import java.util.Map;

import org.bson.types.ObjectId;

import com.mongodb.BasicDBObject;


public class MongoUpdate {
  
  protected BasicDBObject update = new BasicDBObject();

  public MongoUpdate push(String field, Object value) {
    return push(field, value, null);
  }

  public MongoUpdate push(String field, Object value, String prefix) {
    add("$push", prefix, field, value);
    return this;
  }

  public MongoUpdate setOpt(String field, Object value) {
    return setOpt(field, value, null);
  }

  public MongoUpdate setOpt(String field, Object value, String prefix) {
    if (value!=null) {
      add("$set", prefix, field, value);
    }
    return this;
  }

  public MongoUpdate unset(String field) {
    set(field, null, null);
    return this;
  }

  public MongoUpdate set(String field, Object value) {
    return set(field, value, null);
  }
  
  public MongoUpdate set(String field, Object value, String prefix) {
    if (value!=null) {
      add("$set", prefix, field, value);
    } else {
      add("$unset", prefix, field, 1);
    }
    return this;
  }
  
  public MongoUpdate inc(String field, int amount) {
    add("$inc", null, field, amount);
    return this;
  }

  public MongoUpdate field(String field, Map<String,Object> updates) {
    field(null, field, updates, null);
    return this;
  }
  
  public MongoUpdate field(String field, Map<String,Object> updates, String prefix) {
    field(prefix, field, updates, null);
    return this;
  }

  private static interface ValueMapper {
    Object map(Object value);
  }
    
  private void field(String prefix, String field, Map<String,Object> updates, ValueMapper valueMapper) {
    if (field!=null && updates!=null && updates.containsKey(field)) {
      Object value = updates.get(field);
      if (valueMapper!=null) {
        value = valueMapper.map(value);
      }
      set(field,value);
    }
  }

  private void add(String operation, String prefix, String field, Object value) {
    BasicDBObject fields = (BasicDBObject) update.get(operation);
    if (fields==null) {
      fields = new BasicDBObject();
      update.put(operation, fields);
    }
    fields.put(applyPrefix(prefix, field), value);
  }
  
  private static String applyPrefix(String prefix, String field) {
    return prefix!=null ? prefix+field : field;
  }

  public BasicDBObject get() {
    return update;
  }

  public boolean isEmpty() {
    return update.isEmpty();
  }
}
