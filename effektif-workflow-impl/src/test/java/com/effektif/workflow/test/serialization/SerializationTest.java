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
import com.effektif.workflow.test.WorkflowTest;
import com.effektif.workflow.test.execution.CallTest;


@SuiteClasses(CallTest.class)
@RunWith(Suite.class)
public class SerializationTest {
  
  private static final Logger log = LoggerFactory.getLogger(SerializationTest.class);
  
  static WorkflowEngine originalWorkflowEngine;

  @BeforeClass
  public static void switchToSerializingWorkflowEngine() {
    log.debug("Switching to serializing workflow engine");
    originalWorkflowEngine = WorkflowTest.cachedDefaultWorkflowEngine;
    WorkflowTest.cachedDefaultWorkflowEngine = new SerializingWorkflowEngineConfiguration()
      .buildWorkflowEngine();
  }

  @AfterClass
  public static void switchBackToOriginalWorkflowEngine() {
    log.debug("Switching back to original workflow engine");
    WorkflowTest.cachedDefaultWorkflowEngine = originalWorkflowEngine;
  }
}
