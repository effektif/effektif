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
package com.effektif.workflow.test.api;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.effektif.workflow.api.activities.ScriptTask;
import com.effektif.workflow.api.model.TriggerInstance;
import com.effektif.workflow.api.model.UserId;
import com.effektif.workflow.api.types.TextType;
import com.effektif.workflow.api.types.UserIdType;
import com.effektif.workflow.api.workflow.Workflow;
import com.effektif.workflow.api.workflowinstance.WorkflowInstance;
import com.effektif.workflow.impl.identity.IdentityService;
import com.effektif.workflow.impl.identity.User;
import com.effektif.workflow.test.WorkflowTest;


/**
 * @author Tom Baeyens
 */
public class ScriptTest extends WorkflowTest {
  
  @Test
  public void testScript() {
    Workflow workflow = new Workflow()
      .variable("n", new TextType())
      .variable("m", new TextType())
      .activity("s", new ScriptTask()
        .script("message = 'Hello ' + name;")
        .scriptMapping("name", "n")
        .scriptMapping("message", "m"));

    deploy(workflow);
    
    WorkflowInstance workflowInstance = workflowEngine.start(new TriggerInstance()
      .workflowId(workflow.getId())
      .data("n", "World"));

    assertEquals("Hello World", workflowInstance.getVariableValue("m"));
  }

  @Test
  public void testScriptDereferencing() {
    User johndoe = new User()
      .id(JOHN_ID)
      .fullName("John Doe")
      .email("johndoe@localhost");

    configuration.get(IdentityService.class)
      .createUser(johndoe);

    Workflow workflow = new Workflow()
      .variable("user", new UserIdType())
      .variable("name", new TextType())
      .activity("s", new ScriptTask()
        .script("name = user.fullName;"));

    deploy(workflow);
    
    WorkflowInstance workflowInstance = workflowEngine.start(new TriggerInstance()
      .workflowId(workflow.getId())
      .data("user", new UserId(JOHN_ID)));

    assertEquals("John Doe", workflowInstance.getVariableValue("name"));
  }
}
