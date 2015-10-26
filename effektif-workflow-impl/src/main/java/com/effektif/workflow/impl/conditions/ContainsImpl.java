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
import com.effektif.workflow.api.condition.Contains;
import com.effektif.workflow.impl.data.TypedValueImpl;
import com.effektif.workflow.impl.workflowinstance.ScopeInstanceImpl;


/**
 * @author Tom Baeyens
 */
public class ContainsImpl extends ComparatorImpl {

  @Override
  public Class< ? extends Condition> getApiType() {
    return Contains.class;
  }
  
  @Override
  public boolean compare(TypedValueImpl leftValue, TypedValueImpl rightValue, ScopeInstanceImpl scopeInstance) {
    if (isNull(leftValue) && isNull(rightValue)) return true;
    if (isNotNull(leftValue) && isNull(rightValue)) return true;
    if (isNull(leftValue) && isNotNull(rightValue)) return false;
    String leftString = (String) leftValue.value;
    String rightString = (String) rightValue.value;
    return leftString.contains(rightString);
  }

  @Override
  public String getComparatorSymbol() {
    return "<contains>";
  }
}
