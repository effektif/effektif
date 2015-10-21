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
import com.effektif.workflow.api.types.ListType;
import com.effektif.workflow.api.workflow.Extensible;

/**
 * @author Tom Baeyens
 */
public class ParameterDescriptor extends Extensible {

  protected String key;
  protected DataType type;
  protected String name;
  protected String description;
  protected String autoBindKey;

  public boolean isList() {
    return type!=null && ListType.class.isAssignableFrom(type.getClass());
  }

  public String getDescription() {
    return this.description;
  }
  public void setDescription(String description) {
    this.description = description;
  }
  public ParameterDescriptor description(String description) {
    this.description = description;
    return this;
  }

  public String getKey() {
    return this.key;
  }
  public void setKey(String key) {
    this.key = key;
  }
  public ParameterDescriptor key(String key) {
    this.key = key;
    return this;
  }

  public DataType getType() {
    return this.type;
  }
  public void setType(DataType type) {
    this.type = type;
  }
  public ParameterDescriptor type(DataType type) {
    this.type = type;
    return this;
  }

  public String getName() {
    return this.name;
  }
  public void setName(String name) {
    this.name = name;
  }
  public ParameterDescriptor name(String name) {
    this.name = name;
    return this;
  }

  public String getAutoBindKey() {
    return this.autoBindKey;
  }
  public void setAutoBindKey(String autoBindKey) {
    this.autoBindKey = autoBindKey;
  }
  public ParameterDescriptor autoBindKey(String autoBindKey) {
    this.autoBindKey = autoBindKey;
    return this;
  }
}
