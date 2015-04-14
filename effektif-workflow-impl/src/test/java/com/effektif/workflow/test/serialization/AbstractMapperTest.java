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

import com.effektif.workflow.api.activities.UserTask;
import com.effektif.workflow.api.form.Form;
import com.effektif.workflow.api.form.FormField;
import com.effektif.workflow.api.model.RelativeTime;
import com.effektif.workflow.api.workflow.Binding;
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

  // valid object ids
  //  
  //  552ce4fdc2e610a6a3dedb84
  //  552ce4fdc2e610a6a3dedb85
  //  552ce4fdc2e610a6a3dedb86
  //  552ce4fdc2e610a6a3dedb87
  //  552ce4fdc2e610a6a3dedb88
  //  552ce4fdc2e610a6a3dedb89
  //  552ce4fdc2e610a6a3dedb8a
  //  552ce4fdc2e610a6a3dedb8b
  
  @Test
  public void testWorkflowJson() {
    Workflow workflow = new Workflow()
      .id(new WorkflowId("552ce4fdc2e610a6a3dedb78"))
      .variable("v", TextType.INSTANCE)
      .activity(new StartEvent()
        .id("s")
      );
    
    workflow = serialize(workflow);
    
    assertEquals("552ce4fdc2e610a6a3dedb78", workflow.getId().getInternal());
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
      .subWorkflowId(new WorkflowId("551d4f5803649532d21f223f"));
    activity.setSubWorkflowSource("releaseTests");
    
    activity = serialize(activity);
    
    assertEquals(new WorkflowId("551d4f5803649532d21f223f"), activity.getSubWorkflowId());
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
      .toUserId("552ce4fdc2e610a6a3dedb7b")
      .toGroupId("552ce4fdc2e610a6a3dedb7e")
      .cc("dev@example.org")
      .ccUserId("552ce4fdc2e610a6a3dedb7c")
      .ccGroupId("552ce4fdc2e610a6a3dedb7f")
      .bcc("archive@example.org")
      .bccUserId("552ce4fdc2e610a6a3dedb7d")
      .bccGroupId("552ce4fdc2e610a6a3dedb80")
      .subject("New release")
      .bodyText("A new version has been deployed on production.")
      .bodyHtml("<b>A new version has been deployed on production.</b>")
      .attachment(new FileId("552ce4fdc2e610a6a3dedb82"));
    
    activity = serialize(activity);
    
    assertEquals(EmailTask.class, activity.getClass());
    
    assertEquals("sendEmail", activity.getId());
    assertEquals("effektif@example.org", activity.getFromEmailAddress().getValue());
    assertEquals("releases@example.org", activity.getToEmailAddresses().get(0).getValue());
    assertEquals("v1.email", activity.getToEmailAddresses().get(1).getExpression());
    assertEquals(new UserId("552ce4fdc2e610a6a3dedb7b"), activity.getToUserIds().get(0).getValue());
    assertEquals(new GroupId("552ce4fdc2e610a6a3dedb7e"), activity.getToGroupIds().get(0).getValue());

    assertEquals("dev@example.org", activity.getCcEmailAddresses().get(0).getValue());
    assertEquals(new UserId("552ce4fdc2e610a6a3dedb7c"), activity.getCcUserIds().get(0).getValue());
    assertEquals(new GroupId("552ce4fdc2e610a6a3dedb7f"), activity.getCcGroupIds().get(0).getValue());

    assertEquals("archive@example.org", activity.getBccEmailAddresses().get(0).getValue());
    assertEquals(new UserId("552ce4fdc2e610a6a3dedb7d"), activity.getBccUserIds().get(0).getValue());
    assertEquals(new GroupId("552ce4fdc2e610a6a3dedb80"), activity.getBccGroupIds().get(0).getValue());

    assertEquals("New release", activity.getSubject());
    assertEquals("A new version has been deployed on production.", activity.getBodyText());
    assertEquals("<b>A new version has been deployed on production.</b>", activity.getBodyHtml());

    assertEquals(new FileId("552ce4fdc2e610a6a3dedb82"), activity.getAttachmentFileIds().get(0).getValue());
  }

