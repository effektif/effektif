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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.effektif.workflow.api.Configuration;
import com.effektif.workflow.api.types.ListType;
import com.effektif.workflow.api.types.Type;
import com.effektif.workflow.impl.data.AbstractDataType;
import com.effektif.workflow.impl.data.DataType;
import com.effektif.workflow.impl.data.DataTypeService;
import com.effektif.workflow.impl.data.InvalidValueException;
import com.effektif.workflow.impl.data.TypeGenerator;
import com.effektif.workflow.impl.util.Lists;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;


/**
 * @author Tom Baeyens
 */
public class ListTypeImpl extends AbstractDataType<ListType> {
  
  public DataType elementType;
  
  public ListTypeImpl() {
    super(new ListType(), List.class);
  }

  public ListTypeImpl(ListType listTypeApi, Configuration configuration) {
    super(listTypeApi, List.class);
    this.valueClass = List.class;
    Type elementType = listTypeApi.getElementType();
    if (elementType!=null) {
      DataTypeService dataTypeService = configuration.get(DataTypeService.class);
      this.elementType = dataTypeService.createDataType(elementType);
    }
  }
  
  @Override
  public Object convert(Object value, DataType valueType) {
    if (value==null) {
      return value;
    }
    DataType elementValueType = null;
    if (! (value instanceof Collection)) {
      value = Lists.of(value);
      elementValueType = valueType;
    } else if (valueType instanceof ListTypeImpl) {
      elementValueType = ((ListTypeImpl) valueType).elementType; 
    }
    Collection<Object> collection = (Collection<Object>) value;
    if (!requiresConversion(collection)) {
      return collection;
    }
    List<Object> convertedCollection = new ArrayList<>(collection.size());
    for (Object element: collection) {
      convertedCollection.add(elementType.convert(element, elementValueType));
    }
    return convertedCollection;
  }

  protected boolean requiresConversion(Collection<Object> collection) {
    for (Object element: collection) {
      if (element!=null && elementType.convert(element, null)!=element) {
        return true;
      }
    }
    return false;
  }

  @Override
  public TypeGenerator getTypeGenerator() {
    return new TypeGenerator<ListType>() {
      @Override
      public JavaType createJavaType(ListType listType, TypeFactory typeFactory, DataTypeService dataTypeService) {
        Type elementType = listType.getElementType();
        if (elementType==null) {
          return null;
        }
        JavaType elementJavaType = dataTypeService.createJavaType(elementType);
        if (elementJavaType==null) {
          return elementJavaType = typeFactory.constructType(Object.class);
        }
        return typeFactory.constructParametricType(List.class, elementJavaType);
      }
    };
  }

  @Override
  public void validateInternalValue(Object internalValue) throws InvalidValueException {
    if (internalValue==null) {
      return;
    }
    if (!(internalValue instanceof List)) {
      throw new InvalidValueException("Value for must be a list, but was "+internalValue+" ("+internalValue.getClass().getName()+")");
    }
    @SuppressWarnings("unchecked")
    java.util.List<Object> list = (java.util.List<Object>) internalValue;
    for (Object element: list) {
      elementType.validateInternalValue(element);
    }
  }

  @Override
  public Object convertJsonToInternalValue(Object jsonValue) throws InvalidValueException {
    if (jsonValue==null) {
      return null;
    }
    if (!(jsonValue instanceof List)) {
      throw new InvalidValueException("Json value must be a list, but was "+jsonValue+" ("+jsonValue.getClass().getName()+")");
    }
    @SuppressWarnings("unchecked")
    java.util.List<Object> list = (java.util.List<Object>) jsonValue;
    for (int i=0; i<list.size(); i++) {
      Object elementJsonValue = list.get(i);
      Object elementInternalValue = elementType.convertJsonToInternalValue(elementJsonValue);
      list.set(i, elementInternalValue);
    }
    return list;
  }

  @SuppressWarnings("unchecked")
  @Override
  public Object convertInternalToJsonValue(Object internalValue) {
    if (internalValue==null) {
      return null;
    }
    java.util.List<Object> internalValues = (java.util.List<Object>) internalValue;
    java.util.List<Object> jsonValues = new ArrayList<>(internalValues.size());
    for (Object elementInternalValue: internalValues) {
      Object elementJsonValue = elementType.convertInternalToJsonValue(elementInternalValue);
      jsonValues.add(elementJsonValue);
    }
    return jsonValues;
  }
}
