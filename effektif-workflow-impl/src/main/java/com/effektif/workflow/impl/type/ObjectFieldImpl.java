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
package com.effektif.workflow.impl.type;

import java.lang.reflect.Field;

import com.effektif.workflow.api.type.ObjectField;
import com.effektif.workflow.impl.WorkflowParser;
import com.effektif.workflow.impl.WorkflowSerializer;
import com.effektif.workflow.impl.plugin.DataType;


public class ObjectFieldImpl {

  protected String name;
  protected DataType type;
  protected Field field;
  
  public ObjectFieldImpl() {
  }
  
  public ObjectFieldImpl(String name) {
    this.name = name;
  }
  
  public void parse(ObjectField fieldApi, WorkflowParser parser) {
    try {
      this.name = fieldApi.getName();
      this.type = parser.parseType(fieldApi.getType());
      this.field = this.type.getValueClass().getField(name);
      this.field.setAccessible(true);
    } catch (NoSuchFieldException | SecurityException e) {
      throw new RuntimeException();
    }
  }

  public void serialize(Object value, WorkflowSerializer serializer) {
    if (type.isSerializeRequired()) {
      try {
        Object fieldValue = field.get(value);
        type.serialize(fieldValue, serializer);
      } catch (IllegalArgumentException | IllegalAccessException e) {
        throw new RuntimeException();
      }
    }
  }

  public String getName() {
    return this.name;
  }
  public void setName(String name) {
    this.name = name;
  }
  public ObjectFieldImpl name(String name) {
    this.name = name;
    return this;
  }
  
  public DataType getDataType() {
    return this.type;
  }
  public void setDataType(DataType type) {
    this.type = type;
  }
  public ObjectFieldImpl type(DataType type) {
    this.type = type;
    return this;
  }
}
