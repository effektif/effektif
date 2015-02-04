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
package com.effektif.adapter;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.effektif.workflow.api.Configuration;
import com.effektif.workflow.api.command.TypedValue;
import com.effektif.workflow.api.types.Type;
import com.effektif.workflow.impl.adapter.ExecuteRequest;
import com.effektif.workflow.impl.adapter.ExecuteResponse;
import com.effektif.workflow.impl.data.DataType;
import com.effektif.workflow.impl.data.DataTypeService;


public class ActivityContext {
  
  private static final Logger log = LoggerFactory.getLogger(ActivityContext.class);

  protected Configuration configuration;
  protected DataTypeService dataTypeService;
  protected ExecuteRequest executeRequest;
  protected ExecuteResponse executeResponse;
  
  public ActivityContext(Configuration configuration, ExecuteRequest executeRequest) {
    this.configuration = configuration;
    this.dataTypeService = configuration.get(DataTypeService.class);
    this.executeRequest = executeRequest;
    this.executeResponse = new ExecuteResponse();
  }

  public Object getValue(String parameterKey) {
    Map<String, TypedValue> inputParameters = executeRequest.getInputParameters();
    if (!inputParameters.containsKey(parameterKey)) {
      log.debug("Parameter '"+parameterKey+"' not available");
      return null;
    }
    TypedValue parameter = inputParameters.get(parameterKey);
    if (parameter==null) {
      log.debug("Parameter '"+parameterKey+"' is not specified");
      return null;
    }
    Object value = parameter.getValue();
    if (value==null) {
      log.debug("Parameter '"+parameterKey+"' has value null");
      return null;
    }
    Type type = parameter.getType();
    if (type==null) {
      log.debug("Parameter '"+parameterKey+"' doesn't have a type");
      return null;
    }
    DataType<?> dataType = dataTypeService.createDataType(type);
    value = dataType.convertJsonToInternalValue(value);
    return value;
  }


  public ExecuteResponse getExecuteResponse() {
    return this.executeResponse;
  }
  public void setExecuteResponse(ExecuteResponse executeResponse) {
    this.executeResponse = executeResponse;
  }

  public ExecuteRequest getExecuteRequest() {
    return this.executeRequest;
  }
  public void setExecuteRequest(ExecuteRequest executeRequest) {
    this.executeRequest = executeRequest;
  }
  public Object getInputParameterValue(String parameterKey) {
    return executeRequest.getInputParameterValue(parameterKey);
  }
  public void setOutputParameterValue(String greeting, String string) {
  }

  public void onwards() {
    executeResponse.setOnwards(true);
  }
}
