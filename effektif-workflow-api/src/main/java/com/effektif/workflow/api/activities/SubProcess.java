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
import com.effektif.workflow.api.bpmn.XmlElement;
import com.effektif.workflow.api.json.TypeName;
import com.effektif.workflow.api.model.WorkflowId;
import com.effektif.workflow.api.types.DataType;
import com.effektif.workflow.api.workflow.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** 
 * Invokes another workflow and ends when the other workflow instance completes.
 *
 * @see <a href="https://github.com/effektif/effektif/wiki/Call-Activity">Call Activity</a>
 * @author Tom Baeyens
 */ 
@TypeName("subProcess")
@BpmnElement("callActivity")
public class SubProcess extends AbstractBindableActivity {

  protected WorkflowId subWorkflowId; 
  protected Binding<String> subWorkflowSourceId;

  /**
   * Maps sub-workflow variable IDs to parent workflow bindings, e.g. variable expressions.
   */
  protected Map<String,Binding> subWorkflowInputs;

  @Override
  public void readBpmn(BpmnReader r) {
    subWorkflowId = r.readIdAttributeEffektif("subWorkflowId", WorkflowId.class);
    subWorkflowSourceId(r.readStringAttributeEffektif("subWorkflowSourceId"));
    super.readBpmn(r);

    r.startExtensionElements();

    // Read sub-workflow inputs.
    for (XmlElement inputElement : r.readElementsEffektif("subWorkflowInput")) {
      if (subWorkflowInputs == null) {
        subWorkflowInputs = new HashMap<>();
      }
      r.startElement(inputElement);
      String variableId = r.readStringAttributeEffektif("variableId");
      List<Binding<Object>> singleBinding = r.readBindings("binding");
      if (singleBinding != null && !singleBinding.isEmpty()) {
        subWorkflowInputs.put(variableId, singleBinding.get(0));
      }
      r.endElement();
    }

    r.endExtensionElements();
  }

  @Override
  public void writeBpmn(BpmnWriter w) {
    super.writeBpmn(w);
    w.writeIdAttributeEffektif("subWorkflowId", subWorkflowId);
    if (subWorkflowSourceId != null) {
      w.writeStringAttributeEffektif("subWorkflowSourceId", subWorkflowSourceId.getValue());
    }
    if (subWorkflowInputs != null) {
      w.startExtensionElements();
      for (Map.Entry<String, Binding> input : subWorkflowInputs.entrySet()) {
        if (input.getKey() != null && input.getValue() != null) {
          w.startElementEffektif("subWorkflowInput");
          w.writeStringAttributeEffektif("variableId", input.getKey());
          w.writeBinding("binding", input.getValue());
          w.endElement();
        }
      }
      w.endExtensionElements();
    }
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

  public Map<String, Binding> getSubWorkflowInputs() {
    return subWorkflowInputs;
  }

  public void setSubWorkflowInputs(Map<String, Binding> subWorkflowInputs) {
    this.subWorkflowInputs = subWorkflowInputs;
  }

  @Override
  public SubProcess inputExpression(String subWorkflowVariableId, String expression) {
    if (subWorkflowInputs == null) {
      subWorkflowInputs = new HashMap<>();
    }
    subWorkflowInputs.put(subWorkflowVariableId, new Binding().expression(expression));
    return this;
  }

  @Override
  public SubProcess inputValue(String subWorkflowVariableId, Object value) {
    if (subWorkflowInputs == null) {
      subWorkflowInputs = new HashMap<>();
    }
    subWorkflowInputs.put(subWorkflowVariableId, new Binding().value(value));
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
