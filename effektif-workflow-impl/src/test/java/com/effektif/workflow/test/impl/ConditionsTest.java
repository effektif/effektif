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
package com.effektif.workflow.test.impl;

import static org.junit.Assert.*;

import org.junit.Test;

import com.effektif.workflow.api.condition.Comparator;
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
import com.effektif.workflow.api.condition.NotContains;
import com.effektif.workflow.api.condition.NotContainsIgnoreCase;
import com.effektif.workflow.api.condition.NotEquals;
import com.effektif.workflow.api.condition.NotEqualsIgnoreCase;
import com.effektif.workflow.api.model.TriggerInstance;
import com.effektif.workflow.api.types.BooleanType;
import com.effektif.workflow.api.types.ChoiceType;
import com.effektif.workflow.api.types.DataType;
import com.effektif.workflow.api.types.NumberType;
import com.effektif.workflow.api.types.TextType;
import com.effektif.workflow.api.workflow.ExecutableWorkflow;
import com.effektif.workflow.impl.WorkflowEngineImpl;
import com.effektif.workflow.impl.WorkflowParser;
import com.effektif.workflow.impl.conditions.ConditionImpl;
import com.effektif.workflow.impl.conditions.ConditionService;
import com.effektif.workflow.impl.workflowinstance.WorkflowInstanceImpl;
import com.effektif.workflow.test.WorkflowTest;

/**
 * @author Tom Baeyens
 */
public class ConditionsTest extends WorkflowTest {

  private static final ChoiceType CHOICE = new ChoiceType().option("a").option("b").option("c");

  @Test
  public void testBooleanIsTrue() {
    assertTrue(evaluate(BooleanType.INSTANCE, "p", true, new IsTrue().leftExpression("p")));
    assertFalse(evaluate(BooleanType.INSTANCE, "p", false, new IsTrue().leftExpression("p")));
    assertFalse(evaluate(BooleanType.INSTANCE, "p", null, new IsTrue().leftExpression("p")));
  }

  @Test
  public void testBooleanIsFalse() {
    assertFalse(evaluate(BooleanType.INSTANCE, "p", true, new IsFalse().leftExpression("p")));
    assertTrue(evaluate(BooleanType.INSTANCE, "p", false, new IsFalse().leftExpression("p")));
    assertFalse(evaluate(BooleanType.INSTANCE, "p", null, new IsFalse().leftExpression("p")));
  }

  @Test
  public void testChoiceHasValue() {
    assertTrue(evaluate(CHOICE, "v", "b", new HasValue().leftExpression("v")));
    assertFalse(evaluate(CHOICE, "v", null, new HasValue().leftExpression("v")));
  }

  @Test
  public void testChoiceHasNoValue() {
    assertFalse(evaluate(CHOICE, "v", "b", new HasNoValue().leftExpression("v")));
    assertTrue(evaluate(CHOICE, "v", null, new HasNoValue().leftExpression("v")));
  }

  @Test
  public void testChoiceEquals() {
    assertFalse(evaluate(CHOICE, "v", "b", new Equals().leftExpression("v").rightValue("a")));
    assertTrue(evaluate(CHOICE, "v", "b", new Equals().leftExpression("v").rightValue("b")));
    assertFalse(evaluate(CHOICE, "v", "b", new Equals().leftExpression("v").rightValue(null)));
  }

  @Test
  public void testChoiceNotEquals() {
    assertTrue(evaluate(CHOICE, "v", "b", new NotEquals().leftExpression("v").rightValue("a")));
    assertFalse(evaluate(CHOICE, "v", "b", new NotEquals().leftExpression("v").rightValue("b")));
    assertTrue(evaluate(CHOICE, "v", "b", new NotEquals().leftExpression("v").rightValue(null)));
  }

  @Test
  public void testComparatorInvalidNumbers() {
    assertFalse(evaluate(NumberType.INSTANCE, "n", 42, new GreaterThan().leftExpression("n").rightValue("x")));
    assertFalse(evaluate(NumberType.INSTANCE, "n", 42, new GreaterThan().leftExpression("n").rightValue(null)));
  }

  @Test
  public void testNumberEquals() {
    assertTrue(evaluateNumberExpression(new Equals(), 42, 42));
    assertFalse(evaluateNumberExpression(new Equals(), 42, 0));
    assertFalse(evaluateNumberExpression(new Equals(), null, 42));
  }

  @Test
  public void testNumberNotEquals() {
    assertFalse(evaluateNumberExpression(new NotEquals(), 42, 42));
    assertTrue(evaluateNumberExpression(new NotEquals(), 42, 0));
    assertTrue(evaluateNumberExpression(new NotEquals(), null, 42));
  }

