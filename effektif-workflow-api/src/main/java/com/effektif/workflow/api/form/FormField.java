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
package com.effektif.workflow.api.form;

import com.effektif.workflow.api.types.Type;


/**
 * @author Tom Baeyens
 */
public class FormField {

  protected String key;
  protected String name;
  protected Boolean readOnly;
  protected Boolean required;
  protected Type type;

  public String getKey() {
    return this.key;
  }
  public void setKey(String key) {
    this.key = key;
  }
  public FormField key(String key) {
    this.key = key;
    return this;
  }

  public String getName() {
    return this.name;
  }
  public void setName(String name) {
    this.name = name;
  }
  public FormField name(String name) {
    this.name = name;
    return this;
  }

  public Boolean getReadOnly() {
    return this.readOnly;
  }
  public void setReadOnly(Boolean readOnly) {
    this.readOnly = readOnly;
  }
  public FormField readOnly() {
    this.readOnly = true;
    return this;
  }
  
  public Boolean getRequired() {
    return this.required;
  }
  public void setRequired(Boolean required) {
    this.required = required;
  }
  public FormField required() {
    this.required = true;
    return this;
  }
  
  public Type getType() {
    return this.type;
  }
  public void setType(Type type) {
    this.type = type;
  }
  public FormField type(Type type) {
    this.type = type;
    return this;
  }
}
