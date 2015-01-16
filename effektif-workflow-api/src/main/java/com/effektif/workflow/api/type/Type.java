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

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;


@JsonTypeInfo(use=Id.NAME, include=As.PROPERTY, property="type")
public class Type {
  
  /** used during deserialization of api models */
  protected Class<?> apiClass;

  public Type() {
  }
  public Type(Class< ? > apiClass) {
    this.apiClass = apiClass;
  }

  public Class< ? > getApiClass() {
    return apiClass;
  }
  public void setApiClass(Class< ? > apiClass) {
    this.apiClass = apiClass;
  }
  public Type apiClass(Class<?> apiClass) {
    this.apiClass = apiClass;
    return this;
  }
}
