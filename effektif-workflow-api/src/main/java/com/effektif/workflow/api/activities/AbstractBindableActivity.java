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

import com.effektif.workflow.api.json.JsonReader;
import com.effektif.workflow.api.json.JsonWriter;
import com.effektif.workflow.api.workflow.Activity;
import com.effektif.workflow.api.workflow.Binding;
import com.effektif.workflow.api.workflow.Transition;


/**
 * @author Tom Baeyens
 */
public class AbstractBindableActivity extends Activity {

  protected Map<String,Binding> inputBindings; 
  protected Map<String,String> outputBindings;
  
  @Override
  public void writeFields(JsonWriter w) {
    super.writeFields(w);
    w.writeMap("inputBindings", inputBindings);
    w.writeMap("outputBindings", outputBindings);
  }
  
  @Override
  public void readFields(JsonReader r) {
    inputBindings = r.readMap("inputBindings", Binding.class);
    outputBindings = r.readMap("outputBindings", String.class);
    super.readFields(r);
  }
  
  /** copies the static value to the adapter activity when it is invoked,
   * This method uses reflection and the data type service to find the type form the value. */
  public AbstractBindableActivity inputValue(String key, Object value) {
    addInputBinding(key, new Binding().value(value));
    return this;
  }
  
  /** copies the variable from this workflow to the adapter activity when it is invoked */
  public AbstractBindableActivity inputExpression(String key, String expression) {
    addInputBinding(key, new Binding().expression(expression));
    return this;
  }

  /** copies the value specified in the binding from this workflow to the adapter activity when it is invoked */
  protected AbstractBindableActivity addInputBinding(String key, Binding binding) {
    if (inputBindings==null) {
      inputBindings = new HashMap<>();
    }
    inputBindings.put(key, binding);
    return this;
  }

  /** copies the adapter output value into a variable of this workflow when the activity is finished */
  public AbstractBindableActivity outputBinding(String key, String variableId) {
    if (outputBindings==null) {
      outputBindings = new HashMap<>();
    }
    outputBindings.put(key, variableId);
    return this;
  }
  
  public Map<String, Binding> getInputBindings() {
    return inputBindings;
  }

  
  public void setInputBindings(Map<String, Binding> inputBindings) {
    this.inputBindings = inputBindings;
  }

  
  public Map<String, String> getOutputBindings() {
    return outputBindings;
  }

  
  public void setOutputBindings(Map<String, String> outputBindings) {
    this.outputBindings = outputBindings;
  }
}
