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
package com.effektif.workflow.impl.activity.types;

import java.util.ArrayList;
import java.util.List;

import com.effektif.workflow.api.activities.EmailTask;
import com.effektif.workflow.api.model.Attachment;
import com.effektif.workflow.api.model.GroupId;
import com.effektif.workflow.api.model.UserId;
import com.effektif.workflow.api.types.GroupIdType;
import com.effektif.workflow.api.types.TextType;
import com.effektif.workflow.api.types.UserIdType;
import com.effektif.workflow.api.xml.XmlElement;
import com.effektif.workflow.impl.WorkflowParser;
import com.effektif.workflow.impl.activity.AbstractActivityType;
import com.effektif.workflow.impl.bpmn.BpmnReader;
import com.effektif.workflow.impl.bpmn.BpmnWriter;
import com.effektif.workflow.impl.bpmn.ServiceTaskType;
import com.effektif.workflow.impl.email.Email;
import com.effektif.workflow.impl.email.EmailService;
import com.effektif.workflow.impl.identity.IdentityService;
import com.effektif.workflow.impl.template.Hint;
import com.effektif.workflow.impl.template.TextTemplate;
import com.effektif.workflow.impl.workflow.ActivityImpl;
import com.effektif.workflow.impl.workflow.BindingImpl;
import com.effektif.workflow.impl.workflowinstance.ActivityInstanceImpl;


/**
 * @author Tom Baeyens
 */
public class EmailTaskImpl extends AbstractActivityType<EmailTask> {

  private static final String BPMN_ELEMENT_NAME = "serviceTask";
  
  protected EmailService emailService; 
  protected IdentityService identityService; 

  protected BindingImpl<String> fromEmailAddress;

  protected List<BindingImpl<String>> toEmailAddresses;
  protected List<BindingImpl<UserId>> toUserIds;
  protected List<BindingImpl<GroupId>> toGroupIds;

  protected List<BindingImpl<String>> ccEmailAddresses;
  protected List<BindingImpl<UserId>> ccUserIds;
  protected List<BindingImpl<GroupId>> ccGroupIds;

  protected List<BindingImpl<String>> bccEmailAddresses;
  protected List<BindingImpl<UserId>> bccUserIds;
  protected List<BindingImpl<GroupId>> bccGroupIds;

  protected TextTemplate subject;
  protected TextTemplate bodyText;
  protected TextTemplate bodyHtml;
  
  protected List<BindingImpl<Attachment>> attachments;

  public EmailTaskImpl() {
    super(EmailTask.class);
  }
  
  @Override
  public void parse(ActivityImpl activityImpl, EmailTask activity, WorkflowParser parser) {
    super.parse(activityImpl, activity, parser);
    
    emailService = parser.getConfiguration(EmailService.class);
    identityService = parser.getConfiguration(IdentityService.class);

    fromEmailAddress = parser.parseBinding(activity.getFromEmailAddress(), "fromEmailAddress");

    toEmailAddresses = parser.parseBindings(activity.getToEmailAddresses(), "toEmailAddresses");
    toUserIds = parser.parseBindings(activity.getToUserIds(), "toUserIds");
    toGroupIds = parser.parseBindings(activity.getToGroupIds(), "toGroupIds");

    ccEmailAddresses = parser.parseBindings(activity.getCcEmailAddresses(), "ccEmailAddresses");
    ccUserIds = parser.parseBindings(activity.getCcUserIds(), "ccUserIds");
    ccGroupIds = parser.parseBindings(activity.getCcGroupIds(), "ccGroupIds");

    bccEmailAddresses = parser.parseBindings(activity.getBccEmailAddresses(), "bccEmailAddresses");
    bccUserIds = parser.parseBindings(activity.getBccUserIds(), "bccUserIds");
    bccGroupIds = parser.parseBindings(activity.getBccGroupIds(), "bccGroupIds");
    
    subject = parser.parseTextTemplate(activity.getSubject(), Hint.EMAIL, Hint.EMAIL_SUBJECT, Hint.SHORT);
    bodyText = parser.parseTextTemplate(activity.getBodyText(), Hint.EMAIL, Hint.EMAIL_BODY_TEXT);
    bodyHtml = parser.parseTextTemplate(activity.getBodyHtml(), Hint.EMAIL, Hint.EMAIL_BODY_HTML, Hint.HTML);
    
    attachments = parser.parseBindings(activity.getAttachments(), "attachments");
  }

