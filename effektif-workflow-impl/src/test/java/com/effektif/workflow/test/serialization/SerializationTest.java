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
package com.effektif.workflow.test.serialization;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.effektif.workflow.api.WorkflowEngine;
import com.effektif.workflow.impl.WorkflowEngineConfiguration;
import com.effektif.workflow.impl.json.JsonService;
import com.effektif.workflow.impl.task.TaskService;
import com.effektif.workflow.test.TestWorkflowEngineConfiguration;
import com.effektif.workflow.test.WorkflowTest;
import com.effektif.workflow.test.execution.CallTest;


@SuiteClasses(CallTest.class)
@RunWith(Suite.class)
public class SerializationTest {
  
  private static final Logger log = LoggerFactory.getLogger(SerializationTest.class);
  
  static WorkflowEngineConfiguration originalConfiguration;
  static WorkflowEngine originalWorkflowEngine;
  static TaskService originalTaskService;

  @BeforeClass
  public static void switchToSerializingWorkflowEngine() {
    log.debug("Switching to serializing workflow engine");
    originalConfiguration = WorkflowTest.cachedConfiguration;
    originalWorkflowEngine = WorkflowTest.cachedWorkflowEngine;
    originalTaskService = WorkflowTest.cachedTaskService;
    
    WorkflowEngineConfiguration configuration = new TestWorkflowEngineConfiguration();
    WorkflowEngine workflowEngine = configuration.getWorkflowEngine(); 
    TaskService taskService = configuration.getTaskService(); 
    JsonService jsonService = configuration.getServiceRegistry().getService(JsonService.class);
    WorkflowTest.cachedConfiguration = configuration;
    WorkflowTest.cachedWorkflowEngine = new SerializingWorkflowEngineImpl(workflowEngine, jsonService);
    WorkflowTest.cachedTaskService = new SerializingTaskServiceImpl(taskService, jsonService);
  }

  @AfterClass
  public static void switchBackToOriginalWorkflowEngine() {
    log.debug("Switching back to original workflow engine");
    WorkflowTest.cachedConfiguration = originalConfiguration;
    WorkflowTest.cachedWorkflowEngine = originalWorkflowEngine;
    WorkflowTest.cachedTaskService = originalTaskService;
  }
}
