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
package com.effektif.workflow.impl.type;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.effektif.workflow.api.type.ObjectField;
import com.effektif.workflow.api.type.ObjectType;
import com.effektif.workflow.impl.json.JsonService;
import com.effektif.workflow.impl.plugin.AbstractDataType;
import com.effektif.workflow.impl.plugin.ServiceRegistry;


public class ObjectTypeImpl<T extends ObjectType> extends AbstractDataType<T> {
  
  protected List<ObjectFieldImpl> fields;
  protected boolean isSerializeRequired;
  protected JsonService jsonService;

  public ObjectTypeImpl() {
    super(ObjectType.class, null);
  }

  public ObjectTypeImpl(Class< ? > valueClass) {
    super(ObjectType.class, valueClass);
  }

  public ObjectTypeImpl(Class<?> apiClass, Class< ? > valueClass) {
    super(apiClass, valueClass);
  }

  public boolean isSerializeRequired() {
    return isSerializeRequired;
  }

  public Object serialize(Object o) {
    if (isSerializeRequired && fields!=null) {
      for (ObjectFieldImpl field: fields) {
        field.serialize(o);
      }
    }
    return o;
  }

  public Object deserialize(Object o) {
    if (o instanceof Map) {
      o = jsonService.jsonMapToObject((Map<String,Object>) o, valueClass);
    }
    if (isSerializeRequired && fields!=null) {
      for (ObjectFieldImpl field: fields) {
        field.deserialize(o);
      }
    }
    return o;
  }

  @Override
  public void initialize(T typeApi, ServiceRegistry serviceRegistry) {
    super.initialize(typeApi, serviceRegistry);
    List<ObjectField> fieldsApi = typeApi.getFields();
    if (fieldsApi!=null) {
      fields = new ArrayList<>(fieldsApi.size());
      for (ObjectField fieldApi: fieldsApi) {
        ObjectFieldImpl fieldImpl = new ObjectFieldImpl();
        fieldImpl.parse(valueClass, fieldApi, serviceRegistry);
        isSerializeRequired = isSerializeRequired || (fieldImpl.type!=null && fieldImpl.type.isSerializeRequired());
        fields.add(fieldImpl);
      }
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
