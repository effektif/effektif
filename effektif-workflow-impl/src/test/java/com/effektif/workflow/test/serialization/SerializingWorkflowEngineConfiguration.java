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
import com.effektif.workflow.impl.WorkflowEngineConfiguration;
import com.effektif.workflow.impl.json.JsonService;
import com.effektif.workflow.impl.task.TaskService;
import com.effektif.workflow.test.TestWorkflowEngineConfiguration;


public class SerializingWorkflowEngineConfiguration extends WorkflowEngineConfiguration {

  WorkflowEngine workflowEngine;
  TaskService taskService;
  
  public SerializingWorkflowEngineConfiguration() {
    TestWorkflowEngineConfiguration configuration = new TestWorkflowEngineConfiguration()
      .initialize();
    WorkflowEngine workflowEngine = configuration.getWorkflowEngine(); 
    TaskService taskService = configuration.getTaskService(); 
    JsonService jsonService = configuration.getServiceRegistry().getService(JsonService.class);
    this.workflowEngine = new SerializingWorkflowEngineImpl(workflowEngine, jsonService);
    this.taskService = new SerializingTaskServiceImpl(taskService, jsonService);
  }
  
  @Override
  public SerializingWorkflowEngineConfiguration initialize() {
    return this;
  }

  public WorkflowEngine getWorkflowEngine() {
    return workflowEngine;
  }
  
  public TaskService getTaskService() {
    return taskService;
  }
}
