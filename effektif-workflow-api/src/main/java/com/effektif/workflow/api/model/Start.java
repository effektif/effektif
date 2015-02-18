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
package com.effektif.workflow.api.model;

import java.util.LinkedHashMap;
import java.util.Map;

import com.effektif.workflow.api.workflow.Workflow;


public class Start {
  
  protected String workflowId;
  protected String sourceWorkflowId;
  protected String businessKey;
  protected Map<String,Object> variableValues;
  protected Map<String,Object> triggerValues;
  
  public String getWorkflowId() {
    return this.workflowId;
  }
  public void setWorkflowId(String workflowId) {
    this.workflowId = workflowId;
  }

  /** use this specific workflow version. 
   * If you want to use the latest version of a certain workflow, use {@link #workflowSource(String)} */ 
  public Start workflowId(String workflowId) {
    this.workflowId = workflowId;
    return this;
  }
  
  public String getSourceWorkflowId() {
    return this.sourceWorkflowId;
  }
  public void setSourceWorkflowId(String sourceWorkflowId) {
    this.sourceWorkflowId = sourceWorkflowId;
  }
  
  /** use the lastest version of the workflow for the given source.
   * @see Workflow#sourceWorkflowId(String) */
  public Start sourceWorkflowId(String sourceWorkflowId) {
    this.sourceWorkflowId = sourceWorkflowId;
    return this;
  }

  public Map<String,Object> getVariableValues() {
    return this.variableValues;
  }

  public void setVariableValues(Map<String,Object> variableValues) {
    this.variableValues = variableValues;
  }

  public Start variableValue(String variableId, Object value) {
    if (variableValues==null) {
      variableValues = new LinkedHashMap<>();
    }
    variableValues.put(variableId, value);
    return this;
  }

  public Map<String,Object> getTriggerValues() {
    return this.triggerValues;
  }

  public void setTriggerValues(Map<String,Object> triggerValues) {
    this.triggerValues = triggerValues;
  }

  public Start triggerValue(String triggerKey, Object value) {
    if (triggerValues==null) {
      triggerValues = new LinkedHashMap<>();
    }
    triggerValues.put(triggerKey, value);
    return this;
  }
  
  public String getBusinessKey() {
    return this.businessKey;
  }
  public void setBusinessKey(String businessKey) {
    this.businessKey = businessKey;
  }
  /** optional user-defined unique id (unique in the scope of one workflow) 
   * @see Message# */
  public Start businessKey(String businessKey) {
    this.businessKey = businessKey;
    return this;
  }
}
