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
package com.effektif.workflow.impl.types;

import java.lang.reflect.Field;

import com.effektif.workflow.api.types.ObjectField;
import com.effektif.workflow.api.types.Type;
import com.effektif.workflow.impl.plugin.ServiceRegistry;
import com.effektif.workflow.impl.type.DataType;
import com.effektif.workflow.impl.type.DataTypeService;


public class ObjectFieldImpl {

  protected String name;
  protected DataType type;
  protected Field field;
  
  public ObjectFieldImpl(String name) {
    this.name = name;
  }

  public ObjectFieldImpl(Class< ? > objectClass, ObjectField fieldApi, ServiceRegistry serviceRegistry) {
    try {
      this.name = fieldApi.getName();
      Type fieldType = fieldApi.getType();
      DataTypeService dataTypeService = serviceRegistry.getService(DataTypeService.class);
      this.type = dataTypeService.createDataType(fieldType);
      this.field = objectClass.getDeclaredField(name);
      this.field.setAccessible(true);
    } catch (IllegalArgumentException | NoSuchFieldException | SecurityException e) {
      throw new RuntimeException(e);
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
