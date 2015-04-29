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

import java.util.List;
import java.util.Map;

/**
 * An API for deserialising field values from a JSON source.
 *
 * @author Tom Baeyens
 */
public abstract class JsonReader {
  
  Mappings mappings;
  Map<String,Object> currentBeanJsonMap;
  Class<?> currentBeanClass;
  
  public JsonReader(Mappings mappings) {
    this.mappings = mappings;
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
}