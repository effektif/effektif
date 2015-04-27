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

import com.effektif.workflow.api.condition.Condition;
import com.effektif.workflow.api.condition.Not;
import com.effektif.workflow.impl.WorkflowParser;
import com.effektif.workflow.impl.workflowinstance.ScopeInstanceImpl;


/**
 * @author Tom Baeyens
 */
public class NotImpl implements ConditionImpl<Not> {

  protected ConditionImpl condition;

  public ConditionImpl getCondition() {
    return this.condition;
  }
  public void setCondition(ConditionImpl condition) {
    this.condition = condition;
  }
  public NotImpl condition(ConditionImpl condition) {
    this.condition = condition;
    return this;
  }
  
  @Override
  public Class< ? extends Condition> getApiType() {
    return Not.class;
  }

  @Override
  public boolean eval(ScopeInstanceImpl scopeInstance) {
    return !condition.eval(scopeInstance);
  }

  @Override
  public void parse(Not not, ConditionService conditionService, WorkflowParser parser) {
    this.condition = parser.parseCondition(not.getCondition());
  }
}
