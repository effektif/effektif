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
package com.effektif.workflow.impl.adapter;

import java.util.ArrayList;
import java.util.List;

import com.effektif.workflow.api.types.TextType;
import com.effektif.workflow.api.types.Type;


public class ActivityDescriptor {

  protected String activityKey;
  protected List<Parameter> inputParameters;
  protected List<Parameter> outputParameters;

  public ActivityDescriptor activityKey(String activityKey) {
    this.activityKey = activityKey;
    return this;
  }

  public ActivityDescriptor inputParameterString(String inputParameterKey, String inputParameterLabel) {
    inputParameter(inputParameterKey, inputParameterLabel, new TextType());
    return this;
  }

  public ActivityDescriptor inputParameter(String inputParameterKey, String inputParameterLabel, Type type) {
    if (inputParameters==null) {
      inputParameters = new ArrayList<>();
    }
    inputParameters.add(new Parameter()
      .key(inputParameterKey)
      .label(inputParameterLabel)
      .type(type)
    );
    return this;
  }

  public ActivityDescriptor outputParameterString(String outputParameterKey, String outputParameterLabel) {
    outputParameter(outputParameterKey, outputParameterLabel, new TextType());
    return this;
  }

  public ActivityDescriptor outputParameter(String outputParameterKey, String outputParameterLabel, Type type) {
    if (outputParameters==null) {
      outputParameters = new ArrayList<>();
    }
    outputParameters.add(new Parameter()
      .key(outputParameterKey)
      .label(outputParameterLabel)
      .type(type)
    );
    return this;
  }

  public String getActivityKey() {
    return this.activityKey;
  }
  public void setActivityKey(String activityKey) {
    this.activityKey = activityKey;
  }
  
  public List<Parameter> getInputParameters() {
    return this.inputParameters;
  }
  public void setInputParameters(List<Parameter> inputParameters) {
    this.inputParameters = inputParameters;
  }
  
  public List<Parameter> getOutputParameters() {
    return this.outputParameters;
  }
  public void setOutputParameters(List<Parameter> outputParameters) {
    this.outputParameters = outputParameters;
  }
  
}
