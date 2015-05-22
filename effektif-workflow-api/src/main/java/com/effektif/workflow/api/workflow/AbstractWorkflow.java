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

import com.effektif.workflow.api.bpmn.BpmnReader;
import com.effektif.workflow.api.bpmn.BpmnWriter;
import com.effektif.workflow.api.model.WorkflowId;


/**
 * @author Tom Baeyens
 */
public abstract class AbstractWorkflow extends Scope {

  protected WorkflowId id;
  protected Trigger trigger;
  protected Boolean enableCases;
  protected String caseNameTemplate;
  
  public abstract String getSourceWorkflowId();
  
//  @Override
//  public void readJson(JsonReader r) {
//    id = r.readId();
//    trigger = r.readObject("trigger");
//    super.readJson(r);
//  }
//
//  @Override
//  public void writeJson(JsonWriter w) {
//    w.writeId(id);
//    w.writeWritable("trigger", trigger);
//    super.writeJson(w);
//  }

  @Override
  public void readBpmn(BpmnReader r) {
    id = r.readIdAttributeBpmn("id", WorkflowId.class);
    super.readBpmn(r);
  }

  @Override
  public void writeBpmn(BpmnWriter w) {
    w.writeIdAttributeBpmn("id", id);
    w.writeStringAttributeBpmn("name", name);
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
  
  /** optional template to initialize the name of cases */
  public String getCaseNameTemplate() {
    return this.caseNameTemplate;
  }
  public void setCaseNameTemplate(String caseNameTemplate) {
    this.caseNameTemplate = caseNameTemplate;
  }

  public Boolean getEnableCases() {
    return this.enableCases;
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
}
