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
package com.effektif.workflow.impl.activity.types;

import java.util.List;

import com.effektif.workflow.api.activities.MappableActivity;
import com.effektif.workflow.impl.WorkflowParser;
import com.effektif.workflow.impl.activity.AbstractActivityType;
import com.effektif.workflow.impl.workflow.ActivityImpl;


public abstract class MappableActivityImpl<T extends MappableActivity> extends AbstractActivityType<T> {

  protected List<MappingImpl> inputMappings;
  protected List<MappingImpl> outputMappings;

  public MappableActivityImpl(Class< ? > apiClass) {
    super(apiClass);
  }

  @Override
  public void parse(ActivityImpl activityImpl, T mappableActivity, WorkflowParser parser) {
    super.parse(activityImpl, mappableActivity, parser);
    inputMappings = parser.parseMappings(mappableActivity.getInputMappings(), mappableActivity, "inputMappings");
    outputMappings = parser.parseMappings(mappableActivity.getOutputMappings(), mappableActivity, "outputMappings");
  }

  
  public List<MappingImpl> getInputMappings() {
    return inputMappings;
  }

  
  public void setInputMappings(List<MappingImpl> inputMappings) {
    this.inputMappings = inputMappings;
  }

  
  public List<MappingImpl> getOutputMappings() {
    return outputMappings;
  }

  
  public void setOutputMappings(List<MappingImpl> outputMappings) {
    this.outputMappings = outputMappings;
  }
}
