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
package com.effektif.workflow.api.activities;

import java.util.ArrayList;
import java.util.List;

import com.effektif.workflow.api.model.Attachment;
import com.effektif.workflow.api.model.FileId;
import com.effektif.workflow.api.model.GroupId;
import com.effektif.workflow.api.model.UserId;
import com.effektif.workflow.api.types.Type;
import com.effektif.workflow.api.workflow.Activity;
import com.effektif.workflow.api.workflow.Binding;
import com.effektif.workflow.api.workflow.MultiInstance;
import com.effektif.workflow.api.workflow.Timer;
import com.effektif.workflow.api.workflow.Transition;
import com.effektif.workflow.api.workflow.Variable;
import com.fasterxml.jackson.annotation.JsonTypeName;


/**
 * A kind of service task that sends an e-mail. This is modelled as a service task rather than as a BPMN ‘send’ event to
 * make Effektif’s process model easier to understand. A service task is an appropriate mapping because you can think of
 * sending an email as calling an external ‘email connector’ - a black box service external to the process engine.
 *
 * @see <a href="https://github.com/effektif/effektif/wiki/Email-Task">Email Task</a>
 * @author Tom Baeyens
 */
@JsonTypeName("email")
public class EmailTask extends Activity {

  protected Binding<String> fromEmailAddress;

  protected List<Binding<String>> toEmailAddresses;
  protected List<Binding<UserId>> toUserIds;
  protected List<Binding<GroupId>> toGroupIds;

  protected List<Binding<String>> ccEmailAddresses;
  protected List<Binding<UserId>> ccUserIds;
  protected List<Binding<GroupId>> ccGroupIds;

  protected List<Binding<String>> bccEmailAddresses;
  protected List<Binding<UserId>> bccUserIds;
  protected List<Binding<GroupId>> bccGroupIds;

  protected String subject;
  protected String bodyText;
  protected String bodyHtml;
  
  protected List<Binding<Attachment>> attachments;
  
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

  public List<Binding<UserId>> getToUserIds() {
    return this.toUserIds;
  }
  public void setToUserIds(List<Binding<UserId>> toUserIds) {
    this.toUserIds = toUserIds;
  }
  public EmailTask toUserId(String toUserId) {
    return toUserId(new UserId(toUserId));
  }
  public EmailTask toUserId(UserId toUserId) {
    addToUserIdBinding(new Binding<UserId>().value(toUserId));
    return this;
  }
  /** adds the user specified in a variable as a recipient. */ 
  public EmailTask toUserIdExpression(String expression) {
    addToUserIdBinding(new Binding<UserId>().expression(expression));
    return this;
  }
  protected void addToUserIdBinding(Binding<UserId> toUserIdBinding) {
    if (toUserIds==null) {
      toUserIds = new ArrayList<>();
    }
    toUserIds.add(toUserIdBinding);
  }
  
