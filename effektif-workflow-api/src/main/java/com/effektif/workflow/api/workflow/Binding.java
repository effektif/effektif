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
  
  protected String expression;

  /**
   * the fixed value. When serializing and deserializing, the type for this
   * value will be automatically initialized. This value is mutually exclusive
   * with variableId and expression
   */
  public T getValue() {
    return this.value;
  }
  /**
   * the fixed value. When serializing and deserializing, the type for this
   * value will be automatically initialized. This value is mutually exclusive
   * with expression
   */
  public void setValue(T value) {
    this.value = value;
  }
  /**
   * the fixed value. When serializing and deserializing, the type for this
   * value will be automatically initialized. This value is mutually exclusive
   * with variableId and expression
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
}
