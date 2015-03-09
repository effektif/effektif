package com.effektif.example.cli;

import com.effektif.workflow.api.activities.EmailTask;
import com.effektif.workflow.api.activities.UserTask;
import com.effektif.workflow.api.workflow.Workflow;

/**
 * Defines a software release workflow programmatically.
 */
public class SoftwareRelease {

  public static Workflow workflow = new Workflow()
    .sourceWorkflowId("release")
    .activity("Move open issues", new UserTask()
      .transitionToNext())
    .activity("Check continuous integration", new UserTask()
      .transitionToNext())
    .activity("Notify community", new EmailTask()
      .to("releases@example.com")
      .subject("New version released")
      .bodyText("Enjoy!"));
}
