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

import com.effektif.workflow.api.model.EmailId;
import com.effektif.workflow.api.model.TriggerInstance;
import com.effektif.workflow.impl.WorkflowParser;
import com.effektif.workflow.impl.activity.AbstractTriggerImpl;
import com.effektif.workflow.impl.email.EmailTrigger;
import com.effektif.workflow.impl.json.JsonService;
import com.effektif.workflow.impl.workflow.WorkflowImpl;
import com.effektif.workflow.impl.workflowinstance.WorkflowInstanceImpl;

/**
 * @author Peter Hilton
 */
public class EmailTriggerImpl extends AbstractTriggerImpl<EmailTrigger> {

  public String emailIdVariableId;
  public JsonService jsonService;

  public EmailTriggerImpl() {
    super(EmailTrigger.class);
  }

  @Override
  public void parse(WorkflowImpl workflow, EmailTrigger triggerApi, WorkflowParser parser) {
    this.emailIdVariableId = triggerApi.getEmailIdVariableId();
    this.jsonService = parser.getConfiguration(JsonService.class);
  }

  @Override
  public void applyTriggerData(WorkflowInstanceImpl workflowInstance, TriggerInstance triggerInstance, boolean deserialize) {
    Object emailIdObject = triggerInstance.getData(EmailTrigger.EMAIL_ID_KEY);
    if (deserialize) {
      emailIdObject = new EmailId((String)emailIdObject);
    }
    EmailId emailId = (EmailId) emailIdObject;
    if (emailId!=null) {
      workflowInstance.setVariableValue(emailIdVariableId, emailId);
    }
  }
}