  @Override
  public EmailTask readBpmn(XmlElement xml, BpmnReader reader) {
    if (!reader.isLocalPart(xml, BPMN_ELEMENT_NAME) || !reader.hasServiceTaskType(xml, ServiceTaskType.EMAIL)) {
      return null;
    }
    EmailTask task = new EmailTask();
    task.setSubject(reader.readStringValue(xml, "subject"));
    task.setBodyText(reader.readStringValue(xml, "bodyText"));
    task.setBodyHtml(reader.readStringValue(xml, "bodyHtml"));

    task.setToEmailAddresses(reader.readBindings(String.class, TextType.INSTANCE, xml, "to"));
    task.setToGroupIds(reader.readBindings(GroupId.class, GroupIdType.INSTANCE, xml, "to"));
    task.setToUserIds(reader.readBindings(UserId.class, UserIdType.INSTANCE, xml, "to"));

    task.setCcEmailAddresses(reader.readBindings(String.class, TextType.INSTANCE, xml, "cc"));
    task.setCcGroupIds(reader.readBindings(GroupId.class, GroupIdType.INSTANCE, xml, "cc"));
    task.setCcUserIds(reader.readBindings(UserId.class, UserIdType.INSTANCE, xml, "cc"));

    task.setBccEmailAddresses(reader.readBindings(String.class, TextType.INSTANCE, xml, "bcc"));
    task.setBccGroupIds(reader.readBindings(GroupId.class, GroupIdType.INSTANCE, xml, "bcc"));
    task.setBccUserIds(reader.readBindings(UserId.class, UserIdType.INSTANCE, xml, "bcc"));
    return task;
  }

  @Override
  public void writeBpmn(EmailTask task, XmlElement xml, BpmnWriter writer) {
    writer.setBpmnName(xml, BPMN_ELEMENT_NAME);
    writer.writeBpmnAttribute(xml, "id", task.getId());
    writer.writeEffektifType(xml, ServiceTaskType.EMAIL);
    writer.writeStringValue(xml, "subject", task.getSubject());
    writer.writeStringValueAsText(xml, "bodyText", task.getBodyText());
    writer.writeStringValueAsCData(xml, "bodyHtml", task.getBodyHtml());

    writer.writeBindings(xml, "to", (List) task.getToEmailAddresses(), TextType.INSTANCE);
    writer.writeBindings(xml, "to", (List) task.getToGroupIds(), GroupIdType.INSTANCE);
    writer.writeBindings(xml, "to", (List) task.getToUserIds(), UserIdType.INSTANCE);

    writer.writeBindings(xml, "cc", (List) task.getCcEmailAddresses(), TextType.INSTANCE);
    writer.writeBindings(xml, "cc", (List) task.getCcGroupIds(), GroupIdType.INSTANCE);
    writer.writeBindings(xml, "cc", (List) task.getCcUserIds(), UserIdType.INSTANCE);

    writer.writeBindings(xml, "bcc", (List) task.getBccEmailAddresses(), TextType.INSTANCE);
    writer.writeBindings(xml, "bcc", (List) task.getBccGroupIds(), GroupIdType.INSTANCE);
    writer.writeBindings(xml, "bcc", (List) task.getBccUserIds(), UserIdType.INSTANCE);
  }

  @Override
  public void execute(ActivityInstanceImpl activityInstance) {
    List<String> to = resolveEmailAddresses(toEmailAddresses, toUserIds, toGroupIds, activityInstance);
    List<String> cc = resolveEmailAddresses(ccEmailAddresses, ccUserIds, ccGroupIds, activityInstance);
    List<String> bcc = resolveEmailAddresses(bccEmailAddresses, bccUserIds, bccGroupIds, activityInstance);
    
    Email email = new Email()
      .from(resolveFrom(activityInstance))
      .to(to)
      .cc(cc)
      .bcc(bcc)
      .subject(resolve(subject, activityInstance))
      .bodyText(resolve(bodyText, activityInstance))
      .bodyHtml(resolve(bodyHtml, activityInstance));
    email.setAttachments(activityInstance.getValues(attachments));
    
    emailService.send(email);
    
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
          List<BindingImpl<UserId>> userIdBindings,
          List<BindingImpl<GroupId>> groupIdBindings, 
          ActivityInstanceImpl activityInstance) {
    
    List<String> allEmailAddresses = new ArrayList<>();
    List<String> emailAddresses = activityInstance.getValues(emailAddressBindings);
    addEmailAddresses(allEmailAddresses, emailAddresses);
    
    List<UserId> userIds = activityInstance.getValues(userIdBindings);
    if (userIds!=null && !userIds.isEmpty()) {
      emailAddresses = identityService.getUsersEmailAddresses(userIds);
      addEmailAddresses(allEmailAddresses, emailAddresses);
    }

    List<GroupId> groupIds = activityInstance.getValues(groupIdBindings);
    if (groupIds!=null && !groupIds.isEmpty()) {
      emailAddresses = identityService.getGroupsEmailAddresses(groupIds);
      addEmailAddresses(allEmailAddresses, emailAddresses);
    }
    
    return allEmailAddresses;
  }

  protected void addEmailAddresses(List<String> allEmailAddresses, List<String> emailAddresses) {
    if (emailAddresses!=null) {
      for (String emailAddress: emailAddresses) {
        String validatedEmailAddress = emailService.validate(emailAddress);
        if (validatedEmailAddress!=null) {
          allEmailAddresses.add(validatedEmailAddress);
        }
      }
    }
  }
}