  @Test
  public void testNumberGreaterThanExpression() {
    assertFalse(evaluateNumberExpression(new GreaterThan(), 99.0, 100.0));
    assertFalse(evaluateNumberExpression(new GreaterThan(), 99.0, 99.0));
    assertTrue(evaluateNumberExpression(new GreaterThan(), 99.0, 98.0));
  }

  @Test
  public void testNumberGreaterThanOrEqualExpression() {
    assertFalse(evaluateNumberExpression(new GreaterThanOrEqual(), 99.0, 100.0));
    assertTrue(evaluateNumberExpression(new GreaterThanOrEqual(), 99.0, 99.0));
    assertTrue(evaluateNumberExpression(new GreaterThanOrEqual(), 99.0, 98.0));
  }

  @Test
  public void testNumberHasValue() {
    assertTrue(evaluate(NumberType.INSTANCE, "n", 42, new HasValue().leftExpression("n")));
    assertFalse(evaluate(NumberType.INSTANCE, "n", null, new HasValue().leftExpression("n")));
  }

  @Test
  public void testNumberHasNoValue() {
    assertFalse(evaluate(NumberType.INSTANCE, "n", 42, new HasNoValue().leftExpression("n")));
    assertTrue(evaluate(NumberType.INSTANCE, "n", null, new HasNoValue().leftExpression("n")));
  }

  @Test
  public void testNumberLessThanExpression() {
    assertTrue(evaluateNumberExpression(new LessThan(), 99.0, 100.0));
    assertFalse(evaluateNumberExpression(new LessThan(), 99.0, 99.0));
    assertFalse(evaluateNumberExpression(new LessThan(), 99.0, 98.0));
  }

  @Test
  public void testNumberLessThanOrEqualExpression() {
    assertTrue(evaluateNumberExpression(new LessThanOrEqual(), 99.0, 100.0));
    assertTrue(evaluateNumberExpression(new LessThanOrEqual(), 99.0, 99.0));
    assertFalse(evaluateNumberExpression(new LessThanOrEqual(), 99.0, 98.0));
  }

  @Test
  public void testTextContains() {
    assertTrue(evaluateTextExpression(new Contains(), "hello", "hello"));
    assertFalse(evaluateTextExpression(new Contains(), "hello", "Hello"));
    assertFalse(evaluateTextExpression(new Contains(), "hello", "by"));
    assertTrue(evaluateTextExpression(new Contains(), "hello", "ell"));
    assertTrue(evaluateTextExpression(new Contains(), "hello", "hell"));
    assertTrue(evaluateTextExpression(new Contains(), "hello", "ello"));
    assertFalse(evaluateTextExpression(new Contains(), null, "hello"));
  }

  @Test
  public void testTextContainsMultiLine() {
    assertTrue(evaluateTextExpression(new Contains(), "hello\nworld", "hello"));
    assertFalse(evaluateTextExpression(new Contains(), "hello\nworld", "Hello"));
    assertTrue(evaluateTextExpression(new Contains(), "hello\nworld\nGOTO 10", "hello\nworld"));
    assertFalse(evaluateTextExpression(new Contains(), "hello\nworld\nGOTO 10", "Hello\nworld"));
  }

  @Test
  public void testTextContainsIgnoreCase() {
    assertTrue(evaluateTextExpression(new ContainsIgnoreCase(), "HELLO", "hello"));
    assertTrue(evaluateTextExpression(new ContainsIgnoreCase(), "HELLO", "Hello"));
    assertFalse(evaluateTextExpression(new ContainsIgnoreCase(), "HELLO", "by"));
    assertTrue(evaluateTextExpression(new ContainsIgnoreCase(), "HELLO", "ell"));
    assertTrue(evaluateTextExpression(new ContainsIgnoreCase(), "HELLO", "hell"));
    assertTrue(evaluateTextExpression(new ContainsIgnoreCase(), "HELLO", "ello"));
    assertFalse(evaluateTextExpression(new ContainsIgnoreCase(), null, "hello"));
  }

  @Test
  public void testTextContainsIgnoreCaseMultiLine() {
    assertTrue(evaluateTextExpression(new ContainsIgnoreCase(), "hello\nworld", "hello"));
    assertTrue(evaluateTextExpression(new ContainsIgnoreCase(), "hello\nworld", "Hello"));
    assertTrue(evaluateTextExpression(new ContainsIgnoreCase(), "hello\nworld\nGOTO 10", "Hello\nworld"));
    assertTrue(evaluateTextExpression(new ContainsIgnoreCase(), "hello\nworld\nGOTO 10", "Hello\nworld"));
  }

