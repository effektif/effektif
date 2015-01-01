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
import com.effektif.workflow.api.workflow.Binding;
import com.effektif.workflow.api.workflowinstance.WorkflowInstance;
import com.effektif.workflow.impl.BindingImpl;
import com.effektif.workflow.impl.WorkflowEngineImpl;
import com.effektif.workflow.impl.WorkflowStore;
import com.effektif.workflow.impl.definition.ActivityImpl;
import com.effektif.workflow.impl.instance.ActivityInstanceImpl;
import com.effektif.workflow.impl.instance.WorkflowInstanceImpl;
import com.effektif.workflow.impl.plugin.AbstractActivityType;
import com.effektif.workflow.impl.plugin.ConfigurationClass;
import com.effektif.workflow.impl.plugin.ControllableActivityInstance;
import com.effektif.workflow.impl.plugin.Validator;

@ConfigurationClass(Call.class)
public class CallImpl extends AbstractActivityType<Call> {

  BindingImpl<String> subProcessName;
  BindingImpl<String> subProcessId;
  List<CallMappingImpl> inputMappings;
  List<CallMappingImpl> outputMappings;
  
  @Override
  public void validate(ActivityImpl activity, Call call, Validator validator) {
    subProcessId = validator.compileBinding(call.getSubProcessId(), "subProcessId");
    subProcessName = validator.compileBinding(call.getSubProcessId(), "subProcessName");
    inputMappings = validateCallMappings(call.getInputMappings(), validator, "inputMappings");
    outputMappings = validateCallMappings(call.getOutputMappings(), validator, "outputMappings");
  }

  private List<CallMappingImpl> validateCallMappings(List<CallMapping> callMappings, Validator validator, String propertyName) {
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
  public void start(ControllableActivityInstance activityInstance) {
    ActivityInstanceImpl activityInstanceImpl = (ActivityInstanceImpl) activityInstance;
    WorkflowEngineImpl workflowEngine = activityInstanceImpl.workflowEngine;

    String subProcessIdValue = null;
    if (subProcessId!=null) {
      subProcessIdValue = activityInstance.getValue(subProcessId);
    } else if (subProcessName!=null) {
      String subProcessNameValue = activityInstance.getValue(subProcessName);
      WorkflowStore workflowStore = workflowEngine.getWorkflowStore();
      subProcessIdValue = workflowStore.findLatestWorkflowIdByName(subProcessNameValue);
      if (subProcessIdValue==null) {
        throw new RuntimeException("Couldn't find subprocess by name: "+subProcessNameValue);
      }
    }

    StartCommand start = activityInstanceImpl.newSubWorkflowStart(subProcessIdValue);
    
    if (inputMappings!=null) {
      for (CallMappingImpl inputMapping: inputMappings) {
        Object value = activityInstance.getValue(inputMapping.source);
        start.variableValue(inputMapping.destinationVariableId, value);
      }
    }
    
    WorkflowInstance calledProcessInstance = workflowEngine.startWorkflowInstance(start);
    activityInstanceImpl.setCalledWorkflowInstanceId(calledProcessInstance.getId()); 
  }
  
  public void calledProcessInstanceEnded(ControllableActivityInstance activityInstance, WorkflowInstanceImpl calledProcessInstance) {
    if (outputMappings!=null) {
      for (CallMappingImpl outputMapping: outputMappings) {
        Object value = calledProcessInstance.getValue(outputMapping.source);
        activityInstance.setVariableValue(outputMapping.destinationVariableId, value);
      }
    }
  }

  public CallImpl subProcessId(String subProcessId) {
    return subProcessId(new Binding<String>().value(subProcessId));
  }

  public CallImpl subProcessIdExpression(String subProcessIdExpression) {
    return subProcessId(new Binding<String>().expression(subProcessIdExpression));
  }

  public CallImpl subProcessIdVariable(String subProcessIdVariableId) {
    return subProcessId(new Binding<String>().variableDefinitionId(subProcessIdVariableId));
  }

  public CallImpl subProcessId(Binding<String> subProcessIdBinding) {
    this.subProcessId = subProcessIdBinding;
    return this;
  }
  
  public CallImpl inputMapping(String callerVariableId, String calledVariableId) {
    return inputMapping(new Binding<Object>().variableDefinitionId(callerVariableId), calledVariableId);
  }

  public CallImpl inputMapping(Binding<Object> callerBinding, String calledVariableId) {
    CallMapping inputMapping = new CallMapping()
      .source(callerBinding)
      .destinationVariableId(calledVariableId);
    if (inputMappings==null) {
      inputMappings = new ArrayList<>();
    }
    inputMappings.add(inputMapping);
    return this;
  }

  public CallImpl outputMapping(String calledVariableId, String callerVariableId) {
    return outputMapping(new Binding<Object>().variableDefinitionId(calledVariableId), callerVariableId);
  }

  public CallImpl outputMapping(Binding<Object> calledBinding, String callerVariableId) {
    CallMapping inputMapping = new CallMapping()
      .source(calledBinding)
      .destinationVariableId(callerVariableId);
    if (inputMappings==null) {
      inputMappings = new ArrayList<>();
    }
    inputMappings.add(inputMapping);
    return this;
  }

}
