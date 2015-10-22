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
import com.effektif.workflow.api.types.DataType;
import com.effektif.workflow.impl.data.DataTypeImpl;
import com.effektif.workflow.impl.data.DataTypeService;


/**  idea for now.  @see CustomTypeStore, CustomTypeImpl
 * 
 * @author Tom Baeyens
 */
public class ObjectFieldImpl {

  protected String key;
  protected String name;
  protected DataTypeImpl type;
  
  public ObjectFieldImpl(String key) {
    key(key);
  }

  public ObjectFieldImpl(String key, DataTypeImpl type) {
    key(key);
    this.type = type;
  }

  public ObjectFieldImpl(Class< ? > objectClass, ObjectField field, Configuration configuration) {
    key(field.getKey());
    DataType fieldType = field.getType();
    DataTypeService dataTypeService = configuration.get(DataTypeService.class);
    this.type = dataTypeService.createDataType(fieldType);
  }
  
  public Object getFieldValue(Object value) {
    if (value instanceof Map) {
      Map<String,Object> map = (Map<String, Object>) value;
      return map.get(key);
    }
    return null;
  }

  public String getKey() {
    return this.key;
  }
  public void setKey(String key) {
    key(key);
  }
  public ObjectFieldImpl key(String key) {
    this.key = key;
    return this;
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

  public DataTypeImpl getDataType() {
    return this.type;
  }
  public void setDataType(DataTypeImpl type) {
    this.type = type;
  }
  public ObjectFieldImpl type(DataTypeImpl type) {
    this.type = type;
    return this;
  }
}
