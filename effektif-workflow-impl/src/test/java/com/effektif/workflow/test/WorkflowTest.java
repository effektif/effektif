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
package com.effektif.workflow.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.effektif.workflow.api.Configuration;
import com.effektif.workflow.api.WorkflowEngine;
import com.effektif.workflow.api.model.Deployment;
import com.effektif.workflow.api.model.Message;
import com.effektif.workflow.api.model.Start;
import com.effektif.workflow.api.query.WorkflowInstanceQuery;
import com.effektif.workflow.api.query.WorkflowQuery;
import com.effektif.workflow.api.task.Task;
import com.effektif.workflow.api.task.TaskQuery;
import com.effektif.workflow.api.task.TaskService;
import com.effektif.workflow.api.workflow.Workflow;
import com.effektif.workflow.api.workflowinstance.ActivityInstance;
import com.effektif.workflow.api.workflowinstance.ScopeInstance;
import com.effektif.workflow.api.workflowinstance.WorkflowInstance;
import com.effektif.workflow.impl.WorkflowInstanceStore;
import com.effektif.workflow.impl.WorkflowStore;
import com.effektif.workflow.impl.job.Job;
import com.effektif.workflow.impl.job.JobQuery;
import com.effektif.workflow.impl.job.JobStore;
import com.effektif.workflow.impl.json.JsonService;
import com.effektif.workflow.impl.memory.TestConfiguration;
import com.effektif.workflow.impl.workflowinstance.WorkflowInstanceImpl;


/** Base class that allows to reuse tests and run them on different process engines. */
public class WorkflowTest {
  
  public static final Logger log = LoggerFactory.getLogger(WorkflowTest.class);
  
  public static Configuration cachedConfiguration = null;
  
  protected Configuration configuration = null;
  protected WorkflowEngine workflowEngine = null;
  protected TaskService taskService = null;
  
  @Before
  public void initializeWorkflowEngine() {
    if (workflowEngine==null || taskService==null) {
      if (cachedConfiguration==null) {
        cachedConfiguration = new TestConfiguration();
      }
      configuration = cachedConfiguration;
      workflowEngine = configuration.getWorkflowEngine();
      taskService = configuration.getTaskService();
    }
  }
  
  @After
  public void after() {
    if (configuration!=null) {
      logWorkflowEngineContents();
      deleteWorkflowEngineContents();
    }
  }

  public Deployment deploy(Workflow workflow) {
    Deployment deployment = workflowEngine.deployWorkflow(workflow);
    workflow.setId(deployment.getWorkflowId());
    return deployment;
  }

  public WorkflowInstance start(Workflow workflow) {
    return workflowEngine.startWorkflowInstance(new Start()
      .workflowId(workflow.getId()));
  }
  
  public WorkflowInstance sendMessage(WorkflowInstance workflowInstance, String activityInstanceId) {
    return workflowEngine.sendMessage(new Message()
      .workflowInstanceId(workflowInstance.getId())
      .activityInstanceId(activityInstanceId));
  }
  
  public static void assertOpen(WorkflowInstance workflowInstance, String... expectedActivityNames) {
    Map<String,Integer> expectedActivityCounts = new HashMap<String, Integer>();
    if (expectedActivityNames!=null) {
      for (String expectedActivityName: expectedActivityNames) {
        Integer count = expectedActivityCounts.get(expectedActivityName);
        expectedActivityCounts.put(expectedActivityName, count!=null ? count+1 : 1);
      }
    }
    Map<String,Integer> activityCounts = new HashMap<String, Integer>();
    scanActivityCounts(workflowInstance, activityCounts);
    assertEquals(expectedActivityCounts, activityCounts);
  }
  
  static void scanActivityCounts(ScopeInstance scopeInstance, Map<String, Integer> activityCounts) {
    List< ? extends ActivityInstance> activityInstances = scopeInstance.getActivityInstances();
    if (activityInstances!=null) {
      for (ActivityInstance activityInstance : activityInstances) {
        if (!activityInstance.isEnded()) {
          Object activityId = activityInstance.getActivityId();
          Integer count = activityCounts.get(activityId);
          activityCounts.put(activityId.toString(), count != null ? count + 1 : 1);
          scanActivityCounts(activityInstance, activityCounts);
        }
      }
    }
  }
  