  @Test
  public void testTextEquals() {
    assertTrue(evaluateTextExpression(new Equals(), "hello", "hello"));
    assertFalse(evaluateTextExpression(new Equals(), "hello", "Hello"));
    assertFalse(evaluateTextExpression(new Equals(), "hello", "by"));
    assertFalse(evaluateTextExpression(new Equals(), null, "hello"));
  }

  @Test
  public void testTextEqualsMultiLine() {
    assertTrue(evaluateTextExpression(new Equals(), "hello\nworld", "hello\nworld"));
    assertFalse(evaluateTextExpression(new Equals(), "hello\nworld", "Hello\nWorld"));
    assertFalse(evaluateTextExpression(new Equals(), "hello\nworld", "hello world"));
    assertFalse(evaluateTextExpression(new Equals(), "hello\nworld", "helloworld"));
  }

  @Test
  public void testTextEqualsIgnoreCase() {
    assertTrue(evaluateTextExpression(new EqualsIgnoreCase(), "hello", "hello"));
    assertTrue(evaluateTextExpression(new EqualsIgnoreCase(), "hello", "Hello"));
    assertFalse(evaluateTextExpression(new EqualsIgnoreCase(), "hello", "by"));
    assertFalse(evaluateTextExpression(new EqualsIgnoreCase(), null, "hello"));
  }

  @Test
  public void testTextEqualsIgnoreCaseMultiLine() {
    assertTrue(evaluateTextExpression(new EqualsIgnoreCase(), "hello\nworld", "hello\nworld"));
    assertTrue(evaluateTextExpression(new EqualsIgnoreCase(), "hello\nworld", "Hello\nWorld"));
    assertFalse(evaluateTextExpression(new EqualsIgnoreCase(), "hello\nworld", "hello world"));
    assertFalse(evaluateTextExpression(new EqualsIgnoreCase(), "hello\nworld", "helloworld"));
  }

  @Test
  public void testTextHasValue() {
    assertTrue(evaluate(TextType.INSTANCE, "s", "42", new HasValue().leftExpression("s")));
    assertFalse(evaluate(TextType.INSTANCE, "s", null, new HasValue().leftExpression("s")));
  }

  @Test
  public void testTextHasNoValue() {
    assertFalse(evaluate(TextType.INSTANCE, "s", "42", new HasNoValue().leftExpression("s")));
    assertTrue(evaluate(TextType.INSTANCE, "s", null, new HasNoValue().leftExpression("s")));
  }

  @Test
  public void testTextNotContains() {
    assertTrue(evaluateTextExpression(new NotContains(), "hello", "by"));
    assertFalse(evaluateTextExpression(new NotContains(), "hello", "hello"));
    assertFalse(evaluateTextExpression(new NotContains(), "hello", "hell"));
    assertFalse(evaluateTextExpression(new NotContains(), "hello", "ell"));
    assertFalse(evaluateTextExpression(new NotContains(), "hello", "ello"));
    assertTrue(evaluateTextExpression(new NotContains(), "hello", "El"));
    assertTrue(evaluateTextExpression(new NotContains(), "hello", "Hello"));
    assertTrue(evaluateTextExpression(new NotContains(), null, "hello"));
  }

  @Test
  public void testTextNotContainsMultiLine() {
    assertFalse(evaluateTextExpression(new NotContains(), "hello\nworld", "hello"));
    assertTrue(evaluateTextExpression(new NotContains(), "hello\nworld", "Hello"));
    assertTrue(evaluateTextExpression(new NotContains(), "goodbye\nworld", "hello"));
    assertFalse(evaluateTextExpression(new NotContains(), "hello\nworld\nGOTO 10", "hello\nworld"));
    assertTrue(evaluateTextExpression(new NotContains(), "hello\nworld\nGOTO 10", "Hello\nworld"));
    assertTrue(evaluateTextExpression(new NotContains(), "goodbye\nworld\nGOTO 10", "hello\nworld"));
  }

  @Test
  public void testTextNotContainsIgnoreCase() {
    assertTrue(evaluateTextExpression(new NotContainsIgnoreCase(), "HELLO", "by"));
    assertFalse(evaluateTextExpression(new NotContainsIgnoreCase(), "HELLO", "hello"));
    assertFalse(evaluateTextExpression(new NotContainsIgnoreCase(), "HELLO", "hell"));
    assertFalse(evaluateTextExpression(new NotContainsIgnoreCase(), "HELLO", "ell"));
    assertFalse(evaluateTextExpression(new NotContainsIgnoreCase(), "HELLO", "ello"));
    assertFalse(evaluateTextExpression(new NotContainsIgnoreCase(), "hello", "ell"));
    assertTrue(evaluateTextExpression(new NotContainsIgnoreCase(), null, "hello"));
  }

