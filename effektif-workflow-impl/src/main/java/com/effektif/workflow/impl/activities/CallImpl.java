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
package com.effektif.workflow.impl.activities;

import java.util.ArrayList;
import java.util.List;

import com.effektif.workflow.api.activities.Call;
import com.effektif.workflow.api.activities.CallMapping;
import com.effektif.workflow.api.command.Start;
import com.effektif.workflow.api.ref.WorkflowReference;
import com.effektif.workflow.api.types.BindingType;
import com.effektif.workflow.api.types.JavaBeanType;
import com.effektif.workflow.api.types.ListType;
import com.effektif.workflow.api.types.ObjectField;
import com.effektif.workflow.api.types.ObjectType;
import com.effektif.workflow.api.types.TextType;
import com.effektif.workflow.api.types.VariableReferenceType;
import com.effektif.workflow.api.types.WorkflowReferenceType;
import com.effektif.workflow.api.workflowinstance.WorkflowInstance;
import com.effektif.workflow.impl.WorkflowEngineImpl;
import com.effektif.workflow.impl.WorkflowParser;
import com.effektif.workflow.impl.WorkflowStore;
import com.effektif.workflow.impl.plugin.AbstractActivityType;
import com.effektif.workflow.impl.workflow.ActivityImpl;
import com.effektif.workflow.impl.workflow.BindingImpl;
import com.effektif.workflow.impl.workflowinstance.ActivityInstanceImpl;
import com.effektif.workflow.impl.workflowinstance.WorkflowInstanceImpl;

public class CallImpl extends AbstractActivityType<Call> {

  // TODO Boolean waitTillSubWorkflowEnds; add a configuration property to specify if this is fire-and-forget or wait-till-subworkflow-ends
  BindingImpl<String> subWorkflowIdBinding;
  BindingImpl<String> subWorkflowNameBinding;
  List<CallMappingImpl> inputMappings;
  List<CallMappingImpl> outputMappings;

  public CallImpl() {
    super(Call.class);
  }

  @Override
  public ObjectType getDescriptor() {
    return new JavaBeanType(Call.class)
      .description("Invoke another workflow")
      .field(new ObjectField("subWorkflowIdBinding")
        .type(new BindingType(new TextType()))
        .label("Sub worfklow id"))
      .field(new ObjectField("subWorkflowNameBinding")
        .type(new BindingType(new TextType()))
        .label("Sub worfklow name"))
      .field(new ObjectField("inputMappings")
        .label("Input mappings")
        .type(new ListType(new JavaBeanType(CallMapping.class)
          .field(new ObjectField("sourceBinding")
            .type(new BindingType())
            .label("Item in this workflow"))
          .field(new ObjectField("destinationVariableId")
            .label("Variable in the sub workflow")
            .type(new VariableReferenceType())))))
      .field(new ObjectField("outputMappings")
        .label("Output mappings")
        .type(new ListType(new JavaBeanType(CallMapping.class)
          .field(new ObjectField("sourceBinding")
            .label("Item in the sub workflow")
            .type(new BindingType()))
          .field(new ObjectField("destinationVariableId")
            .label("Variable in this workflow")
            .type(new VariableReferenceType())))));
  }

  @Override
  public void parse(ActivityImpl activityImpl, Call call, WorkflowParser workflowParser) {
    subWorkflowIdBinding = workflowParser.parseBinding(call.getSubWorkflowIdBinding(), String.class, false, call, "subWorkflowIdBinding");
    subWorkflowNameBinding = workflowParser.parseBinding(call.getSubWorkflowNameBinding(), String.class, false, call, "subWorkflowNameBinding");
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
        callMappingImpls.add(callMappingImpl);
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

    String subWorkflowId = null;
    if (subWorkflowIdBinding!=null) {
      subWorkflowId = activityInstance.getValue(subWorkflowIdBinding);
    } else if (subWorkflowNameBinding!=null) {
      String subProcessName = activityInstance.getValue(subWorkflowNameBinding);
      WorkflowStore workflowStore = workflowEngine.getWorkflowStore();
      subWorkflowId = workflowStore.findLatestWorkflowIdByName(subProcessName);
      if (subWorkflowId==null) {
        throw new RuntimeException("Couldn't find sub workflow by name: "+subProcessName);
      }
    } else {
      log.debug("No sub workflow binding was configured");
    }
    
    if (subWorkflowId!=null) {
      Start start = new Start()
        .workflowId(subWorkflowId);
      
      if (inputMappings!=null) {
        for (CallMappingImpl inputMapping: inputMappings) {
          Object value = activityInstance.getValue(inputMapping.sourceBinding);
          start.variableValue(inputMapping.destinationVariableId, value);
        }
      }
      
      CallerReference callerReference = new CallerReference(activityInstance.workflowInstance.id, activityInstance.id);
  
      WorkflowInstance calledProcessInstance = workflowEngine.startWorkflowInstance(start, callerReference);
      activityInstanceImpl.setCalledWorkflowInstanceId(calledProcessInstance.getId());
      
    } else {
      log.debug("Skipping call activity because no sub workflow was defined");
      activityInstance.onwards();
    }
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
