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
package com.effektif.workflow.api.form;

import java.util.HashMap;
import java.util.Map;

import com.effektif.workflow.api.model.TypedValue;


public class FormInstance extends Form {

  protected Map<String,TypedValue> values;

  public Map<String,TypedValue> getValues() {
    return this.values;
  }
  public void setValues(Map<String,TypedValue> values) {
    this.values = values;
  }
  public FormInstance values(Map<String,TypedValue> values) {
    this.values = values;
    return this;
  }
  public FormInstance value(String key, TypedValue value) {
    if (this.values==null) {
      this.values = new HashMap<>();
    }
    this.values.put(key, value);
    return this;
  }
}
