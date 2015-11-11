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
package com.effektif.mongo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.effektif.workflow.impl.configuration.Brewable;
import com.effektif.workflow.impl.configuration.Brewery;
import com.mongodb.DB;


public class MongoDb implements Brewable {
  
  public static final Logger log = LoggerFactory.getLogger(MongoDb.class);

  public static final String _ID = "_id";

  protected DB db;
  protected boolean isPretty;
  
  @Override
  public void brew(Brewery brewery) {
    this.db = brewery.get(DB.class);
    this.isPretty = brewery.get(MongoConfiguration.class).isPretty;
  }
  
  public MongoCollection createCollection(String collectionName) {
    return new MongoCollection(db.getCollection(collectionName), isPretty);
  }

  public DB getDb() {
    return this.db;
  }
  public void setDb(DB db) {
    this.db = db;
  }
  
  public boolean isPretty() {
    return this.isPretty;
  }
  public void setPretty(boolean isPretty) {
    this.isPretty = isPretty;
  }
}
