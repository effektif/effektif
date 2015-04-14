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
package com.effektif.workflow.impl.mapper;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.LocalDateTime;
import org.joda.time.ReadableDuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.effektif.workflow.api.mapper.JsonReadable;
import com.effektif.workflow.api.mapper.JsonReader;
import com.effektif.workflow.api.model.Id;
import com.effektif.workflow.api.workflow.Binding;
import com.effektif.workflow.impl.util.Reflection;


/**
 * @author Tom Baeyens
 */
public abstract class AbstractReader implements JsonReader {
  
  public static final Logger log = LoggerFactory.getLogger(AbstractReader.class);
  
  protected Map<String,Object> jsonObject;
  protected Class<?> readableClass;
  protected Mappings mappings; 

  public AbstractReader() {
    this(new Mappings());
  }

  public AbstractReader(Mappings mappings) {
    this.mappings = mappings;
  }

  private static final Class< ? >[] ID_CONSTRUCTOR_PARAMETERS = new Class< ? >[] { String.class };
  @Override
  public <T extends Id> T readId(String fieldName) {
    Object id = jsonObject.remove(fieldName);
    Class<T> idType = (Class<T>) mappings.getFieldType(readableClass, fieldName);
    return readId(id, idType);
  }

  public String readString(String fieldName) {
    return (String) jsonObject.remove(fieldName);
  }
  

  @Override
  public Boolean readBoolean(String fieldName) {
    return (Boolean) jsonObject.remove(fieldName);
  }

  @Override
  public Long readLong(String fieldName) {
    Number number = (Number) jsonObject.remove(fieldName);
    return number instanceof Long ? (Long) number : number.longValue();
  }

  @Override
  public Double readDouble(String fieldName) {
    Number number = (Number) jsonObject.remove(fieldName);
    return number instanceof Double ? (Double) number : number.doubleValue();
  }

  @Override
  public LocalDateTime readDate(String fieldName) {
    Object dateValue = jsonObject.remove(fieldName);
    return readDateValue(dateValue);
  }

  @Override
  public Class< ? > readClass(String fieldName) {
    String className = (String) jsonObject.remove(fieldName);
    return Reflection.loadClass(className);
  }

  @Override
  public <T> T readObject(String fieldName) {
    Map<String, Object> json = (Map<String, Object>) jsonObject.remove(fieldName);
    Class<T> readableType = (Class<T>) mappings.getFieldType(readableClass, fieldName);
    return readObject(json, readableType);
  }

  @Override
  public <T> Map<String, T> readMap(String fieldName) {
    Map<String,Object> jsonMap = (Map<String,Object>) jsonObject.remove(fieldName);
    if (jsonMap==null) {
      return null;
    }
    ParameterizedType mapType = (ParameterizedType) mappings.getFieldType(readableClass, fieldName);
    return readMap(jsonMap, mapType.getActualTypeArguments()[1]);
  }
  
  @Override
  public <T> List<T> readList(String fieldName) {
    List<Object> jsons = (List<Object>) jsonObject.remove(fieldName);
    if (jsons==null) {
      return null;
    }
    ParameterizedType listType = (ParameterizedType) mappings.getFieldType(readableClass, fieldName);
    return readList(jsons, listType.getActualTypeArguments()[0]);
  }

  @Override
  public <T> Binding<T> readBinding(String fieldName) {
    Map<String,Object> jsonBinding = (Map<String, Object>) jsonObject.remove(fieldName);
    if (jsonBinding==null || jsonBinding.isEmpty()) {
      return null;
    }
    ParameterizedType bindingType = (ParameterizedType) mappings.getFieldType(readableClass, fieldName);
    return readBinding(jsonBinding, bindingType.getActualTypeArguments()[0]);
  }
  
  @Override
  public Map<String, Object> readProperties() {
    return jsonObject;
  }

  public Object readObject(Object json, Type type) {
    if (json==null) {
      return null;
    }
    if (type instanceof ParameterizedType) {
      ParameterizedType parameterizedType = (ParameterizedType)type;
      if (Map.class.equals(parameterizedType.getRawType())) {
        return readMap((Map<String,Object>) json, parameterizedType.getActualTypeArguments()[1]);
      }
      if (List.class.equals(parameterizedType.getRawType())) {
        return readList((List<Object>) json, parameterizedType.getActualTypeArguments()[0]);
      }
      if (Binding.class.equals(parameterizedType.getRawType())) {
        return readBinding((Map<String,Object>) json, parameterizedType.getActualTypeArguments()[0]);
      }
      if (Class.class.equals(parameterizedType.getRawType())) {
        return readClass((String) json);
      }
    } else { // type is a class (not parameterized)
      Class<?> clazz = (Class< ? >) type;
      if (json==null
          || type==Object.class
          || type==String.class
          || type==Boolean.class) {
        return json;
      }
      if (Number.class.isAssignableFrom(clazz)) {
        return readNumber(json, type);
      }
      if (Id.class.isAssignableFrom(clazz)) {
        return readId(json, (Class<Id>)type);
      }
      if (type==LocalDateTime.class) {
        return readDateValue(json);
      }
      if (Binding.class.equals(clazz)) {
        return readBinding((Map<String,Object>) json, null);
      }
      if (json instanceof Map) {
        Map<String, Object> jsonMap = (Map<String, Object>) json;
        if (JsonReadable.class.isAssignableFrom(clazz)) {
          return readReadable(jsonMap, (Class<JsonReadable>)clazz);
        } else {
          return readObject(jsonMap, clazz);
        }
      }
    }
    
    throw new RuntimeException("Couldn't parse "+json+" ("+json.getClass().getName()+")");
  }

