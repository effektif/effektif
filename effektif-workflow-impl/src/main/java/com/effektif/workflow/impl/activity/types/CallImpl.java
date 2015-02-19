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
import java.util.List;
import java.util.Map;

import com.effektif.workflow.api.Configuration;
import com.effektif.workflow.api.WorkflowEngine;
import com.effektif.workflow.api.activities.Call;
import com.effektif.workflow.api.model.Start;
import com.effektif.workflow.api.query.WorkflowQuery;
import com.effektif.workflow.api.workflow.Binding;
import com.effektif.workflow.api.workflow.Variable;
import com.effektif.workflow.api.workflow.Workflow;
import com.effektif.workflow.api.workflowinstance.WorkflowInstance;
import com.effektif.workflow.impl.WorkflowEngineImpl;
import com.effektif.workflow.impl.WorkflowParser;
import com.effektif.workflow.impl.WorkflowStore;
import com.effektif.workflow.impl.data.TypedValueImpl;
import com.effektif.workflow.impl.workflow.ActivityImpl;
import com.effektif.workflow.impl.workflow.BindingImpl;
import com.effektif.workflow.impl.workflowinstance.ActivityInstanceImpl;
import com.effektif.workflow.impl.workflowinstance.WorkflowInstanceImpl;

/**
 * @author Tom Baeyens
 */
public class CallImpl extends AbstractBindableActivityImpl<Call> {
  
  // IDEA Boolean waitTillSubWorkflowEnds; add a configuration property to specify if this is fire-and-forget or wait-till-subworkflow-ends
  protected String subWorkflowId;
  protected String subWorkflowSource;

  public CallImpl() {
    super(Call.class);
  }

  @Override
  public void parse(ActivityImpl activityImpl, Call call, WorkflowParser parser) {
    super.parse(activityImpl, call, parser);

    this.subWorkflowId = call.getSubWorkflowId();
    this.subWorkflowSource = call.getSubWorkflowSource();
    
    WorkflowQuery workflowQuery = null;
    if (subWorkflowId!=null) {
      workflowQuery = new WorkflowQuery().workflowId(subWorkflowId);
    } else if (subWorkflowSource!=null) {
      workflowQuery = new WorkflowQuery().workflowSource(subWorkflowSource);
    }
    Workflow subWorkflow = null;
    if (workflowQuery!=null) {
      WorkflowEngine workflowEngine = parser.configuration.getWorkflowEngine();
      List<Workflow> workflows = workflowEngine.findWorkflows(workflowQuery);
      if (workflows!=null && !workflows.isEmpty()) {
        subWorkflow = workflows.get(0);
      }
    }
    
    if (subWorkflow!=null) {
      List<Variable> subWorkflowVariables = subWorkflow.getVariables();
      if (subWorkflowVariables!=null) {
        Map<String, Binding> inputBindingsApi = call.getInputBindings();
        for (Variable subWorkflowVariable: subWorkflowVariables) {
          String subWorkflowVariableId = subWorkflowVariable.getId();
          Binding inputBindingApi = inputBindingsApi.get(subWorkflowVariableId);
          parser.pushContext("inputBindings["+subWorkflowVariableId+"]", inputBindingApi, null);
          BindingImpl<?> bindingImpl = parser.parseBinding(inputBindingApi, subWorkflowVariable.getType(), false, subWorkflowVariableId, true);
          if (bindingImpl!=null) {
            if (inputBindings==null) {
              inputBindings = new HashMap<>();
            }
            inputBindings.put(subWorkflowVariableId, bindingImpl);
          }
          parser.popContext();
        }
      }
      
      // IDEA improve error message by validating the keys to be proper subworkflow variable ids
      this.outputBindings = activityApi.getOutputBindings();
    }
  }

  @Override
  public void execute(ActivityInstanceImpl activityInstance) {
    ActivityInstanceImpl activityInstanceImpl = (ActivityInstanceImpl) activityInstance;
    Configuration configuration = activityInstance.getConfiguration();

    String actualSubWorkflowId = null;
    if (this.subWorkflowId!=null) {
      actualSubWorkflowId = this.subWorkflowId;
    } else if (subWorkflowSource!=null) {
      WorkflowStore workflowStore = configuration.get(WorkflowStore.class);
      actualSubWorkflowId = workflowStore.findLatestWorkflowIdBySource(subWorkflowSource);
      if (actualSubWorkflowId==null) {
        throw new RuntimeException("Couldn't find sub workflow by source: "+subWorkflowSource);
      }
    } else {
      log.debug("No sub workflow binding was configured");
    }
    
    if (actualSubWorkflowId!=null) {
      Start start = new Start()
        .workflowId(actualSubWorkflowId);
      
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
