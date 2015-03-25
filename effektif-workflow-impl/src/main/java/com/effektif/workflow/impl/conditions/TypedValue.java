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
package com.effektif.workflow.impl.conditions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.effektif.model.types.ListType;
import com.effektif.model.types.ReferenceType;
import com.effektif.model.types.Type;
import com.effektif.model.types.ValidationContext;
import com.effektif.model.types.VariableFormat;


/**
 * @author Tom Baeyens
 */
public class TypedValue {
  
  //private static final Logger log = LoggerFactory.getLogger(TypedValue.class);

  public Type type;
  public Object value;

  public TypedValue(Type type, Object value) {
    this.type = type;
    this.value = value;
  }
  
  /**
   * Apply a type if none was applied, yet.
   */
  public void applyType(Type type, VariableFormat format) {
    if (this.type == null && type != null) {
      this.type = type;
      if (this.value != null) {
        value = this.type.parse(value, format);
      }
    }
  }
  
  public Object getValueInRestApi() {
    if (value==null) {
      return null;
    }
    Object restApiValue = type.render(value, VariableFormat.REST_API);
    if (type.isObjectType()) {
      Map<String, Object> restApiValueMap = (Map<String, Object>) restApiValue;
      Map<String,Object> envelope = new HashMap<>();
      envelope.put(ReferenceType.RESTKEY_OBJECT_ID, restApiValueMap.get(ReferenceType.OBJECTFIELD_ID));
      envelope.put(ReferenceType.RESTKEY_OBJECT, restApiValueMap);
      return envelope;

    } else if (type.isListType()) {
      ListType listType = (ListType) type;
      if (listType.elementType instanceof ReferenceType) {
        List<Object> objectIds = new ArrayList<>();
        List<Map<String,Object>> objects = new ArrayList<>();
        for (Map<String,Object> object: (Collection<Map<String,Object>>)restApiValue) {
          objectIds.add(object.get(ReferenceType.OBJECTFIELD_ID));
          objects.add(object);
        }
        Map<String,Object> envelope = new HashMap<>();
        envelope.put(ReferenceType.RESTKEY_OBJECT_IDS, objectIds);
        envelope.put(ReferenceType.RESTKEY_OBJECTS, objects);
        return envelope;
      }
    }
    return restApiValue;
  }

  public Object getValueInEmailFormat() {
    return getFormat(VariableFormat.EMAIL_TEXT);
  }

  public Object getValueInExpressionFormat() {
    return getFormat(VariableFormat.EXPRESSION);
  }

  public Object getValueInJavaScriptFormat() {
    return getFormat(VariableFormat.JAVASCRIPT_JSON);
  }

  public Object getValueInParameterFormat() {
    return getFormat(VariableFormat.PARAMETER);
  }

  public Object getFormat(VariableFormat variableFormat) {
    return type.render(value, variableFormat);
  }

  public void setValueInRestApiFormat(Object value, ValidationContext validationContext) {
    if (value!=null) {
      this.value = type.parse(value, VariableFormat.REST_API, validationContext);
    } else {
      this.value = null;
    }
  }
}
