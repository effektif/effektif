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

import com.effektif.workflow.api.Configuration;
import com.effektif.workflow.api.WorkflowEngine;
import com.effektif.workflow.api.activities.JavaServiceTask;
import com.effektif.workflow.api.model.Deployment;
import com.effektif.workflow.api.model.Message;
import com.effektif.workflow.api.model.TriggerInstance;
import com.effektif.workflow.api.query.WorkflowInstanceQuery;
import com.effektif.workflow.api.query.WorkflowQuery;
import com.effektif.workflow.api.workflow.ExecutableWorkflow;
import com.effektif.workflow.api.workflowinstance.ActivityInstance;
import com.effektif.workflow.api.workflowinstance.ScopeInstance;
import com.effektif.workflow.api.workflowinstance.WorkflowInstance;
import com.effektif.workflow.impl.WorkflowInstanceStore;
import com.effektif.workflow.impl.WorkflowStore;
import com.effektif.workflow.impl.job.Job;
import com.effektif.workflow.impl.job.JobStore;
import com.effektif.workflow.impl.json.JsonStreamMapper;
import com.effektif.workflow.impl.memory.TestConfiguration;
import com.effektif.workflow.impl.workflowinstance.WorkflowInstanceImpl;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


/** Base class that allows to reuse tests and run them on different process engines. */
public class WorkflowTest {

  public static final String FORM_INSTANCE_KEY = "formInstance";

  public static String JOHN_ID = "john";
  public static String MARY_ID = "mary";
  public static String JACK_ID = "jack";
  public static String DEV_ID = "dev";
  public static String OPS_ID = "ops";
  public static String TESTING_ID = "testing";

  public static final Logger log = LoggerFactory.getLogger(WorkflowTest.class);
  
  public static Configuration cachedConfiguration = null;
  private static List<String> messages = null;
  
  protected Configuration configuration = null;
  protected WorkflowEngine workflowEngine = null;
  
  @Rule public TestName name = new TestName();

  @Before
  public void initializeWorkflowEngine() {
    log.debug("\n\n###### Test "+getClass().getSimpleName()+".class, \""+name.getMethodName()+"\" starting ######################################################## \n");

    if (workflowEngine==null) {
      if (cachedConfiguration==null) {
        cachedConfiguration = createConfiguration();
      }
      configuration = cachedConfiguration;
      workflowEngine = configuration.getWorkflowEngine();
    }
  }

  @Before
  public void initializeMessages() {
    messages = new ArrayList<>();
  }

  public TestConfiguration createConfiguration() {
    TestConfiguration testConfiguration = new TestConfiguration();
    testConfiguration.get(JsonStreamMapper.class).pretty();
    testConfiguration.start();
    return testConfiguration;
  }
  
  @After
  public void after() {
    if (configuration!=null) {
      logWorkflowEngineContents();
      deleteWorkflowEngineContents();
    }
  }
  
  public String getMessage(int index) {
    return messages.get(index);
  }
  public List<String> getMessages() {
    return messages;
  }

  @SuppressWarnings("unused") // invoked dynamically with reflection
  private static void recordMessage(String message) {
    messages.add(message);
  }
  public JavaServiceTask msgValue(String message) {
    return new JavaServiceTask()
      .javaClass(WorkflowTest.class)
      .methodName("recordMessage")
      .argValue(message);
  }
  public JavaServiceTask msgExpression(String messageExpression) {
    return new JavaServiceTask()
      .javaClass(WorkflowTest.class)
      .methodName("recordMessage")
      .argExpression(messageExpression);
  }


  public Deployment deploy(ExecutableWorkflow workflow) {
    Deployment deployment = workflowEngine.deployWorkflow(workflow);
    deployment.checkNoErrors();
    workflow.setId(deployment.getWorkflowId());
    return deployment;
  }

  public TriggerInstance createTriggerInstance(ExecutableWorkflow workflow) {
    return new TriggerInstance().workflowId(workflow.getId());
  }

  public WorkflowInstance start(ExecutableWorkflow workflow) {
    return workflowEngine.start(new TriggerInstance()
      .workflowId(workflow.getId()));
  }
  
  public WorkflowInstance start(TriggerInstance triggerInstance) {
    return workflowEngine.start(triggerInstance);
  }
  
  public WorkflowInstance sendMessage(WorkflowInstance workflowInstance, String activityInstanceId) {
    return workflowEngine.send(new Message()
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
    assertEquals("activity counts", expectedActivityCounts, activityCounts);
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
    return workflowEngine.send(new Message()
      .workflowInstanceId(workflowInstance.getId())
      .activityInstanceId(activityInstance.getId()));
  }
  
  protected void logWorkflowEngineContents() {
    try {
      log.debug("\n\n###### Test "+getClass().getSimpleName()+"."+name+" ending ######################################################## \n");
      
      JsonStreamMapper jsonMapper = configuration.get(JsonStreamMapper.class);
      WorkflowStore workflowStore = configuration.get(WorkflowStore.class);
      WorkflowInstanceStore workflowInstanceStore = configuration.get(WorkflowInstanceStore.class);
      JobStore jobStore = configuration.get(JobStore.class);
      // TaskStore taskStore = configuration.get(TaskStore.class);

      StringBuilder cleanLog = new StringBuilder();
      cleanLog.append("Workflow engine contents\n");
      
      List<Job> jobs = jobStore.findAllJobs();
      if (jobs != null && !jobs.isEmpty()) {
        int i = 0;
        cleanLog.append("\n=== jobs ======================================================== \n");
        for (Job job : jobs) {
          cleanLog.append("--- Job ");
          cleanLog.append(i);
          cleanLog.append(" ---\n");
          cleanLog.append(jsonMapper.write(job));
          cleanLog.append("\n");
          i++;
        }
      }

      List<WorkflowInstanceImpl> workflowInstances = workflowInstanceStore.findWorkflowInstances(new WorkflowInstanceQuery());
      if (workflowInstances != null && !workflowInstances.isEmpty()) {
        int i = 0;
        cleanLog.append("\n\n=== workflowInstances ================================================ \n");
        for (WorkflowInstanceImpl workflowInstance : workflowInstances) {
          cleanLog.append("--- Workflow instance ");
          cleanLog.append(i);
          cleanLog.append(" ---\n");
          cleanLog.append(jsonMapper.write(workflowInstance.toWorkflowInstance()));
          cleanLog.append("\n");
          i++;
        }
      }

      List<ExecutableWorkflow> workflows = workflowStore.findWorkflows(new WorkflowQuery());
      if (workflows != null && !workflows.isEmpty()) {
        int i = 0;
        cleanLog.append("\n=== workflows ======================================================== \n");
        for (ExecutableWorkflow workflow : workflows) {
          cleanLog.append("--- Deleted workflow ");
          cleanLog.append(i);
          cleanLog.append(" ---\n");
          cleanLog.append(jsonMapper.write(workflow));
          cleanLog.append("\n");
          i++;
        }
      }
      log.debug(cleanLog.toString());
    } catch (Exception e) {
      log.error("ERROR in test "+getClass().getName()+"."+name, e);
    }
  }
  
  protected void deleteWorkflowEngineContents() {
    WorkflowStore workflowStore = configuration.get(WorkflowStore.class);
    WorkflowInstanceStore workflowInstanceStore = configuration.get(WorkflowInstanceStore.class);
    JobStore jobStore = configuration.get(JobStore.class);

    workflowStore.deleteAllWorkflows();
    workflowInstanceStore.deleteAllWorkflowInstances();
    jobStore.deleteAllJobs();
    jobStore.deleteAllArchivedJobs();
  }
}
