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

import com.effektif.workflow.impl.data.AbstractDataType;
import com.effektif.workflow.impl.data.DataTypeImpl;
import com.effektif.workflow.impl.data.DataTypeService;
import com.effektif.workflow.impl.data.TypedValueImpl;


/**
 * @author Tom Baeyens
 */
public class AnyTypeImpl extends AbstractDataType<AnyType> {

  public AnyTypeImpl() {
    super(AnyType.INSTANCE);
  }

  @Override
  public TypedValueImpl dereference(Object value, String field) {
    if (value instanceof Map) {
      Map mapValue = (Map) value;
      Object fieldValue = mapValue.get(field);
      DataTypeService dataTypeService = configuration.get(DataTypeService.class);
      Class<?> fieldValueClass = fieldValue!=null ? fieldValue.getClass() : null;
      DataTypeImpl fieldDataType = dataTypeService.getDataTypeByValue(fieldValueClass);
      return new TypedValueImpl(fieldDataType, fieldValue);
    }
    return null;
  }
  
  
}
