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
package com.effektif.workflow.api.types;


/**
 * @author Tom Baeyens
 */
public class ObjectField {
  
  protected String label;
  protected String name;
  protected String description;
  protected Type type;

  public ObjectField() {
  }

  public ObjectField(String name) {
    this.name = name;
  }
  
  public String getLabel() {
    return this.label;
  }
  public void setLabel(String label) {
    this.label = label;
  }
  public ObjectField label(String label) {
    this.label = label;
    return this;
  }
  
  public String getDescription() {
    return this.description;
  }
  public void setDescription(String description) {
    this.description = description;
  }
  public ObjectField description(String description) {
    this.description = description;
    return this;
  }

  public String getName() {
    return this.name;
  }
  public void setName(String key) {
    this.name = key;
  }
  public ObjectField name(String name) {
    this.name = name;
    return this;
  }
  
  public Type getType() {
    return this.type;
  }
  public void setType(Type type) {
    this.type = type;
  }
  public ObjectField type(Type type) {
    this.type = type;
    return this;
  }

}
