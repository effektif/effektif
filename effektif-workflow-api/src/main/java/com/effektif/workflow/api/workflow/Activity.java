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
import com.effektif.workflow.api.condition.Condition;


/** Base class containing the configuration data for 
 * specific activity types.
 * 
 * @author Tom Baeyens
 */
public abstract class Activity extends Scope {
  
  protected String id;
  protected String defaultTransitionId;
  protected MultiInstance multiInstance;
  protected List<Transition> outgoingTransitions;
  protected Map<String,InputParameter> in;
  protected Map<String,OutputParameter> out;
  
  @Override
  public void readBpmn(BpmnReader r) {
    id = r.readStringAttributeBpmn("id");
    // outgoing transitions doesn't have to be bpmnized
    super.readBpmn(r);
  }

  @Override
  public void writeBpmn(BpmnWriter w) {
    w.writeStringAttributeBpmn("id", id);
    // outgoing transitions doesn't have to be bpmnized
    super.writeBpmn(w);
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
    transitionTo(new Transition().to(toActivityId));
    return this;
  }
  
  public Activity transitionWithConditionTo(Condition condition, String toActivityId) {
    transitionTo(new Transition()
      .condition(condition)
      .to(toActivityId));
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
  
  public Map<String, InputParameter> getIn() {
    return in;
  }
  public void setIn(Map<String, InputParameter> in) {
    this.in = in;
  }
  public Activity inValue(String key, Object value) {
    inBinding(key, new Binding().value(value));
    return this;
  }
  public Activity inExpression(String key, String expression) {
    inBinding(key, new Binding().expression(expression));
    return this;
  }
  public Activity inBinding(String key, Binding<?> binding) {
    if (in==null) {
      in = new HashMap<>();
    }
    in.put(key, new InputParameter().binding(binding));
    return this;
  }
  public Activity inListBinding(String key, Binding<?> inBinding) {
    if (in==null) {
      in = new HashMap<>();
    }
    InputParameter inParameter = in.get(key);
    if (inParameter==null) {
      inParameter = new InputParameter();
      in.put(key, inParameter);
    }
    inParameter.addBinding(inBinding);
    return this;
  }

  public Map<String, OutputParameter> getOut() {
    return out;
  }
  public void setOut(Map<String, OutputParameter> out) {
    this.out = out;
  }
  public Activity out(String key, String outputVariableId) {
    if (out==null) {
      out = new HashMap<>();
    }
    out.put(key, new OutputParameter()
      .variableId(outputVariableId));
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
