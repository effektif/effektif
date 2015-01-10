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
package com.effektif.workflow.impl.tooling;

import java.util.ArrayList;
import java.util.List;


public class FieldTypeObject extends FieldType {

  protected List<ConfigurationField> fields;

  public List<ConfigurationField> getFields() {
    return this.fields;
  }
  public void setFields(List<ConfigurationField> fields) {
    this.fields = fields;
  }
  public FieldTypeObject fields(List<ConfigurationField> fields) {
    this.fields = fields;
    return this;
  }

  public FieldTypeObject field(ConfigurationField field) {
    if (fields==null) {
      fields = new ArrayList<>();
    }
    fields.add(field);
    return this;
  }
}
