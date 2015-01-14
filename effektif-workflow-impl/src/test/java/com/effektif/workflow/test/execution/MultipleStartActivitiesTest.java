/* Copyright (c) 2014, Effektif GmbH.
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
package com.effektif.workflow.test.execution;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.effektif.workflow.api.activities.UserTask;
import com.effektif.workflow.api.workflow.Workflow;
import com.effektif.workflow.api.workflowinstance.WorkflowInstance;
import com.effektif.workflow.test.WorkflowTest;


public class MultipleStartActivitiesTest extends WorkflowTest {
  
  @Test
  public void testDefaultStartActivitiesParallelExecution() {
    Workflow workflow = new Workflow() 
      .activity(new UserTask("one"))
      .activity(new UserTask("two")
        .transitionTo("three"))
      .activity(new UserTask("three"));
    
    workflow = deploy(workflow);
    WorkflowInstance workflowInstance = start(workflow);
    
    assertOpen(workflowInstance, "one", "two");
    
    workflowInstance = endTask(workflowInstance, "two");

    assertOpen(workflowInstance, "one", "three");

    workflowInstance = endTask(workflowInstance, "one");
    workflowInstance = endTask(workflowInstance, "three");

    assertOpen(workflowInstance);

    assertTrue(workflowInstance.isEnded());
  }
}
