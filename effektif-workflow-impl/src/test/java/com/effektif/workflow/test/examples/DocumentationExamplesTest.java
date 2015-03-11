package com.effektif.workflow.test.examples;

import com.effektif.workflow.api.workflow.Activity;
import com.effektif.workflow.api.workflow.Workflow;
import com.effektif.workflow.impl.bpmn.BpmnWriter;
import junit.framework.TestCase;

import org.junit.Test;

import com.effektif.workflow.api.Configuration;
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
import com.effektif.workflow.api.form.Form;
import com.effektif.workflow.api.form.FormField;
import com.effektif.workflow.api.model.RelativeTime;
import com.effektif.workflow.api.model.UserId;
import com.effektif.workflow.api.model.WorkflowId;
import com.effektif.workflow.api.workflow.Binding;
import com.effektif.workflow.api.workflow.Script;
import com.effektif.workflow.impl.json.JsonService;
import com.effektif.workflow.impl.memory.TestConfiguration;

import javax.swing.*;

/**
 * Stub for a test of JSON and BPMN output, used to generate samples for documentation.
 *
 * @author Peter Hilton
 */
public class DocumentationExamplesTest extends TestCase {

  private static Configuration configuration;

  @Override
  public void setUp() throws Exception {
    if (configuration == null) {
      configuration = new TestConfiguration();
      configuration.getWorkflowEngine();
    }
  }

  @Test
  public void testCall() {
    Call activity = new Call("runTests")
      .subWorkflowName("Run tests")
      .subWorkflowId(new WorkflowId("releaseTests1"));
    activity.setSubWorkflowSource("releaseTests");
    print(activity);
  }

  @Test
  public void testEmailTask() {
    EmailTask activity = new EmailTask()
      // .attachmentId(new FileId("releaseNotes"))
      .bcc("archive@example.org")
      .bodyText("A new version has been deployed on production.")
      .cc("dev@example.org")
      .fromEmailAddress(new Binding<String>().value("effektif@example.org"))
      .subject("New release")
      .to("releases@example.org").toGroupId("releases");
    print(activity);
  }

  @Test
  public void testEmbeddedSubprocess() {
    EmbeddedSubprocess activity = new EmbeddedSubprocess("tagRelease");
    print(activity);
  }

  @Test
  public void testEndEvent() {
    EndEvent activity = new EndEvent("releaseComplete");
    print(activity);
  }

  @Test
  public void testExclusiveGateway() {
    ExclusiveGateway activity = (ExclusiveGateway) new ExclusiveGateway("fork")
      .defaultTransitionId("proceed");
    print(activity);
  }

  @Test
  public void testHttpServiceTask() {
    HttpServiceTask activity = new HttpServiceTask("publishReleaseNotes");
    print(activity);
  }

  @Test
  public void testJavaServiceTask() {
    JavaServiceTask activity = new JavaServiceTask("profilePerformance");
    print(activity);
  }

  @Test
  public void testNoneTask() {
    NoneTask activity = new NoneTask("verifyRequirements");
    print(activity);
  }

  @Test
  public void testParallelGateway() {
    ParallelGateway activity = new ParallelGateway("fork");
    print(activity);
  }

  @Test
  public void testReceiveTask() {
    ReceiveTask activity = new ReceiveTask("buildComplete");
    print(activity);
  }

  @Test
  public void testScriptTask() {
    ScriptTask activity = new ScriptTask("postToTeamChat")
      .script(new Script()
        .language("javascript")
        .script("console.log('TODO');")
        .mapping("Version", "version"));
    print(activity);
  }

  @Test
  public void testStartEvent() {
    StartEvent activity = new StartEvent("codeComplete");
    print(activity);
  }

  /** this shows what properties to set when setting or updating a form in a workflow */
  @Test
  public void testFormInput() {
    Form form = new Form()
      .description("Form description")
      .field("v1")
      .field(new FormField()
        .binding("v2")
        .readOnly()
        .required());
    print(form);
  }

  @Test
  public void testUserTask() {
    Form form = new Form()
      .description("Form description")
      .field(new FormField()
        .id("f1")
        .name("The first field in the form")
        .binding("v1"));
    UserTask activity = new UserTask("smokeTest")
      .name("Smoke test")
      .candidateGroupId("dev")
      .form(form)
      .duedate(RelativeTime.hours(1))
      .reminder(RelativeTime.hours(2))
      .reminderRepeat(RelativeTime.minutes(30))
      .escalate(RelativeTime.hours(4))
      .escalateTo(new Binding().value(new UserId("bofh")));
    print(activity);
  }

  private void printJson(Object o) {
    System.out.println("--- " + o.getClass().getSimpleName() + "----------");
    JsonService jsonService = configuration.get(JsonService.class);
    System.out.println(jsonService.objectToJsonStringPretty(o));
  }

  private void print(Activity activity) {
    printJson(activity);

    Workflow workflow = new Workflow().activity(activity);
    System.out.println(BpmnWriter.writeBpmnDocumentString(workflow, configuration));
  }

  private void print(Form form) {
    UserTask activity = new UserTask().form(form);
    print(activity);
  }
}
