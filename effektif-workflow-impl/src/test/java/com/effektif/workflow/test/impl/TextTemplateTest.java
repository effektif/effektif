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

import com.effektif.workflow.impl.WorkflowParser;
import com.effektif.workflow.impl.template.TextTemplate;
import com.effektif.workflow.impl.template.TextTemplate.ExpressionTemplateElement;
import com.effektif.workflow.impl.template.TextTemplate.StringTemplateElement;
import com.effektif.workflow.impl.template.TextTemplate.TemplateElement;
import com.effektif.workflow.impl.workflowinstance.WorkflowInstanceImpl;
import com.effektif.workflow.test.WorkflowTest;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;

/**
 * @author Tom Baeyens
 */
public class TextTemplateTest extends WorkflowTest {
  
  WorkflowParser workflowParser;
  
  @Before
  public void initializeWorkflowParser() {
    workflowParser = new WorkflowParser(configuration);
    workflowParser.variableIds.add("v1");
    workflowParser.variableIds.add("v2");
  }

  @Test
  public void testTextTemplateParsing() {
    TextTemplate tt = parse("aaa {{v1}} bbb");
    int i = 0;
    assertTemplateString("aaa ", tt, i++);
    assertTemplateExpression("{{v1}}", tt, i++);
    assertTemplateString(" bbb", tt, i++);
    assertResolveTemplate("aaa x bbb", tt); 
    
    tt = parse("aaa {{v1.firstName}} bbb");
    i = 0;
    assertTemplateString("aaa ", tt, i++);
    assertTemplateExpression("{{v1.firstName}}", tt, i++);
    assertTemplateString(" bbb", tt, i++);
    
    tt = parse("aaa {{v1.firstName.lastName}} bbb");
    i = 0;
    assertTemplateString("aaa ", tt, i++);
    assertTemplateExpression("{{v1.firstName.lastName}}", tt, i++);
    assertTemplateString(" bbb", tt, i++);

    tt = parse("{{v1}}{{v2}}");
    i = 0;
    assertTemplateExpression("{{v1}}", tt, i++);
    assertTemplateExpression("{{v2}}", tt, i++);
    assertResolveTemplate("xy", tt); 
  }

  @Test
  public void testUnsetVariableFieldRendersEmptyString() {
    TextTemplate template = parse("{{user.firstName}}");

    WorkflowInstanceImpl workflowInstance = new WorkflowInstanceImpl();
    workflowInstance.configuration = configuration;
    workflowInstance.workflowInstance = workflowInstance;
    workflowInstance.nextVariableInstanceId = 1l;
    workflowInstance.setVariableValue("user", new HashMap<>());
    assertEquals("", template.resolve(workflowInstance));
  }

  public void assertResolveTemplate(String expected, TextTemplate tt) {
    WorkflowInstanceImpl workflowInstance = new WorkflowInstanceImpl();
    workflowInstance.configuration = configuration;
    workflowInstance.workflowInstance = workflowInstance;
    workflowInstance.nextVariableInstanceId = 1l;
    workflowInstance.setVariableValue("v1", "x");
    workflowInstance.setVariableValue("v2", "y");
    assertEquals(expected, tt.resolve(workflowInstance));
  }

  public void assertTemplateString(String expectedStringValue, TextTemplate tt, int i) {
    TemplateElement templateElement = tt.elements.get(i);
    assertTrue(templateElement instanceof StringTemplateElement);
    assertEquals(expectedStringValue, templateElement.toString());
  }

  public void assertTemplateExpression(String expectedStringValue, TextTemplate tt, int i) {
    TemplateElement templateElement = tt.elements.get(i);
    assertTrue(templateElement instanceof ExpressionTemplateElement);
    assertEquals(expectedStringValue, templateElement.toString());
  }

  protected TextTemplate parse(String templateText) {
    return new TextTemplate(templateText, null, workflowParser);
  }
}
