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
package com.effektif.workflow.impl.email;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.effektif.workflow.api.model.Attachment;
import com.effektif.workflow.api.model.EmailId;


/** used for both sending emails {@link EmailService} as 
 * well as storing emails ({@link EmailStore}.
 * 
 * When sending emails, properties id and organizationId are ignored. 
 *  
 * @author Tom Baeyens
 */
public class Email {

  protected EmailId id;
  protected String organizationId;
  protected String from;
  protected String replyTo;
  protected Map<String,String> headers;
  protected List<String> to;
  protected List<String> cc;
  protected List<String> bcc;
  protected String subject;
  protected String bodyText;
  protected String bodyHtml;
  protected List<Attachment> attachments;

  public EmailId getId() {
    return this.id;
  }
  public void setId(EmailId id) {
    this.id = id;
  }

  public String getOrganizationId() {
    return this.organizationId;
  }
  public void setOrganizationId(String organizationId) {
    this.organizationId = organizationId;
  }
  
  public String getFrom() {
    return this.from;
  }
  public void setFrom(String from) {
    this.from = from;
  }
  public Email from(String from) {
    this.from = from;
    return this;
  }

  public String getReplyTo() {
    return this.replyTo;
  }
  public void setReplyTo(String replyTo) {
    this.replyTo = replyTo;
  }
  public Email replyTo(String replyTo) {
    this.replyTo = replyTo;
    return this;
  }

  public Map<String,String> getHeaders() {
    return this.headers;
  }
  public void setHeaders(Map<String,String> headers) {
    this.headers = headers;
  }
  public Email headers(String headerName, String headerValue) {
    if (headers==null) {
      headers = new HashMap<>();
    }
    headers.put(headerName, headerValue);
    return this;
  }

  public List<String> getTo() {
    return this.to;
  }
  public void setTo(List<String> to) {
    this.to = to;
  }
  public Email to(List<String> to) {
    this.to = to;
    return this;
  }
  
  public List<String> getCc() {
    return this.cc;
  }
  public void setCc(List<String> cc) {
    this.cc = cc;
  }
  public Email cc(List<String> cc) {
    this.cc = cc;
    return this;
  }
  
  public List<String> getBcc() {
    return this.bcc;
  }
  public void setBcc(List<String> bcc) {
    this.bcc = bcc;
  }
  public Email bcc(List<String> bcc) {
    this.bcc = bcc;
    return this;
  }
  
  public String getSubject() {
    return this.subject;
  }
  public void setSubject(String subject) {
    this.subject = subject;
  }
  public Email subject(String subject) {
    this.subject = subject;
    return this;
  }
  
  public String getBodyText() {
    return this.bodyText;
  }
  public void setBodyText(String bodyText) {
    this.bodyText = bodyText;
  }
  public Email bodyText(String bodyText) {
    this.bodyText = bodyText;
    return this;
  }
  
  public String getBodyHtml() {
    return this.bodyHtml;
  }
  public void setBodyHtml(String bodyHtml) {
    this.bodyHtml = bodyHtml;
  }
  public Email bodyHtml(String bodyHtml) {
    this.bodyHtml = bodyHtml;
    return this;
  }
  
  public List<Attachment> getAttachments() {
    return this.attachments;
  }
  public void setAttachments(List<Attachment> attachments) {
    this.attachments = attachments;
  }
  public void addAttachment(Attachment attachment) {
    if (this.attachments==null) {
      this.attachments = new ArrayList<>();
    }
    this.attachments.add(attachment);
  }
  public Email attachment(Attachment attachment) {
    addAttachment(attachment);
    return this;
  }
}
