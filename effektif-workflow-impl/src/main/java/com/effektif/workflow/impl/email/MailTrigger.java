package com.effektif.workflow.impl.email;

import com.effektif.workflow.api.workflow.Trigger;

/**
 * Starts a workflow as a result of receiving an email.
 *
 * @author Peter Hilton
 */
public class MailTrigger extends Trigger {

  protected Email email;

  public Email getEmail() { return email; }
  public void setEmail(Email email) { this.email = email; }
  public MailTrigger trigger(Email email) {
    this.email = email;
    return this;
  }
}
