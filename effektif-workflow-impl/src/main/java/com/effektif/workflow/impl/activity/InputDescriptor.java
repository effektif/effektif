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
package com.effektif.workflow.impl.activity;

import com.effektif.workflow.api.types.DataType;


/** describes an input parameter of an activity. 
 * 
 * @author Tom Baeyens
 */
public class InputDescriptor extends ParameterDescriptor {
  
  protected Boolean required;

  public Boolean getRequired() {
    return this.required;
  }
  public void setRequired(Boolean required) {
    this.required = required;
  }
  public boolean isRequired() {
    return Boolean.TRUE.equals(required);
  }
  public InputDescriptor required() {
    this.required = true;
    return this;
  }

  @Override
  public InputDescriptor key(String key) {
    super.key(key);
    return this;
  }
  @Override
  public InputDescriptor type(DataType type) {
    super.type(type);
    return this;
  }
  @Override
  public InputDescriptor name(String name) {
    super.name(name);
    return this;
  }
  @Override
  public InputDescriptor description(String description) {
    super.description(description);
    return this;
  }
  
  @Override
  public InputDescriptor autoBindKey(String autoBindKey) {
    super.autoBindKey(autoBindKey);
    return this;
  }
}
