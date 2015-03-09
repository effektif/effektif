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
package com.effektif.workflow.impl.data.types;

import com.effektif.workflow.api.Configuration;
import com.effektif.workflow.api.model.EmailId;
import com.effektif.workflow.api.model.FileId;
import com.effektif.workflow.api.types.EmailIdType;
import com.effektif.workflow.api.workflow.Binding;
import com.effektif.workflow.api.xml.XmlElement;
import com.effektif.workflow.impl.data.AbstractDataType;
import com.effektif.workflow.impl.data.InvalidValueException;
import com.effektif.workflow.impl.data.TypedValueImpl;
import com.effektif.workflow.impl.email.Email;
import com.effektif.workflow.impl.email.EmailStore;
import com.effektif.workflow.impl.util.Exceptions;



/**
 * @author Tom Baeyens
 */
public class EmailIdTypeImpl extends AbstractDataType<EmailIdType> {
  
  public EmailIdTypeImpl(Configuration configuration) {
    this(EmailIdType.INSTANCE, configuration);
  }

  public EmailIdTypeImpl(EmailIdType emailIdType, Configuration configuration) {
    super(emailIdType, EmailId.class, configuration);
  }

  @Override
  public Object convertJsonToInternalValue(Object jsonValue) throws InvalidValueException {
    return jsonValue!=null ? new EmailId((String)jsonValue) : null;
  }

  @Override
  public Object convertInternalToJsonValue(Object internalValue) {
    return internalValue!=null ? ((EmailId)internalValue).getId() : null;
  }
  
  @Override
  public TypedValueImpl dereference(Object value, String fieldName) {
    EmailId emailId = (EmailId) value;
    EmailStore emailStore = configuration.get(EmailStore.class);
    Email email = emailId!=null ? emailStore.findEmailById(emailId) : null;
    if ("*".equals(fieldName)) {
      return new TypedValueImpl(new EmailTypeImpl(configuration), email);
    }
    return new EmailTypeImpl(configuration).dereference(email, fieldName);
  }

  @Override
  public Binding readValue(XmlElement xml) {
    Exceptions.checkNotNullParameter(xml, "xml");
    String value = xml.attributes.get("fileId");
    return value == null ? null : new Binding().value(new FileId(value.toString()));
  }

  @Override
  public void writeValue(XmlElement xml, Object value) {
    if (value != null) {
      xml.addAttribute("fileId", value.toString());
    }
  }
}
