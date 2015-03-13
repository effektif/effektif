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
import java.util.List;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;


/** Base class containing the configuration data for 
 * specific activity types.
 * 
 * @author Tom Baeyens
 */
@JsonTypeInfo(use=Id.NAME, include=As.PROPERTY, property="type")
public class Activity extends Scope {
  
  protected String id;
  protected String defaultTransitionId;
  protected MultiInstance multiInstance;
  protected List<Transition> outgoingTransitions;
  
  public Activity() {
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
  
  public Activity transitionWithConditionTo(String conditionExpression, String toActivityId) {
    transitionTo(new Transition()
      .condition(conditionExpression)
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
