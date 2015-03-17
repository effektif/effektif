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
import com.effektif.workflow.api.model.GroupId;
import com.effektif.workflow.api.types.GroupIdType;
import com.effektif.workflow.api.workflow.Binding;
import com.effektif.workflow.api.xml.XmlElement;
import com.effektif.workflow.impl.data.AbstractDataType;
import com.effektif.workflow.impl.data.TypedValueImpl;
import com.effektif.workflow.impl.identity.Group;
import com.effektif.workflow.impl.identity.IdentityService;


/**
 * @author Tom Baeyens
 */
public class GroupIdTypeImpl extends AbstractDataType<GroupIdType> {

  public GroupIdTypeImpl() {
    initialize(new GroupIdType(), GroupId.class, configuration);
  }

  public void initialize(Configuration configuration) {
    initialize(new GroupIdType(), GroupId.class, configuration);
  }

  public GroupIdTypeImpl(GroupIdType type, Configuration configuration) {
    initialize(type, GroupId.class, configuration);
  }

  @Override
  public TypedValueImpl dereference(Object value, String fieldName) {
    GroupId groupId = (GroupId) value;
    IdentityService identityService = configuration.get(IdentityService.class);
    Group group = groupId!=null ? identityService.findGroupById(groupId) : null;
    if ("*".equals(fieldName)) {
      return new TypedValueImpl(new GroupTypeImpl(configuration), group);
    }
    return new GroupTypeImpl(configuration).dereference(group, fieldName);
  }

  @Override
  public Binding readValue(XmlElement xml) {
    String value = readStringValue(xml, "groupId");
    return value == null ? null : new Binding().value(new GroupId(value));
  }

  @Override
  public void writeValue(XmlElement xml, Object value) {
    if (value != null) {
      xml.addAttribute("groupId", value.toString());
    }
  }
}
