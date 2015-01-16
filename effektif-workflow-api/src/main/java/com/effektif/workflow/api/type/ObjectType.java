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
package com.effektif.workflow.api.type;

import java.util.ArrayList;
import java.util.List;


public class ObjectType extends Type {

  protected List<ObjectField> fields;
  protected String label;
  protected String description;

  public ObjectType() {
  }
  public ObjectType(Class apiClass) {
    super(apiClass);
  }
  
  public String getLabel() {
    return this.label;
  }
  public void setLabel(String label) {
    this.label = label;
  }
  public ObjectType label(String label) {
    this.label = label;
    return this;
  }

  public String getDescription() {
    return this.description;
  }
  public void setDescription(String description) {
    this.description = description;
  }
  public ObjectType description(String description) {
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

  @Override
  public ObjectType apiClass(Class< ? > apiClass) {
    super.apiClass(apiClass);
    return this;
  }
}
