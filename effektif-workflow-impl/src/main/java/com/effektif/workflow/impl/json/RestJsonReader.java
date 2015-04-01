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
package com.effektif.workflow.impl.json;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.effektif.workflow.api.json.JsonReadable;
import com.effektif.workflow.api.json.JsonReader;
import com.effektif.workflow.api.model.Id;
import com.fasterxml.jackson.databind.ObjectMapper;


/**
 * @author Tom Baeyens
 */
public class RestJsonReader implements JsonReader {
  
  static ObjectMapper objectMapper = new ObjectMapper();
  
  Map<String,Object> jsonObject;
  SubclassMappings subclassMappings; 

  public RestJsonReader() {
    this(new SubclassMappings());
  }

  public RestJsonReader(SubclassMappings subclassMappings) {
    this.subclassMappings = subclassMappings;
  }

  public <T extends JsonReadable> T toObject(String jsonString, Class<T> type) {
    return toObject(new StringReader(jsonString), type);
  }

  public <T extends JsonReadable> T toObject(Reader jsonStream, Class<T> type) {
    try {
      this.jsonObject = objectMapper.readValue(jsonStream, Map.class);
      return readCurrentObject(type);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  protected <T extends JsonReadable> T readCurrentObject(Class<T> type) {
    try {
      Class<T> concreteType = subclassMappings.getConcreteClass(jsonObject, type);
      T o = concreteType.newInstance();
      o.readFields(this);
      return o;
      
    } catch (InstantiationException | IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public <T extends Id> T readId(Class<T> idType) {
    try {
      String internal = readString("id");
      if (internal!=null) {
        Constructor<T> c = idType.getDeclaredConstructor(new Class<?>[]{String.class});
        return c.newInstance(new Object[]{internal});
      }
      return null;
    } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
      throw new RuntimeException(e);
    }
  }

  public String readString(String fieldName) {
    return (String) jsonObject.get(fieldName);
  }

  @Override
  public <T extends JsonReadable> List<T> readList(String fieldName, Class<T> type) {
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
  public <T extends JsonReadable> T readObject(String fieldName, Class<T> type) {
    Map<String,Object> parentJson = jsonObject;
    List<T> objects = new ArrayList<>();
    jsonObject = (Map<String, Object>) parentJson.get(fieldName);
    T object = readCurrentObject(type);
    objects.add(object);
    this.jsonObject = parentJson;
    return object;
  }
}
