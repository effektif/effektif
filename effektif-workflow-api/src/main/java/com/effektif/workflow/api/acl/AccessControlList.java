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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonUnwrapped;


/** Specifies which actions are permitted by whom on a given entity.
 *  
 * @author Tom Baeyens 
 */
public class AccessControlList {
  
  @JsonUnwrapped
  protected Map<String,List<AccessIdentity>> identitiesByAction;

  public Map<String,List<AccessIdentity>> getIdentitiesByAction() {
    return this.identitiesByAction;
  }
  public void setIdentitiesByAction(Map<String,List<AccessIdentity>> identitiesByAction) {
    this.identitiesByAction = identitiesByAction;
  }
  
  public AccessControlList permissions(Map<String,List<AccessIdentity>> identitiesByAction) {
    this.identitiesByAction = identitiesByAction;
    return this;
  }
  /** grants the identity permission for 'action'.
   * Only adds the permission if it is not already included. 
   * For action values, see {@link Access}. */
  public AccessControlList permission(AccessIdentity identity, String action) {
    if (identitiesByAction==null) {
      identitiesByAction = new HashMap<>();
    }
    List<AccessIdentity> identities = identitiesByAction.get(action);
    if (identities==null) {
      identities = new ArrayList<>();
      identitiesByAction.put(action, identities);
    }
    if (!identities.contains(identity)) {
      identities.add(identity);
    }
    return this;
  }
  
  public boolean hasPermission(Authentication authentication, String action) {
    if (authentication==null) {
      return false;
    }
    List<AccessIdentity> identitiesHavingAction = identitiesByAction.get(action);
    if (identitiesHavingAction!=null) {
      String userId = authentication.getUserId();
      if (userId!=null && identitiesHavingAction.contains(new UserIdentity(userId))) {
        return true;
      }
      String organizationId = authentication.getOrganizationId();
      if (organizationId!=null && identitiesHavingAction.contains(new OrganizationIdentity(organizationId))) {
        return true;
      }
      List<String> groupIds = authentication.getGroupIds();
      if (groupIds!=null) {
        for (String groupId: groupIds) {
          if (identitiesHavingAction.contains(new GroupIdentity(groupId))) {
            return true;
          }
        }
      }
    }
    return false;
  }
  
  
  public List<AccessIdentity> getIdentities(String action) {
    return identitiesByAction.get(action);
  }
  
  /** overwrites the access on the task for the action with the given identities if identities!=null. */
  public static void setAccessIdentities(AccessControlledObject accessControlled, String action, List<AccessIdentity> identities) {
    if (identities==null) {
      return;
    }
    AccessControlList access = accessControlled.getAccess();
    if (access==null) {
      access = new AccessControlList();
      accessControlled.setAccess(access);
    }
    access.setIdentities(action, identities);
  }

  public void setIdentities(String action, List<AccessIdentity> identities) {
    if (identitiesByAction==null) {
      identitiesByAction = new HashMap<>();
    }
    identitiesByAction.put(action, identities);
  }
  
  public boolean isEmpty() {
    return identitiesByAction==null || identitiesByAction.isEmpty();
  }
}