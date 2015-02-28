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
import com.effektif.workflow.api.model.UserId;
import com.effektif.workflow.api.types.UserIdType;
import com.effektif.workflow.api.workflow.Binding;
import com.effektif.workflow.api.xml.XmlElement;
import com.effektif.workflow.impl.data.InvalidValueException;
import com.effektif.workflow.impl.data.TypedValueImpl;
import com.effektif.workflow.impl.identity.IdentityService;
import com.effektif.workflow.impl.identity.User;



/**
 * @author Tom Baeyens
 */
public class UserIdTypeImpl extends JavaBeanTypeImpl<UserIdType> {

  public UserIdTypeImpl(Configuration configuration) {
    this(new UserIdType(), configuration);
  }

  public UserIdTypeImpl(UserIdType type, Configuration configuration) {
    super(type, configuration);
  }

  @Override
  public Object convertJsonToInternalValue(Object jsonValue) throws InvalidValueException {
    return jsonValue!=null ? new UserId((String)jsonValue) : null;
  }

  @Override
  public Object convertInternalToJsonValue(Object internalValue) {
    return internalValue!=null ? ((UserId)internalValue).getId() : null;
  }
  
  @Override
  public TypedValueImpl dereference(Object value, String fieldName) {
    if ("user".equals(fieldName)) {
      UserId userId = (UserId) value;
      IdentityService identityService = configuration.get(IdentityService.class);
      User user = identityService.getUser(userId);
      return new TypedValueImpl(new UserTypeImpl(configuration), user);
    }
    return null;
  }

  @Override
  public Binding readValue(XmlElement xml) {
    if (xml == null) {
      throw new IllegalArgumentException("null argument to method");
    }
    String value = xml.attributes.get("userId");
    return value == null ? null : new Binding().value(new UserId(value.toString()));
  }

  @Override
  public void writeValue(XmlElement xml, Object value) {
    if (value != null) {
      xml.addAttribute("userId", value.toString());
    }
  }
}
