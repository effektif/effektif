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
package com.effektif.workflow.test.implementation;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.effektif.workflow.api.deprecated.model.UserId;
import com.effektif.workflow.api.deprecated.types.UserIdType;
import com.effektif.workflow.api.model.TriggerInstance;
import com.effektif.workflow.api.types.DataType;
import com.effektif.workflow.api.workflow.Workflow;
import com.effektif.workflow.impl.WorkflowEngineImpl;
import com.effektif.workflow.impl.WorkflowParser;
import com.effektif.workflow.impl.deprecated.identity.IdentityService;
import com.effektif.workflow.impl.deprecated.identity.User;
import com.effektif.workflow.impl.workflow.ExpressionImpl;
import com.effektif.workflow.impl.workflowinstance.WorkflowInstanceImpl;
import com.effektif.workflow.test.WorkflowTest;


/**
 * @author Tom Baeyens
 */
public class ExpressionTest extends WorkflowTest {

  /**
   * TODO Replace UserId with Link or Money
   */
  @Test
  public void testExpressionUserDereferencing() {
    User johndoe = new User()
      .id("johndoe")
      .fullName("John Doe")
      .email("johndoe@localhost");
    
    configuration.get(IdentityService.class)
      .createUser(johndoe);
    
    assertEquals("johndoe@localhost", 
            evaluate(new UserIdType(), "v", new UserId("johndoe"), "v.email"));
    
    assertEquals("John Doe", 
            evaluate(new UserIdType(), "v", new UserId("johndoe"), "v.fullName"));

    assertEquals("johndoe", 
            evaluate(new UserIdType(), "v", new UserId("johndoe"), "v.id"));

    assertEquals(new UserId("johndoe"), 
            evaluate(new UserIdType(), "v", new UserId("johndoe"), "v"));

    User user = (User) evaluate(new UserIdType(), "v", new UserId("johndoe"), "v.*");
    assertEquals(new UserId("johndoe"),user.getId());
    assertEquals("John Doe",user.getFullName());
    assertEquals("johndoe@localhost",user.getEmail());
  }

  public Object evaluate(DataType type, String variableId, Object value, String expression) {
    Workflow workflow = new Workflow()
      .variable(variableId, type);
    
    deploy(workflow);
    
    TriggerInstance triggerInstance = new TriggerInstance()
      .data(variableId, value)
      .workflowId(workflow.getId());
    
    WorkflowEngineImpl workflowEngineImpl = (WorkflowEngineImpl) workflowEngine;
    WorkflowInstanceImpl workflowInstance = workflowEngineImpl.startInitialize(triggerInstance);
  
    ExpressionImpl expressionImpl = new ExpressionImpl();
    expressionImpl.parse(expression, new WorkflowParser(configuration));
    
    return workflowInstance.getValue(expressionImpl);
  }
}
