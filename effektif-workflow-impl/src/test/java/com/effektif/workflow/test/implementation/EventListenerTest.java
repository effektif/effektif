/*
 * Copyright 2014 Heisenberg Enterprises Ltd.
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

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.effektif.workflow.api.activities.EndEvent;
import com.effektif.workflow.api.activities.NoneTask;
import com.effektif.workflow.api.activities.ParallelGateway;
import com.effektif.workflow.api.activities.StartEvent;
import com.effektif.workflow.api.workflow.Workflow;
import com.effektif.workflow.impl.WorkflowEngineImpl;
import com.effektif.workflow.impl.WorkflowInstanceEventListener;
import com.effektif.workflow.impl.workflow.TransitionImpl;
import com.effektif.workflow.impl.workflowinstance.ActivityInstanceImpl;
import com.effektif.workflow.test.TestWorkflowEngineConfiguration;
import com.effektif.workflow.test.WorkflowTest;
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
public class EventListenerTest extends WorkflowTest {
  
  private class LoggingListener implements WorkflowInstanceEventListener {
    private List<String> events = new ArrayList<>();

    public List<String> getEvents() {
      return Collections.unmodifiableList(events);
    }

    @Override
    public void started(ActivityInstanceImpl instance) {
      events.add("start " + instance.activity.id);
    }

    @Override
    public void ended(ActivityInstanceImpl instance) {
      events.add("end " + instance.activity.id);
    }

    @Override
    public void transition(ActivityInstanceImpl activityInstanceFrom, TransitionImpl transition, ActivityInstanceImpl activityInstanceTo) {
      events.add("take "+transition.from.id+"->"+transition.to.id);
    }
  }

  WorkflowEngineImpl workflowEngineImpl;
  LoggingListener listener;

  @Override
  @Before
  public void initializeWorkflowEngine() {
    super.initializeWorkflowEngine();
    listener = new LoggingListener();
    workflowEngineImpl = (WorkflowEngineImpl) workflowEngine;
    workflowEngineImpl.addListener(listener);
  }
  
  @After
  public void removeListener() {
    workflowEngineImpl.removeListener(listener);
  }

  @Test
  public void testBasicEvents() {
    Workflow w = new Workflow()
      .activity(new StartEvent("s")
        .transitionTo("t"))
      .activity(new NoneTask("t")
        .transitionTo("e"))
      .activity(new EndEvent("e"));

    w = deploy(w);

    start(w);

    int i = 0;
    assertEquals("start s", listener.getEvents().get(i++));
    assertEquals("end s", listener.getEvents().get(i++));
    assertEquals("take s->t", listener.getEvents().get(i++));
    assertEquals("start t", listener.getEvents().get(i++));
    assertEquals("end t", listener.getEvents().get(i++));
    assertEquals("take t->e", listener.getEvents().get(i++));
    assertEquals("start e", listener.getEvents().get(i++));
    assertEquals("end e", listener.getEvents().get(i++));
  }

  @Test
  public void testParallelGatewayFullEvents() {
    //           /- a -\
    //  s - g1 -X       X- g2 - e
    //           \- b -/
    Workflow w = new Workflow()
      .activity(new NoneTask("s")
        .transitionTo("g1"))
      .activity(new ParallelGateway("g1")
        .transitionTo("a")
        .transitionTo("b"))
      .activity(new NoneTask("a")
        .transitionTo("g2"))
      .activity(new NoneTask("b")
        .transitionTo("g2"))
      .activity(new ParallelGateway("g2")
        .transitionTo("e"))
      .activity(new EndEvent("e"));

    w = deploy(w);
    start(w);

    int i = 0;
    assertEquals("start s", listener.getEvents().get(i++));
    assertEquals("end s", listener.getEvents().get(i++));
    assertEquals("take s->g1", listener.getEvents().get(i++));
    assertEquals("start g1", listener.getEvents().get(i++));
    assertEquals("end g1", listener.getEvents().get(i++));
    assertEquals("take g1->a", listener.getEvents().get(i++));
    assertEquals("take g1->b", listener.getEvents().get(i++));
    assertEquals("start a", listener.getEvents().get(i++));
    assertEquals("end a", listener.getEvents().get(i++));
    assertEquals("take a->g2", listener.getEvents().get(i++));
    assertEquals("start b", listener.getEvents().get(i++));
    assertEquals("end b", listener.getEvents().get(i++));
    assertEquals("take b->g2", listener.getEvents().get(i++));
    assertEquals("start g2", listener.getEvents().get(i++));
    assertEquals("end g2", listener.getEvents().get(i++));
    assertEquals("start g2", listener.getEvents().get(i++));
    assertEquals("end g2", listener.getEvents().get(i++));
    assertEquals("take g2->e", listener.getEvents().get(i++));
    assertEquals("start e", listener.getEvents().get(i++));
    assertEquals("end e", listener.getEvents().get(i++));
  }

  @Test
  public void testParallelGatewayDirectEvents() {
    //           /- - -\
    //  s - g1 -X       X- g2 - e
    //           \- b -/
    Workflow w = new Workflow()
      .activity(new StartEvent("s")
        .transitionTo("g1"))
      .activity(new ParallelGateway("g1")
        .transitionTo("g2")
        .transitionTo("b"))
      .activity(new NoneTask("b")
        .transitionTo("g2"))
      .activity(new ParallelGateway("g2")
        .transitionTo("e"))
      .activity(new EndEvent("e"));

    w = deploy(w);
    start(w);

    int i = 0;
    assertEquals("start s", listener.getEvents().get(i++));
    assertEquals("end s", listener.getEvents().get(i++));
    assertEquals("take s->g1", listener.getEvents().get(i++));
    assertEquals("start g1", listener.getEvents().get(i++));
    assertEquals("end g1", listener.getEvents().get(i++));
    assertEquals("take g1->g2", listener.getEvents().get(i++));
    assertEquals("take g1->b", listener.getEvents().get(i++));
    assertEquals("start g2", listener.getEvents().get(i++));
    assertEquals("end g2", listener.getEvents().get(i++));
    assertEquals("start b", listener.getEvents().get(i++));
    assertEquals("end b", listener.getEvents().get(i++));
    assertEquals("take b->g2", listener.getEvents().get(i++));
    assertEquals("start g2", listener.getEvents().get(i++));
    assertEquals("end g2", listener.getEvents().get(i++));
    assertEquals("take g2->e", listener.getEvents().get(i++));
    assertEquals("start e", listener.getEvents().get(i++));
    assertEquals("end e", listener.getEvents().get(i++));
  }
}
