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

import com.effektif.workflow.impl.WorkflowEngineConfiguration;
import com.effektif.workflow.test.WorkflowTest;
import com.effektif.workflow.test.api.CallTest;
import com.effektif.workflow.test.api.EmbeddedSuprocessTest;
import com.effektif.workflow.test.api.ExclusiveGatewayTest;
import com.effektif.workflow.test.api.MultiInstanceTest;
import com.effektif.workflow.test.api.MultipleStartActivitiesTest;
import com.effektif.workflow.test.api.ParallelGatewayTest;
import com.effektif.workflow.test.api.ScriptTest;
import com.effektif.workflow.test.api.SequentialExecutionTest;
import com.effektif.workflow.test.api.TaskTest;


@SuiteClasses({    
  CallTest.class,
  EmbeddedSuprocessTest.class,
  ExclusiveGatewayTest.class,
  MultiInstanceTest.class,
  MultipleStartActivitiesTest.class,
  ParallelGatewayTest.class,
  ScriptTest.class,
  SequentialExecutionTest.class,
  TaskTest.class
})
@RunWith(Suite.class)
public class SerializationTestSuite {
  
  private static final Logger log = LoggerFactory.getLogger(SerializationTestSuite.class);
  
  static WorkflowEngineConfiguration originalConfiguration;

  @BeforeClass
  public static void switchToSerializingWorkflowEngine() {
    log.debug("Switching to serializing workflow engine");
    originalConfiguration = WorkflowTest.cachedConfiguration;
    
    WorkflowTest.cachedConfiguration = new SerializingWorkflowEngineConfiguration();
  }

  @AfterClass
  public static void switchBackToOriginalWorkflowEngine() {
    log.debug("Switching back to original workflow engine");
    WorkflowTest.cachedConfiguration = originalConfiguration;
  }
}
