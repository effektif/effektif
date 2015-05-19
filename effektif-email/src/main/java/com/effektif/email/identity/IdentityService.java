/*
 * Copyright (c) 2013, Effektif GmbH.  All rights reserved.
 */
package com.effektif.email.identity;

import java.util.List;

import com.effektif.workflow.api.deprecated.model.GroupId;
import com.effektif.workflow.api.deprecated.model.UserId;


/**
 * @author Tom Baeyens
 */
public interface IdentityService {

  User createUser(User user);
  List<String> getUsersEmailAddresses(List<UserId> userIds);
  User findUserById(UserId userId);

  Group createGroup(Group group);
  List<String> getGroupsEmailAddresses(List<GroupId> groupIds);
  Group findGroupById(GroupId groupId);
  List<Group> findGroupByIds(List<GroupId> groupIds);
}
