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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.LocalDateTime;

import com.effektif.workflow.api.mapper.JsonReadable;
import com.effektif.workflow.api.mapper.JsonReader;
import com.effektif.workflow.api.model.Id;
import com.effektif.workflow.api.workflow.Binding;


/**
 * @author Tom Baeyens
 */
public abstract class AbstractReader implements JsonReader {
  
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
    Object id = jsonObject.get(fieldName);
    Class<T> idType = (Class<T>) mappings.getFieldType(readableClass, fieldName);
    return readId(id, idType);
  }

  public String readString(String fieldName) {
    return (String) jsonObject.get(fieldName);
  }
  

  @Override
  public Boolean readBoolean(String fieldName) {
    return (Boolean) jsonObject.get(fieldName);
  }

  @Override
  public Long readLong(String fieldName) {
    Number number = (Number) jsonObject.get(fieldName);
    return number instanceof Long ? (Long) number : number.longValue();
  }

  @Override
  public Double readDouble(String fieldName) {
    Number number = (Number) jsonObject.get(fieldName);
    return number instanceof Double ? (Double) number : number.doubleValue();
  }

  @Override
  public LocalDateTime readDate(String fieldName) {
    Object dateValue = jsonObject.get(fieldName);
    return readDateValue(dateValue);
  }

  
  @Override
  public <T extends JsonReadable> T readReadable(String fieldName) {
    Map<String, Object> json = (Map<String, Object>) jsonObject.get(fieldName);
    Class<T> readableType = (Class<T>) mappings.getFieldType(readableClass, fieldName);
    return readReadable(json, readableType);
  }

  @Override
  public <T> Map<String, T> readMap(String fieldName) {
    Map<String,Object> jsonMap = (Map<String,Object>) jsonObject.get(fieldName);
    if (jsonMap==null) {
      return null;
    }
    ParameterizedType mapType = (ParameterizedType) mappings.getFieldType(readableClass, fieldName);
    return readMap(jsonMap, mapType.getActualTypeArguments()[1]);
  }
  
  @Override
  public <T> List<T> readList(String fieldName) {
    List<Object> jsons = (List<Object>) jsonObject.get(fieldName);
    if (jsons==null) {
      return null;
    }
    ParameterizedType listType = (ParameterizedType) mappings.getFieldType(readableClass, fieldName);
    return readList(jsons, listType.getActualTypeArguments()[0]);
  }

  @Override
  public <T> Binding<T> readBinding(String fieldName) {
    Map<String,Object> jsonBinding = (Map<String, Object>) jsonObject.get(fieldName);
    if (jsonBinding==null || jsonBinding.isEmpty()) {
      return null;
    }
    ParameterizedType bindingType = (ParameterizedType) mappings.getFieldType(readableClass, fieldName);
    return readBinding(jsonBinding, bindingType.getActualTypeArguments()[0]);
  }
  
  public Object readObject(Object json, Type type) {
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
    } else { // type is a class (not parameterized)
      Class<?> clazz = (Class< ? >) type;
      if (json==null
          || type==String.class
          || type==Boolean.class
          || Number.class.isAssignableFrom(clazz)) {
        return json;
      }
      if (Id.class.isAssignableFrom(clazz)) {
        return readId(json, (Class<Id>)type);
      }
      if (JsonReadable.class.isAssignableFrom(clazz)) {
        return readReadable((Map<String, Object>) json, (Class<JsonReadable>)clazz);
      }
      if (type==LocalDateTime.class) {
        return readDateValue(json);
      }
    }
    throw new RuntimeException("Couldn't parse "+json+" ("+json.getClass().getName()+")");
  }

  public abstract LocalDateTime readDateValue(Object jsonDate);
  
  public static <T extends Id> T readId(Object jsonId, Class<T> idType) {
    if (jsonId!=null) {
      try {
        jsonId = jsonId.toString();
        Constructor<T> c = idType.getDeclaredConstructor(ID_CONSTRUCTOR_PARAMETERS);
        return (T) c.newInstance(new Object[] { jsonId });
      } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException
              | InvocationTargetException e) {
        throw new RuntimeException(e);
      }
    }
    return null;
  }
  
  public <T extends JsonReadable> T readReadable(Map<String, Object> jsonReadable, Class<T> type) {
    try {
      Map<String,Object> parentJson = jsonObject;
      Class<?> parentClass = readableClass; 
      jsonObject = jsonReadable;
      readableClass = type;
      Class<T> concreteType = mappings.getConcreteClass(jsonObject, type);
      T object = concreteType.newInstance();
      object.readJson(this);
      this.jsonObject = parentJson;
      this.readableClass = parentClass;
      return object;
    } catch (InstantiationException | IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }
  
  public <T> Map<String, T> readMap(Map<String, Object> jsonMap, Type type) {
    Map<String,T> map = new HashMap<>();
    for (String key: jsonMap.keySet()) {
      Object jsonValue = jsonMap.get(key);
      T value = (T) readObject(jsonValue, type);
      map.put(key, value);
    }
    return map;
  }

  public <T> List<T> readList(List<Object> jsonList, Type elementType) {
    List<T> objects = new ArrayList<>();
    for (Object jsonElement: jsonList) {
      T object = (T) readObject(jsonElement, elementType);
      objects.add(object);
    }
    return objects;
  }

  public <T> Binding<T> readBinding(Map<String, Object> jsonBinding, Type valueType) {
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
