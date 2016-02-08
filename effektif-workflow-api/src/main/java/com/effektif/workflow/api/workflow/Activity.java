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
package com.effektif.workflow.api.workflow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.effektif.workflow.api.bpmn.BpmnReader;
import com.effektif.workflow.api.bpmn.BpmnWriter;
import com.effektif.workflow.api.bpmn.XmlElement;
import com.effektif.workflow.api.condition.Condition;
import com.effektif.workflow.api.types.DataType;

/**
 * Base class containing the configuration data for specific activity types.
 *
 * @author Tom Baeyens
 */
public abstract class Activity extends Scope {
  
  protected String id;
  protected String defaultTransitionId;
  protected MultiInstance multiInstance;
  protected List<Transition> outgoingTransitions;
  protected Map<String,InputParameter> inputs;

  /** Maps keys to variable IDs. */
  protected Map<String,String> outputs;
  
  @Override
  public void readBpmn(BpmnReader r) {
    super.readBpmn(r);
    id = r.readStringAttributeBpmn("id");
    defaultTransitionId = r.readStringAttributeEffektif("defaultTransitionId");
    r.startExtensionElements();

    for (XmlElement element : r.readElementsEffektif("multiInstance")) {
      r.startElement(element);
      multiInstance = new MultiInstance();
      multiInstance.readBpmn(r);
      r.endElement();
    }

    // Read input parameters.
    for (XmlElement inputElement : r.readElementsEffektif("input")) {
      if (inputs == null) {
        inputs = new HashMap<>();
      }
      r.startElement(inputElement);
      InputParameter parameter = new InputParameter();

      // Read single binding
      String key = r.readStringAttributeEffektif("key");
      List<Binding<Object>> singleBinding = r.readBindings("binding");
      if (singleBinding != null && !singleBinding.isEmpty()) {
        parameter.setBinding(singleBinding.get(0));
      }

      // Read nested bindings list
      for (XmlElement bindingsElement : r.readElementsEffektif("bindings")) {
        r.startElement(bindingsElement);
        List bindings = r.readBindings("binding");
        parameter.setBindings(bindings);
        r.endElement();
      }

      if (parameter.getBinding() != null || parameter.getBindings() != null) {
        inputs.put(key, parameter);
      }
      r.endElement();
    }

    // Read output parameters.
    for (XmlElement element : r.readElementsEffektif("output")) {
      if (outputs == null) {
        outputs = new HashMap<>();
      }
      r.startElement(element);
      String key = r.readStringAttributeEffektif("key");
      String variableId = r.readStringAttributeEffektif("id");
      outputs.put(key, variableId);
      r.endElement();
    }

    r.endExtensionElements();
  }

  @Override
  public void writeBpmn(BpmnWriter w) {
    w.writeIdAttributeBpmn("id", id);
    w.writeStringAttributeEffektif("defaultTransitionId", defaultTransitionId);

    super.writeBpmn(w);

    if (multiInstance != null || inputs != null || outputs != null) {
      w.startExtensionElements();

      if (multiInstance != null) {
        multiInstance.writeBpmn(w);
      }

      if (inputs != null) {
        for (Map.Entry<String, InputParameter> input : inputs.entrySet()) {
          w.startElementEffektif("input");
          w.writeStringAttributeEffektif("key", input.getKey());
          InputParameter parameter = input.getValue();
          if (parameter.getBinding() != null) {
            w.writeBinding("binding", parameter.getBinding());
          }
          if (parameter.getBindings() != null) {
            w.startElementEffektif("bindings");
            w.writeStringAttributeEffektif("key", input.getKey());
            List bindings = parameter.getBindings();
            w.writeBindings("binding", bindings);
            w.endElement();
          }
          w.endElement();
        }
      }

      if (outputs != null) {
        for (Map.Entry<String, String> parameter : outputs.entrySet()) {
          w.startElementEffektif("output");
          w.writeStringAttributeEffektif("key", parameter.getKey());
          w.writeStringAttributeEffektif("id", parameter.getValue());
          w.endElement();
        }
      }

      w.endExtensionElements();
    }

    if (multiInstance != null) {
      // TODO Don't write the multiInstanceLoopCharacteristics if it's already in Element.bpmn
      w.startElementBpmn("multiInstanceLoopCharacteristics");
      w.endElement();
    }
  }
  
  public String getId() {
    return this.id;
  }
  public void setId(String id) {
    this.id = id;
  }
  public Activity id(String id) {
    this.id = id;
    return this;
  }

