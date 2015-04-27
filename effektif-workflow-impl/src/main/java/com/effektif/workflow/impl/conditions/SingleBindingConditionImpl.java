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
package com.effektif.workflow.impl.conditions;

import com.effektif.workflow.api.condition.SingleBindingCondition;
import com.effektif.workflow.impl.WorkflowParser;
import com.effektif.workflow.impl.workflow.BindingImpl;
import com.effektif.workflow.impl.workflowinstance.ScopeInstanceImpl;


/**
 * @author Tom Baeyens
 */
public abstract class SingleBindingConditionImpl<T extends SingleBindingCondition> implements ConditionImpl<T> {
  
  protected BindingImpl<?> left;

  public BindingImpl<?> getLeft() {
    return this.left;
  }
  public void setLeft(BindingImpl<?> left) {
    this.left = left;
  }
  public SingleBindingConditionImpl left(BindingImpl<?> left) {
    this.left = left;
    return this;
  }
  
  @Override
  public boolean eval(ScopeInstanceImpl scopeInstance) {
    Object value = scopeInstance.getValue(left);
    return eval(value);
  }
  
  protected abstract boolean eval(Object value);
  
  @Override
  public void parse(T condition, ConditionService conditionService, WorkflowParser parser) {
    this.left = parser.parseBinding(condition.getLeft(), "left");
  }
}
