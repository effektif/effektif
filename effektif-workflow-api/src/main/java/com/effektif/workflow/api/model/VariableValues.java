/* Copyright (c) 2014, Effektif GmbH.
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
package com.effektif.workflow.api.model;

import java.util.HashMap;
import java.util.Map;

import com.effektif.workflow.api.types.DataType;


/**
 * @author Tom Baeyens
 */
public class VariableValues {

  protected Map<String,TypedValue> values;

  public Object getValue(String variableId) {
    TypedValue value = values!=null ? values.get(variableId) : null;
    return value!=null ? value.getValue() : null;
  }
  
  public void setValue(String variableId, Object value) {
    setTypedValue(variableId, new TypedValue(value));
  }

  public void setValue(String variableId, Object value, DataType dataType) {
    setTypedValue(variableId, new TypedValue(value, dataType));
  }
  
  public void setTypedValue(String variableId, TypedValue value) {
    if (values==null) {
      values = new HashMap<>();
    }
    values.put(variableId, value);
  }
  
  public VariableValues value(String variableId, Object value) {
    setValue(variableId, value);
    return this;
  }

  public VariableValues value(String variableId, Object value, DataType dataType) {
    setValue(variableId, value, dataType);
    return this;
  }

  public VariableValues typedValue(String variableId, TypedValue value) {
    setTypedValue(variableId, value);
    return this;
  }

  public Map<String, TypedValue> getValues() {
    return values;
  }

  public void setValues(Map<String, TypedValue> values) {
    this.values = values;
  }
}
