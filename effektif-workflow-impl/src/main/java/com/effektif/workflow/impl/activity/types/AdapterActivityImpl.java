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
package com.effektif.workflow.impl.activity.types;

import java.util.HashMap;
import java.util.Map;

import com.effektif.workflow.api.activities.AdapterActivity;
import com.effektif.workflow.api.types.Type;
import com.effektif.workflow.api.workflow.Binding;
import com.effektif.workflow.impl.WorkflowParser;
import com.effektif.workflow.impl.activity.ActivityDescriptor;
import com.effektif.workflow.impl.activity.InputParameter;
import com.effektif.workflow.impl.activity.OutputParameter;
import com.effektif.workflow.impl.adapter.Adapter;
import com.effektif.workflow.impl.adapter.AdapterService;
import com.effektif.workflow.impl.adapter.ExecuteRequest;
import com.effektif.workflow.impl.adapter.ExecuteResponse;
import com.effektif.workflow.impl.data.DataType;
import com.effektif.workflow.impl.data.DataTypeService;
import com.effektif.workflow.impl.workflow.ActivityImpl;
import com.effektif.workflow.impl.workflow.BindingImpl;
import com.effektif.workflow.impl.workflowinstance.ActivityInstanceImpl;


public class AdapterActivityImpl extends AbstractBindableActivityImpl<AdapterActivity> {
  
  protected String adapterId;
  protected String activityKey;
  protected DataTypeService dataTypeService;
  protected AdapterService adapterService;
  protected ActivityDescriptor descriptor;
  protected Map<String,DataType> outputParameterDataTypes;

  public AdapterActivityImpl() {
    super(AdapterActivity.class);
  }
  
  @Override
  public void parse(ActivityImpl activityImpl, AdapterActivity adapterActivity, WorkflowParser parser) {
    super.parse(activityImpl, adapterActivity, parser);
    this.adapterId = adapterActivity.getAdapterId();
    this.activityKey = adapterActivity.getActivityKey();
    this.dataTypeService = parser.getConfiguration(DataTypeService.class);
    this.adapterService = parser.getConfiguration(AdapterService.class);
    Map<String, InputParameter> inputParameters = null;
    Adapter adapter = adapterService.findAdapterById(adapterId);
    this.descriptor = adapter!=null ? adapter.getActivityDescriptor(activityKey) : null;
    if (descriptor!=null) {
      Map<String, OutputParameter> outputParameters = descriptor.getOutputParameters();
      if (outputParameters!=null) {
        this.outputParameterDataTypes = new HashMap<>();
        DataTypeService dataTypeService = parser.getConfiguration(DataTypeService.class);
        for (String outputParameterKey: outputParameters.keySet()) {
          // IDEA if there there is a difference between the parameter type and the 
          //      configured variable type (@see this.outputBindings),
          //      then we could coerse (=apply a conversion) 
          OutputParameter outputParameter = outputParameters.get(outputParameterKey);
          Type type = outputParameter.getType();
          DataType dataType = dataTypeService.createDataType(type);
          outputParameterDataTypes.put(outputParameterKey, dataType);
        }
      }
      inputParameters = descriptor.getInputParameters();
    }
    
    Map<String, Binding> inputBindingsApi = adapterActivity.getInputBindings();
    if (inputBindingsApi!=null && !inputBindingsApi.isEmpty()) {
      for (Map.Entry<String, Binding> entry: inputBindingsApi.entrySet()) {
        String key = entry.getKey();
        Binding inputBinding = entry.getValue();
        InputParameter inputParameter = inputParameters!=null ? inputParameters.get(key) : null;
        parser.pushContext("inputBindings["+key+"]", inputParameter, null);
        if (inputParameter==null) {
          parser.addWarning("Unexpected input binding '%s' in activity '%s'", key, activityApi.getId());
        }
        BindingImpl<?> bindingImpl = parser.parseBinding(inputBinding, inputParameter, true);
        if (bindingImpl!=null) {
          if (inputBindings==null) {
            inputBindings = new HashMap<>();
          }
          inputBindings.put(key, bindingImpl);
        }
        parser.popContext();
      }
    }

    this.outputBindings = activityApi.getOutputBindings();
  }
  
  public ActivityDescriptor getDescriptor() {
    return descriptor;
  }

  @Override
  public void execute(ActivityInstanceImpl activityInstance) {
    ExecuteRequest executeRequest = new ExecuteRequest()
      .activityInstanceId(activityInstance.id)
      .workflowInstanceId(activityInstance.workflowInstance.id)
      .activityKey(activityKey);

    if (inputBindings!=null) {
      for (String adapterKey: inputBindings.keySet()) {
        BindingImpl inputBinding = inputBindings.get(adapterKey);
        Object value = inputBinding.getValue(activityInstance);
        if (value!=null) {
          executeRequest.inputParameter(adapterKey, value);
        }
      }
    }
    
    ExecuteResponse executeResponse = adapterService.executeAdapterActivity(adapterId, executeRequest);
    
    if (outputBindings!=null) {
      Map<String, Object> outputParameterValues = executeResponse.getOutputParameterValues();
      for (String outputParameterKey: outputBindings.keySet()) {
        String variableId = outputBindings.get(outputParameterKey);
        Object value = outputParameterValues.get(outputParameterKey);
        DataType dataType = outputParameterDataTypes.get(outputParameterKey);
        Object deserializedValue = dataType.convertJsonToInternalValue(value);
        activityInstance.setVariableValue(variableId, deserializedValue);
      }
    }
    
    if (executeResponse.isOnwards()) {
      activityInstance.onwards();
    }
  }

  public AdapterActivityImpl adapterId(String adapterId) {
    this.adapterId = adapterId;
    return this;
  }
  
  public String getActivityKey() {
    return this.activityKey;
  }
  public void setActivityKey(String activityKey) {
    this.activityKey = activityKey;
  }
  public AdapterActivityImpl activityKey(String activityKey) {
    this.activityKey = activityKey;
    return this;
  }
}
