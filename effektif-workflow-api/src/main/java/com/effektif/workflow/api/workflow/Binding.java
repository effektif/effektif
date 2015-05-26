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
package com.effektif.workflow.api.workflow;

import com.effektif.workflow.api.types.DataType;


/**
 * A binding stores a value for an activity input parameter, such as a process
 * variable.
 *
 * <p>
 * A binding specifies a value in one of two ways:
 * </p>
 * <ol>
 * <li>a fixed value</li>
 * <li>an expression - see <a href="https://github.com/effektif/effektif/wiki/Expressions">Expressions</a>.</li>
 * </ol>
 *
 * @author Tom Baeyens
 */
public class Binding<T> {

  protected T value;
  protected DataType dataType;
  protected String expression;

  /**
   * Returns the fixed value. When serializing and deserializing, the type for this
   * value will be automatically initialized. This value is mutually exclusive
   * with expression.
   */
  public T getValue() {
    return this.value;
  }

  /**
   * Sets the fixed value. When serializing and deserializing, the type for this
   * value will be automatically initialized. This value is mutually exclusive
   * with expression.
   */
  public void setValue(T value) {
    this.value = value;
  }

  /**
   * Sets the fixed value. When serializing and deserializing, the type for this
   * value will be automatically initialized. This value is mutually exclusive
   * with and expression
   */
  public Binding<T> value(T value) {
    this.value = value;
    return this;
  }
  
  /** specifies how a dynamic value is to be fetched from the variables. 
   * @see https://github.com/effektif/effektif/wiki/Expressions */
  public String getExpression() {
    return this.expression;
  }

  /** specifies how a dynamic value is to be fetched from the variables. 
   * @see https://github.com/effektif/effektif/wiki/Expressions */
  public void setExpression(String expression) {
    this.expression = expression;
  }

  /** specifies how a dynamic value is to be fetched from the variables.
   * @see https://github.com/effektif/effektif/wiki/Expressions */
  public Binding expression(String expression) {
    this.expression = expression;
    return this;
  }

  public boolean isEmpty() {
    return value == null && expression == null;
  }
  
  public DataType getDataType() {
    return dataType;
  }
  
  public void setDataType(DataType dataType) {
    this.dataType = dataType;
  }
  
  public Binding<T> dataType(DataType dataType) {
    this.dataType = dataType;
    return this;
  }
  
  @Override
  public String toString() {
    return "Binding[value=" + value + ",dataType=" + dataType + ",expression=" + expression + "]";
  }
}
