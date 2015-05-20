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
package com.effektif.email;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMessage.RecipientType;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.effektif.workflow.impl.configuration.Brewery;
import com.effektif.workflow.impl.configuration.Plugin;

/** default impl that sends emails using javax.mail.
 * 
 * @author Tom Baeyens
 */
public class OutgoingEmailServiceImpl implements OutgoingEmailService, Plugin {

  private static final Logger log = LoggerFactory.getLogger(OutgoingEmailServiceImpl.class);
  
  protected Properties properties = new Properties();
  protected Authenticator authenticator = null;
  
  @Override
  public void plugin(Brewery brewery) {
  }

  public OutgoingEmailServiceImpl() {
    properties = new Properties();
    property("mail.transport.protocol", "smtp");
    host("localhost");
    portDefault();
  }
  
  public OutgoingEmailServiceImpl host(String hostName) {
    properties.put("mail.smtp.host", hostName);
    return this;
  }

  public OutgoingEmailServiceImpl port(int port) {
    properties.put("mail.smtp.port", Integer.toString(port));
    return this;
  }
  
  public OutgoingEmailServiceImpl portDefault() {
    port(25);
    return this;
  }

  public OutgoingEmailServiceImpl portDefaultSsl() {
    port(465);
    return this;
  }

  public OutgoingEmailServiceImpl portDefaultTls() {
    port(587);
    return this;
  }

  public OutgoingEmailServiceImpl ssl() {
    portDefaultSsl();
    return this;
  }

  public OutgoingEmailServiceImpl ssl(int port) {
    port(port);
    properties.put("mail.smtp.socketFactory.port", Integer.toString(port));
    properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
    properties.put("mail.smtp.socketFactory.fallback", "false");
    return this;
  }

  public OutgoingEmailServiceImpl tls() {
    portDefaultTls();
    properties.setProperty("mail.smtp.starttls.enable", "true");
    return this;
  }

  public OutgoingEmailServiceImpl authenticate(final String username, final String password) {
    properties.setProperty("mail.smtp.auth", "true");
    this.authenticator = new Authenticator() {
      @Override
      protected PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(username, password);
      }
    };
    return this;
  }
  
  public OutgoingEmailServiceImpl from(String mailSmtpFrom) {
    properties.setProperty("mail.smtp.from", mailSmtpFrom);
    return this;
  }

  public OutgoingEmailServiceImpl timeout(long timeout) {
    properties.put("mail.smtp.timeout", Long.toString(timeout));
    return this;
  }

  public OutgoingEmailServiceImpl connectionTimeoutSeconds(long connectionTimeoutSeconds) {
    if (connectionTimeoutSeconds<=0) {
      throw new RuntimeException("Invalid timeout value "+connectionTimeoutSeconds+". Expected positive value expressed in seconds.");
    }
    properties.put("mail.smtp.connectiontimeout", Long.toString(connectionTimeoutSeconds*1000));
    return this;
  }

  public OutgoingEmailServiceImpl property(String key, String value) {
    properties.put(key, value);
    return this;
  }
  
  @Override
  public String validate(String emailAddress) {
    return validateEmailAddress(emailAddress);
  }

  public static String validateEmailAddress(String emailAddress) {
    if (emailAddress != null) {
      try {
        InternetAddress internetAddress = new InternetAddress(emailAddress);
        return internetAddress.toUnicodeString();
      } catch (AddressException e) {
        log.error("Invalid mail address: " + emailAddress, e);
      }
    }
    return null;
  }

  @Override
  public void send(OutgoingEmail email) {
    try {
      Session session = getSession();
      MimeMessage message = createMessage(session, email);
      if (isValid(email)) {
        log.debug("Sending email to "+email.getTo()+" with "+properties.get("mail.smtp.host"));
        Transport.send(message);
      }
    } catch (MessagingException | IOException e) {
      log.error("Problem sending email: "+e.getMessage());
      if (properties!=null) {
        for (Object key: properties.keySet()) {
          log.error(key+"="+properties.get(key));
        }
      }
      throw new RuntimeException("Problem sending email: "+e.getMessage(), e);
    }
  }

  protected boolean isValid(OutgoingEmail email) {
    if (email.getTo()==null || email.getTo().isEmpty() || email.getTo().contains(null)) {
      log.error("NOT sending mail: no TO recipients specified");
      return false;
    }
    return true;
  }

  protected MimeMessage createMessage(Session session, OutgoingEmail email) throws AddressException, MessagingException, IOException {
    MimeMessage message = new MimeMessage(session);
    Map<String, String> headers = email.getHeaders();
    if (headers!=null) {
      for (String header: headers.keySet()) {
        String value = headers.get(header);
        message.addHeader(header, value);
      }
    }
    if (email.getFrom()!=null) {
      email.setFrom(email.getFrom());
    }
    if (email.getTo()!=null) {
      for (String to: email.getTo()) {
        message.addRecipient(RecipientType.TO, createAddress(to));
      }
    }
    if (email.getCc()!=null) {
      for (String cc: email.getCc()) {
        message.addRecipient(RecipientType.CC, createAddress(cc));
      }
    }
    if (email.getBcc()!=null) {
      for (String bcc: email.getBcc()) {
        message.addRecipient(RecipientType.BCC, createAddress(bcc));
      }
    }
    if (email.getSubject()!=null) {
      message.setSubject(email.getSubject());
    }
    MimeMultipart content = new MimeMultipart();
    if (email.getBodyText()!=null) {
      MimeBodyPart part = createBodyPartText(email.getBodyText());
      content.addBodyPart(part);
    }
    if (email.getBodyHtml()!=null) {
      MimeBodyPart part = createBodyPartHtml(email.getBodyHtml());
      content.addBodyPart(part);
    }
    if (email.getAttachments()!=null) {
      for (Attachment attachment: email.getAttachments()) {
        MimeBodyPart part = createBodyPartAttachment(attachment);
        content.addBodyPart(part);
      }
    }
    message.setContent(content);
    return message;
  }

  protected MimeBodyPart createBodyPartText(String bodyText) throws MessagingException {
    MimeBodyPart bodyPart = new MimeBodyPart();
    bodyPart.setContent(bodyText, "text/plain; charset=utf-8");
    return bodyPart;
  }

  protected MimeBodyPart createBodyPartHtml(String bodyHtml) throws MessagingException {
    MimeBodyPart bodyPart = new MimeBodyPart();
    bodyPart.setContent(bodyHtml, "text/html; charset=utf-8");
    return bodyPart;
  }

  protected MimeBodyPart createBodyPartAttachment(Attachment attachment) throws IOException, MessagingException {
    MimeBodyPart bodyPart = new MimeBodyPart();
    bodyPart.setFileName(attachment.getFileName());
    DataSource dataSource = new ByteArrayDataSource(attachment.getInputStream(), attachment.getContentType());
    bodyPart.setDataHandler(new DataHandler(dataSource));
    return bodyPart;
  }

  protected Address createAddress(String emailAddress) throws AddressException {
    return new InternetAddress(emailAddress);
  }

  public Session getSession() {
    return Session.getInstance(properties, authenticator);
  }
}
