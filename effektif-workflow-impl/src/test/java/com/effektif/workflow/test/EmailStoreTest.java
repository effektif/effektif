package com.effektif.workflow.test;

import com.effektif.workflow.api.Configuration;
import com.effektif.workflow.api.model.EmailId;
import com.effektif.workflow.impl.email.Email;
import com.effektif.workflow.impl.email.EmailStore;
import com.effektif.workflow.impl.memory.TestConfiguration;
import com.sun.tools.javac.util.List;
import junit.framework.TestCase;
import org.junit.Test;

/**
 * Tests {@link com.effektif.workflow.impl.email.EmailStore} storage and retrieval.
 */
public class EmailStoreTest extends TestCase {

  protected static Configuration configuration;
  protected static EmailStore emailStore;


  private Email mail;

  @Override protected void setUp() throws Exception {
    if (configuration != null) {
      throw new IllegalStateException("already configured");
    }

    configuration = new TestConfiguration();
    emailStore = configuration.get(EmailStore.class);

    mail = new Email()
      .from("ci@example.com")
      .replyTo("ci@example.com")
      .headers("X-SourceWorkflowId", "release")
      .to(List.of("dev@example.com"))
      .cc(List.of("releases@example.com"))
      .bcc(List.of("archive@example.com"))
      .subject("New release")
      .bodyText("A new version has been deployed on production.")
      .bodyHtml("<p>A new version has been deployed on production.</p>");
    mail.setOrganizationId("43");
  }

  @Test
  public void testStoreEmail() {
    EmailId newEmailId = emailStore.createEmail(mail).getId();
    Email storedMail = emailStore.findEmailById(newEmailId);
    assertNotNull(storedMail);

    assertEquals(mail.getFrom(), storedMail.getFrom());
    assertEquals(mail.getReplyTo(), storedMail.getReplyTo());

    assertTrue(storedMail.getHeaders().containsKey("X-SourceWorkflowId"));
    assertEquals(mail.getHeaders().get("X-SourceWorkflowId"), storedMail.getHeaders().get("X-SourceWorkflowId"));

    assertEquals(1, mail.getTo().size());
    assertEquals(1, mail.getCc().size());
    assertEquals(1, mail.getBcc().size());

    assertEquals(mail.getTo().get(0), storedMail.getTo().get(0));
    assertEquals(mail.getCc().get(0), storedMail.getCc().get(0));
    assertEquals(mail.getBcc().get(0), storedMail.getBcc().get(0));

    assertEquals(mail.getSubject(), storedMail.getSubject());
    assertEquals(mail.getBodyText(), storedMail.getBodyText());
    assertEquals(mail.getBodyHtml(), storedMail.getBodyHtml());
    assertEquals(mail.getId(), storedMail.getId());
    assertEquals(mail.getOrganizationId(), storedMail.getOrganizationId());
  }
}
