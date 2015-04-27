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
package com.effektif.workflow.impl.email;

import com.effektif.workflow.api.mapper.BpmnReader;
import com.effektif.workflow.api.mapper.BpmnWriter;
import com.effektif.workflow.api.mapper.TypeName;
import com.effektif.workflow.api.workflow.Trigger;

/**
 * Starts a workflow as a result of receiving an email, instead of starting a workflow directly and setting workflow
 * variables for initial data.
 *
 * <p>When you use an email trigger, you provide either an {@link com.effektif.workflow.api.model.EmailId} for a
 * previously-stored email, or an actual {@link com.effektif.workflow.impl.email.Email}, which will be stored and made
 * available via an <code>EmailId</code>.
 *
 * @author Peter Hilton
 */
@TypeName("email")
public class EmailTrigger extends Trigger {

  public static final String EMAIL_ID_KEY = "emailId";

  /** Optional variable that specifies the name to use for the process variable, instead of <code>EMAIL_ID_KEY</code>. */
  protected String emailIdVariableId;

  /**
   * Returns the variable ID to use to look up the {@link com.effektif.workflow.api.model.EmailId},
   * using {@link #EMAIL_ID_KEY} as a default value.
   */
  public String getEmailIdVariableId() {
    return emailIdVariableId == null ? EMAIL_ID_KEY : emailIdVariableId;
  }
  public void setEmailIdVariableId(String emailIdVariableId) {
    this.emailIdVariableId = emailIdVariableId;
  }
  public EmailTrigger emailIdVariableId(String emailIdVariableId) {
    this.emailIdVariableId = emailIdVariableId;
    return this;
  }

  @Override
  public void readBpmn(BpmnReader r) {
    emailIdVariableId = r.readStringAttributeEffektif("emailIdVariableId");
  }

  @Override
  public void writeBpmn(BpmnWriter w) {
    w.startElementEffektif("trigger");
    w.writeTypeAttribute(this);
    if (emailIdVariableId != null) {
      w.writeStringAttributeEffektif("emailIdVariableId", emailIdVariableId);
    }
    w.endElement();
  }
}
