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
package com.effektif.workflow.api.acl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.effektif.workflow.api.mapper.JsonReadable;
import com.effektif.workflow.api.mapper.JsonReader;
import com.effektif.workflow.api.mapper.JsonWritable;
import com.effektif.workflow.api.mapper.JsonWriter;


/** Specifies which actions are permitted by whom on a given entity.
 *  
 * @author Tom Baeyens 
 */
public class AccessControlList implements JsonWritable, JsonReadable {
  
  /** maps actions to lists of identities */
  protected Map<String,List<AccessIdentity>> permissions;

  @Override
  public void readJson(JsonReader r) {
    permissions = r.readMap("permissions");
  }
  
  @Override
  public void writeJson(JsonWriter w) {
    w.writeMap("permissions", permissions);
  }
  
  public Map<String,List<AccessIdentity>> getPermissions() {
    return this.permissions;
  }
  public void setPermissions(Map<String,List<AccessIdentity>> identitiesByAction) {
    this.permissions = identitiesByAction;
  }
  
  public AccessControlList permissions(Map<String,List<AccessIdentity>> identitiesByAction) {
    this.permissions = identitiesByAction;
    return this;
  }
  /** grants the identity permission for 'action'.
   * Only adds the permission if it is not already included. 
   * For action values, see {@link Access}. */
  public AccessControlList permission(AccessIdentity identity, String action) {
    if (permissions==null) {
      permissions = new HashMap<>();
    }
    List<AccessIdentity> identities = permissions.get(action);
    if (identities==null) {
      identities = new ArrayList<>();
      permissions.put(action, identities);
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
    List<AccessIdentity> identitiesHavingAction = permissions.get(action);
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
    return permissions.get(action);
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
    if (permissions==null) {
      permissions = new HashMap<>();
    }
    permissions.put(action, identities);
  }
  
  public boolean isEmpty() {
    return permissions==null || permissions.isEmpty();
  }
}