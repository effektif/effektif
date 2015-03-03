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
package com.effektif.workflow.impl.memory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.effektif.workflow.api.model.GroupId;
import com.effektif.workflow.api.model.UserId;
import com.effektif.workflow.impl.identity.Group;
import com.effektif.workflow.impl.identity.IdentityService;
import com.effektif.workflow.impl.identity.User;


/**
 * @author Tom Baeyens
 */
public class MemoryIdentityService implements IdentityService {
  
  Map<UserId, User> users = new HashMap<>(); 
  Map<GroupId, Group> groups = new HashMap<>(); 

  public User createUser(User user) {
    users.put(user.getId(), user);
    return user;
  }

  public Group createGroup(Group group) {
    groups.put(group.getId(), group);
    return group;
  }

  @Override
  public List<String> getUsersEmailAddresses(List<UserId> userIds) {
    if (userIds==null) {
      return null;
    }
    List<String> emailAddresses = new ArrayList<>();
    for (UserId userId: userIds) {
      User user = users.get(userId.getId());
      emailAddresses.add(user.getEmail());
    }
    return emailAddresses;
  }

  @Override
  public List<String> getGroupsEmailAddresses(List<GroupId> groupIds) {
    if (groupIds==null) {
      return null;
    }
    List<String> emailAddresses = new ArrayList<>();
    for (GroupId groupId: groupIds) {
      Group group = groups.get(groupId);
      List<String> memberEmailAddresses = getUsersEmailAddresses(group.getMemberIds());
      emailAddresses.addAll(memberEmailAddresses);
    }
    return emailAddresses;
  }

  @Override
  public User findUserById(UserId userId) {
    return users.get(userId);
  }

  @Override
  public Group findGroupById(GroupId groupId) {
    return groups.get(groupId);
  }

  public void deleteUsers() {
    users = new HashMap<>(); 
  }
  public void deleteGroups() {
    groups = new HashMap<>(); 
  }
}
