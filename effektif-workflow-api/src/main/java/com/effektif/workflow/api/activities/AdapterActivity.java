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

import java.util.Map;

import com.effektif.workflow.api.workflow.Activity;
import com.effektif.workflow.api.workflow.Binding;


public class AdapterActivity extends Activity {

  protected String adapterType;
  protected Map<String,Binding> configuration;

  public String getAdapterType() {
    return this.adapterType;
  }
  public void setAdapterType(String adapterType) {
    this.adapterType = adapterType;
  }
  public AdapterActivity adapterType(String adapterType) {
    this.adapterType = adapterType;
    return this;
  }
  
  public Map<String,Binding> getConfiguration() {
    return this.configuration;
  }
  public void setConfiguration(Map<String,Binding> configuration) {
    this.configuration = configuration;
  }
  public AdapterActivity configuration(Map<String,Binding> configuration) {
    this.configuration = configuration;
    return this;
  }
}
