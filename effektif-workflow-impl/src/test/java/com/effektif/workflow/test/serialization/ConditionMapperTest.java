package com.effektif.workflow.test.serialization;/* Copyright (c) 2015, Effektif GmbH.
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

import static org.junit.Assert.*;

import com.effektif.workflow.api.condition.*;
import com.effektif.workflow.impl.util.Lists;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.effektif.workflow.api.workflow.Binding;
import com.effektif.workflow.impl.bpmn.BpmnMapper;

/**
 * Tests BPMN serialisation for conditions.
 *
 * @author Peter Hilton
 */
public class ConditionMapperTest {

  protected static final Logger log = LoggerFactory.getLogger(ConditionMapperTest.class);
  static BpmnMapper bpmnMapper;

  @BeforeClass
  public static void initialize() {
    bpmnMapper = BpmnTest.getBpmnMapper();
  }

  @Test
  public void testContains() {
    testComparator(Contains.class, "version", "PATCH");
  }

  @Test
  public void testNotContains() {
    testComparator(NotContains.class, "version", "PATCH");
  }

  @Test
  public void testContainsIgnoreCase() {
    testComparator(ContainsIgnoreCase.class, "version", "PATCH");
  }

  @Test
  public void testNotContainsIgnoreCase() {
    testComparator(NotContainsIgnoreCase.class, "version", "PATCH");
  }

  @Test
  public void testEquals() {
    testComparator(Equals.class, "version", "3.0");
  }

  @Test
  public void testNotEquals() {
    testComparator(NotEquals.class, "version", "3.0");
  }

  @Test
  public void testEqualsIgnoreCase() {
    testComparator(EqualsIgnoreCase.class, "version", "v3");
  }

  @Test
  public void testNotEqualsIgnoreCase() {
    testComparator(NotEqualsIgnoreCase.class, "version", "v3");
  }

  @Test
  public void testGreaterThan() {
    testComparator(GreaterThan.class, "issues", "10");
  }

  @Test
  public void testGreaterThanOrEqual() {
    testComparator(GreaterThanOrEqual.class, "issues", "10");
  }

  @Test
  public void testLessThan() {
    testComparator(LessThan.class, "issues", "10");
  }

  @Test
  public void testLessThanOrEqual() {
    testComparator(LessThanOrEqual.class, "issues", "10");
  }

  @Test
  public void testHasValue() {
    testSingleCondition(HasValue.class, "testsPassed");
  }

  @Test
  public void testHasNoValue() {
    testSingleCondition(HasNoValue.class, "testsPassed");
  }

  @Test
  public void testIsTrue() {
    testSingleCondition(IsTrue.class, "testsPassed");
  }

  @Test
  public void testIsFalse() {
    testSingleCondition(IsFalse.class, "testsPassed");
  }

  @Test
  public void testAnd() {
    Condition issues = new LessThan().left(new Binding().expression("issues")).right(new Binding().value("10"));
    Condition tests = new IsTrue().left(new Binding<String>().expression("testsPassed"));
    Condition tests2 = new IsTrue().left(new Binding<String>().expression("testsPassed2"));
    And condition = new And().condition(issues).condition(tests).condition(tests2);
    condition = serialize(condition, And.class);

    assertEquals(3, condition.getConditions().size());

    // Note: the IsTrue condition is first, because conditions are deserialised in alphabetical order of class name.
    assertEquals(IsTrue.class, condition.getConditions().get(0).getClass());
    assertEquals("testsPassed", ((IsTrue) condition.getConditions().get(0)).getLeft().getExpression());
    assertEquals("testsPassed2", ((IsTrue) condition.getConditions().get(1)).getLeft().getExpression());

    assertEquals(LessThan.class, condition.getConditions().get(2).getClass());
  }

