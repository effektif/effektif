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
package com.effektif.workflow.impl.identity;

import java.util.ArrayList;
import java.util.List;

import com.effektif.workflow.api.model.GroupId;
import com.effektif.workflow.api.model.UserId;


/**
 * @author Tom Baeyens
 */
public class Group {

  protected GroupId id;

  public GroupId getId() {
    return this.id;
  }
  public void setId(GroupId id) {
    this.id = id;
  }
  public Group id(GroupId id) {
    this.id = id;
    return this;
  }
  public Group id(String id) {
    this.id = new GroupId(id);
    return this;
  }
  
  protected String name;

  public String getName() {
    return this.name;
  }
  public void setName(String name) {
    this.name = name;
  }
  public Group name(String name) {
    this.name = name;
    return this;
  }
  
  protected List<UserId> memberIds;

  public List<UserId> getMemberIds() {
    return this.memberIds;
  }
  public void setMemberIds(List<UserId> memberIds) {
    this.memberIds = memberIds;
  }
  public Group member(User member) {
    if (this.memberIds==null) {
      this.memberIds = new ArrayList<>();
    }
    this.memberIds.add(member.id);
    return this;
  }
}
