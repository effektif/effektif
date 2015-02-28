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
package com.effektif.workflow.api.activities;

import com.effektif.workflow.api.types.Type;
import com.effektif.workflow.api.workflow.Activity;
import com.effektif.workflow.api.workflow.MultiInstance;
import com.effektif.workflow.api.workflow.Timer;
import com.effektif.workflow.api.workflow.Transition;
import com.effektif.workflow.api.workflow.Variable;
import com.fasterxml.jackson.annotation.JsonTypeName;

/** 
 * Invokes another workflow and ends when the other workflow instance completes.
 *
 * BPMN XML: {@code <callActivity id="investigateRequest"/>}
 *
 * @author Tom Baeyens
 */ 
@JsonTypeName("call")
public class Call extends AbstractBindableActivity {

  protected String subWorkflowId; 
  protected String subWorkflowSource; 
  
  public Call() {
  }

  public Call(String id) {
    super(id);
  }

  public Call subWorkflowId(String subWorkflowId) {
    this.subWorkflowId = subWorkflowId;
    return this;
  }

  public Call subWorkflowName(String subWorkflowSource) {
    this.subWorkflowSource = subWorkflowSource;
    return this;
  }
  
  public String getSubWorkflowId() {
    return subWorkflowId;
  }
  
  public String getSubWorkflowSource() {
    return subWorkflowSource;
  }

  public void setSubWorkflowId(String subWorkflowId) {
    this.subWorkflowId = subWorkflowId;
  }
  
  public void setSubWorkflowSource(String subWorkflowSource) {
    this.subWorkflowSource = subWorkflowSource;
  }
  

  @Override
  public Call inputValue(String subWorkflowKey, Object value) {
    super.inputValue(subWorkflowKey, value);
    return this;
  }

  @Override
  public Call outputBinding(String subWorkflowKey, String variableId) {
    super.outputBinding(subWorkflowKey, variableId);
    return this;
  }

  @Override
  public Call multiInstance(MultiInstance multiInstance) {
    super.multiInstance(multiInstance);
    return this;
  }

  @Override
  public Call transitionTo(String toActivityId) {
    super.transitionTo(toActivityId);
    return this;
  }

  @Override
  public Call transitionTo(Transition transition) {
    super.transitionTo(transition);
    return this;
  }

  @Override
  public Call activity(Activity activity) {
    super.activity(activity);
    return this;
  }

  @Override
  public Call transition(Transition transition) {
    super.transition(transition);
    return this;
  }

  @Override
  public Call variable(Variable variable) {
    super.variable(variable);
    return this;
  }

  @Override
  public Call timer(Timer timer) {
    super.timer(timer);
    return this;
  }

  @Override
  public Call id(String id) {
    super.id(id);
    return this;
  }

  @Override
  public Call property(String key, Object value) {
    super.property(key, value);
    return this;
  }

  @Override
  public Call inputExpression(String key, String variableId, String... fields) {
    super.inputExpression(key, variableId, fields);
    return this;
  }
  @Override
  public Call transitionToNext() {
    super.transitionToNext();
    return this;
  }
  @Override
  public Call activity(String id, Activity activity) {
    super.activity(id, activity);
    return this;
  }
  @Override
  public Call transition(String id, Transition transition) {
    super.transition(id, transition);
    return this;
  }
  @Override
  public Call variable(String id, Type type) {
    super.variable(id, type);
    return this;
  }
  @Override
  public Call name(String name) {
    super.name(name);
    return this;
  }
}
