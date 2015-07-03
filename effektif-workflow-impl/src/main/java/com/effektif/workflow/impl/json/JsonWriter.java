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

import java.util.ArrayList;
import java.util.List;

/**
 * An API for serialising values to JSON.
 *
 * @author Tom Baeyens
 */
public abstract class JsonWriter {
  
  // private static final Logger log = LoggerFactory.getLogger(JsonWriter.class);
  
  Mappings mappings;
  List<Object> loopCheckBeans = new ArrayList<>();
  boolean inline;
  
  public JsonWriter(Mappings mappings) {
    this.mappings = mappings;
  }

  public void loopCheckBeanStart(Object bean) {
    for (int i=0; i<loopCheckBeans.size(); i++) {
      if (loopCheckBeans.get(i)==bean) {
        throw new RuntimeException("Loop detected in object graph: "+bean+" ("+bean.getClass().getName()+")");
      }
    }
    loopCheckBeans.add(bean);
  }

  public void loopCheckBeanEnd() {
    loopCheckBeans.remove(loopCheckBeans.size()-1);
  }

  public void writeObject(Object o) {
    if (o!=null) {
      Class<?> clazz = o.getClass();
      JsonTypeMapper jsonTypeMapper = mappings.getTypeMapper(clazz);
      // log.debug("using type mapper "+jsonTypeMapper.getClass().getSimpleName()+" to write object "+o+" ("+o.getClass().getSimpleName()+")");
      jsonTypeMapper.write(o, this);
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
  
  public void writeTypeField(Object bean) {
    mappings.writeTypeField(this, bean);
  }

  public void setInline() {
    inline = true;
  }
  
  public boolean getInline() {
    boolean inline = this.inline;
    this.inline = false;
    return inline;
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
