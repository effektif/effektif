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

import java.util.ArrayList;
import java.util.List;

import com.effektif.workflow.api.condition.And;
import com.effektif.workflow.api.condition.Condition;
import com.effektif.workflow.impl.WorkflowParser;
import com.effektif.workflow.impl.workflowinstance.ScopeInstanceImpl;


/**
 * @author Tom Baeyens
 */
public class AndImpl implements ConditionImpl<And> {

  protected List<ConditionImpl> conditions;

  public List<ConditionImpl> getConditions() {
    return this.conditions;
  }
  public void setConditions(List<ConditionImpl> conditions) {
    this.conditions = conditions;
  }
  
  @Override
  public Class< ? extends Condition> getApiType() {
    return And.class;
  }

  @Override
  public boolean eval(ScopeInstanceImpl scopeInstance) {
    if (conditions != null) {
      for (ConditionImpl condition : conditions) {
        if (!condition.eval(scopeInstance)) {
          return false;
        }
      }
    }
    return true;
  }

  @Override
  public void parse(And and, ConditionService conditionService, WorkflowParser parser) {
    if (and.getConditions()!=null) {
      for (Condition condition: and.getConditions()) {
        ConditionImpl conditionImpl = parser.parseCondition(condition);
        if (conditionImpl != null) {
          if (this.conditions == null) {
            this.conditions = new ArrayList<>();
          }
          this.conditions.add(conditionImpl);
        }
      }
    }
  }
  
  public String toString() {
    if (conditions==null || conditions.isEmpty()) {
      return "true(empty&&)";
    }
    StringBuilder string = null;
    for (ConditionImpl condition: conditions) {
      if (string==null) {
        string = new StringBuilder();
        string.append("(");
        string.append(condition.toString());
      } else {
        string.append(" && ");
        string.append(condition.toString());
      }
    }
    string.append(")");
    return string.toString();
  }
}
