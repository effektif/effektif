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
package com.effektif.adapter.activity;

import com.effektif.adapter.service.Adapter;
import com.effektif.adapter.service.AdapterService;
import com.effektif.adapter.service.ExecuteRequest;
import com.effektif.adapter.service.ExecuteResponse;
import com.effektif.workflow.api.model.TypedValue;
import com.effektif.workflow.api.types.DataType;
import com.effektif.workflow.api.workflow.Binding;
import com.effektif.workflow.impl.WorkflowParser;
import com.effektif.workflow.impl.activity.ActivityDescriptor;
import com.effektif.workflow.impl.activity.InputDescriptor;
import com.effektif.workflow.impl.activity.types.AbstractBindableActivityImpl;
import com.effektif.workflow.impl.data.DataTypeService;
import com.effektif.workflow.impl.workflow.ActivityImpl;
import com.effektif.workflow.impl.workflow.BindingImpl;
import com.effektif.workflow.impl.workflow.VariableImpl;
import com.effektif.workflow.impl.workflow.WorkflowImpl;
import com.effektif.workflow.impl.workflowinstance.ActivityInstanceImpl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author Tom Baeyens
 */
public class AdapterActivityImpl extends AbstractBindableActivityImpl<AdapterActivity> {
  
  protected String adapterId;
  protected String activityKey;
  protected DataTypeService dataTypeService;
  protected AdapterService adapterService;
  protected ActivityDescriptor descriptor;
  protected Map<String, InputDescriptor> inputDescriptors;
  
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
    
    Adapter adapter = adapterService.findAdapterById(adapterId);
    this.descriptor = adapter!=null ? adapter.getActivityDescriptor(activityKey) : null;
    if (descriptor!=null) {
      inputDescriptors = new HashMap<>();
      for (InputDescriptor desc : descriptor.getInputDescriptors()) {
        inputDescriptors.put(desc.getKey(), desc);
      }
    }
    
    Map<String, Binding> inputBindingsApi = adapterActivity.getInputBindings();
    if (inputBindingsApi!=null && !inputBindingsApi.isEmpty()) {
      for (Map.Entry<String, Binding> entry: inputBindingsApi.entrySet()) {
        String key = entry.getKey();
        Binding inputBinding = entry.getValue();
        InputDescriptor inputDescriptor = inputDescriptors!=null ? inputDescriptors.get(key) : null;
        parser.pushContext("inputBindings["+key+"]", inputDescriptor, null, null);
        if (inputDescriptor==null) {
          inputDescriptor = substituteMissingDescriptor(parser.workflow, inputBinding);
          parser.addWarning("Unexpected input binding '%s' in activity '%s'", key, activity.getId());
        }
        DataType type = inputDescriptor.getType();
        String bindingName = inputDescriptor.getKey();
        boolean required = inputDescriptor.isRequired();
        BindingImpl<?> bindingImpl = parser.parseBinding(inputBinding, bindingName, required, type);
        if (bindingImpl!=null) {
          if (inputBindings==null) {
            inputBindings = new HashMap<>();
          }
          inputBindings.put(key, bindingImpl);
        }
        parser.popContext();
      }
    }

    this.outputBindings = activity.getOutputBindings();
  }

  protected InputDescriptor substituteMissingDescriptor(WorkflowImpl workflow, Binding inputBinding) {
    InputDescriptor inputDescriptor = null;
    VariableImpl variable = workflow.getVariables().get(inputBinding.getExpression());

    if (variable != null) {
      inputDescriptor = new InputDescriptor().name(variable.id).type(variable.type.getDataType());
    }

    return inputDescriptor;
  }

  public ActivityDescriptor getDescriptor() {
    return descriptor;
  }

  @Override
  public void execute(ActivityInstanceImpl activityInstance) {
    ExecuteRequest executeRequest = new ExecuteRequest()
      .activityInstanceId(activityInstance.id)
      .workflowInstanceId(activityInstance.workflowInstance.id)
      .activityKey(activityKey)
      .activityId(activityInstance.activity.id)
      .workflowId(activityInstance.workflow.id.getInternal());

    if (inputBindings!=null) {
      for (String adapterKey: inputBindings.keySet()) {
        
        Object value = null;
        if (isList(adapterKey)) {
          List<BindingImpl<Object>> inputBindings = inputListBindings!=null ? inputListBindings.get(adapterKey) : null;
          value = inputBindings!=null ? activityInstance.getValues(inputBindings) : null;
        } else {
          BindingImpl inputBinding = inputBindings.get(adapterKey);
          value = activityInstance.getValue(inputBinding);
        }
        
        if (value!=null) {
          executeRequest.inputParameter(adapterKey, value);
        }
      }
    }
    
    ExecuteResponse executeResponse = adapterService.executeAdapterActivity(adapterId, executeRequest);
    
    if (outputBindings!=null) {
      Map<String, TypedValue> outputParameterValues = executeResponse.getOutputParameterValues();
      for (String outputParameterKey: outputBindings.keySet()) {
        String variableId = outputBindings.get(outputParameterKey);
        TypedValue typedValue = outputParameterValues.get(outputParameterKey);
//        DataTypeImpl dataType = outputParameterDataTypes.get(outputParameterKey);
        if(typedValue != null)
          activityInstance.setVariableValue(variableId, typedValue.getValue());
        else log.warn("Variable type not defined for variable " + variableId);
      }
    }

    if (executeResponse != null && executeResponse.isOnwards()) {
      activityInstance.onwards();
    }
  }

  protected boolean isList(String adapterKey) {
    InputDescriptor inputDescriptor = inputDescriptors!=null ? inputDescriptors.get(adapterKey) : null;
    return inputDescriptor!=null ? inputDescriptor.isList() : false;
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
