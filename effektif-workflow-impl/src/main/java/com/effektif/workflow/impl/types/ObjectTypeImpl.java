/* Copyright 2014 Effektif GmbH.
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

import java.util.ArrayList;
import java.util.List;

import com.effektif.workflow.api.types.ObjectField;
import com.effektif.workflow.api.types.ObjectType;
import com.effektif.workflow.impl.json.JsonService;
import com.effektif.workflow.impl.plugin.ServiceRegistry;
import com.effektif.workflow.impl.type.AbstractDataType;
import com.effektif.workflow.impl.type.InvalidValueException;


public class ObjectTypeImpl<T extends ObjectType> extends AbstractDataType<T> {
  
  protected List<ObjectFieldImpl> fields;
  protected JsonService jsonService;

  public ObjectTypeImpl() {
    super(ObjectType.class);
  }

  public ObjectTypeImpl(ObjectType typeApi, ServiceRegistry serviceRegistry) {
    this(typeApi, serviceRegistry, null);
  }

  public ObjectTypeImpl(ObjectType typeApi, ServiceRegistry serviceRegistry, Class<?> valueClass) {
    super(typeApi.getClass());
    this.valueClass = valueClass;
    List<ObjectField> fieldsApi = typeApi.getFields();
    if (fieldsApi!=null) {
      fields = new ArrayList<>(fieldsApi.size());
      for (ObjectField fieldApi: fieldsApi) {
        ObjectFieldImpl fieldImpl = new ObjectFieldImpl(valueClass, fieldApi, serviceRegistry);
        fields.add(fieldImpl);
      }
    }
  }

  public Object getFieldValue(Object o, ObjectFieldImpl field) {
    try {
      return field.field.get(o);
    } catch (IllegalArgumentException | IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public Object convertJsonToInternalValue(Object jsonValue) throws InvalidValueException {
    return null;
  }

  public List<ObjectFieldImpl> getFields() {
    return this.fields;
  }
  public void setFields(List<ObjectFieldImpl> fields) {
    this.fields = fields;
  }
  public ObjectTypeImpl field(ObjectFieldImpl field) {
    if (this.fields==null) {
      this.fields = new ArrayList<>();
    }
    this.fields.add(field);
    return this;
  }

  @Override
  public Class< ? > getValueClass() {
    return valueClass;
  }
}
