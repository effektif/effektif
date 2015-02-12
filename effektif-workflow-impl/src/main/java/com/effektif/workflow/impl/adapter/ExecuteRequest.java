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
package com.effektif.workflow.impl.adapter;

import java.util.HashMap;
import java.util.Map;

import com.effektif.workflow.api.model.TypedValue;


public class ExecuteRequest {
  
  // private static final Logger log = LoggerFactory.getLogger(ExecuteRequest.class);

  protected String activityKey;
  protected String workflowInstanceId;
  protected String activityInstanceId;
  protected Map<String,TypedValue> inputParameters;
  
  public String getActivityKey() {
    return this.activityKey;
  }
  public void setActivityKey(String activityKey) {
    this.activityKey = activityKey;
  }
  public ExecuteRequest activityKey(String activityKey) {
    this.activityKey = activityKey;
    return this;
  }

  public String getWorkflowInstanceId() {
    return this.workflowInstanceId;
  }
  public void setWorkflowInstanceId(String workflowInstanceId) {
    this.workflowInstanceId = workflowInstanceId;
  }
  public ExecuteRequest workflowInstanceId(String workflowInstanceId) {
    this.workflowInstanceId = workflowInstanceId;
    return this;
  }

  public String getActivityInstanceId() {
    return this.activityInstanceId;
  }
  public void setActivityInstanceId(String activityInstanceId) {
    this.activityInstanceId = activityInstanceId;
  }
  public ExecuteRequest activityInstanceId(String activityInstanceId) {
    this.activityInstanceId = activityInstanceId;
    return this;
  }

  public Map<String, TypedValue> getInputParameters() {
    return inputParameters;
  }
  
  public void setInputParameters(Map<String, TypedValue> inputParameters) {
    this.inputParameters = inputParameters;
  }
  public void inputParameter(String parameterKey, TypedValue typedValue) {
    if (inputParameters==null) {
      inputParameters = new HashMap<>();
    }
    inputParameters.put(parameterKey, typedValue);
  }

  public Object getInputParameterValue(String inputParameterKey) {
    TypedValue typedValue = inputParameters!=null ? inputParameters.get(inputParameterKey) : null;
    return typedValue!=null ? typedValue.getValue() : null;
  }
}
