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
package com.effektif.workflow.test.jsonspike;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.joda.time.LocalDateTime;
import org.junit.BeforeClass;
import org.junit.Test;

import com.effektif.workflow.api.activities.Call;
import com.effektif.workflow.api.activities.EmbeddedSubprocess;
import com.effektif.workflow.api.activities.EndEvent;
import com.effektif.workflow.api.activities.ExclusiveGateway;
import com.effektif.workflow.api.activities.StartEvent;
import com.effektif.workflow.api.model.WorkflowId;
import com.effektif.workflow.api.workflow.Transition;
import com.effektif.workflow.api.workflow.Workflow;
import com.effektif.workflow.impl.util.Lists;
import com.effektif.workflow.test.jsonspike.json.JsonStreamMapper;


/**
 * @author Tom Baeyens
 */
public class WorkflowSerializationTest {
  
  static JsonStreamMapper jsonStreamMapper = null;
  
  @BeforeClass
  public static void initialize() {
    jsonStreamMapper = new JsonStreamMapper();
    jsonStreamMapper.pretty();
  }

  public <T> T serialize(T o) {
    String jsonString = jsonStreamMapper.write(o);
    System.out.println(jsonString);
    return jsonStreamMapper.readString(jsonString, o.getClass());
  }
  
  @Test 
  public void testWorkflow() {
    LocalDateTime now = new LocalDateTime();

    Map<String,Object> p = new HashMap<>();
    p.put("str", "s");
    p.put("lis", Lists.of("a", 1, true));
    p.put("num", Long.MAX_VALUE);
    p.put("dou", Double.MAX_VALUE);
    p.put("boo", true);
    
    Workflow workflow = new Workflow()
      .id(new WorkflowId("i"))
      .createTime(now)
      .description("d")
      .name("w")
      .property("p", p);
    
    workflow = serialize(workflow);
    
    assertNotNull(workflow);
    assertEquals("w", workflow.getName());
    assertEquals(now, workflow.getCreateTime());
  }

  @Test 
  public void testCall() {
    Workflow workflow = new Workflow()
      .activity(new Call()
        .subWorkflowName("sws"));

    workflow = serialize(workflow);
    
    assertNotNull(workflow);
  }

  @Test
  public void testEndEvent() {
    EndEvent activity = new EndEvent();
    activity.setId("releaseComplete");
    activity.setName("software released");
    activity.setDescription("Ends the process when the release is complete.");
    activity = serialize(activity);
    assertEquals(EndEvent.class, activity.getClass());
    assertEquals("releaseComplete", activity.getId());
    assertEquals("software released", activity.getName());
    assertEquals("Ends the process when the release is complete.", activity.getDescription());
    assertNull(activity.getOutgoingTransitions());
  }

  @Test
  public void testExclusiveGateway() {
    ExclusiveGateway activity = (ExclusiveGateway) new ExclusiveGateway()
      .id("test-ok")
      .defaultTransitionId("proceed");
    activity = serialize(activity);
    assertEquals(ExclusiveGateway.class, activity.getClass());
    assertEquals("test-ok", activity.getId());
    assertEquals("proceed", activity.getDefaultTransitionId());
  }

  @Test
  public void testEmbeddedSubprocess() {
    Workflow workflow = new Workflow()
      .activity("phase1", new EmbeddedSubprocess()
        .name("phase one")
        .activity("start", new StartEvent())
        .activity("end", new EndEvent())
        .transition(new Transition().from("start").to("end")));
    
    workflow = serialize(workflow);
    
    EmbeddedSubprocess embeddedSubprocess = (EmbeddedSubprocess) workflow.getActivities().get(0);
    assertEquals("phase1", embeddedSubprocess.getId());
    assertEquals("phase one", embeddedSubprocess.getName());
    
    StartEvent startEvent = (StartEvent) embeddedSubprocess.getActivities().get(0);
    assertEquals("start", startEvent.getId());
    EndEvent endEvent = (EndEvent) embeddedSubprocess.getActivities().get(1);
    assertEquals("end", endEvent.getId());
    Transition transition = embeddedSubprocess.getTransitions().get(0);
    assertEquals("start", transition.getFrom());
    assertEquals("end", transition.getTo());
  }

}
