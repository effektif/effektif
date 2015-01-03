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
package com.effektif.workflow.test;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.effektif.workflow.api.WorkflowEngine;
import com.effektif.workflow.api.query.WorkflowInstanceQuery;
import com.effektif.workflow.api.query.WorkflowQuery;
import com.effektif.workflow.api.workflow.Workflow;
import com.effektif.workflow.api.workflowinstance.WorkflowInstance;
import com.effektif.workflow.impl.WorkflowEngineImpl;
import com.effektif.workflow.impl.json.JsonService;


/** Base class that allows to reuse tests and run them on different process engines.
 *  
 * @author Walter White
 */
public class WorkflowTest {
  
  public static final Logger log = LoggerFactory.getLogger(WorkflowTest.class);
  
  protected WorkflowEngine workflowEngine = null;
  
  @Before
  public void before() {
    workflowEngine = new TestWorkflowEngineConfiguration()
      .buildWorkflowEngine();
  }
  
  @After
  public void after() {
    logWorkflowEngineContents();
  }

  protected void logWorkflowEngineContents() {
    log.debug("\n\n###### Test ended, logging workflow engine contents ######################################################## \n");
    
    JsonService jsonService = ((WorkflowEngineImpl) workflowEngine).getServiceRegistry().getService(JsonService.class);

    StringBuilder cleanLog = new StringBuilder();
    cleanLog.append("Cleaning up workflow engine\n");
    
//    List<Job> jobs = jobService.newJobQuery().asList();
//    if (jobs != null && !jobs.isEmpty()) {
//      int i = 0;
//      cleanLog.append("\n### jobs ######################################################## \n");
//      for (Job job : jobs) {
//        jobService.deleteJob(job.getId());
//        cleanLog.append("--- Deleted job ");
//        cleanLog.append(i);
//        cleanLog.append(" ---\n");
//        cleanLog.append(jsonService.objectToJsonStringPretty(job));
//        cleanLog.append("\n");
//        i++;
//      }
//    }

//    List<Task> tasks = taskService.newTaskQuery().asList();
//    if (tasks != null && !tasks.isEmpty()) {
//      int i = 0;
//      cleanLog.append("\n### tasks ######################################################## \n");
//      for (Task task : tasks) {
//        taskService.deleteTask(task.getId());
//        cleanLog.append("--- Deleted task ");
//        cleanLog.append(i);
//        cleanLog.append(" ---\n");
//        cleanLog.append(jsonService.objectToJsonStringPretty(task));
//        cleanLog.append("\n");
//        i++;
//      }
//    }

    List<WorkflowInstance> workflowInstances = workflowEngine.findWorkflowInstances(new WorkflowInstanceQuery());
    if (workflowInstances != null && !workflowInstances.isEmpty()) {
      int i = 0;
      cleanLog.append("\n\n### workflowInstances ################################################ \n");
      for (WorkflowInstance workflowInstance : workflowInstances) {
        cleanLog.append("--- Deleted workflow instance ");
        cleanLog.append(i);
        cleanLog.append(" ---\n");
        cleanLog.append(jsonService.objectToJsonStringPretty(workflowInstance));
        cleanLog.append("\n");
        i++;
      }
    }
    List< ? extends Workflow> workflows = workflowEngine.findWorkflows(new WorkflowQuery());
    if (workflows != null && !workflows.isEmpty()) {
      int i = 0;
      cleanLog.append("\n### workflows ######################################################## \n");
      for (Workflow workflow : workflows) {
        cleanLog.append("--- Deleted workflow ");
        cleanLog.append(i);
        cleanLog.append(" ---\n");
        cleanLog.append(jsonService.objectToJsonStringPretty(workflow));
        cleanLog.append("\n");
        i++;
      }
    }
    log.debug(cleanLog.toString());
  }
}
