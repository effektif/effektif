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

import java.util.HashMap;
import java.util.Map;

import com.effektif.workflow.api.type.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;


@JsonTypeInfo(use=Id.NAME, include=As.PROPERTY, property="type")
public class Variable {
  
  protected String id;
  protected Type type;
  protected Map<String,Object> properties;

  public Variable() {
  }

  public Variable(String id, Type type) {
    this.id = id;
    this.type = type;
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

  public Map<String,Object> getProperties() {
    return this.properties;
  }
  public void setProperties(Map<String,Object> properties) {
    this.properties = properties;
  }
  
  public Variable property(String key,Object value) {
    if (properties==null) {
      properties = new HashMap<>();
    }
    this.properties.put(key, value);
    return this;
  }
}
