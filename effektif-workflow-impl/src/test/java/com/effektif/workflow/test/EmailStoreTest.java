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
package com.effektif.workflow.test;

import junit.framework.TestCase;

import org.junit.Test;

import com.effektif.workflow.api.Configuration;
import com.effektif.workflow.api.deprecated.model.EmailId;
import com.effektif.workflow.impl.deprecated.email.EmailStore;
import com.effektif.workflow.impl.deprecated.email.PersistentEmail;
import com.effektif.workflow.impl.memory.TestConfiguration;
import com.google.common.collect.ImmutableList;

/**
 * Tests {@link com.effektif.workflow.impl.deprecated.email.EmailStore} storage and retrieval.
 */
public class EmailStoreTest extends TestCase {

  protected static Configuration configuration;
  protected static EmailStore emailStore;


  private PersistentEmail email;

  @Override protected void setUp() throws Exception {
    configuration = new TestConfiguration();
    emailStore = configuration.get(EmailStore.class);

    email = new PersistentEmail()
      .from("ci@example.com")
      .replyTo("ci@example.com")
      .headers("X-SourceWorkflowId", "release")
      .to(ImmutableList.of("dev@example.com"))
      .cc(ImmutableList.of("releases@example.com"))
      .bcc(ImmutableList.of("archive@example.com"))
      .subject("New release")
      .bodyText("A new version has been deployed on production.")
      .bodyHtml("<p>A new version has been deployed on production.</p>");
    email.setOrganizationId("43");
  }

  @Test
  public void testStoreEmail() {
    emailStore.insertEmail(email);
    EmailId newEmailId = email.getId();
    PersistentEmail storedMail = emailStore.findEmailById(newEmailId);
    assertNotNull(storedMail);

    assertEquals(email.getFrom(), storedMail.getFrom());
    assertEquals(email.getReplyTo(), storedMail.getReplyTo());

    assertTrue(storedMail.getHeaders().containsKey("X-SourceWorkflowId"));
    assertEquals(email.getHeaders().get("X-SourceWorkflowId"), storedMail.getHeaders().get("X-SourceWorkflowId"));

    assertEquals(1, email.getTo().size());
    assertEquals(1, email.getCc().size());
    assertEquals(1, email.getBcc().size());

    assertEquals(email.getTo().get(0), storedMail.getTo().get(0));
    assertEquals(email.getCc().get(0), storedMail.getCc().get(0));
    assertEquals(email.getBcc().get(0), storedMail.getBcc().get(0));

    assertEquals(email.getSubject(), storedMail.getSubject());
    assertEquals(email.getBodyText(), storedMail.getBodyText());
    assertEquals(email.getBodyHtml(), storedMail.getBodyHtml());
    assertEquals(email.getId(), storedMail.getId());
    assertEquals(email.getOrganizationId(), storedMail.getOrganizationId());
  }
}
