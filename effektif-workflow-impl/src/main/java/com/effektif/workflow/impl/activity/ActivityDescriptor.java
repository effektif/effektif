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
package com.effektif.workflow.impl.activity;

import java.util.HashMap;
import java.util.Map;

import com.effektif.workflow.api.types.TextType;
import com.effektif.workflow.api.types.Type;


public class ActivityDescriptor {

  protected String activityKey;
  protected String label;
  protected String description;

  protected Map<String,InputParameter> inputParameters;
  protected Map<String,OutputParameter> outputParameters;

  public ActivityDescriptor inputParameterString(String inputParameterKey, String inputParameterLabel) {
    inputParameter(inputParameterKey, inputParameterLabel, new TextType());
    return this;
  }

  public ActivityDescriptor inputParameter(String inputParameterKey, String inputParameterLabel, Type type) {
    inputParameter(new InputParameter()
      .key(inputParameterKey)
      .label(inputParameterLabel)
      .type(type)
    );
    return this;
  }

  public ActivityDescriptor inputParameter(InputParameter parameter) {
    if (inputParameters==null) {
      inputParameters = new HashMap<>();
    }
    inputParameters.put(parameter.key, parameter);
    return this;
  }

  public ActivityDescriptor outputParameterString(String outputParameterKey, String outputParameterLabel) {
    outputParameter(outputParameterKey, outputParameterLabel, new TextType());
    return this;
  }

  public ActivityDescriptor outputParameter(String outputParameterKey, String outputParameterLabel, Type type) {
    outputParameter(new OutputParameter()
      .key(outputParameterKey)
      .label(outputParameterLabel)
      .type(type)
    );
    return this;
  }
  
  public ActivityDescriptor outputParameter(OutputParameter outputParameter) {
    if (outputParameters==null) {
      outputParameters = new HashMap<>();
    }
    outputParameters.put(outputParameter.key, outputParameter);
    return this;
  }

  public String getActivityKey() {
    return this.activityKey;
  }
  public void setActivityKey(String activityKey) {
    this.activityKey = activityKey;
  }
  public ActivityDescriptor activityKey(String activityKey) {
    this.activityKey = activityKey;
    return this;
  }

  public String getLabel() {
    return this.label;
  }
  public void setLabel(String label) {
    this.label = label;
  }
  public ActivityDescriptor label(String label) {
    this.label = label;
    return this;
  }

  public String getDescription() {
    return this.description;
  }
  public void setDescription(String description) {
    this.description = description;
  }
  public ActivityDescriptor description(String description) {
    this.description = description;
    return this;
  }
  
  public Map<String, InputParameter> getInputParameters() {
    return inputParameters;
  }

  
  public void setInputParameters(Map<String, InputParameter> inputParameters) {
    this.inputParameters = inputParameters;
  }

  
  public Map<String, OutputParameter> getOutputParameters() {
    return outputParameters;
  }

  
  public void setOutputParameters(Map<String, OutputParameter> outputParameters) {
    this.outputParameters = outputParameters;
  }
}
