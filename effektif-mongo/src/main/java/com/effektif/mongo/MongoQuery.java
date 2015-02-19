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
package com.effektif.mongo;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bson.types.ObjectId;

import com.effektif.workflow.api.acl.Access;
import com.effektif.workflow.api.acl.AccessIdentity;
import com.effektif.workflow.api.acl.Authorization;
import com.effektif.workflow.api.acl.Authorizations;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;


public class MongoQuery {
  
  public BasicDBObject query = new BasicDBObject();
  
  public MongoQuery _id(ObjectId id) {
    query.append("_id", id);
    return this;
  }

  public MongoQuery organizationId(ObjectId organizationId) {
    query.append("organizationId", organizationId);
    return this;
  }
  
  public MongoQuery equal(String fieldName, Object dbValue) {
    query.put(fieldName, dbValue);
    return this;
  }

  public MongoQuery gt(String fieldName, Object dbValue) {
    query.put(fieldName, new BasicDBObject("$gt", dbValue));
    return this;
  }

  public MongoQuery access(String... actions) {
    Authorization authorization = Authorizations.current();
    if (authorization!=null) {
      String organizationId = authorization.getOrganizationId();
      String actorId = authorization.getActorId();
      List<String> groupIds = authorization.getGroupIds();
      Set<String> identityIds = new HashSet<>();
      if (organizationId != null) {
        identityIds.add(organizationId);
      }
      if (actorId != null) {
        identityIds.add(actorId);
      }
      if (groupIds != null) {
        identityIds.addAll(groupIds);
      }
      BasicDBList or = new BasicDBList();
      or.add(new BasicDBObject("access", new BasicDBObject("$exists", false)));
      if (identityIds != null && !identityIds.isEmpty()) {
        for (String action : actions) {
          or.add(new BasicDBObject("access." + action + ".id", new BasicDBObject("$in", identityIds)));
        }
      }
      query.append("$or", or);
    }
    return this;
  }
  
  public MongoQuery doesNotExist(String field) {
    query.append(field, new BasicDBObject("$exists", false));
    return this;
  }
  
  public MongoQuery exists(String field) {
    query.append(field, new BasicDBObject("$exists", true));
    return this;
  }
  
  public BasicDBObject get() {
    return query;
  }
}
