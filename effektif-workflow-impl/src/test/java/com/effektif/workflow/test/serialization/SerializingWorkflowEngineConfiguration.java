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

import com.effektif.workflow.api.Configuration;
import com.effektif.workflow.api.TaskService;
import com.effektif.workflow.api.WorkflowEngine;
import com.effektif.workflow.impl.WorkflowInstanceStore;
import com.effektif.workflow.impl.WorkflowStore;
import com.effektif.workflow.impl.job.JobService;
import com.effektif.workflow.impl.job.JobStore;
import com.effektif.workflow.impl.json.JsonService;
import com.effektif.workflow.impl.memory.TestConfiguration;


public class SerializingWorkflowEngineConfiguration implements Configuration {

  WorkflowEngine workflowEngine;
  TaskService taskService;
  Configuration configuration;
  
  public SerializingWorkflowEngineConfiguration() {
    TestConfiguration configuration = new TestConfiguration();
    WorkflowEngine workflowEngine = configuration.getWorkflowEngine(); 
    TaskService taskService = configuration.getTaskService(); 
    JsonService jsonService = configuration.get(JsonService.class);
    this.workflowEngine = new SerializingWorkflowEngineImpl(workflowEngine, jsonService);
    this.taskService = new SerializingTaskServiceImpl(taskService, jsonService);
    this.configuration = configuration;
  }
  
  public WorkflowEngine getWorkflowEngine() {
    return workflowEngine;
  }
  
  public TaskService getTaskService() {
    return taskService;
  }

  @Override
  public <T> T get(Class<T> type) {
    if (JsonService.class.isAssignableFrom(type)
        || WorkflowStore.class.isAssignableFrom(type)
        || WorkflowInstanceStore.class.isAssignableFrom(type)
        || JobStore.class.isAssignableFrom(type)
        || JobService.class.isAssignableFrom(type)) {
      return configuration.get(type);
    } else if (WorkflowEngine.class.isAssignableFrom(type)) {
      return (T) workflowEngine;
    } else if (TaskService.class.isAssignableFrom(type)) {
      return (T) taskService;
    }
    throw new RuntimeException("damn.. i didn't expect you needed "+type);
  }
}