  @Test
  public void testOr() {
    Condition issues = new LessThan().left(new Binding().expression("issues")).right(new Binding().value("10"));
    Condition tests = new IsTrue().left(new Binding<String>().expression("testsPassed"));
    Or condition = new Or().condition(issues).condition(tests);
    condition = serialize(condition, Or.class);

    assertEquals(2, condition.getConditions().size());

    // Note: the IsTrue condition is first, because conditions are deserialised in alphabetical order of class name.
    assertEquals(IsTrue.class, condition.getConditions().get(0).getClass());
    assertEquals("testsPassed", ((IsTrue) condition.getConditions().get(0)).getLeft().getExpression());

    assertEquals(LessThan.class, condition.getConditions().get(1).getClass());
  }

  @Test
  public void testNot() {
    Condition issues = new LessThan().left(new Binding().expression("issues")).right(new Binding().value("10"));
    Not condition = new Not().condition(issues);
    condition = serialize(condition, Not.class);
    assertEquals(LessThan.class, condition.getCondition().getClass());
    LessThan deserialisedIssues = (LessThan) condition.getCondition();
    assertEquals("issues", deserialisedIssues.getLeft().getExpression());
    assertEquals("10", deserialisedIssues.getRight().getValue());
  }

  @Test
  public void testUnspecified() {
    Unspecified unspecified = new Unspecified()
      .left(new Binding<>().expression("left"))
      .right(new Binding<>().expression("right"))
      // single condition
      .condition(new Equals().leftExpression("one").rightExpression("two"));
    // multiple conditions
    unspecified.setConditions(Lists.of(
      new Equals().leftExpression("three").rightExpression("four"),
      new Equals().leftExpression("five").rightExpression("six")));

    unspecified = serialize(unspecified, Unspecified.class);

    assertNotNull(unspecified.getLeft());
    assertEquals("left", unspecified.getLeft().getExpression());
    assertNotNull(unspecified.getRight());
    assertEquals("right", unspecified.getRight().getExpression());
    // single and multiple conditions end up all in multiple conditions if the list is bigger than 1
    assertNull(unspecified.getCondition());
    assertNotNull(unspecified.getConditions());
    assertEquals(3, unspecified.getConditions().size());
    assertEquals("one", ((Equals) unspecified.getConditions().get(0)).getLeft().getExpression());
    assertEquals("two", ((Equals) unspecified.getConditions().get(0)).getRight().getExpression());
    assertEquals("three", ((Equals) unspecified.getConditions().get(1)).getLeft().getExpression());
    assertEquals("four", ((Equals) unspecified.getConditions().get(1)).getRight().getExpression());
    assertEquals("five", ((Equals) unspecified.getConditions().get(2)).getLeft().getExpression());
    assertEquals("six", ((Equals) unspecified.getConditions().get(2)).getRight().getExpression());
  }

  /**
   * Tests a comparator’s BPMN serialisation with instance that compares the given expression and value.
   */
  private <T extends Comparator> void testComparator(Class<T> type, String expression, String value) {
    try {
      T condition = (T) type.newInstance();
      condition.setLeft(new Binding<String>().expression(expression));
      condition.setRight(new Binding<String>().value(value));
      condition = serialize(condition, type);
      assertEquals(expression, condition.getLeft().getExpression());
      assertEquals(value, condition.getRight().getValue());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Tests a comparator’s BPMN serialisation with an instance that has the given expression.
   */
  private <T extends SingleBindingCondition> void testSingleCondition(Class<T> type, String expression) {
    try {
      T condition = (T) type.newInstance();
      condition.setLeft(new Binding<String>().expression(expression));
      condition = serialize(condition, type);
      assertEquals(expression, condition.getLeft().getExpression());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  protected <T extends Condition> T serialize(T condition, Class<T> conditionClass) {
    String xmlString = bpmnMapper.writeToString(condition);
    log.info("\n" + xmlString + "\n");
    T deserialisedCondition = bpmnMapper.readCondition(xmlString, conditionClass);
    assertNotNull(deserialisedCondition);
    assertTrue(deserialisedCondition.getClass().equals(conditionClass));
    return deserialisedCondition;
  }
}
