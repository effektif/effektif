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
package com.effektif.workflow.impl.activity;

import com.effektif.workflow.api.types.Type;


public class InputParameter<T> extends Parameter {
  
  protected Boolean required;
  protected Boolean disableValue;
  protected Boolean disableVariable;
  protected Boolean disableExpression;
  

  public Boolean getDisableExpression() {
    return this.disableExpression;
  }
  public void setDisableExpression(Boolean disableExpression) {
    this.disableExpression = disableExpression;
  }
  public InputParameter disableExpression() {
    this.disableExpression = true;
    return this;
  }

  public Boolean getDisableVariable() {
    return this.disableVariable;
  }
  public void setDisableVariable(Boolean disableVariable) {
    this.disableVariable = disableVariable;
  }
  public InputParameter disableVariable() {
    this.disableVariable = true;
    return this;
  }

  public Boolean getDisableValue() {
    return this.disableValue;
  }
  public void setDisableValue(Boolean disableValue) {
    this.disableValue = disableValue;
  }
  public InputParameter disableValue() {
    this.disableValue = true;
    return this;
  }

  public Boolean getRequired() {
    return this.required;
  }
  public void setRequired(Boolean required) {
    this.required = required;
  }
  public InputParameter required(Boolean required) {
    this.required = required;
    return this;
  }
  
  @Override
  public InputParameter key(String key) {
    super.key(key);
    return this;
  }
  @Override
  public InputParameter type(Type type) {
    super.type(type);
    return this;
  }
  @Override
  public InputParameter label(String label) {
    super.label(label);
    return this;
  }
  @Override
  public InputParameter description(String description) {
    super.description(description);
    return this;
  }
  public boolean isRequired() {
    return Boolean.TRUE.equals(required);
  }
}
