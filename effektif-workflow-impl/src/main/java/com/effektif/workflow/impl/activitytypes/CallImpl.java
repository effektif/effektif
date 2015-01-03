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
package com.effektif.workflow.impl.activitytypes;

import java.util.ArrayList;
import java.util.List;

import com.effektif.workflow.api.activities.Call;
import com.effektif.workflow.api.activities.CallMapping;
import com.effektif.workflow.api.command.StartCommand;
import com.effektif.workflow.api.workflowinstance.WorkflowInstance;
import com.effektif.workflow.impl.WorkflowEngineImpl;
import com.effektif.workflow.impl.WorkflowStore;
import com.effektif.workflow.impl.definition.ActivityImpl;
import com.effektif.workflow.impl.definition.BindingImpl;
import com.effektif.workflow.impl.definition.WorkflowValidator;
import com.effektif.workflow.impl.instance.ActivityInstanceImpl;
import com.effektif.workflow.impl.instance.WorkflowInstanceImpl;
import com.effektif.workflow.impl.plugin.AbstractActivityType;

public class CallImpl extends AbstractActivityType<Call> {

  BindingImpl<String> subProcessName;
  BindingImpl<String> subProcessId;
  List<CallMappingImpl> inputMappings;
  List<CallMappingImpl> outputMappings;
  
  public CallImpl() {
    super(Call.class);
  }

  @Override
  public void validate(ActivityImpl activity, Call call, WorkflowValidator validator) {
    subProcessId = validator.compileBinding(call.getSubProcessId(), "subProcessId");
    subProcessName = validator.compileBinding(call.getSubProcessName(), "subProcessName");
    inputMappings = validateCallMappings(call.getInputMappings(), validator, "inputMappings");
    outputMappings = validateCallMappings(call.getOutputMappings(), validator, "outputMappings");
  }

  private List<CallMappingImpl> validateCallMappings(List<CallMapping> callMappings, WorkflowValidator validator, String propertyName) {
    if (callMappings!=null) {
      List<CallMappingImpl> callMappingImpls = new ArrayList<>(callMappings.size());
      int i=0;
      for (CallMapping callMapping: callMappings) {
        CallMappingImpl callMappingImpl = new CallMappingImpl();
        callMappingImpl.source = validator.compileBinding(callMapping.getSource(), propertyName+"["+i+"]");
        callMappingImpl.destinationVariableId = callMapping.getDestinationVariableId();
        i++;
      }
      return callMappingImpls;
    }
    return null;
  }

  @Override
  public void execute(ActivityInstanceImpl activityInstance) {
    ActivityInstanceImpl activityInstanceImpl = (ActivityInstanceImpl) activityInstance;
    WorkflowEngineImpl workflowEngine = activityInstanceImpl.workflowEngine;

    String subProcessIdValue = null;
    if (subProcessId!=null) {
      subProcessIdValue = activityInstance.getValue(subProcessId);
    } else if (subProcessName!=null) {
      String subProcessNameValue = activityInstance.getValue(subProcessName);
      WorkflowStore workflowStore = workflowEngine.getWorkflowStore();
      String organizationId = activityInstanceImpl.workflow.organizationId;
      subProcessIdValue = workflowStore.findLatestWorkflowIdByName(subProcessNameValue, organizationId);
      if (subProcessIdValue==null) {
        throw new RuntimeException("Couldn't find subprocess by name: "+subProcessNameValue);
      }
    }

    StartCommand start = new StartCommand()
      .workflowId(subProcessIdValue);
    
    if (inputMappings!=null) {
      for (CallMappingImpl inputMapping: inputMappings) {
        Object value = activityInstance.getValue(inputMapping.source);
        start.variableValue(inputMapping.destinationVariableId, value);
      }
    }
    
    CallerReference callerReference = new CallerReference(activityInstance.workflowInstance.id, activityInstance.id);
    
    WorkflowInstance calledProcessInstance = workflowEngine.startWorkflowInstance(start, callerReference);
    activityInstanceImpl.setCalledWorkflowInstanceId(calledProcessInstance.getId()); 
  }
  
  public void calledProcessInstanceEnded(ActivityInstanceImpl activityInstance, WorkflowInstanceImpl calledProcessInstance) {
    if (outputMappings!=null) {
      for (CallMappingImpl outputMapping: outputMappings) {
        Object value = calledProcessInstance.getValue(outputMapping.source);
        activityInstance.setVariableValue(outputMapping.destinationVariableId, value);
      }
    }
  }
}
