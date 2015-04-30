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
package com.effektif.workflow.api.deprecated.types;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import com.effektif.workflow.api.deprecated.model.GroupId;
import com.effektif.workflow.api.deprecated.model.UserId;
import com.effektif.workflow.api.json.TypeName;
import com.effektif.workflow.api.types.DataType;


/**
 * @author Tom Baeyens
 */
@TypeName("userId")
public class UserIdType extends DataType {

  public static final UserIdType INSTANCE = new UserIdType();
  
  protected List<UserId> candidateIds;
  protected List<GroupId> candidateGroupIds;
  
  public List<UserId> getCandidateIds() {
    return this.candidateIds;
  }
  public void setCandidateIds(List<UserId> candidateIds) {
    this.candidateIds = candidateIds;
  }
  public UserIdType candidateId(String candidateId) {
    return this.candidateId(new UserId(candidateId));
  }
  /** candidates extend the userId variable type to become a process role.
   * You can bind the user task assignee to a userId variable and use 
   * this field to specify the candidates. */
  public UserIdType candidateId(UserId candidateId) {
    if (candidateIds==null) {
      candidateIds = new ArrayList<>();
    }
    candidateIds.add(candidateId);
    return this;
  }
  
  public List<GroupId> getCandidateGroupIds() {
    return this.candidateGroupIds;
  }
  public void setCandidateGroupIds(List<GroupId> candidateGroupIds) {
    this.candidateGroupIds = candidateGroupIds;
  }
  /** candidates extend the userId variable type to become a process role.
   * You can bind the user task assignee to a userId variable and use 
   * this field to specify the candidates. */
  public UserIdType candidateGroupId(GroupId candidateGroupId) {
    if (candidateGroupIds==null) {
      candidateGroupIds = new ArrayList<>();
    }
    candidateGroupIds.add(candidateGroupId);
    return this;
  }
  
  @Override
  public Type getValueType() {
    return UserId.class;
  }
}
