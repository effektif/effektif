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
import com.effektif.workflow.api.workflow.MultiInstance;
import com.effektif.workflow.api.workflow.Timer;
import com.effektif.workflow.api.workflow.Transition;
import com.effektif.workflow.api.workflow.Variable;
import com.fasterxml.jackson.annotation.JsonTypeName;


/** delegates to an activity that is running on an external adapter server */
@JsonTypeName("adapterActivity")
public class AdapterActivity extends Activity {

  protected String adapterId;
  protected String activityKey;
  protected Map<String,String> inputMappings; 
  protected Map<String,TypedValue> inputMappingValues; 
  protected Map<String,String> outputMappings; 

  public String getAdapterId() {
    return this.adapterId;
  }
  public void setAdapterId(String adapterId) {
    this.adapterId = adapterId;
  }
  public AdapterActivity adapterId(String adapterId) {
    this.adapterId = adapterId;
    return this;
  }
  
  public String getActivityKey() {
    return this.activityKey;
  }
  public void setActivityKey(String activityKey) {
    this.activityKey = activityKey;
  }
  public AdapterActivity activityKey(String activityKey) {
    this.activityKey = activityKey;
    return this;
  }
  
  /** copies the variable from this workflow to the adapter activity when it is invoked */
  public AdapterActivity inputMapping(String adapterKey, String variableId) {
    if (inputMappings==null) {
      inputMappings = new HashMap<>();
    }
    inputMappings.put(adapterKey, variableId);
    return this;
  }

  /** copies the static value to the adapter activity when it is invoked,
   * This method uses reflection and the data type service to find the type form the value. */
  public AdapterActivity inputMappingValue(String adapterKey, Object value) {
    inputMappingValue(adapterKey, new TypedValue().value(value));
    return this;
  }

  /** copies the static typed value to the adapter activity when it is invoked */
  public AdapterActivity inputMappingValue(String adapterKey, TypedValue typedValue) {
    if (inputMappingValues==null) {
      inputMappingValues = new HashMap<>();
    }
    inputMappingValues.put(adapterKey, typedValue);
    return this;
  }

  /** copies the adapter output value into a variable of this workflow when the activity is finished */
  public AdapterActivity outputMapping(String variableId, String adapterKey) {
    if (outputMappings==null) {
      outputMappings = new HashMap<>();
    }
    outputMappings.put(variableId, adapterKey);
    return this;
  }
  
  public Map<String, String> getInputMappings() {
    return inputMappings;
  }

  
  public void setInputMappings(Map<String, String> inputMappings) {
    this.inputMappings = inputMappings;
  }

  
  public Map<String, String> getOutputMappings() {
    return outputMappings;
  }

  
  public void setOutputMappings(Map<String, String> outputMappings) {
    this.outputMappings = outputMappings;
  }

  
  public Map<String, TypedValue> getInputMappingValues() {
    return inputMappingValues;
  }

  
  public void setInputMappingValues(Map<String, TypedValue> inputMappingValues) {
    this.inputMappingValues = inputMappingValues;
  }

  @Override
  public AdapterActivity multiInstance(MultiInstance multiInstance) {
    super.multiInstance(multiInstance);
    return this;
  }

  @Override
  public AdapterActivity transitionTo(String toActivityId) {
    super.transitionTo(toActivityId);
    return this;
  }

  @Override
  public AdapterActivity transitionTo(Transition transition) {
    super.transitionTo(transition);
    return this;
  }

  @Override
  public AdapterActivity activity(Activity activity) {
    super.activity(activity);
    return this;
  }

  @Override
  public AdapterActivity transition(Transition transition) {
    super.transition(transition);
    return this;
  }

  @Override
  public AdapterActivity variable(Variable variable) {
    super.variable(variable);
    return this;
  }

  @Override
  public AdapterActivity timer(Timer timer) {
    super.timer(timer);
    return this;
  }

  @Override
  public AdapterActivity id(String id) {
    super.id(id);
    return this;
  }

  @Override
  public AdapterActivity property(String key, Object value) {
    super.property(key, value);
    return this;
  }
}
