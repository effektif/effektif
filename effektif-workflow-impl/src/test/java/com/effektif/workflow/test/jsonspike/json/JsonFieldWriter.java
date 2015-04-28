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

import java.util.List;

public abstract class JsonFieldWriter {
  
  Mappings mappings;
  
  public JsonFieldWriter(Mappings mappings) {
    this.mappings = mappings;
  }

  public void writeBean(Object bean) {
    if (bean!=null) {
      objectStart();
      Class< ? extends Object> beanClass = bean.getClass();
      List<FieldMapping> fieldMappings = mappings.getFieldMappings(beanClass);
      for (FieldMapping fieldMapping: fieldMappings) {
        fieldMapping.writeField(bean, this);
      }
      objectEnd();
    } else {
      writeNull();
    }
  }

  public void writeObject(Object o) {
    if (o!=null) {
      Class<?> clazz = o.getClass();
      TypeMapper typeMapper = mappings.getTypeMapper(clazz);
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
