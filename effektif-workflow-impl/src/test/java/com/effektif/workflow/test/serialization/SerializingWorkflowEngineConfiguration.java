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

import com.effektif.workflow.api.WorkflowEngine;
import com.effektif.workflow.impl.WorkflowEngineImpl;
import com.effektif.workflow.impl.memory.MemoryWorkflowEngineConfiguration;


public class SerializingWorkflowEngineConfiguration extends MemoryWorkflowEngineConfiguration {

  @Override
  protected void initializeExecutorService() {
    synchronous();
  }

  @Override
  public WorkflowEngine buildWorkflowEngine() {
    return new SerializingWorkflowEngineImpl(new WorkflowEngineImpl(this));
  }
}
