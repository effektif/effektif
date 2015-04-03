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

import com.effektif.workflow.api.mapper.Writer;
import com.effektif.workflow.api.types.Type;


/**
 * @author Tom Baeyens
 */
public class Variable extends Element {
  
  protected String id;
  protected Type type;

  public Variable() {
  }
  
  @Override
  public void writeFields(Writer w) {
  }

  public String getId() {
    return this.id;
  }
  public void setId(String id) {
    this.id = id;
  }
  public Variable id(String id) {
    this.id = id;
    return this;
  }

  public Type getType() {
    return this.type;
  }
  public void setType(Type type) {
    this.type = type;
  }
  public Variable type(Type type) {
    this.type = type;
    return this;
  }

  @Override
  public Variable name(String name) {
    super.name(name);
    return this;
  }

  @Override
  public Variable description(String description) {
    super.description(description);
    return this;
  }

  @Override
  public Variable property(String key, Object value) {
    super.property(key, value);
    return this;
  }

  @Override
  public Variable propertyOpt(String key, Object value) {
    super.propertyOpt(key, value);
    return this;
  }
}
