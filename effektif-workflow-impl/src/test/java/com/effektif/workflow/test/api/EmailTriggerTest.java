/* Copyright (c) 2015, Effektif GmbH.
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
package com.effektif.workflow.test.api;

import static org.junit.Assert.*;

import org.junit.Test;

import com.effektif.workflow.api.model.EmailId;
import com.effektif.workflow.api.model.TriggerInstance;
import com.effektif.workflow.api.types.EmailIdType;
import com.effektif.workflow.api.workflow.Workflow;
import com.effektif.workflow.api.workflowinstance.WorkflowInstance;
import com.effektif.workflow.impl.email.Email;
import com.effektif.workflow.impl.email.EmailTrigger;
import com.effektif.workflow.test.WorkflowTest;

/**
 * @author Peter Hilton
 */
public class EmailTriggerTest extends WorkflowTest {
  
  @Test
  public void testEmailTrigger() {
    Workflow workflow = new Workflow()
      .trigger(new EmailTrigger());

    deploy(workflow);
    
    Email email = new Email();

    WorkflowInstance workflowInstance = workflowEngine.start(
      new TriggerInstance().workflowId(workflow.getId()).data(EmailTrigger.EMAIL_KEY, email));

    Object emailVariable = workflowInstance.getVariableValue(EmailTrigger.EMAIL_ID_KEY);
    assertNotNull("Email ID not null", emailVariable);
    assertEquals("Email ID has correct type", EmailId.class, emailVariable.getClass());
  }

  /**
   * Tests specifying a custom variable ID for the {@link com.effektif.workflow.api.model.EmailId} workflow variable
   * in the trigger.
   */
  @Test
  public void testEmailTriggerVariableId() {
    Workflow workflow = new Workflow()
      .variable("triggerEmail", new EmailIdType())
      .trigger(new EmailTrigger().emailIdVariableId("triggerEmail"));

    deploy(workflow);

    Email email = new Email();

    WorkflowInstance workflowInstance = workflowEngine.start(
      new TriggerInstance().workflowId(workflow.getId()).data(EmailTrigger.EMAIL_KEY, email));

    Object emailVariable = workflowInstance.getVariableValue("triggerEmail");
    assertNotNull("Email ID not null", emailVariable);
    assertEquals("Email ID has correct type", EmailId.class, emailVariable.getClass());
  }

  /**
   * Repeats the test in {@link #testEmailTriggerVariableId} but does not declare the workflow variable, to test that
   * the variable’s type is automatically set correctly to {@link EmailIdType}.
   */
  @Test
  public void testEmailTriggerVariableIdDetectType() {
    Workflow workflow = new Workflow()
      .trigger(new EmailTrigger().emailIdVariableId("triggerEmail"));

    deploy(workflow);

    Email email = new Email();

    WorkflowInstance workflowInstance = workflowEngine.start(
      new TriggerInstance().workflowId(workflow.getId()).data(EmailTrigger.EMAIL_KEY, email));

    Object emailVariable = workflowInstance.getVariableValue("triggerEmail");
    assertNotNull("Email ID not null", emailVariable);
    assertEquals("Email ID has correct type", EmailId.class, emailVariable.getClass());
  }
}
