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

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.effektif.workflow.impl.json.FieldMapping;
import com.effektif.workflow.impl.json.JsonReader;
import com.effektif.workflow.impl.json.JsonWriter;


/**
 * Maps a JavaBean to a {@link Map} field for JSON serialisation and deserialisation.
 *
 * @author Tom Baeyens
 */
public class BeanMapper<T extends Object> extends AbstractTypeMapper<T> {

  Class<?> clazz;
  Type type;
  
  public BeanMapper(Class<?> clazz, Type type) {
    this.clazz = clazz;
    this.type = type;
  }

  @Override
  public T read(Object jsonValue, JsonReader jsonReader) {
    Map<String,Object> jsonMap = (Map<String, Object>) jsonValue;
    Class<?> concreteClazz = mappings.getConcreteClass(jsonMap, clazz);
    Object bean = null;
    try {
      bean = concreteClazz.newInstance();
    } catch (InstantiationException | IllegalAccessException e) {
      throw new RuntimeException(e);
    }
    List<FieldMapping> fieldMappings = mappings.getFieldMappings(concreteClazz, type);
    for (FieldMapping fieldMapping: fieldMappings) {
      fieldMapping.readField(jsonMap, bean, jsonReader);
    }
    return (T) bean;
  }

  @Override
  public void write(T bean, JsonWriter jsonWriter) {
    jsonWriter.loopCheckBeanStart(bean);
    jsonWriter.objectStart();
    Class< ? extends Object> beanClass = bean.getClass();
    jsonWriter.writeTypeField(bean);
    List<FieldMapping> fieldMappings = mappings.getFieldMappings(beanClass, type);
    for (FieldMapping fieldMapping: fieldMappings) {
      fieldMapping.writeField(bean, jsonWriter);
    }
    jsonWriter.objectEnd();
    jsonWriter.loopCheckBeanEnd();
  }

  @Override
  public String toString() {
    return "BeanMapper<" + clazz.getSimpleName() + ">";
  }
}
