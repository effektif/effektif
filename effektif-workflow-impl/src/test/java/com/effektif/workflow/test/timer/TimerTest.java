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
package com.effektif.workflow.test.timer;

import org.junit.Test;

import com.effektif.workflow.api.activities.ReceiveTask;
import com.effektif.workflow.api.workflow.ExecutableWorkflow;
import com.effektif.workflow.test.WorkflowTest;


/**
 * @author Tom Baeyens
 */
public class TimerTest extends WorkflowTest {

  @Test
  public void testTimer() {
    ExecutableWorkflow workflow = new ExecutableWorkflow()
      .activity("r", new ReceiveTask()
        .timer(new BoundaryEventTimer()
          .dueDateExpression("0 minutes")));
    
    deploy(workflow);

    // WorkflowInstance workflowInstance = start(workflow);
  }
}
