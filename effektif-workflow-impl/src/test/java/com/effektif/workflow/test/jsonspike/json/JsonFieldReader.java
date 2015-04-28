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
package com.effektif.workflow.test.jsonspike.json;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * An API for deserialising field values from a JSON source.
 *
 * @author Tom Baeyens
 */
public abstract class JsonFieldReader {
  
  Mappings mappings;
  Map<String,Object> currentBeanJsonMap;
  Class<?> currentBeanClass;
  
  public JsonFieldReader(Mappings mappings) {
    this.mappings = mappings;
  }

  protected Object readObject(Object jsonValue, Type type) {
    TypeMapper typeMapper = mappings.getTypeMapper(jsonValue, type);
    return typeMapper.read(jsonValue, type, this);
  }

  public Object readBean(Map<String,Object> beanJsonMap, Class<?> clazz) {
    try {
      Map<String,Object> parentMap = currentBeanJsonMap;
      Class<?> parentClass = currentBeanClass;
      currentBeanJsonMap = beanJsonMap;
      currentBeanClass = clazz; 
      Class<?> concreteClazz = mappings.getConcreteClass(beanJsonMap, clazz);
      Object bean = concreteClazz.newInstance();
      Class< ? extends Object> beanClass = bean.getClass();
      List<FieldMapping> fieldMappings = mappings.getFieldMappings(beanClass);
      for (FieldMapping fieldMapping: fieldMappings) {
        fieldMapping.readField(beanJsonMap, bean, this);
      }
      currentBeanJsonMap = parentMap;
      currentBeanClass = parentClass;
      return bean;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  protected String getJsonFieldName(String fieldName) {
    return mappings.getJsonFieldName(currentBeanClass, fieldName);
  }
  
  public String readFieldString(String fieldName) {
    String jsonFieldName = getJsonFieldName(fieldName);
    return (String) currentBeanJsonMap.get(jsonFieldName);
  }

  public Boolean readFieldBoolean(String fieldName) {
    String jsonFieldName = getJsonFieldName(fieldName);
    return (Boolean) currentBeanJsonMap.get(jsonFieldName);
  }

  public Number readFieldNumber(String fieldName) {
    String jsonFieldName = getJsonFieldName(fieldName);
    return (Number) currentBeanJsonMap.get(jsonFieldName);
  }

  public Object readFieldObject(String fieldName, Type type) {
    String jsonFieldName = getJsonFieldName(fieldName);
    Object jsonFieldValue = currentBeanJsonMap.get(jsonFieldName);
    if (jsonFieldValue==null) {
      return null;
    }
    return readObject(jsonFieldValue, type);
  }

  public List<Object> readFieldArray(String fieldName, Type elementType) {
    String jsonFieldName = getJsonFieldName(fieldName);
    List<Object> jsonList = (List<Object>) currentBeanJsonMap.get(jsonFieldName);
    List<Object> objectList = new ArrayList<>();
    for (Object jsonElement: jsonList) {
      Object objectElement = readObject(jsonElement, elementType);
      objectList.add(objectElement);
    }
    return objectList;
  }
}