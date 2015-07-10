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

import static org.junit.Assert.*;

import org.junit.Test;

import com.effektif.workflow.api.model.TriggerInstance;
import com.effektif.workflow.api.types.ListType;
import com.effektif.workflow.api.types.TextType;
import com.effektif.workflow.api.workflow.ExecutableWorkflow;
import com.effektif.workflow.api.workflow.MultiInstance;
import com.effektif.workflow.api.workflowinstance.WorkflowInstance;
import com.effektif.workflow.impl.util.Lists;
import com.effektif.workflow.test.WorkflowTest;


/**
 * @author Tom Baeyens
 */
public class MultiInstanceTest extends WorkflowTest {
  
  @Test
  public void testMultiInstanceBasics() throws Exception {
    ExecutableWorkflow workflow = new ExecutableWorkflow()
      .variable("reviewers", new ListType(TextType.INSTANCE))
      .activity("Review", msgExpression("reviewer")
        .multiInstance(new MultiInstance()
          .valuesExpression("reviewers")
          .variable("reviewer", TextType.INSTANCE)));
    
    deploy(workflow);
    
    WorkflowInstance workflowInstance = workflowEngine.start(new TriggerInstance()
      .workflowId(workflow.getId())
      .data("reviewers", Lists.of(
              "jack",
              "john", 
              "mary")));

    assertTrue(workflowInstance.isEnded());
    
    assertEquals("jack", getMessage(0));
    assertEquals("john", getMessage(1));
    assertEquals("mary", getMessage(2));
  }
}
