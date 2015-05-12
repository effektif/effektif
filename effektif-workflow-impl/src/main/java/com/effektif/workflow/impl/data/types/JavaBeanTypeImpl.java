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

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Map;

import com.effektif.workflow.api.Configuration;
import com.effektif.workflow.api.types.DataType;
import com.effektif.workflow.api.types.JavaBeanType;
import com.effektif.workflow.impl.data.DataTypeImpl;
import com.effektif.workflow.impl.data.DataTypeService;
import com.effektif.workflow.impl.data.InvalidValueException;
import com.effektif.workflow.impl.json.JavaBeanValueMapper;


/**
 * @author Tom Baeyens
 */
public class JavaBeanTypeImpl<T extends DataType> extends ObjectTypeImpl<T> {
  
  protected JavaBeanValueMapper valueMapper;
  protected Class<?> valueClass;
  
  public JavaBeanTypeImpl() {
    super((T) new JavaBeanType(), null);
  }
  
  public JavaBeanTypeImpl(DataType typeApi) {
    super((T) typeApi, null);
    this.valueClass = (Class<?>)typeApi.getValueType();
  }

  public void setConfiguration(Configuration configuration) {
    super.setConfiguration(configuration);
    this.valueMapper = configuration.get(JavaBeanValueMapper.class);
    initializeFields();
  }
  
  protected void initializeFields() {
    scanFields(valueClass);
  }

  protected void scanFields(Class< ? > valueType) {
    if (valueType!=null) {
      for (Field field : valueType.getDeclaredFields()) {
        if (!Modifier.isStatic(field.getModifiers())) {
          addField(field);
        }
      }
      Class< ? > superclass = valueType.getSuperclass();
      if (superclass != Object.class) {
        scanFields(superclass);
      }
    }
  }

  protected void addField(Field field) {
    DataTypeService dataTypeService = configuration.get(DataTypeService.class);
    DataTypeImpl dataType = dataTypeService.getDataTypeByValue(field.getType());
    JavaBeanFieldImpl javaBeanField = new JavaBeanFieldImpl(field, dataType);
    addField(javaBeanField);
  }

  @Override
  public boolean isStatic() {
    return false;
  }

  @Override
  public void validateInternalValue(Object internalValue) throws InvalidValueException {
    if (internalValue==null) {
      return;
    }
    if (! valueClass.isAssignableFrom(internalValue.getClass())) {
      throw new InvalidValueException("Invalid internal value: was "+internalValue+" ("+internalValue.getClass().getName()+"), expected "+valueClass.getName());
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public Object convertJsonToInternalValue(Object jsonValue) throws InvalidValueException {
    if (jsonValue==null) return null;
    if (Map.class.isAssignableFrom(jsonValue.getClass())) {
      return valueMapper.read(jsonValue, valueClass);
    }
    throw new InvalidValueException("Couldn't convert json: "+jsonValue+" ("+jsonValue.getClass().getName()+")");
  }
  
  @Override
  public Object convertInternalToJsonValue(Object internalValue) {
    return internalValue;
  }
  
  public Class< ? > getValueClass() {
    return valueClass;
  }
  
  public void setJsonService(JavaBeanValueMapper jsonMapper) {
    this.valueMapper = jsonMapper;
  }
}
