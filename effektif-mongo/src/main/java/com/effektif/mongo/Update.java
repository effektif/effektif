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

import com.mongodb.BasicDBObject;


public class Update {
  
  protected BasicDBObject update = new BasicDBObject();

  public Update push(String field, Object value) {
    return push(field, value, null);
  }

  public Update push(String field, Object value, String prefix) {
    add("$push", prefix, field, value);
    return this;
  }

  public Update pushAll(String field, Object value) {
    return pushAll(field, value, null);
  }

  public Update pushAll(String field, Object value, String prefix) {
    add("$pushAll", prefix, field, value);
    return this;
  }

  public Update pull(String field, Object value) {
    return pull(field, value, null);
  }

  public Update pull(String field, Object value, String prefix) {
    add("$pull", prefix, field, value);
    return this;
  }

  public Update pullAll(String field, Object value) {
    return pullAll(field, value, null);
  }

  public Update pullAll(String field, Object value, String prefix) {
    add("$pullAll", prefix, field, value);
    return this;
  }

  public Update addToSet(String field, Object value) {
    addToSet(field, value, null);
    return this;
  }

  public Update addToSet(String field, Object value, String prefix) {
    add("$addToSet", prefix, field, value);
    return this;
  }

  public Update setOpt(String field, Object value) {
    return setOpt(field, value, null);
  }

  public Update setOpt(String field, Object value, String prefix) {
    if (value!=null) {
      add("$set", prefix, field, value);
    }
    return this;
  }

  public Update unset(String field) {
    set(field, null, null);
    return this;
  }

  public Update set(String field, Object value) {
    return set(field, value, null);
  }
  
  public Update set(String field, Object value, String prefix) {
    if (value!=null) {
      add("$set", prefix, field, value);
    } else {
      add("$unset", prefix, field, 1);
    }
    return this;
  }
  
  public Update inc(String field, int amount) {
    add("$inc", null, field, amount);
    return this;
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
