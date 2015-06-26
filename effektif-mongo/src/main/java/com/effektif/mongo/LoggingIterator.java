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

import java.util.Iterator;

import org.slf4j.Logger;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;


public class LoggingIterator implements Iterator<BasicDBObject> {
  
  public static final Logger log = MongoDb.log;

  protected Iterator<DBObject> iterator;
  protected MongoCollection mongoCollection;

  public LoggingIterator(MongoCollection mongoCollection, Iterator<DBObject> iterator) {
    this.mongoCollection = mongoCollection;
    this.iterator = iterator;
  }

  @Override
  public boolean hasNext() {
    return iterator.hasNext();
  }

  @Override
  public BasicDBObject next() {
    DBObject next = iterator.next();
    if (log.isDebugEnabled()) {
      log.debug("<-"+mongoCollection.getDbCollection().getName()+"-- "+mongoCollection.toString(next));
    }
    return (BasicDBObject) next;
  }

  @Override
  public void remove() {
    throw new RuntimeException("not supported");
  }
}
