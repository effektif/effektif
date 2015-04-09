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

import org.junit.Test;

import com.effektif.workflow.api.activities.Call;
import com.effektif.workflow.api.activities.EmailTask;
import com.effektif.workflow.api.activities.StartEvent;
import com.effektif.workflow.api.mapper.JsonReadable;
import com.effektif.workflow.api.model.FileId;
import com.effektif.workflow.api.model.GroupId;
import com.effektif.workflow.api.model.UserId;
import com.effektif.workflow.api.model.WorkflowId;
import com.effektif.workflow.api.workflow.Activity;
import com.effektif.workflow.api.workflow.Workflow;
import com.effektif.workflow.impl.mapper.Mappings;


/**
 * @author Tom Baeyens
 */
public abstract class AbstractMapperTest {
  
  protected static Mappings mappings = null; 
          
  public static void initializeMappings() {
    mappings = new Mappings();
    mappings.registerBaseClass(Activity.class);
    mappings.registerSubClass(StartEvent.class);
    mappings.registerSubClass(Call.class);
    mappings.registerSubClass(EmailTask.class);
  }

  @Test
  public void testWorkflowJson() {
    Workflow workflow = new Workflow()
      .id(new WorkflowId("551d4f5803649532d21f223f"))
      .activity(new StartEvent()
        .id("s")
      );
    
    workflow = serialize(workflow);
    
    assertEquals("551d4f5803649532d21f223f", workflow.getId().getInternal());
    assertEquals(StartEvent.class, workflow.getActivities().get(0).getClass());
    assertEquals("s", workflow.getActivities().get(0).getId());
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

  protected abstract <T extends JsonReadable> T serialize(T o);

  @Test
  public void testEmailTask() {
    EmailTask activity = new EmailTask()
      .id("sendEmail")
      .from("effektif@example.org")
      .to("releases@example.org")
      .toExpression("v1.email")
      .toUserId("johndoe")
      .toGroupId("releases")
      .cc("dev@example.org")
      .ccUserId("joesmoe")
      .ccGroupId("support")
      .bcc("archive@example.org")
      .bccUserId("jackblack")
      .bccGroupId("management")
      .subject("New release")
      .bodyText("A new version has been deployed on production.")
      .bodyHtml("<b>A new version has been deployed on production.</b>")
      .attachment(new FileId("releaseNotes"));
    
    activity = serialize(activity);
    
    assertEquals(EmailTask.class, activity.getClass());
    
    assertEquals("sendEmail", activity.getId());
    assertEquals("effektif@example.org", activity.getFromEmailAddress().getValue());
    assertEquals("releases@example.org", activity.getToEmailAddresses().get(0).getValue());
    assertEquals("v1.email", activity.getToEmailAddresses().get(1).getExpression());
    assertEquals(new UserId("johndoe"), activity.getToUserIds().get(0).getValue());
    assertEquals(new GroupId("releases"), activity.getToGroupIds().get(0).getValue());

    assertEquals("dev@example.org", activity.getCcEmailAddresses().get(0).getValue());
    assertEquals(new UserId("joesmoe"), activity.getCcUserIds().get(0).getValue());
    assertEquals(new GroupId("support"), activity.getCcGroupIds().get(0).getValue());

    assertEquals("archive@example.org", activity.getBccEmailAddresses().get(0).getValue());
    assertEquals(new UserId("jackblack"), activity.getBccUserIds().get(0).getValue());
    assertEquals(new GroupId("management"), activity.getBccGroupIds().get(0).getValue());

    assertEquals("New release", activity.getSubject());
    assertEquals("A new version has been deployed on production.", activity.getBodyText());
    assertEquals("<b>A new version has been deployed on production.</b>", activity.getBodyHtml());

    assertEquals(new FileId("releaseNotes"), activity.getAttachmentFileIds().get(0).getValue());
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
//
//  @Test
//  public void testUserTask() {
//    Form form = new Form()
//      .description("Form description")
//      .field(new FormField()
//        .id("f1")
//        .name("The first field in the form")
//        .bindingExpression("v1"));
//    UserTask activity = new UserTask()
//      .id("smokeTest")
//      .name("Smoke test")
//      .candidateGroupId("dev")
//      .form(form)
//      .duedate(RelativeTime.hours(1))
//      .reminder(RelativeTime.hours(2))
//      .reminderRepeat(RelativeTime.minutes(30))
//      .escalate(RelativeTime.hours(4))
//      .escalateTo(new Binding().value(new UserId("bofh")));
//    print(activity);
//  }
//

}
