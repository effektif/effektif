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


/**
 * @author Tom Baeyens
 */
public class TriggerInstance {
  
  protected String workflowId;
  protected String sourceWorkflowId;
  protected String businessKey;
  protected Map<String,Object> data;
  
  public String getWorkflowId() {
    return this.workflowId;
  }
  public void setWorkflowId(String workflowId) {
    this.workflowId = workflowId;
  }

  /** use this specific workflow version. 
   * If you want to use the latest version of a certain workflow, use {@link #workflowSource(String)} */ 
  public TriggerInstance workflowId(String workflowId) {
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
  public TriggerInstance sourceWorkflowId(String sourceWorkflowId) {
    this.sourceWorkflowId = sourceWorkflowId;
    return this;
  }

  public Map<String,Object> getData() {
    return this.data;
  }

  /** @see #data(String, Object) */
  public void setData(Map<String,Object> data) {
    this.data = data;
  }

  /** this data is passed to the trigger or is set as variable values.
   * In case there is no trigger specified, the data keys have to 
   * represent the variable ids. */
  public TriggerInstance data(String key, Object value) {
    if (data==null) {
      data = new LinkedHashMap<>();
    }
    data.put(key, value);
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
  public TriggerInstance businessKey(String businessKey) {
    this.businessKey = businessKey;
    return this;
  }
}
