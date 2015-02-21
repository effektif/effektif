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
package com.effektif.workflow.impl.slack;

import org.junit.Test;

import com.effektif.workflow.api.WorkflowEngine;
import com.effektif.workflow.api.model.Deployment;
import com.effektif.workflow.api.model.TriggerInstance;
import com.effektif.workflow.api.task.TaskService;
import com.effektif.workflow.api.workflow.Workflow;
import com.effektif.workflow.impl.activity.ActivityTypeService;
import com.effektif.workflow.impl.memory.TestConfiguration;


/**
 * @author Tom Baeyens
 */
public class SlackTest {

  @Test
  public void testSlack() {
    SlackService slackService = new SlackService();
    slackService.addAccount(new SlackAccount("slackaccountid"));

    TestConfiguration configuration = new TestConfiguration();
    configuration.getBrewery().ingredient(slackService);
    
    ActivityTypeService activityTypeService = configuration.get(ActivityTypeService.class);
    activityTypeService.registerActivityType(new SlackPostImpl());
    
    WorkflowEngine workflowEngine = configuration.getWorkflowEngine();
    // TaskService taskService = configuration.getTaskService();
  
    Workflow workflow = new Workflow()
      .activity("1", new SlackPost()
        .slackAccountId("slackaccountid")
        .channel("channel1")
        .message("message1")
        .name("post"));
    
    Deployment deployment = workflowEngine.deployWorkflow(workflow);
    
    workflowEngine.start(new TriggerInstance()
      .workflowId(deployment.getWorkflowId())
    );
  }
}
