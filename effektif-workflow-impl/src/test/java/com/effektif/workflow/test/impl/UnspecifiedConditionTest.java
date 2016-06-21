package com.effektif.workflow.test.impl;

import com.effektif.workflow.api.condition.And;
import com.effektif.workflow.api.condition.Equals;
import com.effektif.workflow.api.condition.Or;
import com.effektif.workflow.api.condition.Unspecified;
import com.effektif.workflow.api.workflow.ExecutableWorkflow;
import com.effektif.workflow.api.workflow.Transition;
import com.effektif.workflow.impl.WorkflowParser;
import com.effektif.workflow.impl.conditions.AndImpl;
import com.effektif.workflow.impl.conditions.EqualsImpl;
import com.effektif.workflow.impl.conditions.OrImpl;
import com.effektif.workflow.impl.workflow.WorkflowImpl;
import com.effektif.workflow.test.WorkflowTest;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class UnspecifiedConditionTest extends WorkflowTest {

  WorkflowParser workflowParser;

  @Before
  public void initializeWorkflowParser() {
    workflowParser = new WorkflowParser(configuration);
  }

  @Test
  public void testDoNotParseUnspecifiedCondition() {
    Unspecified condition = new Unspecified();

    assertNull(workflowParser.parseCondition(condition));
  }

  @Test
  public void testAndIgnoreUnspecifiedSubCondition() {
    And andCondition = new And()
      .condition(new Unspecified())
      .condition(new Equals());

    AndImpl andImpl = (AndImpl) workflowParser.parseCondition(andCondition);
    assertEquals(1, andImpl.getConditions().size());
    assertEquals(EqualsImpl.class, andImpl.getConditions().get(0).getClass());
  }

  @Test
  public void testOrIgnoreUnspecifiedSubCondition() {
    Or orCondition = new Or()
      .condition(new Unspecified())
      .condition(new Equals());

    OrImpl orImpl = (OrImpl) workflowParser.parseCondition(orCondition);
    assertEquals(1, orImpl.getConditions().size());
    assertEquals(EqualsImpl.class, orImpl.getConditions().get(0).getClass());
  }

  @Test
  public void testTransitionIgnoreUnspecifiedCondition() {
    ExecutableWorkflow workflow = new ExecutableWorkflow()
      .transition("t1", new Transition().condition(new Unspecified()));

    WorkflowImpl workflowImpl = workflowParser.parse(workflow);
    assertEquals(1, workflowImpl.getTransitions().size());
    assertNull(workflowImpl.getTransitions().get(0).condition);
  }

}
