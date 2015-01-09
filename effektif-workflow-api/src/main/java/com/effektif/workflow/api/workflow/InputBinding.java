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
package com.effektif.workflow.api.workflow;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;


/** Describes how the value is obtained 
 * for an activity input parameter. */
@JsonTypeInfo(use=Id.NAME, include=As.PROPERTY, property="type")
public class InputBinding {

  /** the reference used by the activity implementation to retrieve the value */
  protected String key;
  
  public InputBinding() {
  }

  public InputBinding(String key) {
    this.key = key;
  }
  
  public String getKey() {
    return this.key;
  }
  public void setKey(String key) {
    this.key = key;
  }
  /** the reference used by the activity implementation to retrieve the value */
  public InputBinding key(String key) {
    this.key = key;
    return this;
  }
}
