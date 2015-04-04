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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import com.effektif.workflow.api.mapper.Readable;
import com.effektif.workflow.api.mapper.Reader;
import com.effektif.workflow.api.model.Id;


/**
 * @author Tom Baeyens
 */
public abstract class AbstractReader implements Reader {
  
  public static DateTimeFormatter DATE_FORMAT = ISODateTimeFormat.dateTimeParser();

  protected Map<String,Object> jsonObject;
  protected Mappings mappings; 

  public AbstractReader() {
    this(new Mappings());
  }

  public AbstractReader(Mappings mappings) {
    this.mappings = mappings;
  }

  protected <T extends Readable> T readCurrentObject(Class<T> type) {
    try {
      Class<T> concreteType = mappings.getConcreteClass(jsonObject, type);
      T o = concreteType.newInstance();
      o.readFields(this);
      return o;
      
    } catch (InstantiationException | IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

  private static final Class< ? >[] ID_CONSTRUCTOR_PARAMETERS = new Class< ? >[] { String.class };
  @Override
  public <T extends Id> T readId(String fieldName, Class<T> idType) {
    Object id = jsonObject.get(fieldName);
    return createId(id, idType);
  }

  public static <T extends Id> T createId(Object idInternal, Class<T> idType) {
    if (idInternal!=null) {
      try {
        idInternal = idInternal.toString();
        Constructor<T> c = idType.getDeclaredConstructor(ID_CONSTRUCTOR_PARAMETERS);
        return (T) c.newInstance(new Object[] { idInternal });
      } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException
              | InvocationTargetException e) {
        throw new RuntimeException(e);
      }
    }
    return null;
  }

  public String readString(String fieldName) {
    return (String) jsonObject.get(fieldName);
  }

  @Override
  public <T extends Readable> List<T> readList(String fieldName, Class<T> type) {
    List<Map<String,Object>> jsons = (List<Map<String, Object>>) jsonObject.get(fieldName);
    if (jsons==null) {
      return null;
    }
    Map<String,Object> parentJson = jsonObject;
    List<T> objects = new ArrayList<>();
    for (Map<String,Object> jsonElement: jsons) {
      jsonObject = jsonElement;
      T object = readCurrentObject(type);
      objects.add(object);
    }
    this.jsonObject = parentJson;
    return objects;
  }
  
  @Override
  public <T extends Readable> T readObject(String fieldName, Class<T> type) {
    Map<String,Object> parentJson = jsonObject;
    List<T> objects = new ArrayList<>();
    jsonObject = (Map<String, Object>) parentJson.get(fieldName);
    T object = readCurrentObject(type);
    objects.add(object);
    this.jsonObject = parentJson;
    return object;
  }
  
  @Override
  public <T> Map<String, T> readMap(String fieldName, Class<T> valueType) {
    Map<String,Object> jsons = (Map<String,Object>) jsonObject.get(fieldName);
    if (jsons==null) {
      return null;
    }
    Map<String,Object> parentJson = jsonObject;
    Map<String,T> map = new HashMap<>();
    for (String key: jsons.keySet()) {
      Object jsonElement = jsons.get(key);
      T value = readAny(jsonElement, valueType);
      map.put(key, value);
    }
    this.jsonObject = parentJson;
    return map;
  }

  protected <T> T readAny(Object json, Class<T> type) {
    if (json==null
        || type==String.class
        || type==Boolean.class
        || Number.class.isAssignableFrom(type)) {
      return (T) json;
    }
    if (Readable.class.isAssignableFrom(type)) {
      Map<String,Object> parentJson = jsonObject;
      jsonObject = (Map<String, Object>) json;
      T object = (T) readCurrentObject((Class<Readable>)type);
      jsonObject = parentJson;
      return object;
    }
    if (type==LocalDateTime.class) {
      return (T) DATE_FORMAT.parseLocalDateTime((String)json);
    }
    throw new RuntimeException("Couldn't parse "+json+" ("+json.getClass().getName()+")");
  }

}
