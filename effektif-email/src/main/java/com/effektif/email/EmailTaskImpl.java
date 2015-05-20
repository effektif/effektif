/*
 * Copyright (c) 2013, Effektif GmbH.  All rights reserved.
 */
package com.effektif.email;

import java.util.ArrayList;
import java.util.List;

import com.effektif.workflow.impl.WorkflowParser;
import com.effektif.workflow.impl.activity.AbstractActivityType;
import com.effektif.workflow.impl.template.Hint;
import com.effektif.workflow.impl.template.TextTemplate;
import com.effektif.workflow.impl.workflow.ActivityImpl;
import com.effektif.workflow.impl.workflow.BindingImpl;
import com.effektif.workflow.impl.workflowinstance.ActivityInstanceImpl;


/**
 * @author Tom Baeyens
 */
public class EmailTaskImpl extends AbstractActivityType<EmailTask> {

  protected OutgoingEmailService outgoingEmailService; 

  protected BindingImpl<String> fromEmailAddress;
  protected List<BindingImpl<String>> toEmailAddresses;
  protected List<BindingImpl<String>> ccEmailAddresses;
  protected List<BindingImpl<String>> bccEmailAddresses;
  protected TextTemplate subject;
  protected TextTemplate bodyText;
  protected TextTemplate bodyHtml;
  
  public EmailTaskImpl() {
    super(EmailTask.class);
  }
  
  @Override
  public void execute(ActivityInstanceImpl activityInstance) {
    List<String> to = resolveEmailAddresses(toEmailAddresses, activityInstance);
    List<String> cc = resolveEmailAddresses(ccEmailAddresses, activityInstance);
    List<String> bcc = resolveEmailAddresses(bccEmailAddresses, activityInstance);
    
    OutgoingEmail email = new OutgoingEmail()
      .from(resolveFrom(activityInstance))
      .to(to)
      .cc(cc)
      .bcc(bcc)
      .subject(resolve(subject, activityInstance))
      .bodyText(resolve(bodyText, activityInstance))
      .bodyHtml(resolve(bodyHtml, activityInstance));
    
    outgoingEmailService.send(email);
    
    activityInstance.onwards();
  }

  protected String resolve(TextTemplate textTemplate, ActivityInstanceImpl activityInstance) {
    return textTemplate!=null ? textTemplate.resolve(activityInstance) : null;
  }

  protected String resolveFrom(ActivityInstanceImpl activityInstance) {
    // TODO current implementation uses a configurable process email
    // Christian, could you document in 2 lines some pointers about the 
    // current supported features that we surely need to migrate?
    return null;
  }

  protected List<String> resolveEmailAddresses(
          List<BindingImpl<String>> emailAddressBindings, 
          ActivityInstanceImpl activityInstance) {
    List<String> allEmailAddresses = new ArrayList<>();
    List<String> emailAddresses = activityInstance.getValues(emailAddressBindings);
    addEmailAddresses(allEmailAddresses, emailAddresses);
    return allEmailAddresses;
  }

  protected void addEmailAddresses(List<String> allEmailAddresses, List<String> emailAddresses) {
    if (emailAddresses!=null) {
      for (String emailAddress: emailAddresses) {
        String validatedEmailAddress = outgoingEmailService.validate(emailAddress);
        if (validatedEmailAddress!=null) {
          allEmailAddresses.add(validatedEmailAddress);
        }
      }
    }
  }

  @Override
  public void parse(ActivityImpl activityImpl, EmailTask activity, WorkflowParser parser) {
    super.parse(activityImpl, activity, parser);
    
    outgoingEmailService = parser.getConfiguration(OutgoingEmailService.class);

    fromEmailAddress = parser.parseBinding(activity.getFromEmailAddress(), "fromEmailAddress");
    toEmailAddresses = parser.parseBindings(activity.getToEmailAddresses(), "toEmailAddresses");
    ccEmailAddresses = parser.parseBindings(activity.getCcEmailAddresses(), "ccEmailAddresses");
    bccEmailAddresses = parser.parseBindings(activity.getBccEmailAddresses(), "bccEmailAddresses");
    subject = parser.parseTextTemplate(activity.getSubject(), Hint.EMAIL, Hint.EMAIL_SUBJECT, Hint.SHORT);
    bodyText = parser.parseTextTemplate(activity.getBodyText(), Hint.EMAIL, Hint.EMAIL_BODY_TEXT);
    bodyHtml = parser.parseTextTemplate(activity.getBodyHtml(), Hint.EMAIL, Hint.EMAIL_BODY_HTML, Hint.HTML);
  }
}
