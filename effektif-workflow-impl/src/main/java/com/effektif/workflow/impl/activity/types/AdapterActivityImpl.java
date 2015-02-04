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

import java.util.Map;

import com.effektif.workflow.api.activities.AdapterActivity;
import com.effektif.workflow.api.command.TypedValue;
import com.effektif.workflow.api.types.Type;
import com.effektif.workflow.impl.WorkflowParser;
import com.effektif.workflow.impl.activity.ActivityDescriptor;
import com.effektif.workflow.impl.adapter.Adapter;
import com.effektif.workflow.impl.adapter.AdapterService;
import com.effektif.workflow.impl.adapter.ExecuteRequest;
import com.effektif.workflow.impl.adapter.ExecuteResponse;
import com.effektif.workflow.impl.data.DataTypeService;
import com.effektif.workflow.impl.data.TypedValueImpl;
import com.effektif.workflow.impl.workflow.ActivityImpl;
import com.effektif.workflow.impl.workflow.BindingImpl;
import com.effektif.workflow.impl.workflowinstance.ActivityInstanceImpl;


public class AdapterActivityImpl extends AbstractBindableActivityImpl<AdapterActivity> {
  
  protected String adapterId;
  protected String activityKey;
  protected DataTypeService dataTypeService;
  protected AdapterService adapterService;
  protected ActivityDescriptor descriptor;

  public AdapterActivityImpl() {
    super(AdapterActivity.class);
  }
  
  @Override
  public void parse(ActivityImpl activityImpl, AdapterActivity adapterActivity, WorkflowParser parser) {
    this.adapterId = adapterActivity.getAdapterId();
    this.activityKey = adapterActivity.getActivityKey();
    this.dataTypeService = parser.getConfiguration(DataTypeService.class);
    this.adapterService = parser.getConfiguration(AdapterService.class);
    Adapter adapter = adapterService.findAdapterById(adapterId);
    this.descriptor = adapter!=null ? adapter.getActivityDescriptor(activityKey) : null;
    // super.parse will parse the input bindings based on the descriptor
    super.parse(activityImpl, adapterActivity, parser);
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
        Type type = null;
        Object value = null;
        TypedValueImpl typedValueImpl = inputBinding.getTypedValue(activityInstance);
        if (typedValueImpl!=null) {
          value = typedValueImpl.value;
          if (typedValueImpl.type!=null) {
            type = typedValueImpl.type.serialize();
          } else if (value!=null) {
            type = dataTypeService.getTypeByValue(value);
          }
        }
        if (value!=null) {
          executeRequest.inputParameter(adapterKey, new TypedValue()
            .type(type)
            .value(value));
        }
      }
    }
    
    ExecuteResponse executeResponse = adapterService.executeAdapterActivity(adapterId, executeRequest);
    
    if (outputBindings!=null) {
      Map<String, TypedValue> outputParameterValues = executeResponse.getOutputParameterValues();
      for (String adapterKey: outputBindings.keySet()) {
        String variableId = outputBindings.get(adapterKey);
        TypedValue typedValue = outputParameterValues.get(adapterKey);
        activityInstance.setVariableValue(variableId, typedValue);
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
