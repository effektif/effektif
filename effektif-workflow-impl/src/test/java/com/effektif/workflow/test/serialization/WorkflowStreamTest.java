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
import com.effektif.workflow.api.types.BooleanType;
import com.effektif.workflow.api.types.ChoiceType;
import com.effektif.workflow.api.types.DateType;
import com.effektif.workflow.api.types.EmailAddressType;
import com.effektif.workflow.api.types.JavaBeanType;
import com.effektif.workflow.api.types.LinkType;
import com.effektif.workflow.api.types.ListType;
import com.effektif.workflow.api.types.MoneyType;
import com.effektif.workflow.api.types.NumberType;
import com.effektif.workflow.api.types.TextType;
import com.effektif.workflow.api.workflow.Activity;
import com.effektif.workflow.api.workflow.Binding;
import com.effektif.workflow.api.workflow.ExecutableWorkflow;
import com.effektif.workflow.api.workflow.MultiInstance;
import com.effektif.workflow.api.workflow.Transition;
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
  public void testActivity() {
    Activity activity = new NoneTask()
      .id("verifyRequirements")
      .defaultTransitionId("continue")
      .multiInstance(new MultiInstance()
        .valuesExpression("reviewers")
        .variable("reviewer", TextType.INSTANCE));
    activity = serialize(activity);
    assertEquals("verifyRequirements", activity.getId());
    assertEquals("continue", activity.getDefaultTransitionId());
    assertNotNull(activity.getMultiInstance());
    assertEquals("reviewer", activity.getMultiInstance().getVariable().getId());
    assertEquals("reviewers", activity.getMultiInstance().getValues().get(0).getExpression());
  }

  @Test
  public void testCall() {
    LocalDateTime now = new LocalDateTime();
    ExecutableWorkflow workflow = new ExecutableWorkflow()
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
  public void testExecutableWorkflow() {
    LocalDateTime now = new LocalDateTime();

    Map<String,Object> p = new HashMap<>();
    p.put("str", "s");
    p.put("lis", Lists.of("a", 1, true));
    p.put("num", Long.MAX_VALUE);
    p.put("dou", Double.MAX_VALUE);
    p.put("boo", true);

    String workflowIdInternal = getWorkflowIdInternal();

    ExecutableWorkflow workflow = new ExecutableWorkflow()
      .id(new WorkflowId(workflowIdInternal))
      .name("Software release")
      .description("Regular software production release process.")
      .createTime(now)
      .creatorId("iamdevloper")
      .sourceWorkflowId("source")
      .variable("v", TextType.INSTANCE)
      .activity("start", new StartEvent())
      .activity("end", new EndEvent())
      .transition(new Transition().fromId("start").toId("end"))
      .property("str", "s")
      .property("lis", Lists.of("a", 1, true))
      .property("num", Long.MAX_VALUE)
      .property("dou", Double.MAX_VALUE)
      .property("boo", true);
    workflow.setEnableCases(true);

    workflow = serialize(workflow);

    assertNotNull(workflow);
    assertEquals(workflowIdInternal, workflow.getId().getInternal());
    assertEquals("Software release", workflow.getName());
    assertEquals("Regular software production release process.", workflow.getDescription());
    assertEquals(now, workflow.getCreateTime());
    assertEquals("iamdevloper", workflow.getCreatorId());
    assertEquals("source", workflow.getSourceWorkflowId());
    assertEquals("start", ((StartEvent)workflow.getActivities().get(0)).getId());
    assertEquals("end", ((EndEvent)workflow.getActivities().get(1)).getId());
    assertEquals("start", workflow.getTransitions().get(0).getFromId());
    assertEquals("end", workflow.getTransitions().get(0).getToId());

    // Not tested, pending implementation.
    //    assertEquals(p.get("str"), workflow.getProperty("str"));
    //    assertEquals(p.get("lis"), workflow.getProperty("lis"));
    //    assertEquals(p.get("num"), workflow.getProperty("num"));
    //    assertEquals(p.get("dou"), workflow.getProperty("dou"));
    //    assertEquals(p.get("boo"), workflow.getProperty("boo"));

    assertTrue(workflow.isEnableCases());
  }

  @Test
  public void testEmbeddedSubprocess() {
    ExecutableWorkflow workflow = new ExecutableWorkflow()
      .activity("phase1",
        new EmbeddedSubprocess().name("phase one").activity("start", new StartEvent()).activity("end", new EndEvent())
          .transition(new Transition().fromId("start").toId("end")));
    
    workflow = serialize(workflow);
    
    EmbeddedSubprocess embeddedSubprocess = (EmbeddedSubprocess) workflow.getActivities().get(0);
    assertEquals("phase1", embeddedSubprocess.getId());
    assertEquals("phase one", embeddedSubprocess.getName());
    
    StartEvent startEvent = (StartEvent) embeddedSubprocess.getActivities().get(0);
    assertEquals("start", startEvent.getId());
    EndEvent endEvent = (EndEvent) embeddedSubprocess.getActivities().get(1);
    assertEquals("end", endEvent.getId());
    Transition transition = embeddedSubprocess.getTransitions().get(0);
    assertEquals("start", transition.getFromId());
    assertEquals("end", transition.getToId());
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
  public void testInOutParameters() {
    ExecutableWorkflow workflow = new ExecutableWorkflow()
      .activity("a", new NoneTask()
        .inputValue("in1", "value1")
        .inputExpression("in2", "expression2")
        .inputListBinding("in3", new Binding<Object>().value("listValue1"))
        .inputListBinding("in3", new Binding<Object>().expression("listExpression2"))
        .output("out1", "var1"));
    workflow = serialize(workflow);
    
    Activity activity = workflow.getActivities().get(0);
    assertEquals("value1", activity.getInputs().get("in1").getBinding().getValue());
    assertEquals("expression2", activity.getInputs().get("in2").getBinding().getExpression());
    assertEquals("listValue1", activity.getInputs().get("in3").getBindings().get(0).getValue());
    assertEquals("listExpression2", activity.getInputs().get("in3").getBindings().get(1).getExpression());
    assertEquals("var1", activity.getOutputs().get("out1").getVariableId());
  }

  @Test
  public void testVariables() {
    ExecutableWorkflow workflow = new ExecutableWorkflow()
      .variable("variable01", BooleanType.INSTANCE)
      .variable("variable02", new ChoiceType().option("Red pill").option("Blue pill"))
      .variable("variable03", new DateType().date())
      .variable("variable04", EmailAddressType.INSTANCE)
      .variable("variable05", new JavaBeanType(Integer.class))
      .variable("variable06", LinkType.INSTANCE)
      .variable("variable07", new ListType(NumberType.INSTANCE))
      .variable("variable08", MoneyType.INSTANCE)
      .variable("variable09", NumberType.INSTANCE)
      .variable("variable10", new TextType().multiLine());

    workflow = serialize(workflow);

    assertNotNull(workflow.getVariables());
    assertEquals(10, workflow.getVariables().size());

    assertEquals(BooleanType.class, workflow.getVariables().get(0).getType().getClass());

    assertEquals(ChoiceType.class, workflow.getVariables().get(1).getType().getClass());
    assertEquals("Red pill", ((ChoiceType) workflow.getVariables().get(1).getType()).getOptions().get(0).getId());

    assertEquals(DateType.class, workflow.getVariables().get(2).getType().getClass());
    assertEquals("date", ((DateType) workflow.getVariables().get(2).getType()).getKind());

    assertEquals(EmailAddressType.class, workflow.getVariables().get(3).getType().getClass());

    assertEquals(JavaBeanType.class, workflow.getVariables().get(4).getType().getClass());
    assertEquals(Integer.class, ((JavaBeanType) workflow.getVariables().get(4).getType()).getJavaClass());

    assertEquals(LinkType.class, workflow.getVariables().get(5).getType().getClass());

    assertEquals(ListType.class, workflow.getVariables().get(6).getType().getClass());
    assertEquals(NumberType.class, ((ListType) workflow.getVariables().get(6).getType()).getElementType().getClass());

    assertEquals(MoneyType.class, workflow.getVariables().get(7).getType().getClass());
    assertEquals(NumberType.class, workflow.getVariables().get(8).getType().getClass());

    assertEquals(TextType.class, workflow.getVariables().get(9).getType().getClass());
    assertTrue(((TextType) workflow.getVariables().get(9).getType()).isMultiLine());

  }
}
