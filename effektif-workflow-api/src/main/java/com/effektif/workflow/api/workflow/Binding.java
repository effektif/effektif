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

import com.effektif.workflow.api.command.TypedValue;
import com.effektif.workflow.api.types.Type;
import com.effektif.workflow.api.types.TypeHelper;


/** Describes how the value is obtained 
 * for an activity input parameter. */
public class Binding<T> {

  /** the fixed input value.  
   * This is mutually exclusive with variableId and expression */
  protected TypedValue typedValue;

  /** reference to the variable that will contain the input value.  
   * This is mutually exclusive with value and expression */
  protected String variableId;

  /** expression that will produce the input value.  
   * This is mutually exclusive with variableId and value */
  protected String expression;

  public Object getValue() {
    return typedValue!=null ? typedValue.getValue() : null;
  }
  
  public Binding value(Object value) {
    return valueTyped(value, TypeHelper.getTypeByValue(value));
  }

  /** sets a fixed value with the given type.*/
  // The naming is like this for code completion convenience.
  // This way, developers can always start typing 'value' and then 
  // look for that method that allowed them to pass in the type.
  public Binding valueTyped(Object value, Type type) {
    this.typedValue = new TypedValue()
      .value(value)
      .type(type);
    return this;
  }

  public Binding typedValue(TypedValue typedValue) {
    this.typedValue = typedValue;
    return this;
  }
  
  public TypedValue getTypedValue() {
    return typedValue;
  }
  
  public void setTypedValue(TypedValue typedValue) {
    this.typedValue = typedValue;
  }

  public String getVariableId() {
    return this.variableId;
  }
  public void setVariableId(String variableId) {
    this.variableId = variableId;
  }
  public Binding variableId(String variableId) {
    this.variableId = variableId;
    return this;
  }

  public String getExpression() {
    return this.expression;
  }
  public void setExpression(String expression) {
    this.expression = expression;
  }
  public Binding expression(String expression) {
    this.expression = expression;
    return this;
  }
}
