/*
 * Copyright (c) 2013, Effektif GmbH.  All rights reserved.
 */
package com.effektif.email;

import java.util.ArrayList;
import java.util.List;

import com.effektif.workflow.api.bpmn.BpmnElement;
import com.effektif.workflow.api.bpmn.BpmnReader;
import com.effektif.workflow.api.bpmn.BpmnTypeAttribute;
import com.effektif.workflow.api.bpmn.BpmnWriter;
import com.effektif.workflow.api.json.TypeName;
import com.effektif.workflow.api.types.DataType;
import com.effektif.workflow.api.workflow.Activity;
import com.effektif.workflow.api.workflow.Binding;
import com.effektif.workflow.api.workflow.MultiInstance;
import com.effektif.workflow.api.workflow.Timer;
import com.effektif.workflow.api.workflow.Transition;
import com.effektif.workflow.api.workflow.Variable;


/**
 * A kind of service task that sends an e-mail. This is modelled as a service task rather than as a BPMN ‘send’ event to
 * make Effektif’s process model easier to understand. A service task is an appropriate mapping because you can think of
 * sending an email as calling an external ‘email connector’ - a black box service external to the process engine.
 *
 * @see <a href="https://github.com/effektif/effektif/wiki/Email-Task">Email Task</a>
 * @author Tom Baeyens
 */
@TypeName("email")
@BpmnElement("serviceTask")
@BpmnTypeAttribute(attribute="type", value="email")
public class EmailTask extends Activity {

  protected Binding<String> fromEmailAddress;
  protected List<Binding<String>> toEmailAddresses;
  protected List<Binding<String>> ccEmailAddresses;
  protected List<Binding<String>> bccEmailAddresses;

  protected String subject;
  protected String bodyText;
  protected String bodyHtml;
  
  @Override
  public void readBpmn(BpmnReader r) {
    r.startExtensionElements();
    fromEmailAddress = r.readBinding("fromEmailAddress", String.class);
    toEmailAddresses = r.readBindings("toEmailAddress");
    ccEmailAddresses = r.readBindings("ccEmailAddress");
    bccEmailAddresses = r.readBindings("bccEmailAddress");
    subject = r.readTextEffektif("subject");
    bodyText = r.readTextEffektif("bodyText");
    bodyHtml = r.readTextEffektif("bodyHtml");
    r.endExtensionElements();
    super.readBpmn(r);
  }
  
  @Override
  public void writeBpmn(BpmnWriter w) {
    super.writeBpmn(w);
    w.startExtensionElements();
    w.writeBinding("fromEmailAddress", fromEmailAddress);
    w.writeBindings("toEmailAddress", toEmailAddresses);
    w.writeBindings("ccEmailAddress", ccEmailAddresses);
    w.writeBindings("bccEmailAddress", bccEmailAddresses);
    w.writeTextElementEffektif("subject", subject);
    w.writeCDataTextEffektif("bodyText", bodyText);
    w.writeCDataTextEffektif("bodyHtml", bodyHtml);
    w.endExtensionElements();
  }

  @Override
  public EmailTask id(String id) {
    super.id(id);
    return this;
  }

  public Binding<String> getFromEmailAddress() {
    return this.fromEmailAddress;
  }
  public void setFromEmailAddress(Binding<String> fromEmailAddress) {
    this.fromEmailAddress = fromEmailAddress;
  }
  /** optional email address to be used as the sender of the email. */  
  public EmailTask fromEmailAddress(Binding<String> fromEmailAddress) {
    this.fromEmailAddress = fromEmailAddress;
    return this;
  }
  public EmailTask from(String fromEmailAddress) {
    fromEmailAddress(new Binding<String>().value(fromEmailAddress));
    return this;
  }

  public List<Binding<String>> getToEmailAddresses() {
    return this.toEmailAddresses;
  }
  public void setToEmailAddresses(List<Binding<String>> toEmailAddresses) {
    this.toEmailAddresses = toEmailAddresses;
  }
  public EmailTask to(String toEmailAddress) {
    addToEmailAddress(new Binding().value(toEmailAddress));
    return this;
  }
  /** adds the email address specified in a variable to the list of 'to' recipients.
   * @see <a href="http://github.../expressions">See Expressions</a> 
   *  */ 
  public EmailTask toExpression(String expression) {
    addToEmailAddress(new Binding().expression(expression));
    return this;
  }
  protected void addToEmailAddress(Binding<String> toEmailAddressBinding) {
    if (toEmailAddresses==null) {
      toEmailAddresses = new ArrayList<>();
    }
    toEmailAddresses.add(toEmailAddressBinding);
  }
  
