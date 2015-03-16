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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bson.types.ObjectId;

import com.effektif.workflow.impl.json.JsonService;
import com.effektif.workflow.impl.json.LocalDateTimeDeserializer;
import com.mongodb.BasicDBObject;


/** adds some transformations so jackson serialization can be used on a mongodb object.
 * 
 * @author Tom Baeyens
 */
public class MongoWriter {
  
  Object o;
  JsonService jsonService;
  boolean convertId;
  List<String> userIdFields = new ArrayList<>();
  List<String> timeFields = new ArrayList<>();

  public MongoWriter(Object o, JsonService jsonService) {
    this.o =  o;
    this.jsonService = jsonService;
  }

  public MongoWriter convertId() {
    this.convertId = true;
    return this;
  }

  public MongoWriter convertUserId(String fieldName) {
    userIdFields.add(fieldName);
    return this;
  }

  public MongoWriter convertTime(String fieldName) {
    timeFields.add(fieldName);
    return this;
  }
  
  public BasicDBObject get() {
    if (o==null) {
      return null;
    }
    
    Map<String,Object> jsonMap = jsonService.objectToJsonMap(o);
    BasicDBObject dbObject = new BasicDBObject(jsonMap);

    if (convertId) {
      String id = (String) jsonMap.remove("id");
      if (id!=null) {
        dbObject.put("_id", new ObjectId(id));
      }
    }
    
    for (String userIdField: userIdFields) {
      String value = (String) dbObject.get(userIdField);
      if (value!=null) {
        dbObject.put(userIdField, new ObjectId(value));
      }
    }

    for (String timeField: timeFields) {
      String value = (String) dbObject.get(timeField);
      if (value!=null) {
        dbObject.put(timeField, LocalDateTimeDeserializer.formatter.parseLocalDateTime(value));
      }
    }

    return dbObject;
  }
}
