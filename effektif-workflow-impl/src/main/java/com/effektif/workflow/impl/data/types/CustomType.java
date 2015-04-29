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
package com.effektif.workflow.impl.data.types;

import java.util.ArrayList;
import java.util.List;

import com.effektif.workflow.api.json.TypeName;


/** this is just an idea for now. moves to api when mature.
 * 
 * represents a json object type that internally is parsed to a hash map. 
 * 
 * @author Tom Baeyens
 */
@TypeName("custom")
public class CustomType extends ObjectType {

  protected String id;
  protected String key;
  protected String organizationId;
  protected String label;
  protected String description;
  protected List<ObjectField> fields;

  public String getOrganizationId() {
    return this.organizationId;
  }
  public void setOrganizationId(String organizationId) {
    this.organizationId = organizationId;
  }
  public CustomType organizationId(String organizationId) {
    this.organizationId = organizationId;
    return this;
  }

  public String getId() {
    return this.id;
  }
  public void setId(String id) {
    this.id = id;
  }
  public CustomType id(String id) {
    this.id = id;
    return this;
  }

  public String getKey() {
    return this.key;
  }
  public void setKey(String key) {
    this.key = key;
  }
  public CustomType key(String key) {
    this.key = key;
    return this;
  }

  public String getLabel() {
    return this.label;
  }
  public void setLabel(String label) {
    this.label = label;
  }
  public CustomType label(String label) {
    this.label = label;
    return this;
  }

  public String getDescription() {
    return this.description;
  }
  public void setDescription(String description) {
    this.description = description;
  }
  public CustomType description(String description) {
    this.description = description;
    return this;
  }

  public List<ObjectField> getFields() {
    return this.fields;
  }
  public void setFields(List<ObjectField> fields) {
    this.fields = fields;
  }
  public ObjectType fields(List<ObjectField> fields) {
    this.fields = fields;
    return this;
  }

  public ObjectType field(ObjectField field) {
    if (fields==null) {
      fields = new ArrayList<>();
    }
    fields.add(field);
    return this;
  }
}
