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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.effektif.workflow.api.activities.EmbeddedSubprocess;
import com.effektif.workflow.api.activities.EndEvent;
import com.effektif.workflow.api.activities.ExclusiveGateway;
import com.effektif.workflow.api.activities.HttpServiceTask;
import com.effektif.workflow.api.activities.JavaServiceTask;
import com.effektif.workflow.api.activities.NoneTask;
import com.effektif.workflow.api.activities.ParallelGateway;
import com.effektif.workflow.api.activities.ReceiveTask;
import com.effektif.workflow.api.activities.ScriptTask;
import com.effektif.workflow.api.activities.UserTask;
import com.effektif.workflow.api.form.Form;
import com.effektif.workflow.api.form.FormField;
import com.effektif.workflow.api.model.RelativeTime;
import com.effektif.workflow.api.workflow.Binding;
import com.effektif.workflow.api.workflow.Script;
import org.junit.Test;

import com.effektif.workflow.api.activities.Call;
import com.effektif.workflow.api.activities.EmailTask;
import com.effektif.workflow.api.activities.StartEvent;
import com.effektif.workflow.api.mapper.JsonReadable;
import com.effektif.workflow.api.model.FileId;
import com.effektif.workflow.api.model.GroupId;
import com.effektif.workflow.api.model.UserId;
import com.effektif.workflow.api.model.WorkflowId;
import com.effektif.workflow.api.types.BooleanType;
import com.effektif.workflow.api.types.ChoiceType;
import com.effektif.workflow.api.types.DateType;
import com.effektif.workflow.api.types.EmailIdType;
import com.effektif.workflow.api.types.FileIdType;
import com.effektif.workflow.api.types.GroupIdType;
import com.effektif.workflow.api.types.JavaBeanType;
import com.effektif.workflow.api.types.ListType;
import com.effektif.workflow.api.types.MoneyType;
import com.effektif.workflow.api.types.NumberType;
import com.effektif.workflow.api.types.TextType;
import com.effektif.workflow.api.types.UserIdType;
import com.effektif.workflow.api.workflow.Workflow;
import com.effektif.workflow.impl.mapper.Mappings;


/**
 * @author Tom Baeyens
 */
public abstract class AbstractMapperTest {
  
  protected static Mappings mappings = null; 
          
  public static void initializeMappings() {
    mappings = new Mappings();
    
    mappings.registerSubClass(StartEvent.class);
    mappings.registerSubClass(Call.class);
    mappings.registerSubClass(EmailTask.class);

    mappings.registerSubClass(BooleanType.class);
    mappings.registerSubClass(ChoiceType.class);
    mappings.registerSubClass(DateType.class);
    mappings.registerSubClass(EmailIdType.class);
    mappings.registerSubClass(FileIdType.class);
    mappings.registerSubClass(GroupIdType.class);
    mappings.registerSubClass(JavaBeanType.class);
    mappings.registerSubClass(ListType.class);
    mappings.registerSubClass(MoneyType.class);
    mappings.registerSubClass(NumberType.class);
    mappings.registerSubClass(TextType.class);
    mappings.registerSubClass(UserIdType.class);
    
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
    String[] ids = { "alice", "ben", "charlie" };
    return ids[index];
  }

  protected String workflowId() {
    return "software-release";
  }

  @Test
  public void testWorkflowJson() {
    Workflow workflow = new Workflow()
      .id(new WorkflowId(workflowId()))
      .variable("v", TextType.INSTANCE)
      .activity(new StartEvent()
        .id("s")
      );
    
    workflow = serialize(workflow);
    
    assertEquals(workflowId(), workflow.getId().getInternal());
    assertEquals(StartEvent.class, workflow.getActivities().get(0).getClass());
    assertEquals("s", workflow.getActivities().get(0).getId());

// variables not yet supported by bpmn
//    assertEquals("v", workflow.getVariables().get(0).getId());
//    assertEquals(TextType.class, workflow.getVariables().get(0).getType().getClass());
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
    activity = serialize(activity);
    assertEquals(EndEvent.class, activity.getClass());
    assertEquals("releaseComplete", activity.getId());
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
      .script(new Script()
        .language("javascript")
        .script("console.log('TODO');")
        .mapping("Version", "version"));
    activity = serialize(activity);
    assertEquals(ScriptTask.class, activity.getClass());
    assertEquals("postToTeamChat", activity.getId());
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
    activity = serialize(activity);
    assertEquals(StartEvent.class, activity.getClass());
    assertEquals("codeComplete", activity.getId());
  }

  /** this shows what properties to set when setting or updating a form in a workflow */
  @Test
  public void testFormInput() {
    Form form = new Form()
      .description("Form description")
      .field("v1")
      .field(new FormField()
        .bindingExpression("v2")
        .readOnly()
        .required());
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
  public void testUserTask() {
    Form form = new Form()
      .description("Test results & comments")
      .field(new FormField()
        .id("f1")
        .name("Test summary")
        .bindingExpression("v1"));
    UserTask activity = new UserTask()
      .id("smokeTest")
      .name("Smoke test")
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
}
