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

import com.effektif.workflow.api.Configuration;
import com.effektif.workflow.api.activities.Call;
import com.effektif.workflow.api.model.Start;
import com.effektif.workflow.api.types.TextType;
import com.effektif.workflow.api.workflowinstance.WorkflowInstance;
import com.effektif.workflow.impl.WorkflowEngineImpl;
import com.effektif.workflow.impl.WorkflowParser;
import com.effektif.workflow.impl.WorkflowStore;
import com.effektif.workflow.impl.activity.InputParameter;
import com.effektif.workflow.impl.data.TypedValueImpl;
import com.effektif.workflow.impl.workflow.ActivityImpl;
import com.effektif.workflow.impl.workflow.BindingImpl;
import com.effektif.workflow.impl.workflowinstance.ActivityInstanceImpl;
import com.effektif.workflow.impl.workflowinstance.WorkflowInstanceImpl;

public class CallImpl extends AbstractBindableActivityImpl<Call> {
  
  public static final InputParameter<String> SUBWORKFLOW_ID = new InputParameter<>()
    .type(new TextType())
    .key("subWorkflowIdBinding");

  public static final InputParameter<String> SUBWORKFLOW_NAME = new InputParameter<>()
    .type(new TextType())
    .key("subWorkflowNameBinding");

  // TODO Boolean waitTillSubWorkflowEnds; add a configuration property to specify if this is fire-and-forget or wait-till-subworkflow-ends
  BindingImpl<String> subWorkflowIdBinding;
  BindingImpl<String> subWorkflowNameBinding;

  public CallImpl() {
    super(Call.class);
  }

  @Override
  public void parse(ActivityImpl activityImpl, Call call, WorkflowParser parser) {
    super.parse(activityImpl, call, parser);
    this.subWorkflowIdBinding = parser.parseBinding(call.getSubWorkflowIdBinding(), SUBWORKFLOW_ID);
    this.subWorkflowNameBinding = parser.parseBinding(call.getSubWorkflowNameBinding(), SUBWORKFLOW_NAME);
  }

  @Override
  public void execute(ActivityInstanceImpl activityInstance) {
    ActivityInstanceImpl activityInstanceImpl = (ActivityInstanceImpl) activityInstance;
    Configuration configuration = activityInstance.getConfiguration();

    String subWorkflowId = null;
    if (subWorkflowIdBinding!=null) {
      subWorkflowId = activityInstance.getValue(subWorkflowIdBinding);
    } else if (subWorkflowNameBinding!=null) {
      String subProcessName = activityInstance.getValue(subWorkflowNameBinding);
      WorkflowStore workflowStore = configuration.get(WorkflowStore.class);
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
      
      if (inputBindings!=null) {
        for (String subWorkflowKey: inputBindings.keySet()) {
          BindingImpl<?> subWorkflowBinding = inputBindings.get(subWorkflowKey);
          Object value = activityInstance.getValue(subWorkflowBinding);
          start.variableValue(subWorkflowKey, value);
        }
      }
      
      CallerReference callerReference = new CallerReference(activityInstance.workflowInstance.id, activityInstance.id);
  
      WorkflowEngineImpl workflowEngine = configuration.get(WorkflowEngineImpl.class);
      WorkflowInstance calledProcessInstance = workflowEngine.startWorkflowInstance(start, callerReference);
      activityInstanceImpl.setCalledWorkflowInstanceId(calledProcessInstance.getId());
      
    } else {
      log.debug("Skipping call activity because no sub workflow was defined");
      activityInstance.onwards();
    }
  }
  
  public void calledProcessInstanceEnded(ActivityInstanceImpl activityInstance, WorkflowInstanceImpl calledProcessInstance) {
    if (outputBindings!=null) {
      for (String subWorkflowVariableId: outputBindings.keySet()) {
        String variableId = outputBindings.get(subWorkflowVariableId);
        TypedValueImpl typedValue = calledProcessInstance.getTypedValue(subWorkflowVariableId);
        activityInstance.setVariableValue(variableId, typedValue);
      }
    }
  }
}
