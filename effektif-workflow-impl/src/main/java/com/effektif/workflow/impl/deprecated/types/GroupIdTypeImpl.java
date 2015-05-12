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
package com.effektif.workflow.impl.deprecated.types;

import com.effektif.workflow.api.Configuration;
import com.effektif.workflow.api.deprecated.model.GroupId;
import com.effektif.workflow.api.deprecated.types.GroupIdType;
import com.effektif.workflow.impl.data.AbstractDataType;
import com.effektif.workflow.impl.data.InvalidValueException;
import com.effektif.workflow.impl.data.TypedValueImpl;
import com.effektif.workflow.impl.deprecated.identity.Group;
import com.effektif.workflow.impl.deprecated.identity.IdentityService;


/**
 * @author Tom Baeyens
 */
public class GroupIdTypeImpl extends AbstractDataType<GroupIdType> {

  protected GroupTypeImpl groupTypeImpl;
  
  public GroupIdTypeImpl() {
    super(new GroupIdType());
  }

  public void setConfiguration(Configuration configuration) {
    super.setConfiguration(configuration);
    this.groupTypeImpl = getSingletonDataType(GroupTypeImpl.class);
  }
  
  @Override
  public Object convertJsonToInternalValue(Object jsonValue) throws InvalidValueException {
    return jsonValue!=null ? new GroupId((String)jsonValue) : null;
  }

  @Override
  public Object convertInternalToJsonValue(Object internalValue) {
    return internalValue!=null ? ((GroupId)internalValue).getInternal() : null;
  }


  @Override
  public TypedValueImpl dereference(Object value, String fieldName) {
    GroupId groupId = (GroupId) value;
    IdentityService identityService = configuration.get(IdentityService.class);
    Group group = groupId!=null ? identityService.findGroupById(groupId) : null;
    if ("*".equals(fieldName)) {
      return new TypedValueImpl(groupTypeImpl, group);
    }
    return groupTypeImpl.dereference(group, fieldName);
  }
}
