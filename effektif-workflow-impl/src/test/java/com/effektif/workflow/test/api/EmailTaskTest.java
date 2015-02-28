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

import static org.junit.Assert.*;

import org.junit.Test;

import com.effektif.workflow.api.activities.EmailTask;
import com.effektif.workflow.api.model.TriggerInstance;
import com.effektif.workflow.api.workflow.Workflow;
import com.effektif.workflow.impl.email.Email;
import com.effektif.workflow.test.WorkflowTest;


/**
 * @author Tom Baeyens
 */
public class EmailTaskTest extends WorkflowTest {

  @Test
  public void testTask() throws Exception {
    Workflow workflow = new Workflow()
      .activity("1", new EmailTask()
        .to("johndoe@example.com")
        .subject("hi")
        .bodyText("by")
        .bodyHtml("<b>by</b>"));
    
    deploy(workflow);
    
    start(workflow);

    Email email = getEmail(0);
    assertNotNull(email);
    // assertEquals("", email.getFrom());
    assertEquals("johndoe@example.com", email.getTo().get(0));
    assertEquals("hi", email.getSubject());
    assertEquals("by", email.getBodyText());
    assertEquals("<b>by</b>", email.getBodyHtml());
  }

  @Test
  public void testTaskTemplatVariables() throws Exception {
    Workflow workflow = new Workflow()
      .activity("1", new EmailTask()
        .subject("ba{{fruit}}na")
        .bodyText("im{{animal}}la")
        .bodyHtml("<b>un{{state}}cious</b>"));
    
    deploy(workflow);
    
    workflowEngine.start(new TriggerInstance()
      .data("fruit", "na")
      .data("animal", "pa")
      .data("state", "cons")
      .workflowId(workflow.getId()));

    Email email = getEmail(0);
    assertNotNull(email);
    // assertEquals("", email.getFrom());
    assertEquals("banana", email.getSubject());
    assertEquals("impala", email.getBodyText());
    assertEquals("<b>unconscious</b>", email.getBodyHtml());
  }
}
