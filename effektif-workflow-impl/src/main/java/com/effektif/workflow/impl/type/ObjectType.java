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
package com.effektif.workflow.impl.type;

import java.util.ArrayList;
import java.util.List;


public class ObjectType extends AbstractDataType {
  
  protected String label;
  protected String description;
  protected Class<?> apiClass;
  protected List<ObjectField> fields;

  public ObjectType() {
    super(null);
  }

  @Override
  public Object convertJsonToInternalValue(Object jsonValue) throws InvalidValueException {
    return null;
  }

  public List<ObjectField> getFields() {
    return this.fields;
  }
  public void setFields(List<ObjectField> fields) {
    this.fields = fields;
  }
  public ObjectType field(ObjectField field) {
    if (this.fields==null) {
      this.fields = new ArrayList<>();
    }
    this.fields.add(field);
    return this;
  }

  public Class<?> getApiClass() {
    return this.apiClass;
  }
  public void setApiClass(Class<?> apiClass) {
    this.apiClass = apiClass;
  }
  public ObjectType apiClass(Class<?> apiClass) {
    this.apiClass = apiClass;
    return this;
  }
}
