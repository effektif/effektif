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
import com.effektif.workflow.impl.activity.AbstractActivityType;
import com.effektif.workflow.impl.adapter.AdapterService;
import com.effektif.workflow.impl.adapter.ExecuteRequest;
import com.effektif.workflow.impl.adapter.ExecuteResponse;
import com.effektif.workflow.impl.data.DataTypeService;
import com.effektif.workflow.impl.data.TypedValueImpl;
import com.effektif.workflow.impl.workflow.ActivityImpl;
import com.effektif.workflow.impl.workflowinstance.ActivityInstanceImpl;


public class AdapterActivityImpl extends AbstractActivityType<AdapterActivity> {
  
  protected String adapterId;
  protected String activityKey;
  protected Map<String,String> inputMappings; 
  protected Map<String,TypedValue> inputMappingValues; 
  protected Map<String,String> outputMappings; 
  protected DataTypeService dataTypeService;
  protected AdapterService adapterService;

  public AdapterActivityImpl() {
    super(AdapterActivity.class);
  }
  
  @Override
  public void parse(ActivityImpl activityImpl, AdapterActivity adapterActivity, WorkflowParser parser) {
    super.parse(activityImpl, adapterActivity, parser);
    this.adapterId = adapterActivity.getAdapterId();
    this.activityKey = adapterActivity.getActivityKey();
    this.inputMappings = adapterActivity.getInputMappings();
    this.inputMappingValues = adapterActivity.getInputMappingValues();
    this.outputMappings = adapterActivity.getOutputMappings();
    this.dataTypeService = parser.getConfiguration(DataTypeService.class);
    this.adapterService = parser.getConfiguration(AdapterService.class);
  }

  @Override
  public void execute(ActivityInstanceImpl activityInstance) {
    ExecuteRequest executeRequest = new ExecuteRequest()
      .activityInstanceId(activityInstance.id)
      .workflowInstanceId(activityInstance.workflowInstance.id)
      .activityKey(activityKey);

    if (inputMappings!=null) {
      for (String adapterKey: inputMappings.keySet()) {
        String variableId = inputMappings.get(adapterKey);
        Type type = null;
        Object value = null;
        TypedValueImpl typedValueImpl = activityInstance.getTypedValue(variableId);
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
    if (inputMappingValues!=null) {
      for (String adapterKey: inputMappingValues.keySet()) {
        TypedValue typedValue = inputMappingValues.get(adapterKey);
        executeRequest.inputParameter(adapterKey, typedValue);
      }
    }
    
    
    ExecuteResponse executeResponse = adapterService.executeAdapterActivity(adapterId, executeRequest);
    
    if (outputMappings!=null) {
      Map<String, TypedValue> outputParameterValues = executeResponse.getOutputParameterValues();
      for (String variableId: outputMappings.keySet()) {
        String adapterKey = outputMappings.get(variableId);
        TypedValue typedValue = outputParameterValues.get(adapterKey);
        activityInstance.setVariableValue(variableId, typedValue.getValue());
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
