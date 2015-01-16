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
package com.effektif.workflow.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.effektif.workflow.impl.json.JsonService;
import com.effektif.workflow.impl.plugin.DataType;
import com.effektif.workflow.impl.workflow.VariableImpl;
import com.effektif.workflow.impl.workflow.WorkflowImpl;
import com.fasterxml.jackson.annotation.JsonIgnore;


@Deprecated
public abstract class VariableRequestImpl {

  @JsonIgnore
  public JsonService jsonService;
  
  public Map<String,Object> variableValues;
  public Map<String,DataType> variableDataTypes;

  /** extra user defined information only accessible in the process as long as this request is executed synchronous. */
  public Map<String,Object> transientContext;

  public VariableRequestImpl() {
  }

  public VariableRequestImpl(JsonService jsonService) {
    super();
    this.jsonService = jsonService;
  }

  public void serialize(WorkflowImpl workflow) {
    if (variableValues!=null && !variableValues.isEmpty()) {
      Map<String,Object> serializedValues = new HashMap<>();
      Collection<VariableImpl> variableDefinitions = workflow.variables.values();
      if (variableDefinitions!=null) {
        for (VariableImpl variableDefinition: variableDefinitions) {
          String variableId = variableDefinition.id;
          Object internalValue = variableValues.get(variableId);
          Object serializedValue = variableDefinition.type.convertInternalToJsonValue(internalValue);
          serializedValues.put(variableId, serializedValue);
        }
      }
      variableValues = serializedValues;
    }
  }

  public void deserialize(WorkflowImpl workflow) {
    if (variableValues!=null && !variableValues.isEmpty()) {
      Map<String,Object> internalValues = new HashMap<>();
      Collection<VariableImpl> variableDefinitions = workflow.variables.values();
      if (variableDefinitions!=null) {
        for (VariableImpl variableDefinition: variableDefinitions) {
          String variableId = variableDefinition.id;
          Object jsonValue = variableValues.get(variableId);
          Object internalValue = variableDefinition.type.convertJsonToInternalValue(jsonValue);
          internalValues.put(variableId, internalValue);
        }
      }
      variableValues = internalValues;
    }
  }

  public VariableRequestImpl variableValue(String variableDefinitionId, Object value) {
    if (variableValues==null) {
      variableValues = new LinkedHashMap<>();
    }
    variableValues.put(variableDefinitionId, value);
    return this;
  }

  public VariableRequestImpl variableValue(String variableDefinitionId, Object value, DataType type) {
    if (variableValues==null) {
      variableValues = new LinkedHashMap<>();
      variableDataTypes = new HashMap<>();
    }
    variableValues.put(variableDefinitionId, value);
    variableDataTypes.put(variableDefinitionId, type);
    return this;
  }

//  public VariableRequestImpl variableValue(String variableDefinitionId, Object value, Class<?> javaBeanClass) {
//    JavaBeanType javaBeanType = new JavaBeanType(javaBeanClass);
//    javaBeanType.setJsonService(jsonService);
//    variableValue(variableDefinitionId, value, javaBeanType);
//    return this;
//  }

  public VariableRequestImpl transientContext(String key, Object value) {
    if (transientContext==null) {
      transientContext = new HashMap<>();
    }
    transientContext.put(key, value);
    return this;
  }

  public VariableRequestImpl transientContext(Map<String, Object> transientContext) {
    this.transientContext = transientContext;
    return this;
  }
}
