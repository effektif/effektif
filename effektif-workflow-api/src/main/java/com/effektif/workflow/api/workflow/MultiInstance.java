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


public class MultiInstance {

  protected Variable variable;
  protected Binding collection;

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
  
  public Binding getCollection() {
    return this.collection;
  }
  public void setCollection(Binding collection) {
    this.collection = collection;
  }
  public MultiInstance collectionVariableId(String collectionVariableId) {
    this.collection = new Binding().variableId(collectionVariableId);
    return this;
  }
  public MultiInstance collectionExpression(String collectionExpression) {
    this.collection = new Binding().expression(collectionExpression);
    return this;
  }
}
