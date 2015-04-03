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

import org.bson.types.ObjectId;
import org.slf4j.Logger;

import com.effektif.mongo.MongoFileService.FieldsFile;
import com.effektif.workflow.api.acl.Authentication;
import com.effektif.workflow.api.acl.Authentications;
import com.effektif.workflow.api.model.EmailId;
import com.effektif.workflow.impl.configuration.Brewable;
import com.effektif.workflow.impl.configuration.Brewery;
import com.effektif.workflow.impl.email.EmailStore;
import com.effektif.workflow.impl.email.PersistentEmail;
import com.effektif.workflow.impl.mapper.deprecated.JsonService;
import com.mongodb.BasicDBObject;


/**
 * @author Tom Baeyens
 */
public class MongoEmailStore implements EmailStore, Brewable {

  public static final Logger log = MongoDb.log;
  
  public MongoCollection emailsCollection;
  public MongoMapper<PersistentEmail> mongoMapper;
  
  public interface FieldsEmail {
    String _ID = "_id";
    String ORGANIZATION_ID = "organizationId";
  }

  @Override
  public void brew(Brewery brewery) {
    MongoDb mongoDb = brewery.get(MongoDb.class);
    MongoConfiguration mongoConfiguration = brewery.get(MongoConfiguration.class);
    this.emailsCollection = mongoDb.createCollection(mongoConfiguration.getFilesCollectionName());
    JsonService jsonService = brewery.get(JsonService.class);
    this.mongoMapper = new MongoMapper<>(PersistentEmail.class, jsonService)
        .convertId();
  }
  
  @Override
  public void insertEmail(PersistentEmail email) {
    if (email!=null) {
      BasicDBObject dbFile = mongoMapper.write(email);
      emailsCollection.insert("insert-email", dbFile);
      ObjectId id = (ObjectId) dbFile.get("_id");
      email.setId(new EmailId(id.toString()));
    }
  }

  @Override
  public PersistentEmail findEmailById(EmailId emailId) {
    Authentication authentication = Authentications.current();
    String organizationId = authentication!=null ? authentication.getOrganizationId() : null;
  
    BasicDBObject query = new MongoQuery()
      ._id(new ObjectId(emailId.getInternal()))
      .equalOpt(FieldsFile.ORGANIZATION_ID, organizationId)
      .get();
  
    BasicDBObject dbFile = emailsCollection.findOne("get-email", query);
  
    return mongoMapper.read(dbFile);
  }
}