  public String getDefaultTransitionId() {
    return this.defaultTransitionId;
  }
  public void setDefaultTransitionId(String defaultTransitionId) {
    this.defaultTransitionId = defaultTransitionId;
  }
  public Activity defaultTransitionId(String defaultTransitionId) {
    this.defaultTransitionId = defaultTransitionId;
    return this;
  }

  /**
   * @see <a href="https://github.com/effektif/effektif/wiki/Multi-instance-tasks">Multi-instance tasks</a>
   */
  public MultiInstance getMultiInstance() {
    return this.multiInstance;
  }
  public void setMultiInstance(MultiInstance multiInstance) {
    this.multiInstance = multiInstance;
  }
  public Activity multiInstance(MultiInstance multiInstance) {
    this.multiInstance = multiInstance;
    return this;
  }
  public Activity transitionTo(String toActivityId) {
    transitionTo(new Transition().toId(toActivityId));
    return this;
  }

  public Activity transitionWithConditionTo(Condition condition, String toActivityId) {
    transitionTo(new Transition()
      .condition(condition)
      .toId(toActivityId));
    return this;
  }

  public Activity transitionToNext() {
    transitionTo(new Transition().toNext());
    return this;
  }

  public Activity transitionTo(Transition transition) {
    if (this.outgoingTransitions==null) {
      this.outgoingTransitions = new ArrayList<>();
    }
    this.outgoingTransitions.add(transition);
    return this;
  }

  public List<Transition> getOutgoingTransitions() {
    return outgoingTransitions;
  }

  public void setOutgoingTransitions(List<Transition> outgoingTransitions) {
    this.outgoingTransitions = outgoingTransitions;
  }

  public Map<String, InputParameter> getInputs() {
    return inputs;
  }

  public void setInputs(Map<String, InputParameter> inputs) {
    this.inputs = inputs;
  }

  public Activity inputValue(String key, Object value) {
    inputValue(key, value, null);
    return this;
  }
  public Activity inputValue(String key, Object value, DataType dataType) {
    inputBinding(key, new Binding().value(value).type(dataType));
    return this;
  }

  public Activity inputExpression(String key, String expression) {
    inputBinding(key, new Binding().expression(expression));
    return this;
  }

  public Activity inputBinding(String key, Binding<?> binding) {
    if (inputs==null) {
      inputs = new HashMap<>();
    }
    inputs.put(key, new InputParameter().binding(binding));
    return this;
  }

  public Activity inputListExpression(String key, String expression) {
    inputListBinding(key, new Binding<Object>().expression(expression));
    return this;
  }

  public Activity inputListValue(String key, Object value) {
    inputListBinding(key, new Binding().value(value));
    return this;
  }
  protected void setInputBindings(String key, List<Binding<?>> bindings) {
    if (inputs==null) {
      inputs = new HashMap<>();
    }
    InputParameter parameter = inputs.get(key);
    if (parameter==null) {
      parameter = new InputParameter();
      inputs.put(key, parameter);
    }
    parameter.setBindings(bindings);
  }

  public Activity inputListBinding(String key, Binding<?> inputBinding) {
    if (inputs==null) {
      inputs = new HashMap<>();
    }
    InputParameter parameter = inputs.get(key);
    if (parameter==null) {
      parameter = new InputParameter();
      inputs.put(key, parameter);
    }
    parameter.addBinding(inputBinding);
    return this;
  }

  public Map<String, String> getOutputs() {
    return outputs;
  }
  public void setOutputs(Map<String, String> outputs) {
    this.outputs = outputs;
  }
  public Activity output(String key, String outputVariableId) {
    if (outputs==null) {
      outputs = new HashMap<>();
    }
    outputs.put(key, outputVariableId);
    return this;
  }
  
  @Override
  public Activity activity(Activity activity) {
    super.activity(activity);
    return this;
  }
  @Override
  public Activity activity(String id, Activity activity) {
    super.activity(id, activity);
    return this;
  }
  @Override
  public Activity transition(Transition transition) {
    super.transition(transition);
    return this;
  }
  @Override
  public Activity transition(String id, Transition transition) {
    super.transition(id, transition);
    return this;
  }
  @Override
  public Activity variable(Variable variable) {
    super.variable(variable);
    return this;
  }
  @Override
  public Activity timer(Timer timer) {
    super.timer(timer);
    return this;
  }
  @Override
  public Activity property(String key, Object value) {
    super.property(key, value);
    return this;
  }
}
