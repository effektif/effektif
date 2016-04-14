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

import com.effektif.workflow.api.types.DataType;
import com.effektif.workflow.impl.WorkflowParser;
import com.effektif.workflow.impl.data.AbstractDataType;
import com.effektif.workflow.impl.data.DataTypeImpl;
import com.effektif.workflow.impl.data.DataTypeService;
import com.effektif.workflow.impl.data.TypedValueImpl;
import com.effektif.workflow.impl.template.Hint;
import com.effektif.workflow.impl.template.Hints;

import java.util.HashMap;
import java.util.Map;


/**
 * @author Tom Baeyens
 */
public class ObjectTypeImpl<T extends DataType> extends AbstractDataType<T> {
  
  public Map<String,ObjectFieldImpl> fields;

  public ObjectTypeImpl() {
    super((T)ObjectType.INSTANCE);
  }

  public ObjectTypeImpl(T typeApi) {
    super(typeApi);
  }
  
  @Override
  public DataTypeImpl parseDereference(String field, WorkflowParser parser) {
    if (fields!=null) {
      ObjectFieldImpl objectField = fields.get(field);
      if (objectField!=null) {
        return objectField.type;
      } else {
        parser.addWarning("Field '%s' does not exist", field);
      }
    }
    return new AnyTypeImpl();
  }

  @Override
  public TypedValueImpl dereference(Object value, String fieldName) {
    ObjectFieldImpl field = fields!=null ? fields.get(fieldName) : null;
    if (field!=null) {
      DataTypeImpl fieldType = field.type;
      Object fieldValue = field.getFieldValue(value);
      return new TypedValueImpl(fieldType, fieldValue);

    } else if ("toText".equals(fieldName)) {
      String textValue = convertInternalToText(value, new Hints().add(Hint.TO_TEXT));
      return new TypedValueImpl(new TextTypeImpl(), textValue);

    } else if (value instanceof Map) {
      Map mapValue = (Map) value;
      Object fieldValue = mapValue.get(fieldName);
      DataTypeService dataTypeService = configuration.get(DataTypeService.class);
      Class<?> fieldValueClass = fieldValue!=null ? fieldValue.getClass() : null;
      DataTypeImpl fieldDataType = dataTypeService.getDataTypeByValue(fieldValueClass);
      return new TypedValueImpl(fieldDataType, fieldValue);
    }
    return null;
  }

  public void addField(ObjectFieldImpl field) {
    if (fields==null) {
      fields = new HashMap<>();
    }
    fields.put(field.getKey(), field);
  }

  @Override
  public String getFieldLabel(String field) {
    ObjectFieldImpl objectField = fields!=null ? fields.get(field) : null;
    return objectField!=null ? objectField.getName() : field;
  }
}
