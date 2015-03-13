package com.effektif.workflow.impl.email;

import com.effektif.workflow.api.workflow.Trigger;

/**
 * Starts a workflow as a result of receiving an email.
 *
 * @author Peter Hilton
 */
public class EmailTrigger extends Trigger {

  public static final String EMAIL_KEY = "email";
  public static final String EMAIL_ID_KEY = "emailId";
  
  protected String emailIdVariableId;

  public String getEmailIdVariableId() {
    return this.emailIdVariableId;
  }
  public void setEmailIdVariableId(String emailIdVariableId) {
    this.emailIdVariableId = emailIdVariableId;
  }
  public EmailTrigger emailIdVariableId(String emailIdVariableId) {
    this.emailIdVariableId = emailIdVariableId;
    return this;
  }
}
