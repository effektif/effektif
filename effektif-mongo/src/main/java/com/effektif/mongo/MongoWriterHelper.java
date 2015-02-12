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
import java.util.List;

import org.joda.time.LocalDateTime;

import com.mongodb.BasicDBObject;


public class MongoWriterHelper {

  public static void putOpt(BasicDBObject o, String fieldName, Object value) {
    if (fieldName!=null && value!=null) {
      o.put(fieldName, value);
    }
  }
  public static void putOptTime(BasicDBObject o, String fieldName, LocalDateTime value) {
    if (fieldName!=null && value!=null) {
      o.put(fieldName, value.toDate());
    }
  }

  @SuppressWarnings("unchecked")
  public void addListElementOpt(BasicDBObject dbParentScope, String fieldName, Object element) {
    if (element!=null) {
      List<Object> list = (List<Object>) dbParentScope.get(fieldName);
      if (list == null) {
        list = new ArrayList<>();
        dbParentScope.put(fieldName, list);
      }
      list.add(element);
    }
  }

}
