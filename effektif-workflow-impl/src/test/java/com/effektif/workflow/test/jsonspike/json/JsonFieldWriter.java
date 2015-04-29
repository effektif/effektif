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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An API for serialising field values to JSON.
 *
 * @author Tom Baeyens
 */
public abstract class JsonFieldWriter {
  
  private static final Logger log = LoggerFactory.getLogger(JsonFieldWriter.class);
  
  Mappings mappings;
  List<Object> loopCheckBeans = new ArrayList<>();
  
  public JsonFieldWriter(Mappings mappings) {
    this.mappings = mappings;
  }

  public void writeBean(Object bean) {
    if (bean!=null) {
      loopCheckBeanStart(bean);
      objectStart();
      Class< ? extends Object> beanClass = bean.getClass();
      mappings.writeTypeField(this, bean);
      List<FieldMapping> fieldMappings = mappings.getFieldMappings(beanClass);
      for (FieldMapping fieldMapping: fieldMappings) {
        fieldMapping.writeField(bean, this);
      }
      objectEnd();
      loopCheckBeanEnd();
    } else {
      writeNull();
    }
  }

  public void writeMap(Map map) {
    if (map!=null) {
      objectStart();
      for (Object key: map.keySet()) {
        if (key!=null) {
          if (!(key instanceof String)) {
            throw new RuntimeException("Only String keys allowed: "+key+" ("+key.getClass().getName()+"): Occurred when writing map "+map);
          }
          writeFieldName((String)key);
          writeObject(map.get(key));
        }
      }
      objectEnd();
    } else {
      writeNull();
    }
  }

  private void loopCheckBeanStart(Object bean) {
    for (int i=0; i<loopCheckBeans.size(); i++) {
      if (loopCheckBeans.get(i)==bean) {
        throw new RuntimeException("Loop detected in object graph: "+bean+" ("+bean.getClass().getName()+")");
      }
    }
    loopCheckBeans.add(bean);
  }

  private void loopCheckBeanEnd() {
    loopCheckBeans.remove(loopCheckBeans.size()-1);
  }

  public void writeObject(Object o) {
    if (o!=null) {
      Class<?> clazz = o.getClass();
      TypeMapper typeMapper = mappings.getTypeMapper(clazz);
      log.debug("using type mapper "+typeMapper.getClass().getSimpleName()+" to write object "+o+" ("+o.getClass().getSimpleName()+")");
      typeMapper.write(o, this);
    } else {
      writeNull();
    }
  }
  
  public void writeArray(List< ? > list) {
    arrayStart();
    for (Object element: list) {
      writeObject(element);
    }
    arrayEnd();
  }
  
  public abstract void objectStart();
  public abstract void writeFieldName(String fieldName);
  public abstract void objectEnd();

  public abstract void arrayEnd();
  public abstract void arrayStart();

  public abstract void writeNull();
  public abstract void writeString(String s);
  public abstract void writeBoolean(Boolean b);
  public abstract void writeNumber(Number n);
}
