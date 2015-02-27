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

import java.util.List;

import com.effektif.workflow.api.activities.EmailTask;
import com.effektif.workflow.api.ref.FileId;
import com.effektif.workflow.api.ref.GroupId;
import com.effektif.workflow.api.ref.UserId;
import com.effektif.workflow.api.types.UserIdType;
import com.effektif.workflow.api.xml.XmlElement;
import com.effektif.workflow.impl.WorkflowParser;
import com.effektif.workflow.impl.activity.AbstractActivityType;
import com.effektif.workflow.impl.activity.InputParameter;
import com.effektif.workflow.impl.bpmn.BpmnReader;
import com.effektif.workflow.impl.bpmn.BpmnWriter;
import com.effektif.workflow.impl.bpmn.ServiceTaskType;
import com.effektif.workflow.impl.template.TextTemplate;
import com.effektif.workflow.impl.workflow.ActivityImpl;
import com.effektif.workflow.impl.workflow.BindingImpl;
import com.effektif.workflow.impl.workflowinstance.ActivityInstanceImpl;


/**
 * @author Tom Baeyens
 */
public class EmailTaskImpl extends AbstractActivityType<EmailTask> {

  private static final String BPMN_ELEMENT_NAME = "serviceTask";
  private static final InputParameter TO_USER_IDS = new InputParameter()
    .list()
    .type(UserIdType.INSTANCE)
    .key("toUserIds");
  
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
  
  protected List<BindingImpl<FileId>> attachments;

  public EmailTaskImpl() {
    super(EmailTask.class);
  }
  
  @Override
  public void parse(ActivityImpl activityImpl, EmailTask activity, WorkflowParser parser) {
    super.parse(activityImpl, activity, parser);
//    toEmailAddresses = parser.parseBindings(activity.getToUserIds(), );
//    toUserIds = parser.parseBindings(activity.getToUserIds(), TO_USER_IDS);
//    toGroupIds = parser.parseBindings(activity.getToUserIds(), TO_USER_IDS);
//    toGroupIds = parser.parseBindings(activity.getToUserIds(), TO_GROUP_IDS);
  }

  @Override
  public EmailTask readBpmn(XmlElement xml, BpmnReader reader) {
    if (!reader.isLocalPart(xml, BPMN_ELEMENT_NAME) || !reader.hasServiceTaskType(xml, ServiceTaskType.EMAIL)) {
      return null;
    }
    EmailTask task = new EmailTask();
    task.id(reader.readBpmnAttribute(xml, "id"));
    return task;
  }

  @Override
  public void writeBpmn(EmailTask task, XmlElement xml, BpmnWriter writer) {
    writer.setBpmnName(xml, BPMN_ELEMENT_NAME);
    writer.writeBpmnAttribute(xml, "id", task.getId());
    writer.writeEffektifType(xml, ServiceTaskType.EMAIL);
  }

  @Override
  public void execute(ActivityInstanceImpl activityInstance) {
  }
}
