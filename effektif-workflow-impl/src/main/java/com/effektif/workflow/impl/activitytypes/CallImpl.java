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
import com.effektif.workflow.api.type.BindingType;
import com.effektif.workflow.api.type.ListType;
import com.effektif.workflow.api.type.ObjectField;
import com.effektif.workflow.api.type.ObjectType;
import com.effektif.workflow.api.type.VariableIdType;
import com.effektif.workflow.api.type.WorkflowIdType;
import com.effektif.workflow.api.type.WorkflowNameType;
import com.effektif.workflow.api.workflowinstance.WorkflowInstance;
import com.effektif.workflow.impl.WorkflowEngineImpl;
import com.effektif.workflow.impl.WorkflowParser;
import com.effektif.workflow.impl.WorkflowSerializer;
import com.effektif.workflow.impl.WorkflowStore;
import com.effektif.workflow.impl.plugin.AbstractActivityType;
import com.effektif.workflow.impl.workflow.ActivityImpl;
import com.effektif.workflow.impl.workflow.BindingImpl;
import com.effektif.workflow.impl.workflowinstance.ActivityInstanceImpl;
import com.effektif.workflow.impl.workflowinstance.WorkflowInstanceImpl;

public class CallImpl extends AbstractActivityType<Call> {

  // TODO Boolean waitTillSubWorkflowEnds; add a configuration property to specify if this is fire-and-forget or wait-till-subworkflow-ends
  BindingImpl<String> subProcessName;
  BindingImpl<String> subProcessId;
  List<CallMappingImpl> inputMappings;
  List<CallMappingImpl> outputMappings;

  public CallImpl() {
    super(Call.class);
  }

  @Override
  public ObjectType getDescriptor() {
    return new ObjectType(Call.class)
      .description("Invoke another workflow")
      .field(new ObjectField("subWorkflowId")
        .type(new BindingType(new WorkflowIdType()))
        .label("Fixed sub worfklow version"))
      .field(new ObjectField("subWorkflowName")
        .type(new BindingType(new WorkflowNameType()))
        .label("Sub workflow latest version"))
      .field(new ObjectField("inputMappings")
        .label("Input mappings")
        .type(new ListType(new ObjectType(CallMapping.class)
          .field(new ObjectField("sourceBinding")
            .type(new BindingType())
            .label("Item in this workflow"))
          .field(new ObjectField("Variable in the sub workflow")
            .label("destinationVariableId")
            .type(new VariableIdType())))))
      .field(new ObjectField("Output mappings")
        .name("outputMappings")
        .type(new ListType(new ObjectType()
          .field(new ObjectField("Item in the sub workflow")
            .label("sourceBinding")
            .type(new BindingType()))
          .field(new ObjectField("Variable in this workflow")
            .label("destinationVariableId")
            .type(new VariableIdType())))));
  }

  @Override
  public void parse(ActivityImpl activityImpl, Call call, WorkflowParser workflowParser) {
    subProcessId = workflowParser.parseBinding(call.getSubWorkflowIdBinding(), String.class, false, call, "subWorkflowIdBinding");
    subProcessName = workflowParser.parseBinding(call.getSubWorkflowNameBinding(), String.class, false, call, "subWorkflowNameBinding");
    inputMappings = parseMappings(call.getInputMappings(), call, "inputMappings", workflowParser);
    outputMappings = parseMappings(call.getOutputMappings(), call, "outputMappings", workflowParser);
  }

  protected List<CallMappingImpl> parseMappings(List<CallMapping> callMappingsApi, Call call, String fieldName, WorkflowParser workflowParser) {
    if (callMappingsApi!=null) {
      List<CallMappingImpl> callMappingImpls = new ArrayList<>(callMappingsApi.size());
      int i=0;
      for (CallMapping callMapping: callMappingsApi) {
        CallMappingImpl callMappingImpl = new CallMappingImpl();
        callMappingImpl.sourceBinding = workflowParser.parseBinding(callMapping.getSourceBinding(), Object.class, false, call, fieldName+"["+i+"]");
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
      subProcessIdValue = workflowStore.findLatestWorkflowIdByName(subProcessNameValue, activityInstance.requestContext);
      if (subProcessIdValue==null) {
        throw new RuntimeException("Couldn't find subprocess by name: "+subProcessNameValue);
      }
    }

    StartCommand start = new StartCommand()
      .workflowId(subProcessIdValue);
    
    if (inputMappings!=null) {
      for (CallMappingImpl inputMapping: inputMappings) {
        Object value = activityInstance.getValue(inputMapping.sourceBinding);
        start.variableValue(inputMapping.destinationVariableId, value);
      }
    }
    
    CallerReference callerReference = new CallerReference(activityInstance.workflowInstance.id, activityInstance.id);

    WorkflowInstance calledProcessInstance = workflowEngine.startWorkflowInstance(start, callerReference, activityInstance.requestContext);
    activityInstanceImpl.setCalledWorkflowInstanceId(calledProcessInstance.getId()); 
  }
  
  public void calledProcessInstanceEnded(ActivityInstanceImpl activityInstance, WorkflowInstanceImpl calledProcessInstance) {
    if (outputMappings!=null) {
      for (CallMappingImpl outputMapping: outputMappings) {
        Object value = calledProcessInstance.getValue(outputMapping.sourceBinding);
        activityInstance.setVariableValue(outputMapping.destinationVariableId, value);
      }
    }
  }
}
