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

import com.effektif.workflow.api.types.Type;


/**
 * @author Tom Baeyens
 */
public class Parameter {

  protected String key;
  protected Type type;
  protected String label;
  protected String description;

  public String getDescription() {
    return this.description;
  }
  public void setDescription(String description) {
    this.description = description;
  }
  public Parameter description(String description) {
    this.description = description;
    return this;
  }

  public String getKey() {
    return this.key;
  }
  public void setKey(String key) {
    this.key = key;
  }
  public Parameter key(String key) {
    this.key = key;
    return this;
  }

  public Type getType() {
    return this.type;
  }
  public void setType(Type type) {
    this.type = type;
  }
  public Parameter type(Type type) {
    this.type = type;
    return this;
  }

  public String getLabel() {
    return this.label;
  }
  public void setLabel(String label) {
    this.label = label;
  }
  public Parameter label(String label) {
    this.label = label;
    return this;
  }
}
