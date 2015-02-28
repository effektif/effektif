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
package com.effektif.workflow.test.implementation;

import static org.junit.Assert.*;

import org.junit.Test;

import com.effektif.workflow.api.activities.EmailTask;
import com.effektif.workflow.api.model.TriggerInstance;
import com.effektif.workflow.api.model.UserId;
import com.effektif.workflow.api.types.UserIdType;
import com.effektif.workflow.api.workflow.Workflow;
import com.effektif.workflow.impl.identity.User;
import com.effektif.workflow.impl.memory.MemoryIdentityService;
import com.effektif.workflow.test.WorkflowTest;


/**
 * @author Tom Baeyens
 */
public class ExpressionTest extends WorkflowTest {

  @Test
  public void testExpression() {
    User johndoe = new User()
      .id("johndoe")
      .fullName("John Doe")
      .email("johndoe@localhost");
    
    configuration.get(MemoryIdentityService.class)
      .addUser(johndoe);
    
    Workflow workflow = new Workflow()
      .variable("initiatorId", new UserIdType())
      .activity("1", new EmailTask()
        .toExpression("initiatorId", "user", "email"));
    
    deploy(workflow);
    
    workflowEngine.start(new TriggerInstance()
      .data("initiatorId", new UserId("johndoe"))
      .workflowId(workflow.getId()));
    
    assertEquals("johndoe@localhost", getEmail(0).getTo().get(0));
  }
}
