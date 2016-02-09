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

import com.mongodb.*;
import org.slf4j.Logger;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;


public class LoggingCursor extends DBCursor {
  
  public static final Logger log = MongoDb.log;

  protected DBCursor cursor;
  protected MongoCollection mongoCollection;

  public LoggingCursor(MongoCollection mongoCollection, DBCursor cursor) {
    super(cursor.getCollection(), cursor.getQuery(), cursor.getKeysWanted(), cursor.getReadPreference());
    this.mongoCollection = mongoCollection;
    this.cursor = cursor;
  }

  @Override
  public DBObject next() {
    DBObject next = cursor.next();
    if (log.isDebugEnabled()) {
      log.debug("<-"+cursor.getCollection().getName()+"-- "+mongoCollection.toString(next));
    }
    return next;
  }

  @Override
  public DBObject tryNext() {
    DBObject next = cursor.tryNext();
    if (log.isDebugEnabled()) {
      log.debug("<-"+cursor.getCollection().getName()+"-- "+mongoCollection.toString(next));
    }
    return next;
  }


  @Override
  public DBCursor sort(DBObject orderBy) {
    log.debug("--"+cursor.getCollection().getName()+"-> sort="+orderBy);
    return cursor.sort(orderBy);
  }

  @Override
  public DBCursor limit(int n) {
    log.debug("--"+cursor.getCollection().getName()+"-> limit="+n);
    return cursor.limit(n);
  }


  @Override
  public DBCursor comment(String comment) {
    return cursor.comment(comment);
  }

  @Override
  public DBCursor maxScan(int max) {
    return cursor.maxScan(max);
  }

  @Override
  public DBCursor max(DBObject max) {
    return cursor.max(max);
  }

  @Override
  public DBCursor min(DBObject min) {
    return cursor.min(min);
  }

  @Override
  public DBCursor returnKey() {
    return cursor.returnKey();
  }

  @Override
  public DBCursor showDiskLoc() {
    return cursor.showDiskLoc();
  }

  @Override
  public DBCursor copy() {
    return cursor.copy();
  }

  @Override
  public Iterator<DBObject> iterator() {
    return cursor.iterator();
  }

  @Override
  public DBCursor addSpecial(String name, Object o) {
    return cursor.addSpecial(name, o);
  }

  @Override
  public DBCursor hint(DBObject indexKeys) {
    return cursor.hint(indexKeys);
  }

  @Override
  public DBCursor hint(String indexName) {
    return cursor.hint(indexName);
  }

  @Override
  public DBCursor maxTime(long maxTime, TimeUnit timeUnit) {
    return cursor.maxTime(maxTime, timeUnit);
  }

  @Override
  public DBCursor snapshot() {
    return cursor.snapshot();
  }

  @Override
  public DBObject explain() {
    return cursor.explain();
  }

  @Override
  public DBCursor batchSize(int n) {
    return cursor.batchSize(n);
  }

  @Override
  public int getBatchSize() {
    return cursor.getBatchSize();
  }

  @Override
  public int getLimit() {
    return cursor.getLimit();
  }

  @Override
  public DBCursor skip(int n) {
    return cursor.skip(n);
  }

  @Override
  public long getCursorId() {
    return cursor.getCursorId();
  }

  @Override
  public void close() {
    cursor.close();
  }

  @SuppressWarnings("deprecation")
  @Override
  public DBCursor slaveOk() {
    return cursor.slaveOk();
  }

  @Override
  public DBCursor addOption(int option) {
    if (cursor != null) return cursor.addOption(option);
    else return this;
  }

  @Override
  public DBCursor setOptions(int options) {
    return cursor.setOptions(options);
  }

  @Override
  public DBCursor resetOptions() {
    return cursor.resetOptions();
  }

  @Override
  public int getOptions() {
    return cursor.getOptions();
  }

  @Override
  public int numSeen() {
    return cursor.numSeen();
  }

  @Override
  public boolean hasNext() {
    return cursor.hasNext();
  }

  @Override
  public DBObject curr() {
    return cursor.curr();
  }

  @Override
  public void remove() {
    cursor.remove();
  }

  @Override
  public int length() {
    return cursor.length();
  }

  @Override
  public List<DBObject> toArray() {
    return cursor.toArray();
  }

  @Override
  public List<DBObject> toArray(int max) {
    return cursor.toArray(max);
  }

  @Override
  public int itcount() {
    return cursor.itcount();
  }

  @Override
  public int count() {
    return cursor.count();
  }

  @Override
  public DBObject one() {
    return cursor.one();
  }

  @Override
  public int size() {
    return cursor.size();
  }

  @Override
  public DBObject getKeysWanted() {
    return cursor.getKeysWanted();
  }

  @Override
  public DBObject getQuery() {
    return cursor.getQuery();
  }

  @Override
  public DBCollection getCollection() {
    return cursor.getCollection();
  }

  @Override
  public ServerAddress getServerAddress() {
    return cursor.getServerAddress();
  }

  @Override
  public DBCursor setReadPreference(ReadPreference preference) {
    return cursor.setReadPreference(preference);
  }

  @Override
  public ReadPreference getReadPreference() {
    return cursor.getReadPreference();
  }

  @Override
  public DBCursor setDecoderFactory(DBDecoderFactory fact) {
    return cursor.setDecoderFactory(fact);
  }

  @Override
  public DBDecoderFactory getDecoderFactory() {
    return cursor.getDecoderFactory();
  }

  @Override
  public String toString() {
    return cursor.toString();
  }
}
