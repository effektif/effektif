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
package com.effektif.workflow.impl.workflow;

import java.util.ArrayList;
import java.util.List;

import com.effektif.workflow.api.workflow.Activity;
import com.effektif.workflow.api.workflow.Scope;
import com.effektif.workflow.impl.WorkflowParser;
import com.effektif.workflow.impl.activity.ActivityType;
import com.effektif.workflow.impl.activity.ActivityTypeService;


/**
 * @author Tom Baeyens
 */
public class ActivityImpl extends ScopeImpl {
  
  public String id;
  public Activity activity;
  public ActivityType activityType;
  /** the list of transitions for which this activity is the destination.
   * This field is not persisted nor jsonned. It is derived from the parent's {@link ScopeImpl#transitions} */
  public List<TransitionImpl> incomingTransitions;
  /** the list of transitions for which this activity is the source.
   * This field is not persisted nor jsonned. It is derived from the parent's {@link ScopeImpl#transitions} */
  public List<TransitionImpl> outgoingTransitions;
  public TransitionImpl defaultTransition;
  
  /// Activity Definition Builder methods ////////////////////////////////////////////////

  public void parse(Activity activity, Scope parentScope, ScopeImpl parentScopeImpl, WorkflowParser parser) {
    super.parse(activity, parentScopeImpl, parser);
    this.id = activity.getId();
    this.activity = activity;
    if (id==null) {
      parser.addError("Activity has no id");
    } else if ("".equals(id)) {
      parser.addError("Activity has a empty string as id", id);
    } else if (id.contains(".")) {
      parser.addError("Activity '%s' has a dot in the id", id);
    } else if (parser.activityIds.contains(id)) {
      parser.addError("Duplicate activity id '%s'", id);
    } else {
      parser.activityIds.add(id);
    }

    ActivityTypeService activityTypeService = parser.getConfiguration(ActivityTypeService.class);
    this.activityType = activityTypeService.instantiateActivityType(activity);
    // some activity types need to validate incoming and outgoing transitions, 
    // that's why they are NOT parsed here, but after the transitions.
    if (this.activityType==null) {
      parser.addError("Activity '%s' has no activityType configured", id);
    }
  }

  public boolean isMultiInstance() {
    return activityType.getMultiInstance() != null; 
  }

  public String getIdText() {
    return id;
  }

  /// other methods ////////////////////////////

  public void addOutgoingTransition(TransitionImpl transitionDefinition) {
    if (outgoingTransitions==null) {
      outgoingTransitions = new ArrayList<TransitionImpl>();
    }
    outgoingTransitions.add(transitionDefinition);
  }

  public boolean hasOutgoingTransitions() {
    return outgoingTransitions!=null && !outgoingTransitions.isEmpty();
  }
  
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public List<TransitionImpl> getOutgoingTransitions() {
    return (List) outgoingTransitions;
  }

  public void setOutgoingTransitions(List<TransitionImpl> outgoingTransitionDefinitions) {
    this.outgoingTransitions = outgoingTransitionDefinitions;
  }

  public void addIncomingTransition(TransitionImpl transitionDefinition) {
    if (incomingTransitions==null) {
      incomingTransitions = new ArrayList<TransitionImpl>();
    }
    incomingTransitions.add(transitionDefinition);
  }

  public boolean hasIncomingTransitionDefinitions() {
    return incomingTransitions!=null && !incomingTransitions.isEmpty();
  }

  public List<TransitionImpl> getIncomingTransitions() {
    return incomingTransitions;
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

  public TransitionImpl getDefaultTransition() {
    return defaultTransition;
  }
  
  public void setDefaultTransition(TransitionImpl defaultTransition) {
    this.defaultTransition = defaultTransition;
  }

  public String toString() {
    String activityName = activity.getName();
    String activityTypeName = activityType.getActivityApiClass().getSimpleName();
    return "["+activityTypeName+"|"+(activityName!=null?activityName+"|":"")+(id!=null?id.toString():Integer.toString(System.identityHashCode(this)))+"]";
  }

  public String getId() {
    return id;
  }
}