//  @Test
//  public void testEmbeddedSubprocess() {
//    EmbeddedSubprocess activity = new EmbeddedSubprocess();
//    activity.setId("phase1");
//    print(activity);
//  }
//
//  @Test
//  public void testEndEvent() {
//    EndEvent activity = new EndEvent();
//    activity.setId("releaseComplete");
//    print(activity);
//  }
//
//  @Test
//  public void testExclusiveGateway() {
//    ExclusiveGateway activity = (ExclusiveGateway) new ExclusiveGateway()
//      .id("ok?")
//      .defaultTransitionId("proceed");
//    print(activity);
//  }
//
//  @Test
//  public void testHttpServiceTask() {
//    HttpServiceTask activity = new HttpServiceTask();
//    activity.setId("publishReleaseNotes");
//    print(activity);
//  }
//
//  @Test
//  public void testJavaServiceTask() {
//    JavaServiceTask activity = new JavaServiceTask();
//    activity.setId("profilePerformance");
//    print(activity);
//  }
//
//  @Test
//  public void testNoneTask() {
//    NoneTask activity = new NoneTask();
//    activity.setId("verifyRequirements");
//    print(activity);
//  }
//
//  @Test
//  public void testParallelGateway() {
//    ParallelGateway activity = new ParallelGateway();
//    activity.setId("fork");
//    print(activity);
//  }
//
//  @Test
//  public void testReceiveTask() {
//    ReceiveTask activity = new ReceiveTask();
//    activity.setId("buildComplete");
//    print(activity);
//  }
//
//  @Test
//  public void testScriptTask() {
//    ScriptTask activity = new ScriptTask()
//      .id("postToTeamChat")
//      .script(new Script()
//        .language("javascript")
//        .script("console.log('TODO');")
//        .mapping("Version", "version"));
//    print(activity);
//  }
//
//  @Test
//  public void testStartEvent() {
//    StartEvent activity = new StartEvent();
//    activity.setId("codeComplete");
//    print(activity);
//  }
//
//  /** this shows what properties to set when setting or updating a form in a workflow */
//  @Test
//  public void testFormInput() {
//    Form form = new Form()
//      .description("Form description")
//      .field("v1")
//      .field(new FormField()
//        .bindingExpression("v2")
//        .readOnly()
//        .required());
//    print(form);
//  }

  @Test
  public void testUserTask() {
    Form form = new Form()
      .description("Form description")
      .field(new FormField()
        .id("f1")
        .name("The first field in the form")
        .bindingExpression("v1"));
    UserTask activity = new UserTask()
      .id("smokeTest")
      .name("Smoke test")
      .candidateGroupId("dev")
      .form(form)
      .duedate(RelativeTime.hours(1))
      .reminder(RelativeTime.hours(2))
      .reminderRepeat(RelativeTime.minutes(30))
      .escalate(RelativeTime.hours(4))
      .escalateTo(new Binding().value(new UserId("bofh")));

    activity = serialize(activity);

    assertEquals(UserTask.class, activity.getClass());
    assertEquals("smokeTest", activity.getId());
    assertEquals("Smoke test", activity.getName());
    assertEquals("dev", activity.getCandidateGroupIds().get(0).getValue().getInternal());
    assertEquals(RelativeTime.hours(1), activity.getDuedate());
    assertEquals(RelativeTime.hours(2), activity.getReminder());
    assertEquals(RelativeTime.minutes(30), activity.getReminderRepeat());
    assertEquals(RelativeTime.hours(4), activity.getEscalate());
    assertEquals(new UserId("bofh"), activity.getEscalateToId().getValue());

    assertEquals(Form.class, activity.getForm().getClass());
  }
}