  public static String getActivityInstanceId(WorkflowInstance workflowInstance, String activityId) {
    ActivityInstance activityInstance = workflowInstance.findOpenActivityInstance(activityId);
    Assert.assertNotNull("No open activity instance found "+activityId+" not found", activityInstance);
    return activityInstance.getId();
  }
  
  public WorkflowInstance endTask(WorkflowInstance workflowInstance, String activityId) {
    ActivityInstance activityInstance = workflowInstance.findOpenActivityInstance(activityId);
    assertNotNull("Activity '"+activityId+"' not in workflow instance", activityInstance);
    return workflowEngine.sendMessage(new Message()
      .workflowInstanceId(workflowInstance.getId())
      .activityInstanceId(activityInstance.getId()));
  }

  protected void logWorkflowEngineContents() {
    log.debug("\n\n###### Test ended, logging workflow engine contents ######################################################## \n");
    
    JsonService jsonService = configuration.get(JsonService.class);
    WorkflowStore workflowStore = configuration.get(WorkflowStore.class);
    WorkflowInstanceStore workflowInstanceStore = configuration.get(WorkflowInstanceStore.class);
    JobStore jobStore = configuration.get(JobStore.class);
    TaskService taskService = configuration.get(TaskService.class);

    StringBuilder cleanLog = new StringBuilder();
    cleanLog.append("Workflow engine contents\n");
    
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

    List<Job> jobs = jobStore.findJobs(new JobQuery());
    if (jobs != null && !jobs.isEmpty()) {
      int i = 0;
      cleanLog.append("\n### jobs ######################################################## \n");
      for (Job job : jobs) {
        cleanLog.append("--- Job ");
        cleanLog.append(i);
        cleanLog.append(" ---\n");
        cleanLog.append(jsonService.objectToJsonStringPretty(job));
        cleanLog.append("\n");
        i++;
      }
    }

    List<Task> tasks = taskService.findTasks(new TaskQuery());
    if (tasks != null && !tasks.isEmpty()) {
      int i = 0;
      cleanLog.append("\n### tasks ######################################################## \n");
      for (Task task : tasks) {
        cleanLog.append("--- Task ");
        cleanLog.append(i);
        cleanLog.append(" ---\n");
        cleanLog.append(jsonService.objectToJsonStringPretty(task));
        cleanLog.append("\n");
        i++;
      }
    }

    List<WorkflowInstanceImpl> workflowInstances = workflowInstanceStore.findWorkflowInstances(new WorkflowInstanceQuery());
    if (workflowInstances != null && !workflowInstances.isEmpty()) {
      int i = 0;
      cleanLog.append("\n\n### workflowInstances ################################################ \n");
      for (WorkflowInstanceImpl workflowInstance : workflowInstances) {
        cleanLog.append("--- Workflow instance ");
        cleanLog.append(i);
        cleanLog.append(" ---\n");
        cleanLog.append(jsonService.objectToJsonStringPretty(workflowInstance.toWorkflowInstance()));
        cleanLog.append("\n");
        i++;
      }
    }

    List<Workflow> workflows = workflowStore.findWorkflows(new WorkflowQuery());
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
  
  protected void deleteWorkflowEngineContents() {
    WorkflowStore workflowStore = configuration.get(WorkflowStore.class);
    WorkflowInstanceStore workflowInstanceStore = configuration.get(WorkflowInstanceStore.class);
    JobStore jobStore = configuration.get(JobStore.class);
    TaskService taskService = configuration.get(TaskService.class);

    workflowStore.deleteWorkflows(new WorkflowQuery());
    workflowInstanceStore.deleteWorkflowInstances(new WorkflowInstanceQuery());
    taskService.deleteTasks(new TaskQuery());
    jobStore.deleteJobs(new JobQuery());
    jobStore.deleteArchivedJobs(new JobQuery());
  }
}