  protected Object readNumber(Object json, Type type) {
    if (json==null) {
      return null;
    }
    if (!(json instanceof Number)) {
      throw new RuntimeException("Can't convert json "+json+" ("+json.getClass().getName()+") to a number");
    }
    Number number = (Number) json;
    if (type==null) {
      return json;
    }
    if (type==Long.class) {
      return number.longValue();
    }
    if (type==Double.class) {
      return number.doubleValue();
    }
    throw new RuntimeException("The model should only contain Long and Double fields");
  }

  public abstract LocalDateTime readDateValue(Object jsonDate);
  
  public static <T extends Id> T readId(Object jsonId, Class<T> idType) {
    if (jsonId==null) {
      return null;
    }
    try {
      jsonId = jsonId.toString();
      Constructor<T> c = idType.getDeclaredConstructor(ID_CONSTRUCTOR_PARAMETERS);
      return (T) c.newInstance(new Object[] { jsonId });
    } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException
            | InvocationTargetException e) {
      throw new RuntimeException(e);
    }
  }
  
  public <T extends JsonReadable> T readReadable(Map<String, Object> json, Class<T> clazz) {
    if (json==null) {
      return null;
    }
    try {
      Map<String,Object> parentJson = jsonObject;
      Class<?> parentClass = readableClass; 
      jsonObject = json;
      readableClass = clazz;
      Class<T> concreteType = mappings.getConcreteClass(jsonObject, clazz);
      T object = concreteType.newInstance();
      object.readJson(this);
      this.jsonObject = parentJson;
      this.readableClass = parentClass;
      return object;
    } catch (InstantiationException | IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }
  
  public <T> T readObject(Map<String, Object> json, Class<T> clazz) {
    if (json==null) {
      return null;
    }
    try {
      Map<String,Object> parentJson = jsonObject;
      Class<?> parentClass = readableClass; 
      jsonObject = json;
      readableClass = clazz;
      Class<T> concreteType = mappings.getConcreteClass(jsonObject, clazz);
      T object = concreteType.newInstance();
      readFieldsInto(object, clazz);
      this.jsonObject = parentJson;
      this.readableClass = parentClass;
      return object;
    } catch (InstantiationException | IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

  public void readFieldsInto(Object o, Class<?> clazz) {
    List<Field> fields = mappings.getAllFields(clazz);
    if (fields!=null) {
      for (Field field : fields) {
        readFieldInto(o, field);
      }
    }
  }
  
  public void readFieldInto(Object o, Field field) {
    try {
      Type fieldType = field.getGenericType();
      Object fieldJson = jsonObject.get(getJsonFieldName(field));
      Object fieldValue = readObject(fieldJson, fieldType);
      field.set(o, fieldValue);
    } catch (Exception e) {
      throw new RuntimeException("Problem reading field "+field.toString()+": "+e.getMessage(), e);
    }
  }

  protected String getJsonFieldName(Field field) {
    return field.getName();
  }

  public <T> Map<String, T> readMap(Map<String, Object> jsonMap, Type valueType) {
    if (jsonMap==null) {
      return null;
    }
    Map<String,T> map = new HashMap<>();
    for (String key: jsonMap.keySet()) {
      Object jsonFieldValue = jsonMap.get(key);
      T fieldValue = (T) readObject(jsonFieldValue, valueType);
      map.put(key, fieldValue);
    }
    return map;
  }

  public <T> List<T> readList(List<Object> jsonList, Type elementType) {
    if (jsonList==null) {
      return null;
    }
    List<T> objects = new ArrayList<>();
    for (Object jsonElement: jsonList) {
      T object = (T) readObject(jsonElement, elementType);
      objects.add(object);
    }
    return objects;
  }

  public <T> Binding<T> readBinding(Map<String, Object> jsonBinding, Type valueType) {
    if (jsonBinding==null) {
      return null;
    }
    Binding<T> binding = new Binding();
    Object jsonValue = jsonBinding.get("value");
    if (jsonValue!=null) {
      Object value = readObject(jsonValue, valueType);
      binding.setValue((T) value);
    }
    String expression = (String) jsonBinding.get("expression");
    if (expression!=null) {
      binding.setExpression(expression);
    }
    return binding;
  }
}
