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
import com.effektif.workflow.api.condition.Condition;
import com.effektif.workflow.api.condition.IsTrue;
import com.effektif.workflow.api.deprecated.activities.UserTask;
import com.effektif.workflow.api.deprecated.types.EmailIdType;
import com.effektif.workflow.api.deprecated.types.FileIdType;
import com.effektif.workflow.api.deprecated.types.GroupIdType;
import com.effektif.workflow.api.deprecated.types.UserIdType;
import com.effektif.workflow.api.model.WorkflowId;
import com.effektif.workflow.api.types.ChoiceType;
import com.effektif.workflow.api.types.EmailAddressType;
import com.effektif.workflow.api.types.LinkType;
import com.effektif.workflow.api.types.ListType;
import com.effektif.workflow.api.types.MoneyType;
import com.effektif.workflow.api.types.NumberType;
import com.effektif.workflow.api.types.TextType;
import com.effektif.workflow.api.workflow.Binding;
import com.effektif.workflow.api.workflow.Transition;
import com.effektif.workflow.api.workflow.Variable;
import com.effektif.workflow.api.workflow.Workflow;
import com.effektif.workflow.impl.json.JsonStreamMapper;
import com.effektif.workflow.impl.util.Lists;


/**
 * Tests workflow serialisation to JSON, by serialising and deserialising workflow objects.
 *
 * @author Tom Baeyens
 */
public class WorkflowStreamTest {
  
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
      .createTime(now)
      .description("d")
      .name("w")
      .property("str", "s")
      .property("lis", Lists.of("a", 1, true))
      .property("num", Long.MAX_VALUE)
      .property("dou", Double.MAX_VALUE)
      .property("boo", true);
    
    workflow = serialize(workflow);
    
    assertNotNull(workflow);
    assertEquals(workflowIdInternal, workflow.getId().getInternal());
    assertEquals("w", workflow.getName());
//    assertEquals(p.get("str"), workflow.getProperty("str"));
//    assertEquals(p.get("lis"), workflow.getProperty("lis"));
//    assertEquals(p.get("num"), workflow.getProperty("num"));
//    assertEquals(p.get("dou"), workflow.getProperty("dou"));
//    assertEquals(p.get("boo"), workflow.getProperty("boo"));
    assertEquals(now, workflow.getCreateTime());
  }

// TODO merge with the above test
//  @Test
//  public void testWorkflow() {
//    Workflow workflow = new Workflow()
//      .id(new WorkflowId(workflowId()))
//      .name("Software release")
//      .description("Regular software production release process.")
//      .sourceWorkflowId(workflowId())
//      .variable("v", TextType.INSTANCE)
//      .activity("s", new StartEvent())
//      .activity("task", new UserTask())
//      .transition(new Transition().from("s").to("task"));
//
//    workflow = serialize(workflow);
//
//    assertEquals(workflowId(), workflow.getId().getInternal());
//    assertEquals("Software release", workflow.getName());
//    assertEquals("Regular software production release process.", workflow.getDescription());
//    assertEquals(workflowId(), workflow.getSourceWorkflowId());
//    assertEquals(StartEvent.class, workflow.getActivities().get(0).getClass());
//    assertEquals("s", workflow.getActivities().get(0).getId());
//  }


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
 
//  @Test
//  public void testTransition() {
//    Condition condition = new IsTrue().left(new Binding().expression("testsPassed"));
//
//    Workflow workflow = new Workflow()
//      .activity("start", new StartEvent())
//      .activity("smokeTest", new UserTask())
//      .activity("checkTestResult", new ExclusiveGateway().defaultTransitionId("to-failed"))
//      .activity("passed", new EndEvent())
//      .activity("failed", new EndEvent())
//      .transition("to-smokeTest", new Transition().from("start").to("smokeTest").description("Starting the process"))
//      .transition("to-checkTestResult", new Transition().from("smokeTest").to("checkTestResult"))
//      .transition("to-passed", new Transition().from("checkTestResult").to("passed").condition(condition))
//      .transition("to-failed", new Transition().from("checkTestResult").to("failed"));
//
//    workflow = serialize(workflow);
//
//    assertEquals(4, workflow.getTransitions().size());
//    assertEquals("to-smokeTest", workflow.getTransitions().get(0).getId());
//    assertEquals("Starting the process", workflow.getTransitions().get(0).getDescription());
//    assertEquals("start", workflow.getTransitions().get(0).getFrom());
//    assertEquals("smokeTest", workflow.getTransitions().get(0).getTo());
//
//    assertEquals("to-passed", workflow.getTransitions().get(2).getId());
//    IsTrue deserialisedCondition = (IsTrue) workflow.getTransitions().get(2).getCondition();
//    assertEquals("testsPassed", deserialisedCondition.getLeft().getExpression());
//  }

//  @Test
//  public void testVariables() {
//    Workflow workflow = new Workflow()
//      .variable(new Variable().type(TextType.INSTANCE).id("v").name("version").description("Release version"))
//      .variable("mailing-list", EmailAddressType.INSTANCE)
//      .variable("announcement", EmailIdType.INSTANCE)
//      .variable("source-bundle", FileIdType.INSTANCE)
//      .variable("release-notes", LinkType.INSTANCE)
//      .variable("team-bonus", MoneyType.INSTANCE)
//      .variable("bugs-fixed", NumberType.INSTANCE)
//      .variable("dev-team", GroupIdType.INSTANCE)
//      .variable("product-owner", UserIdType.INSTANCE)
//      .variable("distribution", new ChoiceType().option("Internal").option("External"))
//      .variable("stakeholders", new ListType().elementType(UserIdType.INSTANCE));
//
//    workflow = serialize(workflow);
//
//    assertNotNull(workflow.getVariables());
//    assertEquals(11, workflow.getVariables().size());
//
//    // Basic properties
//    assertEquals("v", workflow.getVariables().get(0).getId());
//    assertEquals("version", workflow.getVariables().get(0).getName());
//    assertEquals("Release version", workflow.getVariables().get(0).getDescription());
//    assertEquals(TextType.class, workflow.getVariables().get(0).getType().getClass());
//
//    // Other static types
//    assertEquals(EmailAddressType.class, workflow.getVariables().get(1).getType().getClass());
//    assertEquals(EmailIdType.class, workflow.getVariables().get(2).getType().getClass());
//    assertEquals(FileIdType.class, workflow.getVariables().get(3).getType().getClass());
//    assertEquals(LinkType.class, workflow.getVariables().get(4).getType().getClass());
//    assertEquals(MoneyType.class, workflow.getVariables().get(5).getType().getClass());
//    assertEquals(NumberType.class, workflow.getVariables().get(6).getType().getClass());
//    assertEquals(GroupIdType.class, workflow.getVariables().get(7).getType().getClass());
//    assertEquals(UserIdType.class, workflow.getVariables().get(8).getType().getClass());
//
//    // Complex types
//    assertEquals(ChoiceType.class, workflow.getVariables().get(9).getType().getClass());
//    ChoiceType choiceType = (ChoiceType) workflow.getVariables().get(9).getType();
//    assertEquals(2, choiceType.getOptions().size());
//    assertEquals("Internal", choiceType.getOptions().get(0).getId());
//    assertEquals("External", choiceType.getOptions().get(1).getId());
//
//    assertEquals(ListType.class, workflow.getVariables().get(10).getType().getClass());
//    ListType listType = (ListType) workflow.getVariables().get(10).getType();
//    assertEquals(UserIdType.class, listType.getElementType().getClass());
//  }

}
