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

import com.effektif.workflow.api.activities.ReceiveTask;
import com.effektif.workflow.api.activities.StartEvent;
import com.effektif.workflow.api.model.TriggerInstance;
import com.effektif.workflow.api.workflow.ExecutableWorkflow;
import com.effektif.workflow.api.workflowinstance.WorkflowInstance;
import com.effektif.workflow.test.WorkflowTest;


/**
 * @author Tom Baeyens
 */
public class MultipleStartActivitiesTest extends WorkflowTest {

  @Test
  public void testDefaultStartActivitiesParallelExecution() {
    ExecutableWorkflow workflow = new ExecutableWorkflow()
      .activity("one", new ReceiveTask())
      .activity("two", new ReceiveTask()
              .transitionTo("three"))
      .activity("three", new ReceiveTask());

    deploy(workflow);
    WorkflowInstance workflowInstance = start(workflow);

    assertOpen(workflowInstance, "one", "two");

    workflowInstance = endTask(workflowInstance, "two");

    assertOpen(workflowInstance, "one", "three");

    workflowInstance = endTask(workflowInstance, "one");
    workflowInstance = endTask(workflowInstance, "three");

    assertOpen(workflowInstance);

    assertTrue(workflowInstance.isEnded());
  }


  @Test
  public void testMultipleStartEventsSingleActivityTrigger() {
    ExecutableWorkflow workflow = new ExecutableWorkflow()
      .activity("start 1", new StartEvent()
              .transitionTo("receive 1"))
      .activity("receive 1", new ReceiveTask())
      .activity("start 2", new StartEvent()
              .transitionTo("receive 2"))
      .activity("receive 2", new ReceiveTask());

      deploy(workflow);

      WorkflowInstance workflowInstance = start(new TriggerInstance()
              .workflowId(workflow.getId())
              .addStartActivityId("start 1"));

      assertOpen(workflowInstance, "receive 1");

      // check that only one activityInstance was created, it should exist now in "receive 1",
      // there should NOT be one in "receive 2".
      assertNotNull(workflowInstance.findOpenActivityInstance("receive 1"));
      assertNull(workflowInstance.findOpenActivityInstance("receive 2"));

    // Now test that all startEvents will be fired when addStartActivityId() method is not called.
    ExecutableWorkflow workflow2 = new ExecutableWorkflow()
            .activity("start 1", new StartEvent()
                    .transitionTo("receive 1"))
            .activity("receive 1", new ReceiveTask())
            .activity("start 2", new StartEvent()
                    .transitionTo("receive 2"))
            .activity("receive 2", new ReceiveTask());

    deploy(workflow2);

    WorkflowInstance workflowInstance2 = start(new TriggerInstance()
            .workflowId(workflow2.getId()));

    assertOpen(workflowInstance2, "receive 1", "receive 2");
  }
}
