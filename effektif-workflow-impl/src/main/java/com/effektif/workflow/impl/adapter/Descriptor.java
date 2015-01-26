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
package com.effektif.workflow.impl.adapter;

import com.effektif.workflow.api.types.ObjectField;
import com.effektif.workflow.api.types.ObjectType;
import com.fasterxml.jackson.annotation.JsonTypeName;


@JsonTypeName("descriptor")
public class Descriptor extends ObjectType {
  
  /** identifies the activity within the scope of an adapter */
  protected String key;

  public String getKey() {
    return this.key;
  }
  public void setKey(String key) {
    this.key = key;
  }
  /** identifies the activity within the scope of an adapter */
  public Descriptor key(String key) {
    this.key = key;
    return this;
  }

  @Override
  public Descriptor label(String label) {
    super.label(label);
    return this;
  }

  @Override
  public Descriptor description(String description) {
    super.description(description);
    return this;
  }

  @Override
  public Descriptor field(ObjectField field) {
    super.field(field);
    return this;
  }
}
