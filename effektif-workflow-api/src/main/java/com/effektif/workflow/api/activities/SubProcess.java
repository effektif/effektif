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

import com.effektif.workflow.api.bpmn.BpmnElement;
import com.effektif.workflow.api.bpmn.BpmnReader;
import com.effektif.workflow.api.bpmn.BpmnWriter;
import com.effektif.workflow.api.json.TypeName;
import com.effektif.workflow.api.model.WorkflowId;
import com.effektif.workflow.api.types.DataType;
import com.effektif.workflow.api.workflow.*;

/** 
 * Invokes another workflow and ends when the other workflow instance completes.
 *
 * @see <a href="https://github.com/effektif/effektif/wiki/Call-Activity">Call Activity</a>
 * @author Tom Baeyens
 */ 
@TypeName("subProcess")
@BpmnElement("subProcess")
public class SubProcess extends AbstractBindableActivity {

  protected WorkflowId subWorkflowId; 
  protected Binding<String> subWorkflowSourceId;

  @Override
  public void readBpmn(BpmnReader r) {
    subWorkflowId = r.readIdAttributeEffektif("subWorkflowId", WorkflowId.class);
    subWorkflowSourceId(r.readStringAttributeEffektif("subWorkflowSourceId"));
    super.readBpmn(r);
  }

  @Override
  public void writeBpmn(BpmnWriter w) {
    super.writeBpmn(w);
    w.writeIdAttributeEffektif("subWorkflowId", subWorkflowId);
    w.writeStringAttributeEffektif("subWorkflowSourceId", subWorkflowSourceId);
  }

  @Override
  public SubProcess id(String id) {
    super.id(id);
    return this;
  }

  public SubProcess subWorkflowId(WorkflowId subWorkflowId) {
    this.subWorkflowId = subWorkflowId;
    return this;
  }

  public SubProcess subWorkflowSourceId(String id) {
    setSubWorkflowSourceId(id);
    return this;
  }
  
  public WorkflowId getSubWorkflowId() {
    return subWorkflowId;
  }
  
  public String getSubWorkflowSourceId() {
    return subWorkflowSourceId == null ? null : subWorkflowSourceId.getValue();
  }

  public void setSubWorkflowId(WorkflowId id) {
    this.subWorkflowId = id;
  }
  
  public void setSubWorkflowSourceId(String id) {
    this.subWorkflowSourceId = new Binding<String>().value(id);
  }


  @Override
  public SubProcess inputExpression(String key, String expression) {
    super.inputExpression(key, expression);
    return this;
  }

  @Override
  public SubProcess inputValue(String subWorkflowKey, Object value) {
    super.inputValue(subWorkflowKey, value);
    return this;
  }

  @Override
  public SubProcess output(String subWorkflowKey, String variableId) {
    super.output(subWorkflowKey, variableId);
    return this;
  }

  /**
   * @see <a href="https://github.com/effektif/effektif/wiki/Multi-instance-tasks">Multi-instance tasks</a>
   */
  @Override
  public SubProcess multiInstance(MultiInstance multiInstance) {
    super.multiInstance(multiInstance);
    return this;
  }

  @Override
  public SubProcess transitionTo(String toActivityId) {
    super.transitionTo(toActivityId);
    return this;
  }

  @Override
  public SubProcess transitionTo(Transition transition) {
    super.transitionTo(transition);
    return this;
  }

  @Override
  public SubProcess activity(Activity activity) {
    super.activity(activity);
    return this;
  }

  @Override
  public SubProcess transition(Transition transition) {
    super.transition(transition);
    return this;
  }

  @Override
  public SubProcess variable(Variable variable) {
    super.variable(variable);
    return this;
  }

  @Override
  public SubProcess timer(Timer timer) {
    super.timer(timer);
    return this;
  }

  @Override
  public SubProcess property(String key, Object value) {
    super.property(key, value);
    return this;
  }
  @Override
  public SubProcess transitionToNext() {
    super.transitionToNext();
    return this;
  }
  @Override
  public SubProcess activity(String id, Activity activity) {
    super.activity(id, activity);
    return this;
  }
  @Override
  public SubProcess transition(String id, Transition transition) {
    super.transition(id, transition);
    return this;
  }
  @Override
  public SubProcess variable(String id, DataType type) {
    super.variable(id, type);
    return this;
  }
  @Override
  public SubProcess name(String name) {
    super.name(name);
    return this;
  }
}
