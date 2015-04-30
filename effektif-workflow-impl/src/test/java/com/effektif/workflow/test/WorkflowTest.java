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

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.effektif.workflow.api.Configuration;
import com.effektif.workflow.api.WorkflowEngine;
import com.effektif.workflow.api.deprecated.form.FormInstance;
import com.effektif.workflow.api.deprecated.task.CaseService;
import com.effektif.workflow.api.deprecated.task.Task;
import com.effektif.workflow.api.deprecated.task.TaskQuery;
import com.effektif.workflow.api.deprecated.task.TaskService;
import com.effektif.workflow.api.deprecated.triggers.FormTrigger;
import com.effektif.workflow.api.model.Deployment;
import com.effektif.workflow.api.model.Message;
import com.effektif.workflow.api.model.TriggerInstance;
import com.effektif.workflow.api.query.WorkflowInstanceQuery;
import com.effektif.workflow.api.query.WorkflowQuery;
import com.effektif.workflow.api.workflow.Workflow;
import com.effektif.workflow.api.workflowinstance.ActivityInstance;
import com.effektif.workflow.api.workflowinstance.ScopeInstance;
import com.effektif.workflow.api.workflowinstance.WorkflowInstance;
import com.effektif.workflow.impl.WorkflowInstanceStore;
import com.effektif.workflow.impl.WorkflowStore;
import com.effektif.workflow.impl.deprecated.TaskStore;
import com.effektif.workflow.impl.deprecated.email.EmailStore;
import com.effektif.workflow.impl.deprecated.email.OutgoingEmail;
import com.effektif.workflow.impl.deprecated.email.TestOutgoingEmailService;
import com.effektif.workflow.impl.deprecated.file.File;
import com.effektif.workflow.impl.deprecated.file.FileService;
import com.effektif.workflow.impl.deprecated.json.JsonMapper;
import com.effektif.workflow.impl.job.Job;
import com.effektif.workflow.impl.job.JobQuery;
import com.effektif.workflow.impl.job.JobStore;
import com.effektif.workflow.impl.json.JsonStreamMapper;
import com.effektif.workflow.impl.memory.MemoryIdentityService;
import com.effektif.workflow.impl.memory.TestConfiguration;
import com.effektif.workflow.impl.workflowinstance.WorkflowInstanceImpl;


/** Base class that allows to reuse tests and run them on different process engines. */
public class WorkflowTest {
  
  public static String JOHN_ID = "john";
  public static String MARY_ID = "mary";
  public static String JACK_ID = "jack";
  public static String DEV_ID = "dev";
  public static String OPS_ID = "ops";
  public static String TESTING_ID = "testing";

  public static final Logger log = LoggerFactory.getLogger(WorkflowTest.class);
  
  public static Configuration cachedConfiguration = null;
  
  protected Configuration configuration = null;
  protected WorkflowEngine workflowEngine = null;
  protected CaseService caseService = null;
  protected TaskService taskService = null;
  protected TestOutgoingEmailService emailService = null;
  protected EmailStore emailStore = null;
  protected FileService fileService = null;
  
  @Rule public TestName name = new TestName();

