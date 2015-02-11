/* Copyright (c) 2014, Effektif GmbH.
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
 * limitations under the License. */
package com.effektif.workflow.api.activities;

import java.util.ArrayList;
import java.util.List;

import com.effektif.workflow.api.workflow.Activity;
import com.effektif.workflow.api.workflow.Binding;
import com.fasterxml.jackson.annotation.JsonTypeName;


@JsonTypeName("email")
public class EmailTask extends Activity {

  protected List<Binding> toBindings;
  protected List<Binding> ccBindings;
  protected List<Binding> bccBindings;
  protected Binding subjectBinding;
  protected Binding bodyTextBinding;
  protected Binding bodyHtmlBinding;
  protected Binding attachmentBindings;
  
  public EmailTask to(String toEmailAddress) {
    addToBinding(new Binding().value(toEmailAddress));
    return this;
  }
  
  public EmailTask toVariableId(String toEmailAddressVariableId) {
    addToBinding(new Binding().variableId(toEmailAddressVariableId));
    return this;
  }
  
  public EmailTask toExpression(String toEmailAddressExpression) {
    addToBinding(new Binding().expression(toEmailAddressExpression));
    return this;
  }
  
  protected void addToBinding(Binding toBinding) {
    if (toBindings==null) {
      toBindings = new ArrayList<>();
    }
    toBindings.add(toBinding);
  }
  
  public List<Binding> getToBindings() {
    return toBindings;
  }
  
  public void setToBindings(List<Binding> toBindings) {
    this.toBindings = toBindings;
  }
  
  public EmailTask cc(String ccEmailAddress) {
    addCcBinding(new Binding().value(ccEmailAddress));
    return this;
  }
  
  public EmailTask ccVariableId(String ccEmailAddressVariableId) {
    addCcBinding(new Binding().variableId(ccEmailAddressVariableId));
    return this;
  }
  
  public EmailTask ccExpression(String ccEmailAddressExpression) {
    addCcBinding(new Binding().expression(ccEmailAddressExpression));
    return this;
  }
  
  protected void addCcBinding(Binding ccBinding) {
    if (ccBindings==null) {
      ccBindings = new ArrayList<>();
    }
    ccBindings.add(ccBinding);
  }
  
  public List<Binding> getCcBindings() {
    return ccBindings;
  }
  
  public void setCcBindings(List<Binding> ccBindings) {
    this.ccBindings = ccBindings;
  }
  
  public EmailTask bcc(String bccEmailAddress) {
    addBccBinding(new Binding().value(bccEmailAddress));
    return this;
  }
  
  public EmailTask bccVariableId(String bccEmailAddressVariableId) {
    addBccBinding(new Binding().variableId(bccEmailAddressVariableId));
    return this;
  }
  
  public EmailTask bccExpression(String bccEmailAddressExpression) {
    addBccBinding(new Binding().expression(bccEmailAddressExpression));
    return this;
  }
  
  protected void addBccBinding(Binding bccBinding) {
    if (bccBindings==null) {
      bccBindings = new ArrayList<>();
    }
    bccBindings.add(bccBinding);
  }
  
  public List<Binding> getBccBindings() {
    return bccBindings;
  }
  
  public void setBccBindings(List<Binding> bccBindings) {
    this.bccBindings = bccBindings;
  }
  
  public EmailTask subject(String subject) {
    setSubjectBinding(new Binding().value(subject));
    return this;
  }
  
  public EmailTask subjectVariableId(String subjectVariableId) {
    setSubjectBinding(new Binding().variableId(subjectVariableId));
    return this;
  }
  
  public EmailTask subjectExpression(String subjectExpression) {
    setSubjectBinding(new Binding().expression(subjectExpression));
    return this;
  }

  public Binding getSubjectBinding() {
    return subjectBinding;
  }
  
  public void setSubjectBinding(Binding subjectBinding) {
    this.subjectBinding = subjectBinding;
  }
  
  public EmailTask bodyText(String bodyText) {
    setBodyTextBinding(new Binding().value(bodyText));
    return this;
  }
  
  public EmailTask bodyTextVariableId(String bodyTextVariableId) {
    setBodyTextBinding(new Binding().variableId(bodyTextVariableId));
    return this;
  }
  
  public EmailTask bodyTextExpression(String bodyTextExpression) {
    setBodyTextBinding(new Binding().expression(bodyTextExpression));
    return this;
  }

  public Binding getBodyTextBinding() {
    return bodyTextBinding;
  }
  
  public void setBodyTextBinding(Binding bodyTextBinding) {
    this.bodyTextBinding = bodyTextBinding;
  }

  public EmailTask bodyHtml(String bodyHtml) {
    setBodyHtmlBinding(new Binding().value(bodyHtml));
    return this;
  }
  
  public EmailTask bodyHtmlVariableId(String bodyHtmlVariableId) {
    setBodyHtmlBinding(new Binding().variableId(bodyHtmlVariableId));
    return this;
  }
  
  public EmailTask bodyHtmlExpression(String bodyHtmlExpression) {
    setBodyHtmlBinding(new Binding().expression(bodyHtmlExpression));
    return this;
  }

  public Binding getBodyHtmlBinding() {
    return bodyHtmlBinding;
  }
  
  public void setBodyHtmlBinding(Binding bodyHtmlBinding) {
    this.bodyHtmlBinding = bodyHtmlBinding;
  }
  
  public Binding getAttachmentBindings() {
    return attachmentBindings;
  }

  public void setAttachmentBindings(Binding attachmentBindings) {
    this.attachmentBindings = attachmentBindings;
  }
}
