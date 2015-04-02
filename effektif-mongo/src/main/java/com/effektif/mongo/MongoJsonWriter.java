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

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bson.types.ObjectId;
import org.joda.time.LocalDateTime;

import com.effektif.workflow.api.json.JsonWritable;
import com.effektif.workflow.api.model.Id;
import com.effektif.workflow.api.model.WorkflowId;
import com.effektif.workflow.api.model.WorkflowInstanceId;
import com.effektif.workflow.impl.json.AbstractJsonWriter;
import com.effektif.workflow.impl.json.deprecated.JsonMappings;
import com.effektif.workflow.impl.util.Lists;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;


/**
 * @author Tom Baeyens
 */
public class MongoJsonWriter extends AbstractJsonWriter {
  
  BasicDBObject dbObject;
  
  Set<Class<?>> entityIdClasses = new HashSet<Class<?>>(Lists.of(
          WorkflowId.class,
          WorkflowInstanceId.class));
  
  public MongoJsonWriter() {
  }

  public MongoJsonWriter(JsonMappings jsonMappings) {
    super(jsonMappings);
  }

  public Object toDbObject(Object o) {
    if (o==null
        || (o instanceof String)
        || (o instanceof Boolean)
        || (o instanceof Number)) {
      return o;
    }
    if (o instanceof JsonWritable) {
      return toDbObject((JsonWritable)o);
    }
    if (o instanceof LocalDateTime) {
      return toDbObject((LocalDateTime)o);
    }
    if (o instanceof Id) {
      return toDbObject((Id)o);
    }
    if (o instanceof List) {
      return toDbObject((List<Object>)o);
    }
    if (o instanceof Map) {
      return toDbObject((Map<String,Object>)o);
    }
    throw new RuntimeException("Don't know how to map to db object "+o+" ("+o.getClass().getName()+")");
  }

  protected Date toDbObject(LocalDateTime date) {
    if (date==null) {
      return null;
    }
    return date.toDate();
  }
  
  public BasicDBObject toDbObject(JsonWritable o) {
    if (o==null) {
      return null;
    }
    BasicDBObject parentDbObject = dbObject;
    dbObject = new BasicDBObject();
    jsonMappings.writeTypeField(this, o);
    o.writeFields(this);
    BasicDBObject newDbObject = dbObject;
    dbObject = parentDbObject;
    return newDbObject;
  }

  protected Object toDbObject(Id id) {
    if (id==null) {
      return null;
    }
    if (entityIdClasses.contains(id.getClass())) {
      return new ObjectId(id.getInternal());
    }
    return id.getInternal();
  }

  protected BasicDBList toDbObject(List<? extends Object> list) {
    if (list==null) {
      return null;
    }
    BasicDBList dbList = new BasicDBList();
    for (Object element: list) {
      Object dbElement = toDbObject(element); 
      dbList.add(dbElement);
    }
    return dbList;
  }

  protected BasicDBObject toDbObject(Map<String,? extends Object> map) {
    if (map==null) {
      return null;
    }
    BasicDBObject dbMap = new BasicDBObject();
    for (String key: map.keySet()) {
      Object value = map.get(key);
      Object dbValue = toDbObject(value);
      dbMap.put(key, dbValue);
    }
    return dbMap;
  }
  
  @Override
  public void writeId(Id id) {
    writeId("_id", id);
  }

  @Override
  public void writeId(String fieldName, Id id) {
    if (id!=null) {
      dbObject.put(fieldName, toDbObject(id));
    }
  }

  @Override
  public void writeString(String fieldName, String stringValue) {
    if (stringValue!=null) {
      dbObject.put(fieldName, stringValue);
    }
  }

  @Override
  public void writeBoolean(String fieldName, Boolean value) {
    if (value!=null) {
      dbObject.put(fieldName, value);
    }
  }
  
  @Override
  public void writeNumber(String fieldName, Number value) {
    if (value!=null) {
      dbObject.put(fieldName, value);
    }
  }

  @Override
  public void writeDate(String fieldName, LocalDateTime value) {
    if (value!=null) {
      dbObject.put(fieldName, value.toDate());
    }
  }

  @Override
  public void writeObject(String fieldName, JsonWritable value) {
    if (value!=null) {
      BasicDBObject dbValue = toDbObject(value); 
      dbObject.put(fieldName, dbValue);
    }
  }

  
  @Override
  public void writeList(String fieldName, List< ? extends Object> list) {
    if (list!=null && !list.isEmpty()) {
      BasicDBList dbList = toDbObject(list);
      dbObject.put(fieldName, dbList);
    }
  }

  @Override
  public void writeFields(Map<String,? extends Object> map) {
    if (map!=null) {
      for (String key: map.keySet()) {
        Object value = map.get(key);
        if (value!=null) {
          Object dbValue = toDbObject(value); 
          dbObject.put(key, dbValue);
        }
      }
    }
  }

  @Override
  public void writeMap(String fieldName, Map<String, ? extends Object> map) {
    if (map!=null) {
      BasicDBObject dbMap = toDbObject(map);
      dbObject.put(fieldName, dbMap);
    }
  }
}
