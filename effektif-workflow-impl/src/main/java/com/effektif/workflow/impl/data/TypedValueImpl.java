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
package com.effektif.workflow.impl.data;

import com.effektif.workflow.api.Configuration;
import com.effektif.workflow.api.model.TypedValue;


public class TypedValueImpl {

  public DataType type;
  public Object value;

  public TypedValueImpl(DataType type, Object value) {
    this.type = type;
    this.value = value;
  }

  public DataType getType() {
    return type;
  }
  
  public void setType(DataType type) {
    this.type = type;
  }
  
  public Object getValue() {
    return value;
  }
  
  public void setValue(Object value) {
    this.value = value;
  }

  public TypedValue serialize() {
    TypedValue typedValue = new TypedValue();
    typedValue.setType(type.serialize());
    typedValue.setValue(value);
    return typedValue;
  }
}
