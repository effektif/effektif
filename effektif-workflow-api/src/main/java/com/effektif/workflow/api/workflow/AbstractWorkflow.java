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

import java.util.Map;

import com.effektif.workflow.api.bpmn.BpmnReader;
import com.effektif.workflow.api.bpmn.BpmnWriter;
import com.effektif.workflow.api.model.WorkflowId;
import com.effektif.workflow.api.workflow.diagram.Diagram;

/**
 * @author Tom Baeyens
 */
public abstract class AbstractWorkflow extends Scope {

  protected WorkflowId id;
  protected Trigger trigger;
  protected Boolean enableCases;
  protected Diagram diagram;

  public abstract String getSourceWorkflowId();
  
  @Override
  public void readBpmn(BpmnReader r) {
    id = r.readIdAttributeBpmn("id", WorkflowId.class);
    super.readBpmn(r);
  }

  @Override
  public void writeBpmn(BpmnWriter w) {
    w.writeIdAttributeBpmn("id", id);
    w.writeStringAttributeBpmn("name", name);
    writeSimpleProperties(w);
  }

  /**
   * Serialises properties with simple Java types as String values.
   */
  private void writeSimpleProperties(BpmnWriter w) {
    if (properties != null) {
      w.startExtensionElements();
      w.writeSimpleProperties(properties);
      w.endExtensionElements();
    }
  }

  public WorkflowId getId() {
    return this.id;
  }
  public void setId(WorkflowId id) {
    this.id = id;
  }
  public AbstractWorkflow id(WorkflowId id) {
    this.id = id;
    return this;
  }

  public Trigger getTrigger() {
    return this.trigger;
  }
  public void setTrigger(Trigger trigger) {
    this.trigger = trigger;
  }
  public AbstractWorkflow trigger(Trigger trigger) {
    this.trigger = trigger;
    return this;
  }
  
  public boolean isEnableCases() {
    return Boolean.TRUE.equals(this.enableCases);
  }
  public void setEnableCases(Boolean enableCases) {
    this.enableCases = enableCases;
  }
  /** enables cases, which means that each workflow instance will also create a corresponding case, 
   * which is a collaboration space around the tasks for a single workflow instance.
   * If enabled, cases will have the same id internal string value as the workflow instances ids. */
  public AbstractWorkflow enableCases() {
    this.enableCases = true;
    return this;
  }

  public Diagram getDiagram() {
    return diagram;
  }

  public void setDiagram(Diagram diagram) {
    this.diagram = diagram;
  }
}
