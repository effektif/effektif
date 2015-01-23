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
package com.effektif.workflow.api.activities;

import java.util.HashMap;
import java.util.Map;

import com.effektif.workflow.api.workflow.Activity;


public class AdapterActivity extends Activity {

  protected String adapterConnectionId;
  protected Map<String,Object> configuration;

  public String getAdapterConnectionId() {
    return this.adapterConnectionId;
  }
  public void setAdapterConnectionId(String adapterConnectionId) {
    this.adapterConnectionId = adapterConnectionId;
  }
  public AdapterActivity adapterConnectionId(String adapterConnectionId) {
    this.adapterConnectionId = adapterConnectionId;
    return this;
  }
  
  public Map<String,Object> getConfiguration() {
    return this.configuration;
  }
  public void setConfiguration(Map<String,Object> configuration) {
    this.configuration = configuration;
  }

  public AdapterActivity configurationValue(String key, Object value) {
    if (configuration==null) {
      configuration = new HashMap<>();
    }
    configuration.put(key, value);
    return this;
  }
}
