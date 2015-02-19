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

import com.effektif.workflow.api.types.BooleanType;


/**
 * @author Tom Baeyens
 */
public class Transition extends Element {

  protected String from;
  protected String to;
  protected Expression condition;
  protected Boolean isToNext;

  public Transition id(String id) {
    this.id = id;
    return this;
  }

  public Transition() {
  }
  
  public Transition(String id) {
    id(id);
  }

  public String getFrom() {
    return this.from;
  }
  public void setFrom(String from) {
    this.from = from;
  }
  public Transition from(String from) {
    this.from = from;
    return this;
  }
  
  public String getTo() {
    return this.to;
  }
  public void setTo(String to) {
    this.to = to;
  }
  public Transition to(String to) {
    this.to = to;
    return this;
  }
  
  public Transition toNext() {
    this.isToNext = true;
    return this;
  }
  
  public Expression getCondition() {
    return this.condition;
  }
  public void setCondition(Expression condition) {
    this.condition = condition;
  }
  public Transition condition(String condition) {
    if (this.condition == null) {
      this.condition = new Expression()
        .type(BooleanType.INSTANCE);
    }
    this.condition.script(condition);
    return this;
  }

  public Transition conditionMapping(String scriptVariableName, String variableId) {
    if (this.condition == null) {
      this.condition = new Expression()
        .type(BooleanType.INSTANCE);
    }
    this.condition.mapping(scriptVariableName, variableId);
    return this;
  }

  public boolean isToNext() {
    return Boolean.TRUE.equals(isToNext);
  }

  @Override
  public Transition name(String name) {
    super.name(name);
    return this;
  }

  @Override
  public Transition description(String description) {
    super.description(description);
    return this;
  }

  @Override
  public Transition property(String key, Object value) {
    super.property(key, value);
    return this;
  }

  @Override
  public Transition propertyOpt(String key, Object value) {
    super.propertyOpt(key, value);
    return this;
  }
  
  
}
