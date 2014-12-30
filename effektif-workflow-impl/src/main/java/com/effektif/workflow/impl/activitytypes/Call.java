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
import com.effektif.workflow.api.workflow.Activity;
import com.effektif.workflow.api.workflowinstance.WorkflowInstance;
import com.effektif.workflow.impl.StartImpl;
import com.effektif.workflow.impl.definition.WorkflowImpl;
import com.effektif.workflow.impl.instance.ActivityInstanceImpl;
import com.effektif.workflow.impl.instance.WorkflowInstanceImpl;
import com.effektif.workflow.impl.plugin.AbstractActivityType;
import com.effektif.workflow.impl.plugin.Binding;
import com.effektif.workflow.impl.plugin.ConfigurationField;
import com.effektif.workflow.impl.plugin.ControllableActivityInstance;
import com.effektif.workflow.impl.plugin.Label;
import com.effektif.workflow.impl.plugin.Validator;


/**
 * @author Walter White
 */
public class Call extends AbstractActivityType {

  @ConfigurationField
  @Label("Subprocess name")
  Binding<String> subProcessNameBinding;

  @ConfigurationField
  @Label("Subprocess id")
  Binding<String> subProcessIdBinding;
  
  /** specifies which variables of this workflow instance (keys) have to be copied to 
   * variables in the called workflow instance (values). */
  @ConfigurationField
  @Label("Input variable mappings")
  List<CallMapping> inputMappings;
  
  /** specifies which variables of the called process (keys) have to be copied to 
   * variables in this process (values). */
  @ConfigurationField
  @Label("Output variable mappings")
  List<CallMapping> outputMappings;

  @Override
  public void start(ControllableActivityInstance activityInstance) {
    ActivityInstanceImpl activityInstanceImpl = (ActivityInstanceImpl) activityInstance;
    String subProcessId = null;
    if (subProcessIdBinding!=null) {
      subProcessId = activityInstance.getValue(subProcessIdBinding);
    } else if (subProcessNameBinding!=null) {
      String subProcessName = activityInstance.getValue(subProcessIdBinding);
      WorkflowImpl subprocess = activityInstanceImpl.workflowEngine.newWorkflowQuery()
        .name(subProcessName)
        .orderByDeployTimeDescending()
        .get();
      if (subprocess!=null) {
        subProcessId = subprocess.id;
      } else {
        throw new RuntimeException("Couldn't find subprocess by name: "+subProcessName);
      }
    }

    StartCommand start = activityInstanceImpl
            .newSubWorkflowStart(subProcessId)
            .transientContext(activityInstance.getWorkflowInstance().getTransientContext());
    
    if (inputMappings!=null) {
      for (CallMapping inputMapping: inputMappings) {
        Object value = activityInstance.getValue(inputMapping.sourceBinding);
        start.variableValue(inputMapping.destinationVariableId, value);
      }
    }
    
    WorkflowInstance calledProcessInstance = start.startWorkflowInstance();
    activityInstanceImpl.setCalledWorkflowInstanceId(calledProcessInstance.getId()); 
  }
  
  public void calledProcessInstanceEnded(ControllableActivityInstance activityInstance, WorkflowInstanceImpl calledProcessInstance) {
    if (outputMappings!=null) {
      for (CallMapping outputMapping: outputMappings) {
        Object value = calledProcessInstance.getValue(outputMapping.sourceBinding);
        activityInstance.setVariableValue(outputMapping.destinationVariableId, value);
      }
    }
  }

  public Call subProcessId(String subProcessId) {
    return subProcessId(new Binding<String>().value(subProcessId));
  }

  public Call subProcessIdExpression(String subProcessIdExpression) {
    return subProcessId(new Binding<String>().expression(subProcessIdExpression));
  }

  public Call subProcessIdVariable(String subProcessIdVariableId) {
    return subProcessId(new Binding<String>().variableDefinitionId(subProcessIdVariableId));
  }

  public Call subProcessId(Binding<String> subProcessIdBinding) {
    this.subProcessIdBinding = subProcessIdBinding;
    return this;
  }
  
  public Call inputMapping(String callerVariableId, String calledVariableId) {
    return inputMapping(new Binding<Object>().variableDefinitionId(callerVariableId), calledVariableId);
  }

  public Call inputMapping(Binding<Object> callerBinding, String calledVariableId) {
    CallMapping inputMapping = new CallMapping()
      .sourceBinding(callerBinding)
      .destinationVariableId(calledVariableId);
    if (inputMappings==null) {
      inputMappings = new ArrayList<>();
    }
    inputMappings.add(inputMapping);
    return this;
  }

  public Call outputMapping(String calledVariableId, String callerVariableId) {
    return outputMapping(new Binding<Object>().variableDefinitionId(calledVariableId), callerVariableId);
  }

  public Call outputMapping(Binding<Object> calledBinding, String callerVariableId) {
    CallMapping inputMapping = new CallMapping()
      .sourceBinding(calledBinding)
      .destinationVariableId(callerVariableId);
    if (inputMappings==null) {
      inputMappings = new ArrayList<>();
    }
    inputMappings.add(inputMapping);
    return this;
  }

  @Override
  public void validate(Activity activity, Validator validator) {
    if (subProcessNameBinding!=null) {
      subProcessNameBinding.validate(null, validator, "subProcessNameBinding");
    }
    if (subProcessIdBinding!=null) {
      subProcessIdBinding.validate(null, validator, "subProcessIdBinding");
    }
    if (inputMappings!=null) {
      for (CallMapping callMapping: inputMappings) {
        callMapping.validate(validator, "inputMappings");
      }
    }
    if (outputMappings!=null) {
      for (CallMapping callMapping: outputMappings) {
        callMapping.validate(validator, "inputMappings");
      }
    }
  }
}
