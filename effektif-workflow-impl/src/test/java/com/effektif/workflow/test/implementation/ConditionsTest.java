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
package com.effektif.workflow.test.implementation;

import static org.junit.Assert.*;

import org.junit.Test;

import com.effektif.workflow.api.condition.Condition;
import com.effektif.workflow.api.condition.Contains;
import com.effektif.workflow.api.condition.Equals;
import com.effektif.workflow.api.deprecated.model.UserId;
import com.effektif.workflow.api.deprecated.types.UserIdType;
import com.effektif.workflow.api.model.TriggerInstance;
import com.effektif.workflow.api.types.TextType;
import com.effektif.workflow.api.types.DataType;
import com.effektif.workflow.api.workflow.Workflow;
import com.effektif.workflow.impl.WorkflowEngineImpl;
import com.effektif.workflow.impl.WorkflowParser;
import com.effektif.workflow.impl.conditions.ConditionImpl;
import com.effektif.workflow.impl.conditions.ConditionService;
import com.effektif.workflow.impl.deprecated.identity.IdentityService;
import com.effektif.workflow.impl.deprecated.identity.User;
import com.effektif.workflow.impl.workflowinstance.WorkflowInstanceImpl;
import com.effektif.workflow.test.WorkflowTest;


/**
 * @author Tom Baeyens
 */
public class ConditionsTest extends WorkflowTest {

  @Test
  public void testConditionTextEquals() {
    assertTrue(evaluate(TextType.INSTANCE, "v", "hello", new Equals()
      .leftExpression("v")
      .rightValue("hello")));
    assertFalse(evaluate(TextType.INSTANCE, "v", "hello", new Equals()
      .leftExpression("v")
      .rightValue("by")));
    assertFalse(evaluate(TextType.INSTANCE, "v", null, new Equals()
      .leftExpression("v")
      .rightValue("hello")));
  }

  @Test
  public void testConditionTextContains() {
    assertTrue(evaluate(TextType.INSTANCE, "v", "hello", new Contains()
      .leftExpression("v")
      .rightValue("ell")));
    assertFalse(evaluate(TextType.INSTANCE, "v", "hello", new Contains()
      .leftExpression("v")
      .rightValue("by")));
    assertFalse(evaluate(TextType.INSTANCE, "v", null, new Contains()
      .leftExpression("v")
      .rightValue("hello")));
  }

  @Test
  public void testConditionUserEquals() {
    User johndoe = new User()
      .id("johndoe")
      .fullName("John Doe")
      .email("johndoe@localhost");
  
    configuration.get(IdentityService.class)
      .createUser(johndoe);
    
    UserId johndoeId = johndoe.getId();

    assertTrue(evaluate(UserIdType.INSTANCE, "v", johndoeId, new Equals()
      .leftExpression("v.id")
      .rightValue("johndoe")));
    assertFalse(evaluate(UserIdType.INSTANCE, "v", johndoeId, new Equals()
      .leftExpression("v.id")
      .rightValue("superman")));
    assertFalse(evaluate(UserIdType.INSTANCE, "v", null, new Equals()
      .leftExpression("v.id")
      .rightValue("johndoe")));
  }

  public boolean evaluate(DataType type, String variableId, Object value, Condition condition) {
    Workflow workflow = new Workflow()
      .variable(variableId, type);
    
    deploy(workflow);
    
    TriggerInstance triggerInstance = new TriggerInstance()
      .data(variableId, value)
      .workflowId(workflow.getId());
    
    WorkflowEngineImpl workflowEngineImpl = (WorkflowEngineImpl) workflowEngine;
    WorkflowInstanceImpl workflowInstance = workflowEngineImpl.startInitialize(triggerInstance);
  
    ConditionService conditionService = configuration.get(ConditionService.class);
    ConditionImpl conditionImpl = conditionService.compile(condition, new WorkflowParser(configuration));
    return conditionImpl.eval(workflowInstance);
  }
}
