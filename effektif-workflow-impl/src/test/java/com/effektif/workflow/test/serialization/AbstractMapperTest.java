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

import org.junit.Test;

import com.effektif.workflow.api.activities.Call;
import com.effektif.workflow.api.activities.EmailTask;
import com.effektif.workflow.api.activities.EmbeddedSubprocess;
import com.effektif.workflow.api.activities.EndEvent;
import com.effektif.workflow.api.activities.ExclusiveGateway;
import com.effektif.workflow.api.activities.HttpServiceTask;
import com.effektif.workflow.api.activities.JavaServiceTask;
import com.effektif.workflow.api.activities.NoneTask;
import com.effektif.workflow.api.activities.ParallelGateway;
import com.effektif.workflow.api.activities.ReceiveTask;
import com.effektif.workflow.api.activities.ScriptTask;
import com.effektif.workflow.api.activities.StartEvent;
import com.effektif.workflow.api.activities.UserTask;
import com.effektif.workflow.api.condition.Condition;
import com.effektif.workflow.api.condition.IsTrue;
import com.effektif.workflow.api.form.Form;
import com.effektif.workflow.api.form.FormField;
import com.effektif.workflow.api.model.FileId;
import com.effektif.workflow.api.model.GroupId;
import com.effektif.workflow.api.model.RelativeTime;
import com.effektif.workflow.api.model.UserId;
import com.effektif.workflow.api.model.WorkflowId;
import com.effektif.workflow.api.triggers.FormTrigger;
import com.effektif.workflow.api.types.ChoiceType;
import com.effektif.workflow.api.types.EmailAddressType;
import com.effektif.workflow.api.types.EmailIdType;
import com.effektif.workflow.api.types.FileIdType;
import com.effektif.workflow.api.types.GroupIdType;
import com.effektif.workflow.api.types.LinkType;
import com.effektif.workflow.api.types.ListType;
import com.effektif.workflow.api.types.MoneyType;
import com.effektif.workflow.api.types.NumberType;
import com.effektif.workflow.api.types.TextType;
import com.effektif.workflow.api.types.UserIdType;
import com.effektif.workflow.api.workflow.Binding;
import com.effektif.workflow.api.workflow.Script;
import com.effektif.workflow.api.workflow.Transition;
import com.effektif.workflow.api.workflow.Variable;
import com.effektif.workflow.api.workflow.Workflow;
import com.effektif.workflow.impl.mapper.Mappings;
import com.effektif.workflow.impl.memory.TestConfiguration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;


/**
 * @author Tom Baeyens
 */
public abstract class AbstractMapperTest {
  
  protected static Mappings mappings = null; 
          
  public static void initializeMappings() {
    mappings = new TestConfiguration().get(Mappings.class);
    mappings.pretty();
  }

  // ID values for tests, overridden by valid Object IDs in MongoDB JSON tests.

  protected String fileId() {
    return "file-attachment";
  }

  protected String groupId() { return groupId(0); }
  protected String groupId(int index) {
    String[] ids = { "dev", "ops", "testing" };
    return ids[index];
  }

  protected String userId() { return userId(0); }
  protected String userId(int index) {
    String[] ids = { "john", "mary", "jack" };
    return ids[index];
  }

  protected String workflowId() {
    return "software-release";
  }

  @Test
  public void testCall() {
    Call activity = new Call()
      .id("runTests")
      .subWorkflowName("Run tests")
      .subWorkflowId(new WorkflowId(workflowId()));
    activity.setSubWorkflowSource("releaseTests");

    activity = serialize(activity);

    assertEquals(new WorkflowId(workflowId()), activity.getSubWorkflowId());
    assertEquals("releaseTests", activity.getSubWorkflowSource());
  }

  protected abstract <T> T serialize(T o);

