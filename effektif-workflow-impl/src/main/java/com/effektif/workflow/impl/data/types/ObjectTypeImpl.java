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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.effektif.workflow.api.Configuration;
import com.effektif.workflow.api.types.ObjectField;
import com.effektif.workflow.api.types.ObjectType;
import com.effektif.workflow.api.types.Type;
import com.effektif.workflow.impl.data.AbstractDataType;
import com.effektif.workflow.impl.data.InvalidValueException;
import com.effektif.workflow.impl.data.TypedValueImpl;
import com.effektif.workflow.impl.json.JsonService;


public class ObjectTypeImpl<T extends ObjectType> extends AbstractDataType<T> {
  
  protected Map<String, ObjectFieldImpl> fields;
  protected JsonService jsonService;

  public ObjectTypeImpl(Class<? extends Type> apiClass) {
    super(apiClass);
  }

  public ObjectTypeImpl(ObjectType typeApi, Configuration configuration) {
    this(typeApi, configuration, null);
  }

  public ObjectTypeImpl(ObjectType typeApi,Configuration configuration, Class<?> valueClass) {
    super(typeApi.getClass());
    this.valueClass = valueClass;
    List<ObjectField> fieldsApi = typeApi.getFields();
    if (fieldsApi!=null) {
      for (ObjectField fieldApi: fieldsApi) {
        ObjectFieldImpl fieldImpl = createField(configuration, valueClass, fieldApi);
        field(fieldImpl);
      }
    }
  }

  protected ObjectFieldImpl createField(Configuration configuration, Class< ? > valueClass, ObjectField fieldApi) {
    return new ObjectFieldImpl(valueClass, fieldApi, configuration);
  }


  @Override
  public Object convertJsonToInternalValue(Object jsonValue) throws InvalidValueException {
    return null;
  }

  
  public ObjectTypeImpl field(ObjectFieldImpl field) {
    if (this.fields==null) {
      this.fields = new LinkedHashMap<>();
    }
    this.fields.put(field.name, field);
    return this;
  }

  @Override
  public Class< ? > getValueClass() {
    return valueClass;
  }

  public void dereference(TypedValueImpl typedValue, String fieldName) {
    if (typedValue==null) {
      return;
    }
    ObjectFieldImpl field = fields.get(fieldName);
    if (typedValue.value==null || field==null) {
      typedValue.type = null;
      typedValue.value = null;
      return;
    }
    typedValue.type = field.type;
    field.dereferenceValue(typedValue);
  }
}
