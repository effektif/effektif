/* Copyright 2014 Effektif GmbH.
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
package com.effektif.workflow.api.command;

import java.util.LinkedHashMap;
import java.util.Map;

import com.effektif.workflow.api.types.Type;


public class Start {
  
  protected String workflowId;
  protected String workflowName;
  protected Map<String,TypedValue> variableValues;
  protected Map<String,TypedValue> triggerValues;

  public String getWorkflowId() {
    return this.workflowId;
  }
  public void setWorkflowId(String workflowId) {
    this.workflowId = workflowId;
  }
  public Start workflowId(String workflowId) {
    this.workflowId = workflowId;
    return this;
  }
  
  public String getWorkflowName() {
    return this.workflowName;
  }
  public void setWorkflowName(String workflowName) {
    this.workflowName = workflowName;
  }
  public Start workflowName(String workflowName) {
    this.workflowName = workflowName;
    return this;
  }

  public Map<String,TypedValue> getVariableValues() {
    return this.variableValues;
  }

  public void setVariableValues(Map<String,TypedValue> variableValues) {
    this.variableValues = variableValues;
  }

  public Start variableValue(String variableId, Object variableValue) {
    variableValue(variableId, variableValue, null);
    return this;
  }

  public Start variableValue(String variableId, Object variableValue, Type type) {
    if (variableValues==null) {
      variableValues = new LinkedHashMap<>();
    }
    variableValues.put(variableId, new TypedValue()
      .value(variableValue)
      .type(type));
    return this;
  }

  public Map<String,TypedValue> getTriggerValues() {
    return this.triggerValues;
  }

  public void setTriggerValues(Map<String,TypedValue> triggerValues) {
    this.triggerValues = triggerValues;
  }

  public Start triggerValue(String triggerKey, Object triggerValue) {
    triggerValue(triggerKey, triggerValue, null);
    return this;
  }

  public Start triggerValue(String triggerKey, Object triggerValue, Type type) {
    if (triggerValues==null) {
      triggerValues = new LinkedHashMap<>();
    }
    triggerValues.put(triggerKey, new TypedValue()
      .value(triggerValue)
      .type(type));
    return this;
  }
}
