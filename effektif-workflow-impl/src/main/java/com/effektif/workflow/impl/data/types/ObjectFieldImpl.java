/*
 * Copyright 2014 Effektif GmbH.
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
 * limitations under the License.
 */
package com.effektif.workflow.impl.data.types;

import java.util.Map;

import com.effektif.workflow.api.Configuration;
import com.effektif.workflow.api.types.Type;
import com.effektif.workflow.impl.data.DataType;
import com.effektif.workflow.impl.data.DataTypeService;


/**  idea for now.  @see CustomTypeStore, CustomTypeImpl
 * 
 * @author Tom Baeyens
 */
public class ObjectFieldImpl {

  protected String name;
  protected DataType type;
  
  public ObjectFieldImpl(String name) {
    this.name = name;
  }

  public ObjectFieldImpl(String name, DataType type) {
    this.name = name;
    this.type = type;
  }

  public ObjectFieldImpl(Class< ? > objectClass, ObjectField field, Configuration configuration) {
    this.name = field.getName();
    Type fieldType = field.getType();
    DataTypeService dataTypeService = configuration.get(DataTypeService.class);
    this.type = dataTypeService.createDataType(fieldType);
  }
  
  public Object getFieldValue(Object value) {
    Map<String,Object> map = (Map<String, Object>) value;
    return map.get(name);
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
