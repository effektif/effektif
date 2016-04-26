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
package com.effektif.workflow.test.api;

import com.effektif.workflow.api.activities.JavaServiceTask;
import com.effektif.workflow.api.model.TriggerInstance;
import com.effektif.workflow.api.types.DataType;
import com.effektif.workflow.api.types.JavaBeanType;
import com.effektif.workflow.api.types.TextType;
import com.effektif.workflow.api.workflow.ExecutableWorkflow;
import com.effektif.workflow.impl.data.types.ObjectType;
import com.effektif.workflow.test.WorkflowTest;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;


/**
 * @author Tom Baeyens
 */
public class ExpressionTest extends WorkflowTest {
  
  static Object expressionValue =  null;
  
  public static void setExpressionValue(Object expressionValue) {
    ExpressionTest.expressionValue = expressionValue;
  }
  
  @Test
  public void testSimpleVariableExpression() {
    assertExpression("v", TextType.INSTANCE, "red", "v", "red");
  }

  @Test
  public void testObjectTypeExpression() {
    Map<String,Object> johnDoeAddress = new HashMap<>();
    johnDoeAddress.put("street", "1st Avenue");
    johnDoeAddress.put("city", "New York");
    
    Map<String,Object> johnDoe = new HashMap<>();
    johnDoe.put("name", "John Doe");
    johnDoe.put("address", johnDoeAddress);
    
    assertExpression("v", ObjectType.INSTANCE, johnDoe, "v", johnDoe);
    assertExpression("v", ObjectType.INSTANCE, johnDoe, "v.name", "John Doe");
    assertExpression("v", ObjectType.INSTANCE, johnDoe, "v.address.street", "1st Avenue");
  }

  public static class NumberBean {
    Integer number;
  }

  @Test
  public void testJavaBeanTypeExpression() {
    NumberBean numberBean = new NumberBean();
    numberBean.number = 3;

    JavaBeanType javaBeanType = new JavaBeanType().javaClass(NumberBean.class);
    assertExpression("n", javaBeanType, numberBean, "n.number", 3);
  }

  protected void assertExpression(String variableId, DataType variableType, Object variableValue, String expression, Object expectedExpressionValue) {
    ExecutableWorkflow workflow = new ExecutableWorkflow()
      .variable(variableId, variableType)
      .activity("invoke hello", new JavaServiceTask()
        .javaClass(ExpressionTest.class)
        .methodName("setExpressionValue")
        .argExpression(expression));
    
    deploy(workflow);
    
    start(new TriggerInstance()
      .workflowId(workflow.getId())
      .data(variableId, variableValue));
    
    assertEquals(expectedExpressionValue, expressionValue);
  }

}
