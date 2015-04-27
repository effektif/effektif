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
package com.effektif.workflow.api.deprecated.acl;

import java.util.ArrayList;
import java.util.List;


/** Default implementation to provide authentication information.
 * 
 * @author Tom Baeyens
 */
public class AuthenticationImpl implements Authentication {

  protected String organizationId;
  protected String userId;
  protected List<String> groupIds;
  
  public String getUserId() {
    return this.userId;
  }
  public void setUserId(String userId) {
    this.userId = userId;
  }
  public AuthenticationImpl actorId(String userId) {
    this.userId = userId;
    return this;
  }
  
  public String getOrganizationId() {
    return this.organizationId;
  }
  public void setOrganizationId(String organizationId) {
    this.organizationId = organizationId;
  }
  public AuthenticationImpl organizationId(String organizationId) {
    this.organizationId = organizationId;
    return this;
  }
  
  public List<String> getGroupIds() {
    return this.groupIds;
  }
  public void setGroupIds(List<String> groupIds) {
    this.groupIds = groupIds;
  }
  public AuthenticationImpl groupId(String groupId) {
    if (groupIds==null) {
      groupIds = new ArrayList<>();
    }
    this.groupIds.add(groupId);
    return this;
  };
}
