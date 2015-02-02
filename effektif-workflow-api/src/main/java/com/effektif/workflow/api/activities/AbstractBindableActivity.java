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

import com.effektif.workflow.api.command.TypedValue;
import com.effektif.workflow.api.workflow.Activity;
import com.effektif.workflow.api.workflow.Binding;


public class AbstractBindableActivity extends Activity {

  protected Map<String,Binding> inputBindings; 
  protected Map<String,String> outputBindings;
  
  public AbstractBindableActivity() {
  }

  public AbstractBindableActivity(String id) {
    super(id);
  }

  /** copies the static value to the adapter activity when it is invoked,
   * This method uses reflection and the data type service to find the type form the value. */
  public AbstractBindableActivity inputValue(String key, Object value) {
    inputBinding(key, new Binding().value(value));
    return this;
  }
  
  /** copies the static typed value to the adapter activity when it is invoked */
  public AbstractBindableActivity inputValue(String key, TypedValue typedValue) {
    inputBinding(key, new Binding().typedValue(typedValue));
    return this;
  }

  /** copies the variable from this workflow to the adapter activity when it is invoked */
  public AbstractBindableActivity inputVariable(String key, String variableId) {
    inputBinding(key, new Binding().variableId(variableId));
    return this;
  }

  /** copies the value from a field inside a variable from this workflow to the adapter activity when it is invoked.
   * @param variableField is a . separated notation that starts with the variableId and then 
   * specifies the fields to be dereferenced 
   * eg "myVariableId.variableField.nestedField" */
  public AbstractBindableActivity inputField(String key, String variableField) {
    inputBinding(key, new Binding().variableField(variableField));
    return this;
  }

  /** copies the result of the expression from this workflow to the adapter activity when it is invoked */
  public AbstractBindableActivity inputExpression(String key, String variableField) {
    inputBinding(key, new Binding().variableField(variableField));
    return this;
  }

  /** copies the value specified in the binding from this workflow to the adapter activity when it is invoked */
  public AbstractBindableActivity inputBinding(String key, Binding binding) {
    if (inputBindings==null) {
      inputBindings = new HashMap<>();
    }
    inputBindings.put(key, binding);
    return this;
  }

  /** copies the adapter output value into a variable of this workflow when the activity is finished */
  public AbstractBindableActivity outputBinding(String variableId, String key) {
    if (outputBindings==null) {
      outputBindings = new HashMap<>();
    }
    outputBindings.put(variableId, key);
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