  @Test
  public void testEmailTask() {
    EmailTask activity = new EmailTask()
      .id("sendEmail")
      .name("Announce release")
      .description("Announce the new software release.")
      .from("effektif@example.org")
      .to("releases@example.org")
      .toExpression("v1.email")
      .toUserId(userId(0))
      .toGroupId(groupId(0))
      .cc("Developers <dev@example.org>")
      .ccUserId(userId(1))
      .ccGroupId(groupId(1))
      .bcc("archive@example.org")
      .bccUserId(userId(2))
      .bccGroupId(groupId(2))
      .subject("New release")
      .bodyText("A new version has been deployed on production.")
      .bodyHtml("<b>A new version has been deployed on production.</b>")
      .attachment(new FileId(fileId()));

    activity = serialize(activity);

    assertEquals(EmailTask.class, activity.getClass());

    assertEquals("sendEmail", activity.getId());
    assertEquals("Announce release", activity.getName());
    assertEquals("Announce the new software release.", activity.getDescription());
    assertEquals("effektif@example.org", activity.getFromEmailAddress().getValue());
    assertEquals("releases@example.org", activity.getToEmailAddresses().get(0).getValue());
    assertEquals("v1.email", activity.getToEmailAddresses().get(1).getExpression());
    assertEquals(new UserId(userId(0)), activity.getToUserIds().get(0).getValue());
    assertEquals(new GroupId(groupId(0)), activity.getToGroupIds().get(0).getValue());

    assertEquals("Developers <dev@example.org>", activity.getCcEmailAddresses().get(0).getValue());
    assertEquals(new UserId(userId(1)), activity.getCcUserIds().get(0).getValue());
    assertEquals(new GroupId(groupId(1)), activity.getCcGroupIds().get(0).getValue());

    assertEquals("archive@example.org", activity.getBccEmailAddresses().get(0).getValue());
    assertEquals(new UserId(userId(2)), activity.getBccUserIds().get(0).getValue());
    assertEquals(new GroupId(groupId(2)), activity.getBccGroupIds().get(0).getValue());

    assertEquals("New release", activity.getSubject());
    assertEquals("A new version has been deployed on production.", activity.getBodyText());
    assertEquals("<b>A new version has been deployed on production.</b>", activity.getBodyHtml());

    assertEquals(new FileId(fileId()), activity.getAttachmentFileIds().get(0).getValue());
  }