  public List<Binding<String>> getCcEmailAddresses() {
    return this.ccEmailAddresses;
  }
  public void setCcEmailAddresses(List<Binding<String>> ccEmailAddresses) {
    this.ccEmailAddresses = ccEmailAddresses;
  }
  public EmailTask cc(String ccEmailAddress) {
    addCcEmailAddressBinding(new Binding().value(ccEmailAddress));
    return this;
  }
  /** adds the email address specified in a variable as a recipient. */ 
  public EmailTask ccExpression(String expression) {
    addCcEmailAddressBinding(new Binding().expression(expression));
    return this;
  }
  protected void addCcEmailAddressBinding(Binding<String> ccEmailAddressBinding) {
    if (ccEmailAddresses==null) {
      ccEmailAddresses = new ArrayList<>();
    }
    ccEmailAddresses.add(ccEmailAddressBinding);
  }

  
  public List<Binding<String>> getBccEmailAddresses() {
    return this.bccEmailAddresses;
  }
  public void setBccEmailAddresses(List<Binding<String>> bccEmailAddresses) {
    this.bccEmailAddresses = bccEmailAddresses;
  }
  public EmailTask bcc(String bccEmailAddress) {
    addBccEmailAddressBinding(new Binding().value(bccEmailAddress));
    return this;
  }
  /** adds the user specified in nested field inside a variable as a recipient. */ 
  public EmailTask bccExpression(String expression) {
    addBccEmailAddressBinding(new Binding().expression(expression));
    return this;
  }
  protected void addBccEmailAddressBinding(Binding<String> bccEmailAddressBinding) {
    if (bccEmailAddresses==null) {
      bccEmailAddresses = new ArrayList<>();
    }
    bccEmailAddresses.add(bccEmailAddressBinding);
  }

  public EmailTask subject(String subject) {
    setSubject(subject);
    return this;
  }

  public String getSubject() {
    return subject;
  }
  
  public void setSubject(String subjectBinding) {
    this.subject = subjectBinding;
  }
  
  public EmailTask bodyText(String bodyText) {
    this.bodyText = bodyText;
    return this;
  }

  public String getBodyText() {
    return bodyText;
  }

  public void setBodyText(String bodyTextBinding) {
    this.bodyText = bodyTextBinding;
  }

  public String getBodyHtml() {
    return bodyHtml;
  }
  public EmailTask bodyHtml(String bodyHtml) {
    this.bodyHtml = bodyHtml;
    return this;
  }


  public void setBodyHtml(String bodyHtmlBinding) {
    this.bodyHtml = bodyHtmlBinding;
  }

  /**
   * @see <a href="https://github.com/effektif/effektif/wiki/Multi-instance-tasks">Multi-instance tasks</a>
   */
  @Override
  public EmailTask multiInstance(MultiInstance multiInstance) {
    super.multiInstance(multiInstance);
    return this;
  }
  @Override
  public EmailTask transitionTo(String toActivityId) {
    super.transitionTo(toActivityId);
    return this;
  }
  @Override
  public EmailTask transitionToNext() {
    super.transitionToNext();
    return this;
  }
  @Override
  public EmailTask transitionTo(Transition transition) {
    super.transitionTo(transition);
    return this;
  }
  @Override
  public EmailTask activity(Activity activity) {
    super.activity(activity);
    return this;
  }
  @Override
  public EmailTask activity(String id, Activity activity) {
    super.activity(id, activity);
    return this;
  }
  @Override
  public EmailTask transition(Transition transition) {
    super.transition(transition);
    return this;
  }
  @Override
  public EmailTask transition(String id, Transition transition) {
    super.transition(id, transition);
    return this;
  }
  @Override
  public EmailTask variable(Variable variable) {
    super.variable(variable);
    return this;
  }
  @Override
  public EmailTask timer(Timer timer) {
    super.timer(timer);
    return this;
  }
  @Override
  public EmailTask property(String key, Object value) {
    super.property(key, value);
    return this;
  }
  @Override
  public EmailTask variable(String id, DataType type) {
    super.variable(id, type);
    return this;
  }
  @Override
  public EmailTask name(String name) {
    super.name(name);
    return this;
  }
  @Override
  public EmailTask description(String description) {
    super.description(description);
    return this;
  }
  @Override
  public EmailTask propertyOpt(String key, Object value) {
    super.propertyOpt(key, value);
    return this;
  }
}
