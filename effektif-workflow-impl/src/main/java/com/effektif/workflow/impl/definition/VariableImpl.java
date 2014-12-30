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
package com.effektif.workflow.impl.definition;

import com.effektif.deprecated.VariableBuilder;
import com.effektif.workflow.api.workflow.Variable;
import com.effektif.workflow.impl.WorkflowEngineImpl;
import com.effektif.workflow.impl.type.DataType;
import com.fasterxml.jackson.annotation.JsonIgnore;


/**
 * @author Walter White
 */
public class VariableImpl implements VariableBuilder, Variable {

  public String id;
  
//  public String dataTypeId;
//  @JsonIgnore
  public DataType dataType;
//  @JsonProperty("dataType")
//  public Map<String,Object> dataTypeJson;
  
  @JsonIgnore
  public Object initialValue;
//  @JsonProperty("initialValue")
//  public Object initialValueJson;

  @JsonIgnore
  public WorkflowEngineImpl workflowEngine;
  @JsonIgnore
  public WorkflowImpl workflow;  
  @JsonIgnore
  public ScopeImpl parent;

  public Long line;
  public Long column;

  public VariableImpl(Variable apiVariable) {
  }

  public VariableImpl id(String id) {
    this.id = id;
    return this;
  }

  public VariableImpl line(Long line) {
    this.line = line;
    return this;
  }

  public VariableImpl column(Long column) {
    this.column = column;
    return this;
  }

  public VariableImpl dataType(DataType dataType) {
    this.dataType = dataType;
    return this;
  }

  public VariableImpl initialValue(Object initialValue) {
    this.initialValue = initialValue;
    return this;
  }
  
  public VariableImpl processEngine(WorkflowEngineImpl workflowEngine) {
    this.workflowEngine = workflowEngine;
    return this;
  }
  
  public VariableImpl processDefinition(WorkflowImpl processDefinition) {
    this.workflow = processDefinition;
    return this;
  }
  
  public VariableImpl parent(ScopeImpl parent) {
    this.parent = parent;
    return this;
  }
  
  public void prepare() {
  }
  
  public ScopeImpl getParent() {
    return parent;
  }

  public void setParent(ScopeImpl parent) {
    this.parent = parent;
  }

  public String getId() {
    return id;
  }
  
  public VariableImpl setId(String id) {
    this.id = id;
    return this;
  }
  
  public WorkflowImpl getWorkflow() {
    return workflow;
  }
  
  public void setWorkflow(WorkflowImpl processDefinition) {
    this.workflow = processDefinition;
  }

  public WorkflowEngineImpl getWorkflowEngine() {
    return workflowEngine;
  }
  
  public void setWorkflowEngine(WorkflowEngineImpl processEngine) {
    this.workflowEngine = processEngine;
  }
  
  public DataType getDataType() {
    return dataType;
  }
  
  public void setDataType(DataType dataType) {
    this.dataType = dataType;
  }
}
