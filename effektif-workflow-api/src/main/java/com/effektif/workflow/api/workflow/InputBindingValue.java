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
package com.effektif.workflow.api.workflow;

import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("value")
public class InputBindingValue extends InputBinding {

  /** the fixed input value.  
   * This is mutually exclusive with variableId and expression */
  protected Object value;
  
  public InputBindingValue() {
  }

  public InputBindingValue(String key, Object value) {
    super(key);
    this.value = value;
  }
  
  public Object getValue() {
    return this.value;
  }
  public void setValue(Object value) {
    this.value = value;
  }
  public InputBinding value(Object value) {
    this.value = value;
    return this;
  }
  
}
