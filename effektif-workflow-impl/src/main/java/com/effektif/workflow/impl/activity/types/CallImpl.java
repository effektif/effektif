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
import java.util.Set;

import com.effektif.workflow.api.Configuration;
import com.effektif.workflow.api.WorkflowEngine;
import com.effektif.workflow.api.activities.Call;
import com.effektif.workflow.api.model.TriggerInstance;
import com.effektif.workflow.api.model.WorkflowId;
import com.effektif.workflow.api.query.WorkflowQuery;
import com.effektif.workflow.api.workflow.Binding;
import com.effektif.workflow.api.workflow.ExecutableWorkflow;
import com.effektif.workflow.api.workflow.Variable;
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
  protected WorkflowId subWorkflowId;
  protected String subWorkflowSource;

  public CallImpl() {
    super(Call.class);
  }

  public CallImpl(Class<Call> activityApiClass) {
    super(activityApiClass);
  }

  /**
   * Returns an optional set of variable IDs to be used as inputs, or null if all available variables are to be used.
   * Used to override the default behaviour to limit the scope exposed to the sub-workflow.
   */
  protected Set<String> inputVariableIds(ExecutableWorkflow subWorkflow) {
    return null;
  }

  @Override
  public void parse(ActivityImpl activityImpl, Call call, WorkflowParser parser) {
    super.parse(activityImpl, call, parser);

    this.subWorkflowId = call.getSubWorkflowId();
    this.subWorkflowSource = call.getSubWorkflowSourceId();
    
    WorkflowQuery workflowQuery = null;
    if (subWorkflowId!=null) {
      workflowQuery = new WorkflowQuery().workflowId(subWorkflowId);
    } else if (subWorkflowSource!=null) {
      workflowQuery = new WorkflowQuery().workflowSource(subWorkflowSource);
    }
    ExecutableWorkflow subWorkflow = null;
    if (workflowQuery!=null) {
      WorkflowEngine workflowEngine = parser.configuration.getWorkflowEngine();
      List<ExecutableWorkflow> workflows = workflowEngine.findWorkflows(workflowQuery);
      if (workflows!=null && !workflows.isEmpty()) {
        subWorkflow = workflows.get(0);
      }
    }

    parseInputsOutputs(call, parser, subWorkflow);
  }


  private void parseInputsOutputs(Call call, WorkflowParser parser, ExecutableWorkflow subWorkflow) {
    if (subWorkflow != null) {
      List<Variable> subWorkflowVariables = subWorkflow.getVariables();
      Set<String> includedVariableIds = inputVariableIds(subWorkflow);
      Map<String, Binding> callInputs = call.getInputBindings();
      if (subWorkflowVariables != null && callInputs != null) {
        for (Variable variable : subWorkflowVariables) {
          String variableId = variable.getId();
          boolean includeVariable = includedVariableIds == null || includedVariableIds.contains(variableId);
          if (includeVariable) {
            Binding callInput = callInputs.get(variableId);
            parser.pushContext("inputBindings[" + variableId + "]", callInput, null, null);
            BindingImpl<?> bindingImpl = parser.parseBinding(callInput, variableId, false, variable.getType());
            if (bindingImpl != null) {
              if (inputBindings == null) {
                inputBindings = new HashMap<>();
              }
              inputBindings.put(variableId, bindingImpl);
            }
            parser.popContext();
          }
        }
      }

      // IDEA improve error message by validating the keys to be proper sub-workflow variable IDs
      this.outputBindings = activity.getOutputBindings();
    } else if (inputBindings!=null && !inputBindings.isEmpty()) {
      parser.addWarning("input and output bindings could not be parsed because the sub-workflow was not found");
    }
  }

  @Override
  public void execute(ActivityInstanceImpl activityInstance) {
    Configuration configuration = activityInstance.getConfiguration();

    WorkflowId actualSubWorkflowId = null;
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
      TriggerInstance triggerInstance = new TriggerInstance()
        .workflowId(actualSubWorkflowId);
      
      triggerInstance.setCallerWorkflowInstanceId(activityInstance.workflowInstance.id);
      triggerInstance.setCallerActivityInstanceId(activityInstance.id);
      
      if (inputBindings!=null) {
        for (String subWorkflowKey: inputBindings.keySet()) {
          BindingImpl<?> subWorkflowBinding = inputBindings.get(subWorkflowKey);
          Object value = activityInstance.getValue(subWorkflowBinding);
          triggerInstance.data(subWorkflowKey, value);
        }
      }
      
      startWorkflowInstance(activityInstance, triggerInstance);
    } else {
      log.debug("Skipping call activity because no sub workflow was defined");
      activityInstance.onwards();
    }
  }

  /**
   * Starts the subworkflow instance.
   */
  protected void startWorkflowInstance(ActivityInstanceImpl activityInstance, TriggerInstance triggerInstance) {
    Configuration configuration = activityInstance.getConfiguration();
    WorkflowEngineImpl workflowEngine = configuration.get(WorkflowEngineImpl.class);
    WorkflowInstanceImpl calledWorkflowInstance = workflowEngine.startInitialize(triggerInstance);
    calledWorkflowInstance.addLockedWorkflowInstance(activityInstance.workflowInstance);
    activityInstance.setCalledWorkflowInstanceId(calledWorkflowInstance.getId());
    workflowEngine.startExecute(calledWorkflowInstance);
  }

  public void calledWorkflowInstanceEnded(ActivityInstanceImpl callerActivity, WorkflowInstanceImpl calledProcessInstance) {
    if (outputBindings!=null) {
      for (String subWorkflowVariableId: outputBindings.keySet()) {
        String variableId = outputBindings.get(subWorkflowVariableId);
        TypedValueImpl typedValue = calledProcessInstance.getTypedValue(subWorkflowVariableId);
        callerActivity.setVariableValue(variableId, typedValue);
      }
    }
  }
}
