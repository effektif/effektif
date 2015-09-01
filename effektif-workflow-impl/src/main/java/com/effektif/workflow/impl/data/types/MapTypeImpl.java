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
import com.effektif.workflow.api.types.DataType;
import com.effektif.workflow.impl.WorkflowParser;
import com.effektif.workflow.impl.data.AbstractDataType;
import com.effektif.workflow.impl.data.DataTypeImpl;
import com.effektif.workflow.impl.data.DataTypeService;
import com.effektif.workflow.impl.data.TypedValueImpl;


/**
 * @author Tom Baeyens
 */
public class MapTypeImpl extends AbstractDataType<MapType> {
  
  DataTypeImpl<?> elementTypeImpl;

  public MapTypeImpl() {
    super(MapType.INSTANCE);
  }

  public MapTypeImpl(MapType type) {
    super(type);
  }
  
  @Override
  public void setConfiguration(Configuration configuration) {
    super.setConfiguration(configuration);
    DataType elementType = type!=null ? type.getElementType() : null;
    if (elementType!=null) {
      DataTypeService dataTypeService = configuration.get(DataTypeService.class);
      elementTypeImpl = dataTypeService.createDataType(elementType);
    }
  }

  @Override
  public DataTypeImpl parseDereference(String field, WorkflowParser parser) {
    return elementTypeImpl;
  }

  @Override
  public TypedValueImpl dereference(Object value, String fieldName) {
    if (value instanceof Map) {
      Map mapValue = (Map) value;
      Object fieldValue = mapValue.get(fieldName);
      return new TypedValueImpl(elementTypeImpl, fieldValue);
    }
    return null;
  }

  @Override
  public boolean isStatic() {
    return false;
  }
}
