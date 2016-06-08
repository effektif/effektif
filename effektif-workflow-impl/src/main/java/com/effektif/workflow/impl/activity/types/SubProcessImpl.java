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
import com.effektif.workflow.api.WorkflowEngine;
import com.effektif.workflow.api.activities.SubProcess;
import com.effektif.workflow.api.model.Message;
import com.effektif.workflow.api.model.TriggerInstance;
import com.effektif.workflow.api.model.WorkflowId;
import com.effektif.workflow.api.query.OrderDirection;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Tom Baeyens
 */
public class SubProcessImpl extends AbstractBindableActivityImpl<SubProcess> {

  // IDEA Boolean waitTillSubWorkflowEnds; add a configuration property to specify if this is fire-and-forget or wait-till-subworkflow-ends
  protected WorkflowId subWorkflowId;
  protected String subWorkflowSourceId;

  public SubProcessImpl() {
    super(SubProcess.class);
  }

  public SubProcessImpl(Class<SubProcess> activityApiClass) {
    super(activityApiClass);
  }

  @Override
  public void parse(ActivityImpl activityImpl, SubProcess subProcess, WorkflowParser parser) {
    super.parse(activityImpl, subProcess, parser);

    this.subWorkflowId = subProcess.getSubWorkflowId();
    this.subWorkflowSourceId = subProcess.getSubWorkflowSourceId();
    
    WorkflowQuery workflowQuery = null;
    if (subWorkflowId!=null) {
      workflowQuery = new WorkflowQuery().workflowId(subWorkflowId);
    } else if (subWorkflowSourceId !=null) {
      workflowQuery = new WorkflowQuery().workflowSource(subWorkflowSourceId)
        .orderByCreateTime(OrderDirection.desc).limit(1);
    }
    ExecutableWorkflow subWorkflow = null;
    if (workflowQuery!=null) {
      WorkflowEngine workflowEngine = parser.configuration.getWorkflowEngine();
      List<ExecutableWorkflow> workflows = workflowEngine.findWorkflows(workflowQuery);
      if (workflows!=null && !workflows.isEmpty()) {
        subWorkflow = workflows.get(0);
      }
    }

    if (subWorkflow != null) {
      if (subWorkflowId == null) {
        subWorkflowId = subWorkflow.getId();
      }
      if (subWorkflowSourceId == null) {
        subWorkflowSourceId = subWorkflow.getSourceWorkflowId();
      }

      List<Variable> subWorkflowVariables = subWorkflow.getVariables();
      Map<String, Binding> inputBindingsApi = subProcess.getSubWorkflowInputs();
      if (subWorkflowVariables != null && inputBindingsApi != null) {
        for (Variable subWorkflowVariable : subWorkflowVariables) {
          String subWorkflowVariableId = subWorkflowVariable.getId();
          Binding inputBindingApi = inputBindingsApi.get(subWorkflowVariableId);
          parser.pushContext("inputBindings[" + subWorkflowVariableId + "]", inputBindingApi, null, null);
          BindingImpl<?> bindingImpl = parser
            .parseBinding(inputBindingApi, subWorkflowVariableId, false, subWorkflowVariable.getType());
          if (bindingImpl != null) {
            if (inputBindings == null) {
              inputBindings = new HashMap<>();
            }
            inputBindings.put(subWorkflowVariableId, bindingImpl);
          }
          parser.popContext();
        }
      }
    }
  }


  @Override
  public void execute(ActivityInstanceImpl activityInstance) {
    Configuration configuration = activityInstance.getConfiguration();

    WorkflowId actualSubWorkflowId = null;
    if (this.subWorkflowId != null) {
      actualSubWorkflowId = this.subWorkflowId;
    }
    else if (subWorkflowSourceId != null) {
      WorkflowStore workflowStore = configuration.get(WorkflowStore.class);
      actualSubWorkflowId = workflowStore.findLatestWorkflowIdBySource(subWorkflowSourceId);
    }

    if (actualSubWorkflowId != null) {
      TriggerInstance triggerInstance = new TriggerInstance()
        .sourceWorkflowId(subWorkflowSourceId)
        .workflowId(actualSubWorkflowId);

      triggerInstance.setCallingWorkflowInstanceId(activityInstance.workflowInstance.id);
      triggerInstance.setCallingActivityInstanceId(activityInstance.id);

      if (inputBindings != null) {
        for (String subWorkflowVariableId: inputBindings.keySet()) {
          BindingImpl<?> subWorkflowBinding = inputBindings.get(subWorkflowVariableId);
          Object value = activityInstance.getValue(subWorkflowBinding);
          triggerInstance.data(subWorkflowVariableId, value);
        }
      }

      startWorkflowInstance(activityInstance, triggerInstance);
    } else {
      reportError(activityInstance, "Cannot execute sub-process action because no sub-process was configured.");
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

  public void calledWorkflowInstanceEnded(final ActivityInstanceImpl callingActivityInstance, WorkflowInstanceImpl calledWorkflowInstance) {
    mapOutputVariables(callingActivityInstance, calledWorkflowInstance);

    calledWorkflowInstance.workflow.getWorkflowEngine()
            .send(new Message()
                    .workflowInstanceId(callingActivityInstance.workflowInstance.getId())
                    .activityInstanceId(callingActivityInstance.getId()),
                callingActivityInstance.workflowInstance);
  }

  protected void mapOutputVariables(ActivityInstanceImpl callerActivityInstance, WorkflowInstanceImpl calledProcessInstance) {
    if (outputBindings!=null) {
      for (String subWorkflowVariableId: outputBindings.keySet()) {
        String variableId = outputBindings.get(subWorkflowVariableId);
        TypedValueImpl typedValue = calledProcessInstance.getTypedValue(subWorkflowVariableId);
        callerActivityInstance.setVariableValue(variableId, typedValue);
      }
    }
  }

  protected void reportError(ActivityInstanceImpl activityInstance, String message) {
    log.warn(message);
  }
}
