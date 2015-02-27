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
import com.effektif.workflow.api.ref.GroupId;
import com.effektif.workflow.api.types.GroupIdType;
import com.effektif.workflow.api.workflow.Binding;
import com.effektif.workflow.api.xml.XmlElement;


/**
 * @author Tom Baeyens
 */
public class GroupIdTypeImpl extends JavaBeanTypeImpl<GroupIdType> {

  public GroupIdTypeImpl(Configuration configuration) {
    this(new GroupIdType(), configuration);
  }

  public GroupIdTypeImpl(GroupIdType type, Configuration configuration) {
    super(type, configuration);
  }

  @Override
  public Binding readValue(XmlElement xml) {
    if (xml == null) {
      throw new IllegalArgumentException("null argument to method");
    }
    String value = xml.attributes.get("groupId");
    return value == null ? null : new Binding().value(new GroupId(value.toString()));
  }

  @Override
  public void writeValue(XmlElement xml, Object value) {
    if (value != null) {
      xml.addAttribute("groupId", value.toString());
    }
  }
}
