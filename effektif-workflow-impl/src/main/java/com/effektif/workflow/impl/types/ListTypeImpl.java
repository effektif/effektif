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
import com.effektif.workflow.impl.plugin.AbstractDataType;
import com.effektif.workflow.impl.plugin.DataType;
import com.effektif.workflow.impl.plugin.PluginService;
import com.effektif.workflow.impl.plugin.ServiceRegistry;


public class ListTypeImpl extends AbstractDataType<ListType> {
  
  protected DataType elementDataType;
  
  /** constructor for json & persistence, dataType is a required field. */
  public ListTypeImpl() {
    super(ListType.class, List.class);
  }

  public ListTypeImpl(DataType elementDataType) {
    super(ListType.class, List.class);
    this.elementDataType = elementDataType;
  }

  @Override
  public boolean isSerializeRequired() {
    return elementDataType!=null ? elementDataType.isSerializeRequired() : false;
  }
  
  @Override
  public Object serialize(Object o) {
    if (o==null || ((List<Object>)o).isEmpty()) {
      return o;
    }
    List<Object> list = (List<Object>) o;
    List<Object> serialized = new ArrayList<>(list.size()); 
    for (Object element: list) {
      serialized.add(elementDataType.serialize(element));
    }
    return serialized;
  }

  @Override
  public Object deserialize(Object o) {
    if (o==null || ((List<Object>)o).isEmpty()) {
      return o;
    }
    List<Object> list = (List<Object>) o;
    List<Object> deserialized = new ArrayList<>(list.size()); 
    for (Object element: list) {
      deserialized.add(elementDataType.deserialize(element));
    }
    return deserialized;
  }

  @Override
  public void initialize(ListType listType, ServiceRegistry serviceRegistry) {
    super.initialize(listType, serviceRegistry);
    PluginService pluginService = serviceRegistry.getService(PluginService.class);
    Type elementType = listType.getElementType();
    if (elementType!=null) {
      this.elementDataType = pluginService.createDataType(elementType);
      this.elementDataType.initialize(elementType, serviceRegistry);
    }
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
