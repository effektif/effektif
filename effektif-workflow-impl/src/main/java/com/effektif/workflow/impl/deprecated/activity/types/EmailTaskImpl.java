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
package com.effektif.workflow.impl.deprecated.activity.types;

import java.util.ArrayList;
import java.util.List;

import com.effektif.workflow.api.deprecated.activities.EmailTask;
import com.effektif.workflow.api.deprecated.model.Attachment;
import com.effektif.workflow.api.deprecated.model.FileId;
import com.effektif.workflow.api.deprecated.model.GroupId;
import com.effektif.workflow.api.deprecated.model.UserId;
import com.effektif.workflow.impl.WorkflowParser;
import com.effektif.workflow.impl.activity.AbstractActivityType;
import com.effektif.workflow.impl.deprecated.email.OutgoingEmail;
import com.effektif.workflow.impl.deprecated.email.OutgoingEmailService;
import com.effektif.workflow.impl.deprecated.file.File;
import com.effektif.workflow.impl.deprecated.file.FileAttachment;
import com.effektif.workflow.impl.deprecated.file.FileService;
import com.effektif.workflow.impl.deprecated.identity.IdentityService;
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
  protected IdentityService identityService; 
  protected FileService fileService; 

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
  
  protected List<BindingImpl<FileId>> attachmentFileIds;

  public EmailTaskImpl() {
    super(EmailTask.class);
  }
  
  @Override
  public void execute(ActivityInstanceImpl activityInstance) {
    List<String> to = resolveEmailAddresses(toEmailAddresses, toUserIds, toGroupIds, activityInstance);
    List<String> cc = resolveEmailAddresses(ccEmailAddresses, ccUserIds, ccGroupIds, activityInstance);
    List<String> bcc = resolveEmailAddresses(bccEmailAddresses, bccUserIds, bccGroupIds, activityInstance);
    
    OutgoingEmail email = new OutgoingEmail()
      .from(resolveFrom(activityInstance))
      .to(to)
      .cc(cc)
      .bcc(bcc)
      .subject(resolve(subject, activityInstance))
      .bodyText(resolve(bodyText, activityInstance))
      .bodyHtml(resolve(bodyHtml, activityInstance));
    
    List<FileId> fileIds = activityInstance.getValues(attachmentFileIds);
    if (fileIds!=null && !fileIds.isEmpty()) {
      List<File> files = fileService.getFilesByIds(fileIds);
      List<Attachment> attachments = new ArrayList<>();
      for (File file : files) {
        FileAttachment fileAttachment = FileAttachment.createFileAttachment(file, fileService);
        attachments.add(fileAttachment);
      }
      email.setAttachments(attachments);
    }
    
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
    identityService = parser.getConfiguration(IdentityService.class);
    fileService = parser.getConfiguration(FileService.class);

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
    
    attachmentFileIds = parser.parseBindings(activity.getAttachmentFileIds(), "attachmentFileIds");
  }
}
