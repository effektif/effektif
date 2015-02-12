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

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.joda.time.LocalDateTime;

import com.mongodb.BasicDBObject;


public class MongoReaderHelper {

  @SuppressWarnings("unchecked")
  protected static List<BasicDBObject> getList(BasicDBObject dbScope, String fieldName) {
    return (List<BasicDBObject>) dbScope.get(fieldName);
  }

  @SuppressWarnings("unchecked")
  protected static Map<String, Object> getMap(BasicDBObject dbObject, String fieldName) {
    return (Map<String,Object>)dbObject.get(fieldName);
  }

  protected static String getString(BasicDBObject dbObject, String fieldName) {
    return (String) dbObject.get(fieldName);
  }

  protected static Long getLong(BasicDBObject dbObject, String fieldName) {
    Object object = dbObject.get(fieldName);
    if (object==null) {
      return null;
    }
    if (object instanceof Long) {
      return (Long) object;
    }
    return ((Number) object).longValue();
  }

  protected static Boolean getBoolean(BasicDBObject dbObject, String fieldName) {
    return (Boolean) dbObject.get(fieldName);
  }

  protected static LocalDateTime getTime(BasicDBObject dbObject, String fieldName) {
    Date date = (Date)dbObject.get(fieldName);
    return (date!=null ? new LocalDateTime(date) : null);
  }

}
