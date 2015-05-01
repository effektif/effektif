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

import static org.junit.Assert.*;

import org.junit.Test;

import com.effektif.workflow.api.deprecated.activities.EmailTask;
import com.effektif.workflow.api.deprecated.types.FileIdType;
import com.effektif.workflow.api.model.TriggerInstance;
import com.effektif.workflow.api.types.ListType;
import com.effektif.workflow.api.workflow.Workflow;
import com.effektif.workflow.impl.deprecated.email.OutgoingEmail;
import com.effektif.workflow.impl.deprecated.file.File;
import com.effektif.workflow.impl.util.Lists;
import com.effektif.workflow.test.WorkflowTest;


/**
 * @author Tom Baeyens
 */
public class EmailTaskTest extends WorkflowTest {

  @Test
  public void testEmailTaskBasics() throws Exception {
    Workflow workflow = new Workflow()
      .activity("1", new EmailTask()
        .to("johndoe@example.com")
        .subject("hi")
        .bodyText("by")
        .bodyHtml("<b>by</b>"));
    
    deploy(workflow);
    
    start(workflow);

    OutgoingEmail email = getOutgoingEmail(0);
    assertNotNull(email);
    // assertEquals("", email.getFrom());
    assertEquals("johndoe@example.com", email.getTo().get(0));
    assertEquals("hi", email.getSubject());
    assertEquals("by", email.getBodyText());
    assertEquals("<b>by</b>", email.getBodyHtml());
  }

  @Test
  public void testEmailTemplateVariables() throws Exception {
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

    OutgoingEmail email = getOutgoingEmail(0);
    assertNotNull(email);
    // assertEquals("", email.getFrom());
    assertEquals("banana", email.getSubject());
    assertEquals("impala", email.getBodyText());
    assertEquals("<b>unconscious</b>", email.getBodyHtml());
  }
  
  @Test
  public void testEmailAttachmentValues() throws Exception {
    File expenseNote = createTestFile("expense note details", "expensenote.txt", "text/plain");
    File receiptOne = createTestFile("receipt one", "receiptone.png", "image/png");
    File receiptTwo = createTestFile("receipt two", "receipttwo.png", "image/png");
    
    Workflow workflow = new Workflow()
      .variable("expenseNote", new FileIdType())
      .variable("receipts", new ListType(new FileIdType()))
      .activity("1", new EmailTask()
        .attachmentExpression("expenseNote")
        .attachmentExpression("receipts"));
    
    deploy(workflow);
    
    workflowEngine.start(new TriggerInstance()
      .data("expenseNote", expenseNote.getId())
      .data("receipts", Lists.of(receiptOne.getId(), receiptTwo.getId()))
      .workflowId(workflow.getId()));

    OutgoingEmail email = getOutgoingEmail(0);
    assertNotNull(email);
    assertEquals("expensenote.txt", email.getAttachments().get(0).getFileName());
    assertEquals("receiptone.png", email.getAttachments().get(1).getFileName());
    assertEquals("receipttwo.png", email.getAttachments().get(2).getFileName());
  }

//  @Test
//  public void testEmailAttachmentExpression() throws Exception {
//    Attachment a1 = new TestAttachment("one", "fn1", "text/plain");
//    Attachment a2 = new TestAttachment("two", "fn2", "text/plain");
//    Attachment a3 = new TestAttachment("three", "fn3", "text/plain");
//    
//    Workflow workflow = new Workflow()
//      .variable("file", new FileIdType())
//      .variable("fileList", new ListType(new FileIdType()))
//      .activity("1", new EmailTask()
//        .attachmentExpression("file")
//        .attachmentExpression("fileList"));
//    
//    deploy(workflow);
//    
//    workflowEngine.start(new TriggerInstance()
//      .data("file", "na")
//      .data("fileList", "pa")
//      .data("state", "cons")
//      .workflowId(workflow.getId()));
//
//    Email email = getEmail(0);
//    assertNotNull(email);
//    // assertEquals("", email.getFrom());
//    assertEquals("banana", email.getSubject());
//    assertEquals("impala", email.getBodyText());
//    assertEquals("<b>unconscious</b>", email.getBodyHtml());
//  }
}
