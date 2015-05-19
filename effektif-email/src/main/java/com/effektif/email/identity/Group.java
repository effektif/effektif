/*
 * Copyright (c) 2013, Effektif GmbH.  All rights reserved.
 */
package com.effektif.email.identity;

import java.util.ArrayList;
import java.util.List;

import com.effektif.workflow.api.deprecated.model.GroupId;
import com.effektif.workflow.api.deprecated.model.UserId;


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