  @Before
  public void initializeWorkflowEngine() {
    log.debug("\n\n###### Test "+getClass().getSimpleName()+"."+name+" starting ######################################################## \n");

    if (workflowEngine==null || taskService==null) {
      if (cachedConfiguration==null) {
        cachedConfiguration = new TestConfiguration();
      }
      configuration = cachedConfiguration;
      workflowEngine = configuration.getWorkflowEngine();
      taskService = configuration.getTaskService();
      caseService = configuration.get(CaseService.class);
      emailService = configuration.get(TestOutgoingEmailService.class);
      emailStore = configuration.get(EmailStore.class);
      fileService = configuration.get(FileService.class);
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
    return workflowEngine.start(new TriggerInstance()
      .workflowId(workflow.getId()));
  }
  
  public WorkflowInstance start(Workflow workflow, FormInstance formInstance) {
    return workflowEngine.start(new TriggerInstance()
      .workflowId(workflow.getId())
      .data(FormTrigger.FORM_INSTANCE_KEY, formInstance));
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
  
  public void assertOpenTaskNames(TaskQuery taskQuery, String... expectedTaskNames) {
    Set<String> expectedTaskNameSet = new HashSet<>();
    if (expectedTaskNames!=null) {
      for (String taskName : expectedTaskNames) {
        expectedTaskNameSet.add(taskName);
      }
    }
    Set<String> taskNameSet = new HashSet<>();
    for (Task task: taskService.findTasks(taskQuery)) {
      taskNameSet.add(task.getName());
    }
    assertEquals(expectedTaskNameSet, taskNameSet);
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
  
  public OutgoingEmail getOutgoingEmail(int index) {
    if (emailService.emails.size()<=index) {
      fail("Can't get email "+index+". There were only "+emailService.emails.size());
    }
    return emailService.emails.get(index);
  }
  
  public File createTestFile(String content, String fileName, String contentType) {
    return createTestFile(content.getBytes(), fileName, contentType);
  }

  public File createTestFile(byte[] bytes, String fileName, String contentType) {
    File file = new File()
      .fileName(fileName)
      .contentType(contentType);
    file = fileService.createFile(file, new ByteArrayInputStream(bytes));
    return file;
  }

  protected void logWorkflowEngineContents() {
    try {
      log.debug("\n\n###### Test "+getClass().getSimpleName()+"."+name+" ending ######################################################## \n");
      
      JsonStreamMapper jsonStreamMapper = configuration.get(JsonStreamMapper.class);
      JsonMapper jsonMapper = configuration.get(JsonMapper.class);
      WorkflowStore workflowStore = configuration.get(WorkflowStore.class);
      WorkflowInstanceStore workflowInstanceStore = configuration.get(WorkflowInstanceStore.class);
      JobStore jobStore = configuration.get(JobStore.class);
      TaskStore taskStore = configuration.get(TaskStore.class);

      StringBuilder cleanLog = new StringBuilder();
      cleanLog.append("Workflow engine contents\n");
      
      List<Job> jobs = jobStore.findJobs(new JobQuery());
      if (jobs != null && !jobs.isEmpty()) {
        int i = 0;
        cleanLog.append("\n=== jobs ======================================================== \n");
        for (Job job : jobs) {
          cleanLog.append("--- Job ");
          cleanLog.append(i);
          cleanLog.append(" ---\n");
          cleanLog.append(jsonMapper.writeToStringPretty(job));
          cleanLog.append("\n");
          i++;
        }
      }

      List<Task> tasks = taskStore.findTasks(new TaskQuery());
      if (tasks != null && !tasks.isEmpty()) {
        int i = 0;
        cleanLog.append("\n=== tasks ======================================================== \n");
        for (Task task : tasks) {
          cleanLog.append("--- Task ");
          cleanLog.append(i);
          cleanLog.append(" ---\n");
          cleanLog.append(jsonMapper.writeToStringPretty(task));
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
          cleanLog.append(jsonStreamMapper.write(workflowInstance.toWorkflowInstance()));
          cleanLog.append("\n");
          i++;
        }
      }

      List<Workflow> workflows = workflowStore.findWorkflows(new WorkflowQuery());
      if (workflows != null && !workflows.isEmpty()) {
        int i = 0;
        cleanLog.append("\n=== workflows ======================================================== \n");
        for (Workflow workflow : workflows) {
          cleanLog.append("--- Deleted workflow ");
          cleanLog.append(i);
          cleanLog.append(" ---\n");
          cleanLog.append(jsonMapper.writeToStringPretty(workflow));
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
    TaskStore taskStore = configuration.get(TaskStore.class);
    emailService.emails.clear();
    configuration.get(MemoryIdentityService.class).deleteUsers();
    configuration.get(MemoryIdentityService.class).deleteGroups();

    workflowStore.deleteWorkflows(new WorkflowQuery());
    workflowInstanceStore.deleteWorkflowInstances(new WorkflowInstanceQuery());
    taskStore.deleteTasks(new TaskQuery());
    jobStore.deleteJobs(new JobQuery());
    jobStore.deleteArchivedJobs(new JobQuery());
  }
}
