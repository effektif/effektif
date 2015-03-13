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
package com.effektif.workflow.test.implementation;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.effektif.workflow.api.activities.NoneTask;
import com.effektif.workflow.api.activities.StartEvent;
import com.effektif.workflow.api.workflow.Workflow;
import com.effektif.workflow.impl.WorkflowEngineImpl;
import com.effektif.workflow.impl.WorkflowExecutionListener;
import com.effektif.workflow.impl.workflow.ActivityImpl;
import com.effektif.workflow.impl.workflow.TransitionImpl;
import com.effektif.workflow.impl.workflowinstance.ActivityInstanceImpl;
import com.effektif.workflow.test.WorkflowTest;


/**
 * @author Tom Baeyens
 */
public class LoopMonitoringTest extends WorkflowTest {
  
  private class LoopMonitor {
    Integer treshold;
    Map<ActivityImpl, Integer> counters = new HashMap<>();
    public LoopMonitor(Integer treshold) {
      this.treshold = treshold;
    }
    public void countStart(ActivityImpl activity) {
      Integer count = counters.get(activity);
      if (count==null) {
        count = 1;
      } else {
        count = count+1;
      }
      if (count>treshold) {
        throw new RuntimeException("Infinite loop suspected");
      }
      counters.put(activity, count);
    }
  }

  private class LoopMonitoringListener implements WorkflowExecutionListener {
    private static final String LOOP_MONITOR_KEY = "counters";
    @Override
    public void started(ActivityInstanceImpl activityInstance) {
      Map<String,Object> workflowProperties = activityInstance.workflowInstance.properties;
      if (workflowProperties==null) {
        activityInstance.workflowInstance.properties = new HashMap<>();
        workflowProperties = new HashMap<>();
      }
      LoopMonitor loopMonitor = (LoopMonitor) workflowProperties.get(LOOP_MONITOR_KEY);
      if (loopMonitor==null) {
        loopMonitor = new LoopMonitor(3);
        workflowProperties.put(LOOP_MONITOR_KEY, loopMonitor);
      }
      loopMonitor.countStart(activityInstance.activity);
    }

    @Override
    public void ended(ActivityInstanceImpl instance) {
    }

    @Override
    public void transition(ActivityInstanceImpl activityInstanceFrom, TransitionImpl transition, ActivityInstanceImpl activityInstanceTo) {
    }
  }
  
  LoopMonitoringListener listener;

  @Override
  @Before
  public void initializeWorkflowEngine() {
    super.initializeWorkflowEngine();
    listener = new LoopMonitoringListener();
    ((WorkflowEngineImpl)workflowEngine)
      .addWorkflowExecutionListener(listener);
  }
  
  @After
  public void removeListener() {
    ((WorkflowEngineImpl)workflowEngine)
      .removeWorkflowExecutionListener(listener);
  }

  @Test
  public void testBasicEvents() {
    Workflow workflow = new Workflow()
      .activity("start", new StartEvent()
        .transitionToNext())
      .activity("groundhog", new NoneTask()
        .transitionTo("groundhog"));

    deploy(workflow);

    try {
      start(workflow);
      fail("Expected exception from infinite loop monitoring");
    } catch (RuntimeException e) {
      assertEquals("Infinite loop suspected", e.getMessage());
    }
  }
}