  @Test
  public void testEmbeddedSubprocess() {
    EmbeddedSubprocess activity = new EmbeddedSubprocess();
    activity.setId("phase1");
    activity = serialize(activity);
    assertEquals(EmbeddedSubprocess.class, activity.getClass());
    assertEquals("phase1", activity.getId());
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
  public void testFormTrigger() {
    Workflow workflow = new Workflow()
      .variable(new Variable().id("version").name("Version number").type(new TextType()))
      .trigger(new FormTrigger().field("version"));

    workflow = serialize(workflow);

    assertNotNull(workflow.getTrigger());
    assertEquals(FormTrigger.class, workflow.getTrigger().getClass());
    FormTrigger trigger = (FormTrigger) workflow.getTrigger();
    assertNotNull(trigger.getForm());
    assertNotNull(trigger.getForm().getFields());
    assertEquals(1, trigger.getForm().getFields().size());
    assertEquals("version", trigger.getForm().getFields().get(0).getBinding().getExpression());
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
  public void testScriptTask() {
    ScriptTask activity = new ScriptTask()
      .id("postToTeamChat")
      .script(new Script().language("javascript")
      .script("console.log('TODO');")
      .mapping("Version", "version"));
    activity.name("Announce release in chat room")
      .description("Announce the release in the developer chat room.");

    activity = serialize(activity);

    assertEquals(ScriptTask.class, activity.getClass());
    assertEquals("postToTeamChat", activity.getId());
    assertEquals("Announce release in chat room", activity.getName());
    assertEquals("Announce the release in the developer chat room.", activity.getDescription());
    assertNotNull(activity.getScript());
    assertEquals("javascript", activity.getScript().getLanguage());
    assertEquals("console.log('TODO');", activity.getScript().getScript());
    assertEquals(1, activity.getScript().getMappings().size());
    assertEquals("version", activity.getScript().getMappings().get("Version"));
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

  /** this shows what properties to set when setting or updating a form in a workflow */
  @Test
  public void testFormInput() {
    Form form = new Form()
      .description("Form description")
      .field("v1")
      .field(new FormField().bindingExpression("v2").readOnly().required());
    form = serialize(form);
    assertEquals(Form.class, form.getClass());
    assertEquals("Form description", form.getDescription());
    assertEquals(2, form.getFields().size());
    assertEquals("v1", form.getFields().get(0).getBinding().getExpression());
    assertEquals("v2", form.getFields().get(1).getBinding().getExpression());
    assertTrue(form.getFields().get(1).isReadOnly());
    assertTrue(form.getFields().get(1).isRequired());
  }

  @Test
  public void testTransition() {
    Condition condition = new IsTrue().left(new Binding().expression("testsPassed"));

    Workflow workflow = new Workflow()
      .activity("start", new StartEvent())
      .activity("smokeTest", new UserTask())
      .activity("checkTestResult", new ExclusiveGateway().defaultTransitionId("to-failed"))
      .activity("passed", new EndEvent())
      .activity("failed", new EndEvent())
      .transition("to-smokeTest", new Transition().from("start").to("smokeTest").description("Starting the process"))
      .transition("to-checkTestResult", new Transition().from("smokeTest").to("checkTestResult"))
      .transition("to-passed", new Transition().from("checkTestResult").to("passed").condition(condition))
      .transition("to-failed", new Transition().from("checkTestResult").to("failed"));

    workflow = serialize(workflow);

    assertEquals(4, workflow.getTransitions().size());
    assertEquals("to-smokeTest", workflow.getTransitions().get(0).getId());
    assertEquals("Starting the process", workflow.getTransitions().get(0).getDescription());
    assertEquals("start", workflow.getTransitions().get(0).getFrom());
    assertEquals("smokeTest", workflow.getTransitions().get(0).getTo());

    assertEquals("to-passed", workflow.getTransitions().get(2).getId());
    IsTrue deserialisedCondition = (IsTrue) workflow.getTransitions().get(2).getCondition();
    assertEquals("testsPassed", deserialisedCondition.getLeft().getExpression());
  }

  @Test
  public void testUserTask() {
    Form form = new Form()
      .description("Test results & comments")
      .field(new FormField().id("f1").name("Test summary").bindingExpression("v1"));
    UserTask activity = new UserTask()
      .id("smokeTest")
      .name("Smoke test")
      .description("Quick check to make sure it isn’t obviously broken.")
      .taskName("Release version {{version}}")
      .assigneeId(userId())
      .candidateGroupId(groupId())
      .form(form)
      .duedate(RelativeTime.hours(1))
      .reminder(RelativeTime.hours(2))
      .reminderRepeat(RelativeTime.minutes(30))
      .escalate(RelativeTime.hours(4))
      .escalateTo(new Binding().value(new UserId(userId())));

    activity = serialize(activity);

    assertEquals(UserTask.class, activity.getClass());
    assertEquals("smokeTest", activity.getId());
    assertEquals("Smoke test", activity.getName());
    assertEquals("Quick check to make sure it isn’t obviously broken.", activity.getDescription());
    assertEquals("Release version {{version}}", activity.getTaskName());
    assertEquals(userId(), activity.getAssigneeId().getValue().getInternal());
    assertEquals(groupId(), activity.getCandidateGroupIds().get(0).getValue().getInternal());
    assertEquals(RelativeTime.hours(1), activity.getDuedate());
    assertEquals(RelativeTime.hours(2), activity.getReminder());
    assertEquals(RelativeTime.minutes(30), activity.getReminderRepeat());
    assertEquals(RelativeTime.hours(4), activity.getEscalate());
    assertEquals(new UserId(userId()), activity.getEscalateToId().getValue());

    assertEquals(Form.class, activity.getForm().getClass());
    assertEquals("Test results & comments", activity.getForm().getDescription());

    assertEquals(1, activity.getForm().getFields().size());
    assertEquals("f1", activity.getForm().getFields().get(0).getId());
    assertEquals("Test summary", activity.getForm().getFields().get(0).getName());
    assertEquals("v1", activity.getForm().getFields().get(0).getBinding().getExpression());
  }

  @Test
  public void testWorkflow() {
    Workflow workflow = new Workflow()
      .id(new WorkflowId(workflowId()))
      .name("Software release")
      .description("Regular software production release process.")
      .sourceWorkflowId(workflowId())
      .variable("v", TextType.INSTANCE)
      .activity("s", new StartEvent())
      .activity("task", new UserTask())
      .transition(new Transition().from("s").to("task"));

    workflow = serialize(workflow);

    assertEquals(workflowId(), workflow.getId().getInternal());
    assertEquals("Software release", workflow.getName());
    assertEquals("Regular software production release process.", workflow.getDescription());
    assertEquals(workflowId(), workflow.getSourceWorkflowId());
    assertEquals(StartEvent.class, workflow.getActivities().get(0).getClass());
    assertEquals("s", workflow.getActivities().get(0).getId());
  }

  @Test
  public void testVariables() {
    Workflow workflow = new Workflow()
      .variable(new Variable().type(TextType.INSTANCE).id("v").name("version").description("Release version"))
      .variable("mailing-list", EmailAddressType.INSTANCE)
      .variable("announcement", EmailIdType.INSTANCE)
      .variable("source-bundle", FileIdType.INSTANCE)
      .variable("release-notes", LinkType.INSTANCE)
      .variable("team-bonus", MoneyType.INSTANCE)
      .variable("bugs-fixed", NumberType.INSTANCE)
      .variable("dev-team", GroupIdType.INSTANCE)
      .variable("product-owner", UserIdType.INSTANCE)
      .variable("distribution", new ChoiceType().option("Internal").option("External"))
      .variable("stakeholders", new ListType().elementType(UserIdType.INSTANCE));

    workflow = serialize(workflow);

    assertNotNull(workflow.getVariables());
    assertEquals(11, workflow.getVariables().size());

    // Basic properties
    assertEquals("v", workflow.getVariables().get(0).getId());
    assertEquals("version", workflow.getVariables().get(0).getName());
    assertEquals("Release version", workflow.getVariables().get(0).getDescription());
    assertEquals(TextType.class, workflow.getVariables().get(0).getType().getClass());

    // Other static types
    assertEquals(EmailAddressType.class, workflow.getVariables().get(1).getType().getClass());
    assertEquals(EmailIdType.class, workflow.getVariables().get(2).getType().getClass());
    assertEquals(FileIdType.class, workflow.getVariables().get(3).getType().getClass());
    assertEquals(LinkType.class, workflow.getVariables().get(4).getType().getClass());
    assertEquals(MoneyType.class, workflow.getVariables().get(5).getType().getClass());
    assertEquals(NumberType.class, workflow.getVariables().get(6).getType().getClass());
    assertEquals(GroupIdType.class, workflow.getVariables().get(7).getType().getClass());
    assertEquals(UserIdType.class, workflow.getVariables().get(8).getType().getClass());

    // Complex types
    assertEquals(ChoiceType.class, workflow.getVariables().get(9).getType().getClass());
    ChoiceType choiceType = (ChoiceType) workflow.getVariables().get(9).getType();
    assertEquals(2, choiceType.getOptions().size());
    assertEquals("Internal", choiceType.getOptions().get(0).getId());
    assertEquals("External", choiceType.getOptions().get(1).getId());

    assertEquals(ListType.class, workflow.getVariables().get(10).getType().getClass());
    ListType listType = (ListType) workflow.getVariables().get(10).getType();
    assertEquals(UserIdType.class, listType.getElementType().getClass());
  }
}
