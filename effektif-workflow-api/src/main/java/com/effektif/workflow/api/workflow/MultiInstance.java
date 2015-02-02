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
package com.effektif.workflow.api.workflow;

import com.effektif.workflow.api.types.Type;


public class MultiInstance {

  protected Variable variable;
  protected Binding valuesBinding;

  public Variable getVariable() {
    return this.variable;
  }
  public void setVariable(Variable variable) {
    this.variable = variable;
  }
  public MultiInstance variable(Variable variable) {
    this.variable = variable;
    return this;
  }
  public MultiInstance variable(String id, Type type) {
    this.variable = new Variable()
      .id(id)
      .type(type);
    return this;
  }
  public Binding getValuesBindings() {
    return this.valuesBinding;
  }
  public void setValueBindings(Binding valuesBinding) {
    this.valuesBinding = valuesBinding;
  }
  public MultiInstance valuesVariableId(String valuesVariableId) {
    valueBinding(new Binding().variableId(valuesVariableId));
    return this;
  }
  public MultiInstance valuesExpression(String valuesExpression) {
    valueBinding(new Binding().expression(valuesExpression));
    return this;
  }
  
  public MultiInstance valueBinding(Binding valueBinding) {
    if (valuesBinding==null) {
      valuesBinding = new Binding();
    }
    valuesBinding.binding(valueBinding);
    return this;
  }
}
