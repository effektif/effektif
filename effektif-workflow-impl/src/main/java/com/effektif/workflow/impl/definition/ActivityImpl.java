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

import java.util.ArrayList;
import java.util.List;

import com.effektif.workflow.api.workflow.Activity;
import com.effektif.workflow.api.workflow.Binding;
import com.effektif.workflow.api.workflow.MultiInstance;
import com.effektif.workflow.api.workflow.Transition;
import com.effektif.workflow.impl.job.JobType;
import com.effektif.workflow.impl.plugin.ActivityType;
import com.effektif.workflow.impl.type.DataType;
import com.fasterxml.jackson.annotation.JsonIgnore;


/**
 * @author Walter White
 */
public class ActivityImpl extends ScopeImpl {

  public ActivityType activityType;

  /** the list of transitions for which this activity is the destination.
   * This field is not persisted nor jsonned. It is derived from the parent's {@link ScopeImpl#transitionDefinitions} */
  @JsonIgnore
  public List<TransitionImpl> incomingTransitions;

  /** the list of transitions for which this activity is the source.
   * This field is not persisted nor jsonned. It is derived from the parent's {@link ScopeImpl#transitionDefinitions} */
  @JsonIgnore
  public List<TransitionImpl> outgoingDefinitions;

  public String defaultTransitionId;
  
  @JsonIgnore
  public TransitionImpl defaultTransition;
  
  public MultiInstance multiInstance;

  /// Activity Definition Builder methods ////////////////////////////////////////////////

  public ActivityImpl(Activity apiActivity) {
    super(apiActivity);
  }

  public ActivityImpl activityType(ActivityType activityType) {
    this.activityType = activityType;
    return this;
  }
  
  public ActivityImpl defaultOutgoingTransitionId(String defaultOutgoingTransitionId) {
    this.defaultTransitionId = defaultOutgoingTransitionId;
    return this;
  }
  
  public ActivityImpl id(String id) {
    this.id = id;
    return this;
  }

  public ActivityImpl line(Long line) {
    super.line(line);
    return this;
  }

  public ActivityImpl column(Long column) {
    super.column(column);
    return this;
  }
  
  @Override
  public ActivityBuilder defaultTransition(String transitionId) {
    this.defaultTransitionId = transitionId;
    return this;
  }
  
  @Override
  public ActivityBuilder forEach(String elementVariableId, DataType elementDataType, String collectionVariableId) {
    forEach(elementVariableId, elementDataType, new Binding<List<Object>>().variableDefinitionId(collectionVariableId));
    return this;
  }
  
  @Override
  public ActivityBuilder forEachExpression(String elementVariableId, DataType elementDataType, String collectionExpression) {
    forEach(elementVariableId, elementDataType, new Binding<List<Object>>().expression(collectionExpression));
    return this;
  }

  protected void forEach(String elementVariableId, DataType elementDataType, Binding<List<Object>> collectionBinding) {
    this.multiInstanceElement = new VariableImpl()
      .id(elementVariableId)
      .dataType(elementDataType)
      .processEngine(workflowEngine)
      .processDefinition(workflow)
      .parent(this);
    this.multiInstance = collectionBinding;
  }
  
  public boolean isMultiInstance() {
    return multiInstance != null; 
  }

  @Override
  public ActivityImpl newActivity() {
    return super.newActivity();
  }
  
  @Override
  public ActivityImpl newActivity(String id, ActivityType activityType) {
    return super.newActivity(id, activityType);
  }

  @Override
  public VariableImpl newVariable() {
    return super.newVariable();
  }

  @Override
  public TransitionImpl newTransition() {
    return super.newTransition();
  }

  @Override
  public TimerDefinitionImpl newTimer(JobType jobType) {
    return super.newTimer(jobType);
  }
  
  /// other methods ////////////////////////////

  public WorkflowPath getPath() {
    return parent.getPath().addActivityDefinitionId(id);
  }

  public void visit(WorkflowVisitor visitor, int index) {
    // If some visitor needs to control the order of types vs other content visited, 
    // then this is the idea you should consider 
    //   if (visitor instanceof OrderedProcessDefinitionVisitor) {
    //     ... also delegate the ordering of this visit to the visitor ... 
    //   } else { ... perform the default as below
    visitor.startActivityDefinition(this, index);
    super.visit(visitor);
    visitor.endActivityDefinition(this, index);
  }

  public void addOutgoingTransition(TransitionImpl transitionDefinition) {
    if (outgoingDefinitions==null) {
      outgoingDefinitions = new ArrayList<TransitionImpl>();
    }
    // TODO: check if this is still needed, protection against duplicate validation phase
    if (! outgoingDefinitions.contains(transitionDefinition)) {
      outgoingDefinitions.add(transitionDefinition);
    }
  }

  public boolean hasOutgoingTransitionDefinitions() {
    return outgoingDefinitions!=null && !outgoingDefinitions.isEmpty();
  }
  
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public List<Transition> getOutgoingTransitions() {
    return (List) outgoingDefinitions;
  }

  public void setOutgoingDefinitions(List<TransitionImpl> outgoingTransitionDefinitions) {
    this.outgoingDefinitions = outgoingTransitionDefinitions;
  }


  public void addIncomingTransition(TransitionImpl transitionDefinition) {
    if (incomingTransitions==null) {
      incomingTransitions = new ArrayList<TransitionImpl>();
    }
    // TODO: check if this is still needed, protection against duplicate validation phase
    if (! incomingTransitions.contains(transitionDefinition)) {
      incomingTransitions.add(transitionDefinition);
    }
  }

  public boolean hasIncomingTransitionDefinitions() {
    return incomingTransitions!=null && !incomingTransitions.isEmpty();
  }

  
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public List<Transition> getIncomingTransitions() {
    return (List) incomingTransitions;
  }

  public void setIncomingTransitions(List<TransitionImpl> incomingTransitionDefinitions) {
    this.incomingTransitions = incomingTransitionDefinitions;
  }
  
  public ActivityType getActivityType() {
    return activityType;
  }
  
  public void setActivityType(ActivityType activityType) {
    this.activityType = activityType;
  }

  public String getDefaultTransitionId() {
    return defaultTransitionId;
  }

  public void setDefaultTransitionId(String defaultOutgoingTransitionId) {
    this.defaultTransitionId = defaultOutgoingTransitionId;
  }

  public TransitionImpl getDefaultTransition() {
    return defaultTransition;
  }
  
  public void setDefaultTransition(TransitionImpl defaultTransition) {
    this.defaultTransition = defaultTransition;
  }

  public String toString() {
    return id!=null ? "["+id.toString()+"]" : "["+Integer.toString(System.identityHashCode(this))+"]";
  }
}