  public List<Binding<GroupId>> getToGroupIds() {
    return this.toGroupIds;
  }
  public void setToGroupIds(List<Binding<GroupId>> toGroupIds) {
    this.toGroupIds = toGroupIds;
  }
  public EmailTask toGroupId(String toGroupId) {
    return toGroupId(new GroupId(toGroupId));
  }
  public EmailTask toGroupId(GroupId toGroupId) {
    addToGroupIdBinding(new Binding<GroupId>().value(toGroupId));
    return this;
  }
  /** adds the user specified in a variable as a recipient. */ 
  public EmailTask toGroupIdExpression(String expression) {
    addToGroupIdBinding(new Binding<GroupId>().expression(expression));
    return this;
  }
  protected void addToGroupIdBinding(Binding<GroupId> toGroupIdBinding) {
    if (toGroupIds==null) {
      toGroupIds = new ArrayList<>();
    }
    toGroupIds.add(toGroupIdBinding);
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

  public List<Binding<UserId>> getCcUserIds() {
    return this.ccUserIds;
  }
  public void setCcUserIds(List<Binding<UserId>> ccUserIds) {
    this.ccUserIds = ccUserIds;
  }
  public EmailTask ccUserId(String ccUserId) {
    return ccUserId(new UserId(ccUserId));
  }
  public EmailTask ccUserId(UserId ccUserId) {
    addCcUserIdBinding(new Binding<UserId>().value(ccUserId));
    return this;
  }
  /** adds the user specified in a variable as a recipient. */ 
  public EmailTask ccUserIdExpression(String expression) {
    addCcUserIdBinding(new Binding<UserId>().expression(expression));
    return this;
  }
  protected void addCcUserIdBinding(Binding<UserId> ccUserIdBinding) {
    if (ccUserIds==null) {
      ccUserIds = new ArrayList<>();
    }
    ccUserIds.add(ccUserIdBinding);
  }
  
  public List<Binding<GroupId>> getCcGroupIds() {
    return this.ccGroupIds;
  }
  public void setCcGroupIds(List<Binding<GroupId>> ccGroupIds) {
    this.ccGroupIds = ccGroupIds;
  }
  public EmailTask ccGroupId(String ccGroupId) {
    return ccGroupId(new GroupId(ccGroupId));
  }
  public EmailTask ccGroupId(GroupId ccGroupId) {
    addCcGroupIdBinding(new Binding<GroupId>().value(ccGroupId));
    return this;
  }
  /** adds the user specified in a variable as a recipient. */ 
  public EmailTask ccGroupIdExpression(String expression) {
    addCcGroupIdBinding(new Binding<GroupId>().expression(expression));
    return this;
  }
  protected void addCcGroupIdBinding(Binding<GroupId> ccGroupIdBinding) {
    if (ccGroupIds==null) {
      ccGroupIds = new ArrayList<>();
    }
    ccGroupIds.add(ccGroupIdBinding);
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

  public List<Binding<UserId>> getBccUserIds() {
    return this.bccUserIds;
  }
  public void setBccUserIds(List<Binding<UserId>> bccUserIds) {
    this.bccUserIds = bccUserIds;
  }
  public EmailTask bccUserId(String bccUserId) {
    return bccUserId(new UserId(bccUserId));
  }
  public EmailTask bccUserId(UserId bccUserId) {
    addBccUserIdBinding(new Binding<UserId>().value(bccUserId));
    return this;
  }
  /** adds the user specified in a variable as a recipient. */ 
  public EmailTask bccUserIdExpression(String expression) {
    addBccUserIdBinding(new Binding<UserId>().expression(expression));
    return this;
  }
  protected void addBccUserIdBinding(Binding<UserId> bccUserIdBinding) {
    if (bccUserIds==null) {
      bccUserIds = new ArrayList<>();
    }
    bccUserIds.add(bccUserIdBinding);
  }
  
  public List<Binding<GroupId>> getBccGroupIds() {
    return this.bccGroupIds;
  }
  public void setBccGroupIds(List<Binding<GroupId>> bccGroupIds) {
    this.bccGroupIds = bccGroupIds;
  }
  public EmailTask bccGroupId(String bccGroupId) {
    return bccGroupId(new GroupId(bccGroupId));
  }
  public EmailTask bccGroupId(GroupId bccGroupId) {
    addBccGroupIdBinding(new Binding<GroupId>().value(bccGroupId));
    return this;
  }
  /** adds the user specified in nested field inside a variable as a recipient. */ 
  public EmailTask bccGroupIdExpression(String expression) {
    addBccGroupIdBinding(new Binding<GroupId>().expression(expression));
    return this;
  }
  protected void addBccGroupIdBinding(Binding<GroupId> bccGroupIdBinding) {
    if (bccGroupIds==null) {
      bccGroupIds = new ArrayList<>();
    }
    bccGroupIds.add(bccGroupIdBinding);
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

  public List<Binding<Attachment>> getAttachments() {
    return this.attachments;
  }
  public void setAttachments(List<Binding<Attachment>> attachments) {
    this.attachments = attachments;
  }
  public EmailTask attachment(Attachment attachment) {
    addAttachment(new Binding<Attachment>().value(attachment));
    return this;
  }
  /** adds the user specified in nested field inside a variable as a recipient. */ 
  public EmailTask attachmentExpression(String expression) {
    addAttachment(new Binding<Attachment>().expression(expression));
    return this;
  }
  protected void addAttachment(Binding<Attachment> attachment) {
    if (attachments==null) {
      attachments = new ArrayList<>();
    }
    attachments.add(attachment);
  }
  
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
  public EmailTask id(String id) {
    super.id(id);
    return this;
  }
  @Override
  public EmailTask property(String key, Object value) {
    super.property(key, value);
    return this;
  }
  @Override
  public EmailTask variable(String id, Type type) {
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
