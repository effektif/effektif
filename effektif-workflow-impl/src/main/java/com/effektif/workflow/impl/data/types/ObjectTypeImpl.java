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
import com.effektif.workflow.api.types.ObjectField;
import com.effektif.workflow.api.types.Type;
import com.effektif.workflow.impl.data.AbstractDataType;
import com.effektif.workflow.impl.data.TypedValueImpl;
import com.effektif.workflow.impl.json.JsonService;


/**
 * @author Tom Baeyens
 */
public class ObjectTypeImpl<T extends Type> extends AbstractDataType<T> {
  
  protected JsonService jsonService;
  public Map<String,ObjectFieldImpl> fields;

  public ObjectTypeImpl(T typeApi, Class<?> valueClass, Configuration configuration) {
    super(typeApi, valueClass);
    initializeFields(configuration);
  }

  protected void initializeFields(Configuration configuration) {
  }

  protected ObjectFieldImpl createField(Configuration configuration, Class< ? > valueClass, ObjectField fieldApi) {
    return new ObjectFieldImpl(valueClass, fieldApi, configuration);
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
