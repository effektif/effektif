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

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.effektif.workflow.api.activities.JavaServiceTask;
import com.effektif.workflow.api.workflow.Workflow;
import com.effektif.workflow.impl.configuration.DefaultConfiguration;
import com.effektif.workflow.test.WorkflowTest;


/**
 * @author Tom Baeyens
 */
public class JavaServiceTaskTest extends WorkflowTest {
  
  // Between these lines might move as test infrastructure to WorkflowTest
  static List<String> messages = new ArrayList<>();
  public static void hello(String message) {
    messages.add(message);
  }
  protected JavaServiceTask msgValue(String message) {
    return new JavaServiceTask()
      .javaClazz(JavaServiceTaskTest.class)
      .methodName("hello")
      .argValue(message);
  }
  protected JavaServiceTask msgExpression(String messageExpression) {
    return new JavaServiceTask()
      .javaClazz(JavaServiceTaskTest.class)
      .methodName("hello")
      .argExpression(messageExpression);
  }
  // Between these lines might move as test infrastructure to WorkflowTest

  @Test
  public void testJavaServiceTaskStaticMethodValue() {
    Workflow workflow = new Workflow()
      .activity("invoke hello", msgValue("world"));
    
    deploy(workflow);
    
    start(workflow);
    
    assertEquals("world", messages.get(0));
  }

  @Test
  public void testJavaServiceTaskStaticMethodExpression() {
    Workflow workflow = new Workflow()
      .activity("invoke hello", msgExpression("msg"));
    
    deploy(workflow);
    
    start(createTriggerInstance(workflow)
      .data("msg", "world"));
    
    assertEquals("world", messages.get(0));
  }
  
  public static class MyBean {
    String msg;
    public void hello(String msg) {
      this.msg = msg;
    }
  }

  @Test
  public void testJavaServiceTaskBean() {
    Workflow workflow = new Workflow()
      .activity("invoke bean", new JavaServiceTask()
      .beanName("myBean")
      .methodName("hello")
      .argValue("world"));
    
    MyBean myBean = new MyBean();
    configuration.set(myBean, "myBean");
    
    deploy(workflow);
    
    start(workflow);
    
    assertEquals("world", myBean.msg);
  }
}
