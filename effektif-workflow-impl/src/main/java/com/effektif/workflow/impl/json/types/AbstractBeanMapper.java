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
package com.effektif.workflow.impl.json.types;

import java.util.List;
import java.util.Map;

import com.effektif.workflow.impl.json.FieldMapping;
import com.effektif.workflow.impl.json.JsonReader;
import com.effektif.workflow.impl.json.JsonWriter;
import com.effektif.workflow.impl.json.TypeMapping;


/**
 * Maps a JavaBean to a {@link Map} field for JSON serialisation and deserialisation.
 *
 * @author Tom Baeyens
 */
public abstract class AbstractBeanMapper<T extends Object> extends AbstractTypeMapper<T> {
  
  String beanTypeName;

  public AbstractBeanMapper(String beanTypeName) {
    this.beanTypeName = beanTypeName;
  }

  protected abstract TypeMapping getTypeMapping(Map<String, Object> jsonObject);
  protected abstract TypeMapping getTypeMapping(Class<?> beanClass);

  @Override
  public T read(Object jsonValue, JsonReader jsonReader) {
    try {
      Map<String,Object> jsonObject = (Map<String, Object>) jsonValue;
      TypeMapping typeMapping = getTypeMapping(jsonObject);
      Object bean = typeMapping.instantiate();
      List<FieldMapping> fieldMappings = typeMapping.getFieldMappings();
      for (FieldMapping fieldMapping: fieldMappings) {
        fieldMapping.readField(jsonObject, bean, jsonReader);
      }
      return (T) bean;
    } catch (ClassCastException e) {
      throw new RuntimeException("Couldn't cast "+jsonValue+" to a map", e);
    }
  }

  @Override
  public void write(T bean, JsonWriter jsonWriter) {
    jsonWriter.loopCheckBeanStart(bean);
    jsonWriter.objectStart();
    Class<?> beanClass = bean.getClass();
    jsonWriter.writeTypeField(bean);
    TypeMapping typeMapping = getTypeMapping(beanClass);
    if (typeMapping==null) {
      throw new RuntimeException(toString()+" didn't have a typeMapping for "+beanClass);
    }
    List<FieldMapping> fieldMappings = typeMapping.getFieldMappings(); 
    for (FieldMapping fieldMapping: fieldMappings) {
      fieldMapping.writeField(bean, jsonWriter);
    }
    jsonWriter.objectEnd();
    jsonWriter.loopCheckBeanEnd();
  }

  @Override
  public String toString() {
    return getClass().getSimpleName()+"<" + beanTypeName + ">";
  }
}
