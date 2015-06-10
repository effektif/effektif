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

import org.junit.Test;

import com.effektif.workflow.api.activities.JavaServiceTask;
import com.effektif.workflow.api.workflow.ExecutableWorkflow;
import com.effektif.workflow.test.WorkflowTest;


/**
 * @author Tom Baeyens
 */
public class JavaServiceTaskTest extends WorkflowTest {
  
  @Test
  public void testJavaServiceTaskStaticMethodValue() {
    ExecutableWorkflow workflow = new ExecutableWorkflow()
      .activity("invoke hello", msgValue("world"));
    
    deploy(workflow);
    
    start(workflow);
    
    assertEquals("world", getMessage(0));
  }

  @Test
  public void testJavaServiceTaskStaticMethodExpression() {
    ExecutableWorkflow workflow = new ExecutableWorkflow()
      .activity("invoke hello", msgExpression("msg"));
    
    deploy(workflow);
    
    start(createTriggerInstance(workflow)
      .data("msg", "world"));
    
    assertEquals("world", getMessage(0));
  }
  
  public static class MyBean {
    String msg;
    public void hello(String msg) {
      this.msg = msg;
    }
  }

  @Test
  public void testJavaServiceTaskBean() {
    ExecutableWorkflow workflow = new ExecutableWorkflow()
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
