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
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
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
  Class<?> objectClass;
  MongoJsonMapper mongoJsonMapper;
  
  public MongoJsonWriter(Mappings mappings, MongoJsonMapper mongoJsonMapper) {
    super(mappings);
    this.mongoJsonMapper = mongoJsonMapper;
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
  public void writeLong(String fieldName, Long value) {
    if (value!=null) {
      dbObject.put(fieldName, value);
    }
  }

  @Override
  public void writeDouble(String fieldName, Double value) {
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
  public void writeClass(String fieldName, Class< ? > value) {
    if (value!=null) {
      dbObject.put(fieldName, value.getName());
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
  public void writeMap(String fieldName, Map<String, ?> map) {
    if (map!=null) {
      ParameterizedType mapType = (ParameterizedType) mappings.getFieldType(objectClass, fieldName);
      BasicDBObject dbMap = toDbObject(map, mapType.getActualTypeArguments()[1]);
      dbObject.put(fieldName, dbMap);
    }
  }
  
  @Override
  public void writeList(String fieldName, List<?> list) {
    if (list!=null && !list.isEmpty()) {
      ParameterizedType listType = (ParameterizedType) mappings.getFieldType(objectClass, fieldName);
      BasicDBList dbList = toDbObject(list, listType.getActualTypeArguments()[0]);
      dbObject.put(fieldName, dbList);
    }
  }

  @Override
  public <T> void writeBinding(String fieldName, Binding<T> binding) {
    if (binding!=null) {
      ParameterizedType bindingType = (ParameterizedType) mappings.getFieldType(objectClass, fieldName);
      BasicDBObject dbBinding = toDbObject(binding, bindingType.getActualTypeArguments()[0]);
      dbObject.put(fieldName, dbBinding);
    }
  }

  @Override
  public void writeProperties(Map<String, Object> properties) {
    if (properties!=null) {
      dbObject.putAll(properties);
    }
  }
  
  public Object toDbObject(Object o) {
    return toDbObject(o, o.getClass());
  }
    
  public Object toDbObject(Object o, Type type) {
    if (o==null
        || o instanceof String
        || o instanceof Boolean
        || o instanceof Number) {
      return o;
    }
    if (o instanceof LocalDateTime) {
      return toDbObject((LocalDateTime) o);
    } else if (o instanceof Id) {
      return toDbObject((Id) o);
    } else if (o instanceof JsonWritable) {
      return toDbObject((JsonWritable) o);
    } else if (o instanceof Map) {
      ParameterizedType mapType = (ParameterizedType) type;
      return toDbObject((Map<String,Object>)o, mapType.getActualTypeArguments()[1]);
    } else if (o instanceof List) {
      ParameterizedType listType = (ParameterizedType) type;
      return toDbObject((List) o, listType.getActualTypeArguments()[0]);
    } else if (o instanceof Binding) {
      ParameterizedType bindingType = type instanceof ParameterizedType ? (ParameterizedType) type : null;
      Type bindingValueType = bindingType!=null ? bindingType.getActualTypeArguments()[0] : null;
      return toDbObject((Binding) o, bindingValueType);
    } else {
      return toDbObjectDefault(o);
    }
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
    Class<?> parentObjectClass = objectClass;
    dbObject = new BasicDBObject();
    objectClass = o.getClass();
    mappings.writeTypeField(this, o);
    o.writeJson(this);
    BasicDBObject newDbObject = dbObject;
    dbObject = parentDbObject;
    objectClass = parentObjectClass;
    return newDbObject;
  }

  public BasicDBObject toDbObjectDefault(Object o) {
    if (o==null) {
      return null;
    }
    BasicDBObject parentDbObject = dbObject;
    Class<?> parentObjectClass = objectClass;
    dbObject = new BasicDBObject();
    objectClass = o.getClass();
    mappings.writeTypeField(this, o);
    writeFields(o, o.getClass());
    BasicDBObject newDbObject = dbObject;
    dbObject = parentDbObject;
    objectClass = parentObjectClass;
    return newDbObject;
  }
  
  public void writeFields(Object o, Class<?> clazz) {
    List<Field> fields = mappings.getAllFields(clazz);
    if (fields!=null) {
      for (Field field : fields) {
        writeField(o, field);
      }
    }
  }
  
  public void writeField(Object o, Field field) {
    try {
      Object fieldValue = field.get(o);
      if (fieldValue!=null) {
        Object dbFieldValue = toDbObject(fieldValue, field.getGenericType());
        String fieldName = mongoJsonMapper.getFieldName(field);
        dbObject.put(fieldName, dbFieldValue);
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  protected Object toDbObject(Id id) {
    if (id==null) {
      return null;
    }
    if (mongoJsonMapper.isObjectIdClass(id.getClass())) {
      return new ObjectId(id.getInternal());
    }
    return id.getInternal();
  }

  protected BasicDBList toDbObject(List<?> list, Type elementType) {
    if (list==null) {
      return null;
    }
    BasicDBList dbList = new BasicDBList();
    for (Object element: list) {
      Object dbElement = toDbObject(element, elementType); 
      dbList.add(dbElement);
    }
    return dbList;
  }

  protected BasicDBObject toDbObject(Map<String,?> map, Type valueType) {
    if (map==null) {
      return null;
    }
    BasicDBObject dbMap = new BasicDBObject();
    for (String key: map.keySet()) {
      Object value = map.get(key);
      Object dbValue = toDbObject(value, valueType);
      dbMap.put(key, dbValue);
    }
    return dbMap;
  }
  
  protected <T> BasicDBObject toDbObject(Binding<T> binding, Type valueType) {
    BasicDBObject dbBinding = new BasicDBObject();
    T value = binding.getValue();
    if (value!=null) {
      Object dbValue = toDbObject(value, valueType);
      dbBinding.put("value", dbValue);
    }
    if (binding.getExpression()!=null) {
      dbBinding.put("expression", binding.getExpression());
    }
    return dbBinding;
  }
}
