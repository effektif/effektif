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

import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bson.types.ObjectId;
import org.joda.time.LocalDateTime;

import com.effektif.workflow.api.mapper.JsonWritable;
import com.effektif.workflow.api.model.Id;
import com.effektif.workflow.api.model.WorkflowId;
import com.effektif.workflow.api.model.WorkflowInstanceId;
import com.effektif.workflow.api.workflow.Binding;
import com.effektif.workflow.impl.mapper.AbstractWriter;
import com.effektif.workflow.impl.mapper.Mappings;
import com.effektif.workflow.impl.util.Lists;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;


/**
 * @author Tom Baeyens
 */
public class MongoJsonWriter extends AbstractWriter {
  
  BasicDBObject dbObject;
  
  Set<Class<?>> entityIdClasses = new HashSet<Class<?>>(Lists.of(
          WorkflowId.class,
          WorkflowInstanceId.class));
  
  public MongoJsonWriter() {
  }

  public MongoJsonWriter(Mappings mappings) {
    super(mappings);
  }

  public Object toDbObject(Object o) {
    if (o==null) {
      return null;
    }
    Class<?> type = o.getClass();
    if (o==null
        || (String.class.isAssignableFrom(type))
        || (Boolean.class.isAssignableFrom(type))
        || (Number.class.isAssignableFrom(type))) {
      return o;
    }
    if (JsonWritable.class.isAssignableFrom(type)) {
      return toDbObject((JsonWritable)o);
    }
    if (LocalDateTime.class.isAssignableFrom(type)) {
      return toDbObject((LocalDateTime)o);
    }
    if (Id.class.isAssignableFrom(type)) {
      return toDbObject((Id)o);
    }
    if (List.class.isAssignableFrom(type)) {
      return toDbObject((List<Object>)o);
    }
    if (Map.class.isAssignableFrom(type)) {
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
    mappings.writeTypeField(this, o);
    o.writeJson(this);
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
  public void writeWritable(String fieldName, JsonWritable value) {
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
  public void writeMapFields(Map<String,? extends Object> map) {
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
  
  @Override
  public <T> void writeBindings(String fieldName, List<Binding<T>> bindings) {
    if (bindings==null || bindings.isEmpty()) {
      return;
    }
    BasicDBList dbBindings = new BasicDBList();
    for (Binding<T> binding: bindings) {
      BasicDBObject dbBinding = toDbObject(binding);
      dbBindings.add(dbBinding);
    }
    dbObject.put(fieldName, dbBindings);
  }

  @Override
  public <T> void writeBinding(String fieldName, Binding<T> binding) {
    if (binding!=null) {
      BasicDBObject dbBinding = toDbObject(binding);
      dbObject.put(fieldName, dbBinding);
    }
  }

  protected <T> BasicDBObject toDbObject(Binding<T> binding) {
    BasicDBObject dbBinding = new BasicDBObject();
    Object value = toDbObject(binding.getValue());
    if (value!=null) {
      dbBinding.put("value", value);
    }
    if (binding.getExpression()!=null) {
      dbBinding.put("expression", binding.getExpression());
    }
    return dbBinding;
  }
}
