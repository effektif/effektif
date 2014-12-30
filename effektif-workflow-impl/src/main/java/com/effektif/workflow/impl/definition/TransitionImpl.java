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
package com.effektif.workflow.impl.definition;

import com.effektif.deprecated.TransitionBuilder;
import com.effektif.workflow.api.workflow.Transition;
import com.effektif.workflow.impl.WorkflowEngineImpl;
import com.effektif.workflow.impl.script.Script;
import com.fasterxml.jackson.annotation.JsonIgnore;


/**
 * @author Walter White
 */
public class TransitionImpl {

  Transition apiTransition;
  
  @JsonIgnore
  public ActivityImpl from;
  @JsonIgnore
  public ActivityImpl to;

  @JsonIgnore
  public WorkflowEngineImpl processEngine;
  @JsonIgnore
  public WorkflowImpl processDefinition;
  @JsonIgnore
  public ScopeImpl parent;
  
  public String id;
  public String fromId;
  public String toId;
  public Long line;
  public Long column;
  public String condition;
  
  @JsonIgnore
  public Script conditionScript;

  public TransitionImpl(Transition apiTransition) {
  }

  public TransitionImpl id(String id) {
    this.id = id;
    return this;
  }

  public TransitionImpl line(Long line) {
    this.line = line;
    return this;
  }

  public TransitionImpl column(Long column) {
    this.column = column;
    return this;
  }
  
  /** Fluent builder to set the source of this transition.
   * @param fromActivityDefinitionName the name of the activity definition. */
  public TransitionImpl from(String fromId) {
    this.fromId = fromId;
    return this;
  }

  public TransitionImpl to(String toId) {
    this.toId = toId;
    return this;
  }
  
  public TransitionImpl condition(String condition) {
    this.condition = condition;
    return this;
  }
  
  public void prepare() {
  }

  public ActivityImpl getFrom() {
    return from;
  }
  
  public void setFrom(ActivityImpl from) {
    this.from = from;
    if (from!=null) {
      this.fromId = from.id;
    }
  }
  
  public ActivityImpl getTo() {
    return to;
  }
  
  public void setTo(ActivityImpl to) {
    this.to = to;
    if (to!=null) {
      this.toId = to.id;
    }
  }

  
  public WorkflowEngineImpl getProcessEngine() {
    return processEngine;
  }

  
  public void setWorkflowEngine(WorkflowEngineImpl processEngine) {
    this.processEngine = processEngine;
  }

  
  public ScopeImpl getParent() {
    return parent;
  }

  
  public void setParent(ScopeImpl parent) {
    this.parent = parent;
  }

  
  public WorkflowImpl getProcessDefinition() {
    return processDefinition;
  }

  
  public void setWorkflow(WorkflowImpl processDefinition) {
    this.processDefinition = processDefinition;
  }

  
  public Script getConditionScript() {
    return conditionScript;
  }

  
  public void setConditionScript(Script conditionScript) {
    this.conditionScript = conditionScript;
  }
  
  public String getId() {
    return id;
  }
}
