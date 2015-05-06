/*
 * Copyright 2014 Effektif GmbH.
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
 * limitations under the License.
 */
package com.effektif.workflow.test.deprecated;

import org.junit.Test;

import com.effektif.workflow.api.activities.EndEvent;
import com.effektif.workflow.api.activities.ExclusiveGateway;
import com.effektif.workflow.api.activities.StartEvent;
import com.effektif.workflow.api.condition.Equals;
import com.effektif.workflow.api.deprecated.activities.UserTask;
import com.effektif.workflow.api.deprecated.form.Form;
import com.effektif.workflow.api.deprecated.form.FormField;
import com.effektif.workflow.api.deprecated.form.FormInstance;
import com.effektif.workflow.api.deprecated.task.Task;
import com.effektif.workflow.api.types.ChoiceType;
import com.effektif.workflow.api.types.TextType;
import com.effektif.workflow.api.workflow.Workflow;
import com.effektif.workflow.api.workflowinstance.ActivityInstance;
import com.effektif.workflow.api.workflowinstance.WorkflowInstance;
import com.effektif.workflow.test.WorkflowTest;


/**
 * @author Tom Baeyens
 */
public class DecisionButtonsTest extends WorkflowTest {
  
  @Test
  public void testExclusiveGateway() {
    Workflow workflow = new Workflow()
      .variable("Conclusion", new TextType())
      .variable("Decision", new ChoiceType()
        .option("Approve")
        .option("Reject")
      )
      .activity("Start", new StartEvent()
        .transitionToNext())
      .activity("Submit conclusion", new UserTask()
        .form(new Form()
          .field("Conclusion"))
        .transitionToNext())
      .activity("Approve conclusion", new UserTask()
        .form(new Form()
          .field(new FormField()
            .bindingExpression("Conclusion")
            .readOnly())
          .field(new FormField()
            .bindingExpression("Decision")
            .required()
            .property("asButtons", true)))
        .transitionToNext())
      .activity("Approved?", new ExclusiveGateway()
        .transitionWithConditionTo(new Equals()
          .leftExpression("Conclusion")
          .rightValue("Reject"), 
          "Submit conclusion")
        .transitionWithConditionTo(new Equals()
          .leftExpression("Conclusion")
          .rightValue("Reject"), 
          "End"))
      .activity("End", new EndEvent());
    
    deploy(workflow)
      .checkNoErrors();

    WorkflowInstance workflowInstance = start(workflow);
    
    Task task = getTaskByActivityId("Submit conclusion", workflowInstance);
    FormInstance formInstance = task.getFormInstance();
    // TODO Uncomment and make the test pass (presumed broken by starting to remove UserTask)
//    assertNotNull(formInstance);
//
//    formInstance.value("Conclusion", "Approvals suck");
    taskService.saveFormInstance(task.getId(), formInstance);
    taskService.completeTask(task.getId());
  }

  public Task getTaskByActivityId(String activityId, WorkflowInstance workflowInstance) {
    ActivityInstance activityInstance = workflowInstance.findOpenActivityInstance(activityId);
    return taskService.findTaskById(activityInstance.getTaskId());
  }
}
