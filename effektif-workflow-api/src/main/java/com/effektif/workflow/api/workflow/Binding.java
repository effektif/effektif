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

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

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
   * This is mutually exclusive with value */
  protected String variableId;

  /** the fields that should be dereferenced in the fetched variableId.  
   * This an optional value if a variableId is specified. */
  protected List<String> fields;

  /** result of resolving the expression will be provided as the value.
   * Can be used for template strings or conversion functions. */
  protected Expression expression;
  
  /** for bindings that expect a list, a list of nested bindings can be 
   * specified.  All the results will be flatened and added to the collection.
   * This is mutually exclusive with the other fields. */
  protected List<Binding<T>> bindings;

  public List<Binding<T>> getBindings() {
    return this.bindings;
  }
  public void setBindings(List<Binding<T>> bindings) {
    this.bindings = bindings;
  }
  public Binding bindings(List<Binding<T>> bindings) {
    this.bindings = bindings;
    return this;
  }
  public Binding binding(Binding<T> binding) {
    if (bindings==null) {
      bindings = new ArrayList<>();
    }
    bindings.add(binding);
    return this;
  }

  public Object getValue() {
    return typedValue!=null ? typedValue.getValue() : null;
  }
  
  public Binding value(Object value) {
    return typedValue(value, TypeHelper.getTypeByValue(value));
  }

  /** sets a fixed value with the given type.*/
  // The naming is like this for code completion convenience.
  // This way, developers can always start typing 'value' and then 
  // look for that method that allowed them to pass in the type.
  public Binding typedValue(Object value, Type type) {
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

  public List<String> getFields() {
    return this.fields;
  }
  public void setField(List<String> fields) {
    this.fields = fields;
  }
  /** .-separated notation that starts with the variableId and then 
   * specifies the fields to be dereferenced 
   * eg "myVariableId.variableField.nestedField" */
  public Binding variableField(String variableFieldExpression) {
    if (variableFieldExpression==null) {
      return this;
    }
    StringTokenizer tokenizer = new StringTokenizer(variableFieldExpression, ".");
    if (!tokenizer.hasMoreTokens()) {
      return this;
    }
    this.variableId = tokenizer.nextToken();
    while (tokenizer.hasMoreTokens()) {
      if (fields == null) {
        fields = new ArrayList<>();
      }
      fields.add(tokenizer.nextToken());
    }
    return this;
  }
  
  public void setFields(List<String> fields) {
    this.fields = fields;
  }

  public Expression getExpression() {
    return this.expression;
  }
  public void setExpression(Expression expression) {
    this.expression = expression;
  }
  public Binding expression(Expression expression) {
    this.expression = expression;
    return this;
  }
  public Binding expression(String expression) {
    this.expression = new Expression()
      .script(expression);
    return this;
  }
}
