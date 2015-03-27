/* Copyright (c) 2014, Effektif GmbH.
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
package com.effektif.workflow.impl;

import java.util.HashMap;
import java.util.Map;

import com.effektif.workflow.api.condition.And;
import com.effektif.workflow.api.condition.Condition;
import com.effektif.workflow.api.condition.Contains;
import com.effektif.workflow.api.condition.ContainsIgnoreCase;
import com.effektif.workflow.api.condition.Equals;
import com.effektif.workflow.api.condition.EqualsIgnoreCase;
import com.effektif.workflow.api.condition.GreaterThan;
import com.effektif.workflow.api.condition.GreaterThanOrEqual;
import com.effektif.workflow.api.condition.HasNoValue;
import com.effektif.workflow.api.condition.HasValue;
import com.effektif.workflow.api.condition.IsFalse;
import com.effektif.workflow.api.condition.IsTrue;
import com.effektif.workflow.api.condition.LessThan;
import com.effektif.workflow.api.condition.LessThanOrEqual;
import com.effektif.workflow.api.condition.Not;
import com.effektif.workflow.api.condition.NotContains;
import com.effektif.workflow.api.condition.NotContainsIgnoreCase;
import com.effektif.workflow.api.condition.NotEquals;
import com.effektif.workflow.api.condition.NotEqualsIgnoreCase;
import com.effektif.workflow.api.condition.Or;
import com.effektif.workflow.impl.conditions.AndImpl;
import com.effektif.workflow.impl.conditions.ConditionImpl;
import com.effektif.workflow.impl.conditions.ContainsIgnoreCaseImpl;
import com.effektif.workflow.impl.conditions.ContainsImpl;
import com.effektif.workflow.impl.conditions.EqualsIgnoreCaseImpl;
import com.effektif.workflow.impl.conditions.EqualsImpl;
import com.effektif.workflow.impl.conditions.GreaterThanImpl;
import com.effektif.workflow.impl.conditions.GreaterThanOrEqualImpl;
import com.effektif.workflow.impl.conditions.HasNoValueImpl;
import com.effektif.workflow.impl.conditions.HasValueImpl;
import com.effektif.workflow.impl.conditions.IsFalseImpl;
import com.effektif.workflow.impl.conditions.IsTrueImpl;
import com.effektif.workflow.impl.conditions.LessThanImpl;
import com.effektif.workflow.impl.conditions.LessThanOrEqualImpl;
import com.effektif.workflow.impl.conditions.NotContainsIgnoreCaseImpl;
import com.effektif.workflow.impl.conditions.NotContainsImpl;
import com.effektif.workflow.impl.conditions.NotEqualsIgnoreCaseImpl;
import com.effektif.workflow.impl.conditions.NotEqualsImpl;
import com.effektif.workflow.impl.conditions.NotImpl;
import com.effektif.workflow.impl.conditions.OrImpl;
import com.effektif.workflow.impl.script.ConditionService;
import com.fasterxml.jackson.databind.ObjectMapper;


/**
 * @author Tom Baeyens
 */
public class ConditionServiceImpl implements ConditionService {

  Map<Class<? extends Condition>,Class<? extends ConditionImpl>> impls = new HashMap<>();

  @Override
  public void registerSubclasses(ObjectMapper objectMapper) {
    // IDEA move this into service loader so users can add their own condition types
    impls.put(And.class, AndImpl.class);
    impls.put(Contains.class, ContainsImpl.class);
    impls.put(ContainsIgnoreCase.class, ContainsIgnoreCaseImpl.class);
    impls.put(Equals.class, EqualsImpl.class);
    impls.put(EqualsIgnoreCase.class, EqualsIgnoreCaseImpl.class);
    impls.put(GreaterThan.class, GreaterThanImpl.class);
    impls.put(GreaterThanOrEqual.class, GreaterThanOrEqualImpl.class);
    impls.put(HasNoValue.class, HasNoValueImpl.class);
    impls.put(HasValue.class, HasValueImpl.class);
    impls.put(IsFalse.class, IsFalseImpl.class);
    impls.put(IsTrue.class, IsTrueImpl.class);
    impls.put(LessThan.class, LessThanImpl.class);
    impls.put(LessThanOrEqual.class, LessThanOrEqualImpl.class);
    impls.put(Not.class, NotImpl.class);
    impls.put(NotContains.class, NotContainsImpl.class);
    impls.put(NotContainsIgnoreCase.class, NotContainsIgnoreCaseImpl.class);
    impls.put(NotEquals.class, NotEqualsImpl.class);
    impls.put(NotEqualsIgnoreCase.class, NotEqualsIgnoreCaseImpl.class);
    impls.put(Or.class, OrImpl.class);

    for (Class<?> apiClass: impls.keySet()) {
      objectMapper.registerSubtypes(apiClass);
    }
  }

  @Override
  public ConditionImpl compile(Condition condition, WorkflowParser parser) {
    if (condition==null) {
      return null;
    }
    Class<? extends ConditionImpl> implClass = impls.get(condition.getClass());
    if (implClass==null) {
      parser.addError("Unknown condition type: %s", condition.getClass().getName());
    }
    try {
      ConditionImpl conditionImpl = implClass.newInstance();
      conditionImpl.parse(condition, this, parser);
      return conditionImpl;
    } catch (InstantiationException | IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }
}
