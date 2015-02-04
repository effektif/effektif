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
package com.effektif.workflow.api.types;

import com.fasterxml.jackson.annotation.JsonTypeName;


/** represents a json object type that internally is parsed to a hash map. */
@JsonTypeName("custom")
public class CustomType extends ObjectType {

  protected String id;
  protected String key;
  protected String organizationId;

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

  @Override
  public CustomType field(ObjectField field) {
    super.field(field);
    return this;
  }
  @Override
  public CustomType label(String label) {
    super.label(label);
    return this;
  }
  @Override
  public CustomType description(String description) {
    super.description(description);
    return this;
  }
}
