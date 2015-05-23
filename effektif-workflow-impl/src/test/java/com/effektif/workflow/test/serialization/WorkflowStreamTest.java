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
import com.effektif.workflow.api.activities.HttpServiceTask;
import com.effektif.workflow.api.activities.JavaServiceTask;
import com.effektif.workflow.api.activities.NoneTask;
import com.effektif.workflow.api.activities.ParallelGateway;
import com.effektif.workflow.api.activities.ReceiveTask;
import com.effektif.workflow.api.activities.StartEvent;
import com.effektif.workflow.api.model.WorkflowId;
import com.effektif.workflow.api.types.TextType;
import com.effektif.workflow.api.workflow.Transition;
import com.effektif.workflow.api.workflow.Workflow;
import com.effektif.workflow.impl.json.JsonStreamMapper;
import com.effektif.workflow.impl.util.Lists;


/**
 * Tests workflow serialisation to JSON, by serialising and deserialising workflow objects.
 *
 * @author Tom Baeyens
 */
public class WorkflowStreamTest {

  protected static JsonStreamMapper jsonStreamMapper = null;

  @BeforeClass
  public static void initialize() {
    if (jsonStreamMapper==null) {
      jsonStreamMapper = new JsonStreamMapper();
      jsonStreamMapper.pretty();
    }
  }

  public static JsonStreamMapper getJsonStreamMapper() {
    initialize();
    return jsonStreamMapper; 
  }

  public <T> T serialize(T o) {
    String jsonString = jsonStreamMapper.write(o);
    System.out.println(jsonString);
    return (T) jsonStreamMapper.readString(jsonString, o.getClass());
  }
  
  protected String getWorkflowIdInternal() {
    return "wid";
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
    
    String workflowIdInternal = getWorkflowIdInternal();
    
    Workflow workflow = new Workflow()
      .id(new WorkflowId(workflowIdInternal))
      .name("Software release")
      .description("Regular software production release process.")
      .createTime(now)
      .sourceWorkflowId("source")
      .variable("v", TextType.INSTANCE)
      .activity("start", new StartEvent())
      .activity("end", new EndEvent())
      .transition(new Transition().from("start").to("end"))
      .property("str", "s")
      .property("lis", Lists.of("a", 1, true))
      .property("num", Long.MAX_VALUE)
      .property("dou", Double.MAX_VALUE)
      .property("boo", true);
    
    workflow = serialize(workflow);
    
    assertNotNull(workflow);
    assertEquals(workflowIdInternal, workflow.getId().getInternal());
    assertEquals("Software release", workflow.getName());
    assertEquals("Regular software production release process.", workflow.getDescription());
    assertEquals("source", workflow.getSourceWorkflowId());
    assertEquals("start", ((StartEvent)workflow.getActivities().get(0)).getId());
    assertEquals("end", ((EndEvent)workflow.getActivities().get(1)).getId());
    assertEquals("start", workflow.getTransitions().get(0).getFrom());
    assertEquals("end", workflow.getTransitions().get(0).getTo());

    // Not tested, pending implementation.
    //assertEquals(p.get("str"), workflow.getProperty("str"));
    //assertEquals(p.get("lis"), workflow.getProperty("lis"));
    //assertEquals(p.get("num"), workflow.getProperty("num"));
    //assertEquals(p.get("dou"), workflow.getProperty("dou"));
    //assertEquals(p.get("boo"), workflow.getProperty("boo"));

    assertEquals(now, workflow.getCreateTime());
  }

  @Test 
  public void testCall() {
    LocalDateTime now = new LocalDateTime();
    Workflow workflow = new Workflow()
      .activity(new Call()
      .id("runTests")
      .inputValue("d", now)
      .inputValue("s", "string")
      .inputExpression("v", "version")
      .subWorkflowSource("Run tests")
      .subWorkflowId(new WorkflowId(getWorkflowIdInternal())));

    workflow = serialize(workflow);
    
    assertNotNull(workflow);
    Call call = (Call) workflow.getActivities().get(0);
    assertEquals(new WorkflowId(getWorkflowIdInternal()), call.getSubWorkflowId());
    assertEquals("Run tests", call.getSubWorkflowSource());
    assertEquals(now, call.getInputBindings().get("d").getValue());
    assertEquals("string", call.getInputBindings().get("s").getValue());
    assertEquals("version", call.getInputBindings().get("v").getExpression());
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
      .activity("phase1",
        new EmbeddedSubprocess().name("phase one").activity("start", new StartEvent()).activity("end", new EndEvent())
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

  @Test
  public void testHttpServiceTask() {
    HttpServiceTask activity = new HttpServiceTask();
    activity.setId("publishReleaseNotes");
    activity = serialize(activity);
    assertEquals(HttpServiceTask.class, activity.getClass());
    assertEquals("publishReleaseNotes", activity.getId());
  }

  @Test
  public void testJavaServiceTask() {
    JavaServiceTask activity = new JavaServiceTask();
    activity.setId("profilePerformance");
    activity = serialize(activity);
    assertEquals(JavaServiceTask.class, activity.getClass());
    assertEquals("profilePerformance", activity.getId());
  }

  @Test
  public void testNoneTask() {
    NoneTask activity = new NoneTask();
    activity.setId("verifyRequirements");
    activity = serialize(activity);
    assertEquals(NoneTask.class, activity.getClass());
    assertEquals("verifyRequirements", activity.getId());
  }

  @Test
  public void testParallelGateway() {
    ParallelGateway activity = new ParallelGateway();
    activity.setId("fork");
    activity = serialize(activity);
    assertEquals(ParallelGateway.class, activity.getClass());
    assertEquals("fork", activity.getId());
  }

  @Test
  public void testReceiveTask() {
    ReceiveTask activity = new ReceiveTask();
    activity.setId("buildComplete");
    activity = serialize(activity);
    assertEquals(ReceiveTask.class, activity.getClass());
    assertEquals("buildComplete", activity.getId());
  }

  @Test
  public void testStartEvent() {
    StartEvent activity = new StartEvent();
    activity.setId("codeComplete");
    activity.setName("code complete");
    activity.setDescription("Starts the process when the code is ready to release.");
    activity = serialize(activity);
    assertEquals(StartEvent.class, activity.getClass());
    assertEquals("codeComplete", activity.getId());
    assertEquals("code complete", activity.getName());
    assertEquals("Starts the process when the code is ready to release.", activity.getDescription());
  }
}
