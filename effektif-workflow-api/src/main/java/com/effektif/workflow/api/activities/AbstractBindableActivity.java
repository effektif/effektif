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
package com.effektif.workflow.api.activities;

import java.util.HashMap;
import java.util.Map;

import com.effektif.workflow.api.workflow.Activity;
import com.effektif.workflow.api.workflow.Binding;
import com.effektif.workflow.api.workflow.InputParameter;

/**
 * An activity that supports input and output bindings. Input bindings expose workflow variables as input to the
 * activity. Output bindings specify variables from the activity that will update workflow variables.
 *
 * The input and output bindings’ data is delegated to the activity input and output parameter.
 *
 * @author Tom Baeyens
 */
public class AbstractBindableActivity extends Activity {

  public Map<String, Binding> getInputBindings() {
    if (inputs == null) {
      return null;
    }
    Map<String,Binding> bindings = new HashMap<>();
    for (Map.Entry<String, InputParameter> parameter : inputs.entrySet()) {
      Binding<?> binding = parameter.getValue().getBinding();
      if (binding != null) {
        bindings.put(parameter.getKey(), binding);
      }
    }
    return bindings;
  }

  public Map<String, String> getOutputBindings() {
    if (outputs == null) {
      return null;
    }
    Map<String,String> bindings = new HashMap<>();
    for (Map.Entry<String, String> parameter : outputs.entrySet()) {
      bindings.put(parameter.getKey(), parameter.getValue());
    }
    return bindings;
  }
}
