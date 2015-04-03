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
import com.effektif.workflow.api.mapper.XmlElement;
import com.effektif.workflow.api.model.UserId;
import com.effektif.workflow.api.types.UserIdType;
import com.effektif.workflow.api.workflow.Binding;
import com.effektif.workflow.impl.data.AbstractDataType;
import com.effektif.workflow.impl.data.InvalidValueException;
import com.effektif.workflow.impl.data.TypedValueImpl;
import com.effektif.workflow.impl.identity.IdentityService;
import com.effektif.workflow.impl.identity.User;



/**
 * @author Tom Baeyens
 */
public class UserIdTypeImpl extends AbstractDataType<UserIdType> {
  
  protected TextTypeImpl textTypeImpl;
  protected UserTypeImpl userTypeImpl;

  public UserIdTypeImpl() {
    super(UserIdType.INSTANCE, String.class);
  }
  
  @Override
  public void setConfiguration(Configuration configuration) {
    super.setConfiguration(configuration);
    this.textTypeImpl = getSingletonDataType(TextTypeImpl.class);
    this.userTypeImpl = getSingletonDataType(UserTypeImpl.class);
  }
  
  @Override
  public Object convertJsonToInternalValue(Object jsonValue) throws InvalidValueException {
    return jsonValue!=null ? new UserId((String)jsonValue) : null;
  }

  @Override
  public Object convertInternalToJsonValue(Object internalValue) {
    return internalValue!=null ? ((UserId)internalValue).getInternal() : null;
  }
  
  @Override
  public TypedValueImpl dereference(Object value, String fieldName) {
    UserId userId = (UserId) value;
    if ("id".equals(fieldName)) {
      return new TypedValueImpl(textTypeImpl, userId.getInternal());
    }
    IdentityService identityService = configuration.get(IdentityService.class);
    User user = userId!=null ? identityService.findUserById(userId) : null;
    if ("*".equals(fieldName)) {
      return new TypedValueImpl(userTypeImpl, user);
    }
    return userTypeImpl.dereference(user, fieldName);
  }

  @Override
  public Binding readValue(XmlElement xml) {
    String value = readStringValue(xml, "userId");
    return value == null ? null : new Binding().value(new UserId(value));
  }

  @Override
  public void writeValue(XmlElement xml, Object value) {
    if (value != null) {
      xml.addAttribute("userId", value.toString());
    }
  }
}
