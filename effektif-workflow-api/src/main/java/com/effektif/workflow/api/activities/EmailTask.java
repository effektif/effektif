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

import com.effektif.workflow.api.ref.FileId;
import com.effektif.workflow.api.ref.GroupId;
import com.effektif.workflow.api.ref.UserId;
import com.effektif.workflow.api.workflow.Activity;
import com.effektif.workflow.api.workflow.Binding;
import com.fasterxml.jackson.annotation.JsonTypeName;


/**
 * A kind of service task that sends an e-mail. This is modelled as a service task rather than as a BPMN ‘send’ event to
 * make Effektif’s process model easier to understand. A service task is an appropriate mapping because you can think of
 * sending an email as calling an external ‘email connector’ - a black box service external to the process engine.
 *
 * BPMN XML: {@code <serviceTask id="sendMail" effektif:type="email">}
 *
 * @author Tom Baeyens
 */
@JsonTypeName("email")
public class EmailTask extends Activity {
  
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
  
  protected List<Binding<FileId>> attachments;
  
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
  public EmailTask toUserIdVariableId(String variableId) {
    addToUserIdBinding(new Binding<UserId>().variableId(variableId));
    return this;
  }
  /** adds the user specified in nested field inside a variable as a recipient. */ 
  public EmailTask toUserIdVariableField(String variableId, String... fields) {
    addToUserIdBinding(new Binding<UserId>().variableId(variableId).fields(fields));
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
  public EmailTask toGroupIdVariableId(String variableId) {
    addToGroupIdBinding(new Binding<GroupId>().variableId(variableId));
    return this;
  }
  /** adds the user specified in nested field inside a variable as a recipient. */ 
  public EmailTask toGroupIdVariableField(String variableId, String... fields) {
    addToGroupIdBinding(new Binding<GroupId>().variableId(variableId).fields(fields));
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
    addToEmailAddressBinding(new Binding().value(toEmailAddress));
    return this;
  }
  /** adds the email address specified in a variable as a recipient. */ 
  public EmailTask toVariableId(String variableId) {
    addToEmailAddressBinding(new Binding().variableId(variableId));
    return this;
  }
  /** adds the user specified in nested field inside a variable as a recipient. */ 
  public EmailTask toVariableField(String variableId, String... fields) {
    addToEmailAddressBinding(new Binding().variableId(variableId).fields(fields));
    return this;
  }
  protected void addToEmailAddressBinding(Binding<String> toEmailAddressBinding) {
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
  public EmailTask ccUserIdVariableId(String variableId) {
    addCcUserIdBinding(new Binding<UserId>().variableId(variableId));
    return this;
  }
  /** adds the user specified in nested field inside a variable as a recipient. */ 
  public EmailTask ccUserIdVariableField(String variableId, String... fields) {
    addCcUserIdBinding(new Binding<UserId>().variableId(variableId).fields(fields));
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
  public EmailTask ccGroupIdVariableId(String variableId) {
    addCcGroupIdBinding(new Binding<GroupId>().variableId(variableId));
    return this;
  }
  /** adds the user specified in nested field inside a variable as a recipient. */ 
  public EmailTask ccGroupIdVariableField(String variableId, String... fields) {
    addCcGroupIdBinding(new Binding<GroupId>().variableId(variableId).fields(fields));
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
  public EmailTask ccVariableId(String variableId) {
    addCcEmailAddressBinding(new Binding().variableId(variableId));
    return this;
  }
  /** adds the user specified in nested field inside a variable as a recipient. */ 
  public EmailTask ccVariableField(String variableId, String... fields) {
    addCcEmailAddressBinding(new Binding().variableId(variableId).fields(fields));
    return this;
  }
  protected void addCcEmailAddressBinding(Binding<String> ccEmailAddressBinding) {
    if (ccEmailAddresses==null) {
      ccEmailAddresses = new ArrayList<>();
    }
    ccEmailAddresses.add(ccEmailAddressBinding);
  }

  public List<Binding<UserId>> getBbccUserIds() {
    return this.bccUserIds;
  }
  public void setBbccUserIds(List<Binding<UserId>> bccUserIds) {
    this.bccUserIds = bccUserIds;
  }
  public EmailTask bccUserId(String bccUserId) {
    return bccUserId(new UserId(bccUserId));
  }
  public EmailTask bccUserId(UserId bccUserId) {
    addBbccUserIdBinding(new Binding<UserId>().value(bccUserId));
    return this;
  }
  /** adds the user specified in a variable as a recipient. */ 
  public EmailTask bccUserIdVariableId(String variableId) {
    addBbccUserIdBinding(new Binding<UserId>().variableId(variableId));
    return this;
  }
  /** adds the user specified in nested field inside a variable as a recipient. */ 
  public EmailTask bccUserIdVariableField(String variableId, String... fields) {
    addBbccUserIdBinding(new Binding<UserId>().variableId(variableId).fields(fields));
    return this;
  }
  protected void addBbccUserIdBinding(Binding<UserId> bccUserIdBinding) {
    if (bccUserIds==null) {
      bccUserIds = new ArrayList<>();
    }
    bccUserIds.add(bccUserIdBinding);
  }
  
  public List<Binding<GroupId>> getBbccGroupIds() {
    return this.bccGroupIds;
  }
  public void setBbccGroupIds(List<Binding<GroupId>> bccGroupIds) {
    this.bccGroupIds = bccGroupIds;
  }
  public EmailTask bccGroupId(String bccGroupId) {
    return bccGroupId(new GroupId(bccGroupId));
  }
  public EmailTask bccGroupId(GroupId bccGroupId) {
    addBbccGroupIdBinding(new Binding<GroupId>().value(bccGroupId));
    return this;
  }
  /** adds the user specified in a variable as a recipient. */ 
  public EmailTask bccGroupIdVariableId(String variableId) {
    addBbccGroupIdBinding(new Binding<GroupId>().variableId(variableId));
    return this;
  }
  /** adds the user specified in nested field inside a variable as a recipient. */ 
  public EmailTask bccGroupIdVariableField(String variableId, String... fields) {
    addBbccGroupIdBinding(new Binding<GroupId>().variableId(variableId).fields(fields));
    return this;
  }
  protected void addBbccGroupIdBinding(Binding<GroupId> bccGroupIdBinding) {
    if (bccGroupIds==null) {
      bccGroupIds = new ArrayList<>();
    }
    bccGroupIds.add(bccGroupIdBinding);
  }
  
  public List<Binding<String>> getBbccEmailAddresses() {
    return this.bccEmailAddresses;
  }
  public void setBbccEmailAddresses(List<Binding<String>> bccEmailAddresses) {
    this.bccEmailAddresses = bccEmailAddresses;
  }
  public EmailTask bcc(String bccEmailAddress) {
    addBbccEmailAddressBinding(new Binding().value(bccEmailAddress));
    return this;
  }
  /** adds the email address specified in a variable as a recipient. */ 
  public EmailTask bccVariableId(String variableId) {
    addBbccEmailAddressBinding(new Binding().variableId(variableId));
    return this;
  }
  /** adds the user specified in nested field inside a variable as a recipient. */ 
  public EmailTask bccVariableField(String variableId, String... fields) {
    addBbccEmailAddressBinding(new Binding().variableId(variableId).fields(fields));
    return this;
  }
  protected void addBbccEmailAddressBinding(Binding<String> bccEmailAddressBinding) {
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
    setBodyText(bodyText);
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

  public void setBodyHtml(String bodyHtmlBinding) {
    this.bodyHtml = bodyHtmlBinding;
  }

  public List<Binding<FileId>> getAttachments() {
    return this.attachments;
  }
  public void setAttachments(List<Binding<FileId>> attachments) {
    this.attachments = attachments;
  }
  public EmailTask attachment(FileId fileId) {
    addAttachmentBinding(new Binding<FileId>().value(fileId));
    return this;
  }
  /** adds the email address specified in a variable as a recipient. */ 
  public EmailTask attachmentVariableId(String variableId) {
    addAttachmentBinding(new Binding<FileId>().variableId(variableId));
    return this;
  }
  /** adds the user specified in nested field inside a variable as a recipient. */ 
  public EmailTask battachmentVariableField(String variableId, String... fields) {
    addAttachmentBinding(new Binding<FileId>().variableId(variableId).fields(fields));
    return this;
  }
  protected void addAttachmentBinding(Binding<FileId> attachmentBinding) {
    if (attachments==null) {
      attachments = new ArrayList<>();
    }
    attachments.add(attachmentBinding);
  }
}
