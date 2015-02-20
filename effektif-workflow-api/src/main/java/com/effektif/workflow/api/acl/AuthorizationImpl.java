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
package com.effektif.workflow.api.acl;

import java.util.ArrayList;
import java.util.List;


/**
 * @author Tom Baeyens
 */
public class AuthorizationImpl implements Authorization {

  protected String organizationId;
  protected String actorId;
  protected List<String> groupIds;
  
  public String getAuthorizedActorId() {
    return this.actorId;
  }
  public void setActorId(String actorId) {
    this.actorId = actorId;
  }
  public AuthorizationImpl actorId(String actorId) {
    this.actorId = actorId;
    return this;
  }
  
  public String getAuthorizedOrganizationId() {
    return this.organizationId;
  }
  public void setOrganizationId(String organizationId) {
    this.organizationId = organizationId;
  }
  public AuthorizationImpl organizationId(String organizationId) {
    this.organizationId = organizationId;
    return this;
  }
  
  public List<String> getAuthorizedGroupIds() {
    return this.groupIds;
  }
  public void setGroupIds(List<String> groupIds) {
    this.groupIds = groupIds;
  }
  public AuthorizationImpl groupId(String groupId) {
    if (groupIds==null) {
      groupIds = new ArrayList<>();
    }
    this.groupIds.add(groupId);
    return this;
  };
}