  @Test
  public void testTextNotContainsIgnoreCaseMultiLine() {
    assertFalse(evaluateTextExpression(new NotContainsIgnoreCase(), "hello\nworld", "hello"));
    assertTrue(evaluateTextExpression(new NotContainsIgnoreCase(), "hello\nworld", "by"));
    assertFalse(evaluateTextExpression(new NotContainsIgnoreCase(), "hello\nworld", "Hello"));
    assertFalse(evaluateTextExpression(new NotContainsIgnoreCase(), "hello\nworld\nGOTO 10", "hello\nworld"));
    assertTrue(evaluateTextExpression(new NotContainsIgnoreCase(), "hello\nworld\nGOTO 10", "Goodbye\nWorld"));
    assertFalse(evaluateTextExpression(new NotContainsIgnoreCase(), "hello\nworld\nGOTO 10", "Hello\nWorld"));
  }

  @Test
  public void testTextNotEquals() {
    assertFalse(evaluateTextExpression(new NotEquals(), "hello", "hello"));
    assertTrue(evaluateTextExpression(new NotEquals(), "hello", "Hello"));
    assertTrue(evaluateTextExpression(new NotEquals(), "hello", "by"));
    assertTrue(evaluateTextExpression(new NotEquals(), null, "hello"));
  }

  @Test
  public void testTextNotEqualsMultiLine() {
    assertFalse(evaluateTextExpression(new NotEquals(), "hello\nworld", "hello\nworld"));
    assertTrue(evaluateTextExpression(new NotEquals(), "hello\nworld", "Hello\nWorld"));
    assertTrue(evaluateTextExpression(new NotEquals(), "hello\nworld", "hello world"));
    assertTrue(evaluateTextExpression(new NotEquals(), "hello\nworld", "helloworld"));
  }

  @Test
  public void testTextNotEqualsIgnoreCase() {
    assertFalse(evaluateTextExpression(new NotEqualsIgnoreCase(), "hello", "hello"));
    assertFalse(evaluateTextExpression(new NotEqualsIgnoreCase(), "hello", "Hello"));
    assertTrue(evaluateTextExpression(new NotEqualsIgnoreCase(), "hello", "by"));
    assertTrue(evaluateTextExpression(new NotEqualsIgnoreCase(), null, "hello"));
  }

  @Test
  public void testTextNotEqualsIgnoreCaseMultiLine() {
    assertFalse(evaluateTextExpression(new NotEqualsIgnoreCase(), "hello\nworld", "hello\nworld"));
    assertFalse(evaluateTextExpression(new NotEqualsIgnoreCase(), "hello\nworld", "Hello\nWorld"));
    assertTrue(evaluateTextExpression(new NotEqualsIgnoreCase(), "hello\nworld", "hello world"));
    assertTrue(evaluateTextExpression(new NotEqualsIgnoreCase(), "hello\nworld", "helloworld"));
  }

  private boolean evaluateNumberExpression(Comparator condition, Number leftValue, Number rightValue) {
    return evaluate(NumberType.INSTANCE, "n", leftValue, condition.leftExpression("n").rightValue(rightValue));
  }

  private boolean evaluateTextExpression(Comparator condition, String leftValue, String rightValue) {
    return evaluate(TextType.INSTANCE, "s", leftValue, condition.leftExpression("s").rightValue(rightValue));
  }

  private boolean evaluate(DataType type, String variableId, Object value, Condition condition) {
    ExecutableWorkflow workflow = new ExecutableWorkflow()
      .variable(variableId, type);
    
    deploy(workflow);
    
    TriggerInstance triggerInstance = new TriggerInstance()
      .data(variableId, value)
      .workflowId(workflow.getId());
    
    WorkflowEngineImpl workflowEngineImpl = (WorkflowEngineImpl) workflowEngine;
    WorkflowInstanceImpl workflowInstance = workflowEngineImpl.startInitialize(triggerInstance);
  
    ConditionService conditionService = configuration.get(ConditionService.class);
    WorkflowParser workflowParser = new WorkflowParser(configuration);
    workflowParser.pushContext("condition", null, null, null);
    ConditionImpl conditionImpl = conditionService.compile(condition, workflowParser);
    return conditionImpl.eval(workflowInstance);
  }
}
