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
package com.effektif.workflow.test.api;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.effektif.workflow.api.activities.UserTask;
import com.effektif.workflow.api.workflow.Workflow;
import com.effektif.workflow.impl.task.TaskQuery;
import com.effektif.workflow.test.WorkflowTest;


public class TaskTest extends WorkflowTest {

  @Test
  public void testTask() throws Exception {
    Workflow workflow = new Workflow()
      .activity(new UserTask("Task one"));
    
    workflow = deploy(workflow);
    
    start(workflow);
    
    assertEquals("Task one", taskService.findTasks(new TaskQuery()).get(0).getName());
  }
}
