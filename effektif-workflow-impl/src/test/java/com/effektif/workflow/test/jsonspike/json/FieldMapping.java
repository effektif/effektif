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

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Map;


public class FieldMapping<T> {
  
  Field field;
  String jsonFieldName;
  Type fieldType;
  TypeMapper typeMapper;
  
  public FieldMapping(Field field, TypeMapper typeMapper) {
    this.field = field;
    this.jsonFieldName = field.getName();
    this.fieldType = field.getGenericType(); 
    this.typeMapper = typeMapper;
  }

  public void writeField(Object bean, JsonFieldWriter jsonFieldWriter) {
    try {
      Object fieldValue = field.get(bean);
      if (fieldValue!=null) {
        jsonFieldWriter.writeFieldName(jsonFieldName);
        typeMapper.write(fieldValue, jsonFieldWriter);
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
  
  public void readField(Map<String,Object> beanJson, Object bean, JsonFieldReader jsonFieldReader) {
    try {
      Object jsonFieldValue = beanJson.get(jsonFieldName);
      if (jsonFieldValue!=null) {
        Object fieldValue = typeMapper.read(jsonFieldValue, fieldType, jsonFieldReader);
        field.set(bean, fieldValue);
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public String getFieldName() {
    return field.getName();
  }

  public TypeMapper getTypeMapper() {
    return typeMapper;
  }
  
  public void setTypeMapper(TypeMapper typeMapper) {
    this.typeMapper = typeMapper;
  }

  public void setJsonFieldName(String jsonFieldName) {
    this.jsonFieldName = jsonFieldName;
  }
}