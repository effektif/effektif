package com.effektif.workflow.test;

import junit.framework.TestCase;

import org.junit.Test;

import com.effektif.workflow.api.Configuration;
import com.effektif.workflow.api.model.EmailId;
import com.effektif.workflow.impl.email.EmailStore;
import com.effektif.workflow.impl.email.PersistentEmail;
import com.effektif.workflow.impl.memory.TestConfiguration;
import com.google.common.collect.ImmutableList;

/**
 * Tests {@link com.effektif.workflow.impl.email.EmailStore} storage and retrieval.
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
