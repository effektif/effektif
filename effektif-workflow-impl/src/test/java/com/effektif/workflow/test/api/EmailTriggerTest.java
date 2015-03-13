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
package com.effektif.workflow.test.api;

import org.junit.Test;

import com.effektif.workflow.api.model.TriggerInstance;
import com.effektif.workflow.api.types.EmailIdType;
import com.effektif.workflow.api.workflow.Workflow;
import com.effektif.workflow.impl.email.Email;
import com.effektif.workflow.impl.email.EmailStore;
import com.effektif.workflow.impl.email.EmailTrigger;
import com.effektif.workflow.impl.util.Lists;
import com.effektif.workflow.test.WorkflowTest;

/**
 * @author Peter Hilton
 */
public class EmailTriggerTest extends WorkflowTest {
  
  @Test
  public void testEmailTrigger() {
    Workflow workflow = new Workflow()
      .variable("triggerEmail", new EmailIdType())
      .trigger(new EmailTrigger()
        .emailIdVariableId("triggerEmail"));
    
    deploy(workflow);
    
    Email email = new Email()
      .from("me")
      .to(Lists.of("you"))
      .subject("hi");
    
    workflowEngine.start(new TriggerInstance()
      .workflowId(workflow.getId())
      .data(EmailTrigger.EMAIL_KEY, email));
    
    // TODO check if the email shows up in the variable "triggerEmail"
  }
}
