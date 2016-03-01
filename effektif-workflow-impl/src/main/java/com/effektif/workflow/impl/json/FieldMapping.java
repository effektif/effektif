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

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.effektif.workflow.impl.util.Reflection;

/**
 * Uses a {@link JsonTypeMapper} to serialise and deserialise a particular API model field.
 */
public class FieldMapping {
  
  private static final Logger log = LoggerFactory.getLogger(FieldMapping.class);
  
  Field field;
  String jsonFieldName;
  Type fieldType;
  JsonTypeMapper jsonTypeMapper;
  
  /** the list of parent fieldNames. not null means inline is activated for this field. */
  Collection<String> inline;
  
  protected FieldMapping() {
  }

  public FieldMapping(Field field, JsonTypeMapper jsonTypeMapper) {
    this.field = field;
    this.field.setAccessible(true);
    this.jsonFieldName = field.getName();
    this.fieldType = field.getGenericType(); 
    this.jsonTypeMapper = jsonTypeMapper;
  }

  public void writeField(Object bean, JsonWriter jsonWriter) {
    try {
      Object fieldValue = field.get(bean);
      if (fieldValue!=null) {
//         log.debug("writing "+Reflection.getSimpleName(field)+" with "+jsonTypeMapper+" : "+fieldValue);
        if (inline!=null) {
          jsonWriter.setInline();
        } else {
          jsonWriter.writeFieldName(jsonFieldName);
        }
        jsonTypeMapper.write(fieldValue, jsonWriter);
      }
    } catch (Exception e) {
      throw new RuntimeException("Error writing field "+field+": "+e.getMessage(), e);
    }
  }
  
  public void readField(Map<String,Object> beanJson, Object bean, JsonReader jsonReader) {
    try {
      Object jsonFieldValue = null;
      if (inline!=null) {
        Map<String,Object> fieldJson = new HashMap<>(beanJson);
        for (String parentFieldName: inline) {
          fieldJson.remove(parentFieldName);
        }
        if (!fieldJson.isEmpty()) {
          jsonFieldValue = fieldJson;
        }
      } else {
        jsonFieldValue = beanJson.get(jsonFieldName);
      }
      if (jsonFieldValue!=null) {
        // log.debug("read "+Reflection.getSimpleName(field)+" with "+jsonTypeMapper+" : "+jsonFieldValue);
        Object fieldValue = jsonTypeMapper.read(jsonFieldValue, jsonReader);
        field.set(bean, fieldValue);
      }
    } catch (Exception e) {
      throw new RuntimeException("Error reading "+field+": "+e.getMessage()+": "+beanJson, e);
    }
  }

  public String getFieldName() {
    return field.getName();
  }

  public JsonTypeMapper getTypeMapper() {
    return jsonTypeMapper;
  }
  
  public void setTypeMapper(JsonTypeMapper jsonTypeMapper) {
    this.jsonTypeMapper = jsonTypeMapper;
  }

  public void setJsonFieldName(String fieldName) {
    if (fieldName == null || fieldName.trim().equals("")) {
      throw new IllegalArgumentException("Provided JSON field is empty");
    }
    this.jsonFieldName = fieldName;
  }
  
  public void setInline(Collection<String> inline) {
    this.inline = inline;
  }
  
  public String toString() {
    return Reflection.getSimpleName(field)+"->"+jsonTypeMapper;
  }
}