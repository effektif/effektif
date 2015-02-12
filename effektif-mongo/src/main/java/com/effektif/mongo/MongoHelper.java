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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.bson.types.ObjectId;
import org.joda.time.LocalDateTime;

import com.effektif.workflow.api.model.RequestContext;
import com.mongodb.BasicDBObject;


public abstract class MongoHelper {

  public static void writeId(Map<String,Object> o, String fieldName, String value) {
    o.put(fieldName, new ObjectId(value));
  }

  public static void writeIdOpt(Map<String,Object> o, String fieldName, String value) {
    if (value!=null) {
      o.put(fieldName, new ObjectId(value));
    }
  }

  public static void writeString(Map<String,Object> o, String fieldName, Object value) {
    writeObject(o, fieldName, value);
  }

  public static void writeStringOpt(Map<String,Object> o, String fieldName, String value) {
    writeObjectOpt(o, fieldName, value);
  }

  public static void writeLongOpt(Map<String,Object> o, String fieldName, Long value) {
    writeObjectOpt(o, fieldName, value);
  }

  public static void writeBooleanOpt(Map<String,Object> o, String fieldName, Object value) {
    writeObjectOpt(o, fieldName, value);
  }

  public static void writeObject(Map<String,Object> o, String fieldName, Object value) {
    o.put(fieldName, value);
  }

  public static void writeObjectOpt(Map<String,Object> o, String fieldName, Object value) {
    if (value!=null) {
      o.put(fieldName, value);
    }
  }
  
  public static void writeTimeOpt(Map<String,Object> o, String fieldName, LocalDateTime value) {
    if (value!=null) {
      o.put(fieldName, value.toDate());
    }
  }

  public static void writeListElementOpt(Map<String,Object> o, String fieldName, Object element) {
    if (element!=null) {
      @SuppressWarnings("unchecked")
      List<Object> list = (List<Object>) o.get(fieldName);
      if (list == null) {
        list = new ArrayList<>();
        o.put(fieldName, list);
      }
      list.add(element);
    }
  }
  
  @SuppressWarnings("unchecked")
  public static List<BasicDBObject> readList(BasicDBObject dbScope, String fieldName) {
    return (List<BasicDBObject>) dbScope.get(fieldName);
  }

  public static Object readObject(BasicDBObject dbObject, String fieldName) {
    return dbObject.get(fieldName);
  }

  public static BasicDBObject readBasicDBObject(BasicDBObject dbObject, String fieldName) {
    return (BasicDBObject) dbObject.get(fieldName);
  }

  public static String readId(BasicDBObject dbObject, String fieldName) {
    Object value = dbObject.get(fieldName);
    return value!=null ? value.toString() : null;
  }

  @SuppressWarnings("unchecked")
  public static Map<String, Object> readObjectMap(BasicDBObject dbObject, String fieldName) {
    return (Map<String,Object>) dbObject.get(fieldName);
  }

  public static String readString(BasicDBObject dbObject, String fieldName) {
    return (String) dbObject.get(fieldName);
  }

  public static Long readLong(BasicDBObject dbObject, String fieldName) {
    Object object = dbObject.get(fieldName);
    if (object==null) {
      return null;
    }
    if (object instanceof Long) {
      return (Long) object;
    }
    return ((Number) object).longValue();
  }

  public static Boolean readBoolean(BasicDBObject dbObject, String fieldName) {
    return (Boolean) dbObject.get(fieldName);
  }

  public static LocalDateTime readTime(BasicDBObject dbObject, String fieldName) {
    Object object = dbObject.get(fieldName);
    if (object==null) {
      return null;
    }
    if (object instanceof Date) {
      return new LocalDateTime((Date)object);
    }
    throw new RuntimeException("date conversion problem: "+object+" ("+object.getClass().getName()+")");
  }

  protected static boolean hasOrganizationId(RequestContext requestContext) {
    return requestContext!=null ? requestContext.getOrganizationId()!=null : false;
  }
}
