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

import com.effektif.workflow.api.types.ListType;
import com.effektif.workflow.api.types.Type;
import com.effektif.workflow.impl.plugin.ServiceRegistry;
import com.effektif.workflow.impl.type.AbstractDataType;
import com.effektif.workflow.impl.type.DataType;
import com.effektif.workflow.impl.type.DataTypeService;
import com.effektif.workflow.impl.type.InvalidValueException;
import com.effektif.workflow.impl.type.TypeGenerator;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;


public class ListTypeImpl extends AbstractDataType<ListType> {
  
  protected DataType elementDataType;
  
  /** constructor for json & persistence, dataType is a required field. */
  public ListTypeImpl() {
    super(ListType.class);
    this.valueClass = List.class;
  }

//  public ListTypeImpl(DataType elementDataType) {
//    super(ListType.class);
//    this.valueClass = List.class;
//    this.elementDataType = elementDataType;
//  }

  public ListTypeImpl(ListType listType, ServiceRegistry serviceRegistry) {
    super(ListType.class);
    this.valueClass = List.class;
    Type elementType = listType.getElementType();
    if (elementType!=null) {
      DataTypeService dataTypeService = serviceRegistry.getService(DataTypeService.class);
      this.elementDataType = dataTypeService.createDataType(elementType);
    }
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
        return typeFactory.constructParametricType(List.class, dataTypeService.createJavaType(elementType));
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
      elementDataType.validateInternalValue(element);
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
      Object elementInternalValue = elementDataType.convertJsonToInternalValue(elementJsonValue);
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
      Object elementJsonValue = elementDataType.convertInternalToJsonValue(elementInternalValue);
      jsonValues.add(elementJsonValue);
    }
    return jsonValues;
  }
}
