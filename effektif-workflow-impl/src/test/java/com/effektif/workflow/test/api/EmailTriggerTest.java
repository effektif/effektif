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

import com.effektif.workflow.api.model.TriggerInstance;
import com.effektif.workflow.api.workflow.Workflow;
import com.effektif.workflow.api.workflowinstance.WorkflowInstance;
import com.effektif.workflow.impl.email.Email;
import com.effektif.workflow.impl.email.EmailTrigger;
import com.effektif.workflow.impl.util.Lists;
import com.effektif.workflow.test.WorkflowTest;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;

/**
 * @author Peter Hilton
 */
public class EmailTriggerTest extends WorkflowTest {
  
  @Test
  public void testEmailTrigger() {
    Workflow workflow = new Workflow()
      .trigger(new EmailTrigger());

    deploy(workflow);
    
    Email email = new Email()
      .from("me")
      .to(Lists.of("you"))
      .subject("hi");

    WorkflowInstance workflowInstance = workflowEngine.start(
            new TriggerInstance().workflowId(workflow.getId()).data(EmailTrigger.EMAIL_KEY, email));

    Object emailVariable = workflowInstance.getVariableValue(EmailTrigger.EMAIL_KEY);
    assertNotNull("Email not null", emailVariable);
    assertEquals("Email has correct type", Email.class, emailVariable.getClass());

    Email storedEmail = (Email) emailVariable;
    assertEquals("Email from", "me", storedEmail.getFrom());
    assertEquals("Email subject", "hi", storedEmail.getSubject());
    assertEquals("Email to length", 1, storedEmail.getTo().size());
    assertEquals("Email to", "you", storedEmail.getTo().get(0));
  }
}
