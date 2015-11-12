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
package com.effektif.workflow.impl.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;

import com.effektif.workflow.api.model.Message;
import com.effektif.workflow.api.workflow.Activity;
import com.effektif.workflow.api.workflow.Binding;
import com.effektif.workflow.api.workflow.InputParameter;
import com.effektif.workflow.impl.WorkflowEngineImpl;
import com.effektif.workflow.impl.WorkflowParser;
import com.effektif.workflow.impl.workflow.ActivityImpl;
import com.effektif.workflow.impl.workflow.InputParameterImpl;
import com.effektif.workflow.impl.workflow.MultiInstanceImpl;
import com.effektif.workflow.impl.workflowinstance.ActivityInstanceImpl;


public abstract class AbstractActivityType<T extends Activity> implements ActivityType<T> {
  
  public static final Logger log = WorkflowEngineImpl.log;
  
  /** the api activity containing the serializable configuration */
  public T activity;
  public Class<?> activityApiClass;
  public MultiInstanceImpl multiInstance;
  public Map<String,InputParameterImpl> inputs;
  public Map<String,String> outputs;

  public AbstractActivityType(Class<T> activityApiClass) {
    this.activityApiClass = activityApiClass;
  }
  
  public Class< ? > getActivityApiClass() {
    return activityApiClass;
  }
  
  @Override
  public ActivityDescriptor getDescriptor() {
    return null;
  }
  
  @Override
  public void parse(ActivityImpl activityImpl, T activity, WorkflowParser parser) {
    this.activity = activity;
    this.multiInstance = parser.parseMultiInstance(activity.getMultiInstance());
    
    Map<String, InputParameter> inputs = activity.getInputs();
    if (inputs!=null) {
      this.inputs = new HashMap<>();
      parser.pushContext("inputs", inputs, this.inputs, null);
      for (String key: inputs.keySet()) {
        InputParameter inParameter = inputs.get(key);
        InputParameterImpl inParameterImpl = new InputParameterImpl(key);
        parser.pushContext(key, inParameter, inParameterImpl, null);
        Binding< ? > singleBinding = inParameter.getBinding();
        if (singleBinding!=null) {
          inParameterImpl.binding = parser.parseBinding(singleBinding, "binding");
        }
        List<Binding<?>> listBindings = inParameter.getBindings();
        if (listBindings!=null) {
          inParameterImpl.bindings = new ArrayList<>();
          for (Binding<?> listBinding: listBindings) {
            inParameterImpl.bindings.add(parser.parseBinding(listBinding, "binding"));
          }
        }
        inParameterImpl.properties = inParameter.getProperties();
        parser.popContext();
        this.inputs.put(key, inParameterImpl);
      }
      parser.popContext();
    }
    
    this.outputs = activity.getOutputs();

  }
  
  /** returns the API activity object */
  @Override
  public T getActivity() {
    return activity;
  }

  public abstract void execute(ActivityInstanceImpl activityInstance);

  public void message(ActivityInstanceImpl activityInstance, Message message) {
    activityInstance.setVariableValues(message);
    activityInstance.onwards();
  }
  
  public void ended(ActivityInstanceImpl activityInstance, ActivityInstanceImpl nestedEndedActivityInstance) {
    if (!activityInstance.hasOpenActivityInstances()) {
      activityInstance.end();
      activityInstance.propagateToParent();
    }
  }
  
  @Override
  public boolean isAsync(ActivityInstanceImpl activityInstance) {
    return false;
  }

  @Override
  public boolean isFlushSkippable() {
    return false;
  }
  
  @Override
  public MultiInstanceImpl getMultiInstance() {
    return multiInstance;
  }

  @Override
  public boolean saveTransitionsTaken() {
    return false;
  }
  
  @Override
  public Map<String, InputParameterImpl> getInputs() {
    return inputs;
  }

  @Override
  public Map<String, String> getOutputs() {
    return outputs;
  }
}
