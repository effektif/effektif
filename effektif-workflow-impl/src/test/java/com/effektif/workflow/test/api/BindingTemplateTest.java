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
import com.effektif.workflow.api.model.TriggerInstance;
import com.effektif.workflow.api.types.ListType;
import com.effektif.workflow.api.types.TextType;
import com.effektif.workflow.api.workflow.ExecutableWorkflow;
import com.effektif.workflow.impl.util.Lists;
import com.effektif.workflow.test.WorkflowTest;


/**
 * @author Tom Baeyens
 */
public class BindingTemplateTest extends WorkflowTest {
  
  static Object templateValue =  null;
  
  public static void setTemplateValue(Object templateValue) {
    BindingTemplateTest.templateValue = templateValue;
  }
  
  @Test
  public void testTextTemplateRenders() {
    ExecutableWorkflow workflow = new ExecutableWorkflow()
      .variable("world", TextType.INSTANCE)
      .activity("invoke hello", new JavaServiceTask()
        .javaClass(BindingTemplateTest.class)
        .methodName("setTemplateValue")
        .argTemplate("hello {{world}}"));
    
    deploy(workflow);
    
    start(new TriggerInstance()
      .workflowId(workflow.getId())
      .data("world", "testrunner"));
    
    assertEquals("hello testrunner", templateValue);
  }

  @Test
  public void testTextTemplateRendersList() {
    ExecutableWorkflow workflow = new ExecutableWorkflow()
      .variable("colours", new ListType(TextType.INSTANCE))
      .activity("render", new JavaServiceTask()
        .javaClass(BindingTemplateTest.class)
        .methodName("setTemplateValue")
        .argTemplate("Colours: {{colours}}"));

    deploy(workflow);

    start(new TriggerInstance()
      .workflowId(workflow.getId())
      .data("colours", Lists.of("red", "orange", "yellow")));

    assertEquals("Colours: \n\n* red\n* orange\n* yellow\n\n", templateValue);
  }

}
