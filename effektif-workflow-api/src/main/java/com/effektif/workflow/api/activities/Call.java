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

import com.effektif.workflow.api.mapper.BpmnElement;
import com.effektif.workflow.api.mapper.BpmnFieldMappings;
import com.effektif.workflow.api.mapper.BpmnMappable;
import com.effektif.workflow.api.mapper.Reader;
import com.effektif.workflow.api.mapper.TypeName;
import com.effektif.workflow.api.mapper.Writer;
import com.effektif.workflow.api.model.WorkflowId;
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
 * @see <a href="https://github.com/effektif/effektif/wiki/Call-Activity">Call Activity</a>
 * @author Tom Baeyens
 */ 
@JsonTypeName("call")
@TypeName("call")
@BpmnElement("callActivity")
public class Call extends AbstractBindableActivity implements BpmnMappable {

  protected WorkflowId subWorkflowId; 
  protected String subWorkflowSource;
  
  @Override
  public void initializeBpmnFieldMappings(BpmnFieldMappings fieldMappings) {
    fieldMappings.mapToEffektif("subWorkflowId");
    fieldMappings.mapToEffektif("subWorkflowSource");
  }

  @Override
  public void writeFields(Writer w) {
    super.writeFields(w);
    w.writeId("subWorkflowId", subWorkflowId);
    w.writeString("subWorkflowSource", subWorkflowSource);
  }

  @Override
  public void readFields(Reader r) {
    subWorkflowId = r.readId("subWorkflowId", WorkflowId.class);
    subWorkflowSource = r.readString("subWorkflowSource");
    super.readFields(r);
  }

  @Override
  public Call id(String id) {
    super.id(id);
    return this;
  }

  public Call subWorkflowId(WorkflowId subWorkflowId) {
    this.subWorkflowId = subWorkflowId;
    return this;
  }

  public Call subWorkflowName(String subWorkflowSource) {
    this.subWorkflowSource = subWorkflowSource;
    return this;
  }
  
  public WorkflowId getSubWorkflowId() {
    return subWorkflowId;
  }
  
  public String getSubWorkflowSource() {
    return subWorkflowSource;
  }

  public void setSubWorkflowId(WorkflowId subWorkflowId) {
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

  /**
   * @see <a href="https://github.com/effektif/effektif/wiki/Multi-instance-tasks">Multi-instance tasks</a>
   */
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
  public Call property(String key, Object value) {
    super.property(key, value);
    return this;
  }

  @Override
  public Call inputExpression(String key, String expression) {
    super.inputExpression(key, expression);
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
