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
package com.effektif.mongo.deprecated;

import java.lang.reflect.Field;
import java.util.Date;

import org.joda.time.LocalDateTime;

import com.effektif.workflow.api.model.Id;
import com.effektif.workflow.impl.deprecated.json.AbstractJsonReader;
import com.effektif.workflow.impl.deprecated.json.Mappings;


/**
 * MongoDB JSON deserialisation implementation, characterised by MongoDB object IDs and dates.
 *
 * @author Tom Baeyens
 */
public class MongoJsonReader extends AbstractJsonReader {

  MongoJsonMapper mongoJsonMapper;

  public MongoJsonReader(Mappings mappings, MongoJsonMapper mongoJsonMapper) {
    super(mappings);
    this.mongoJsonMapper = mongoJsonMapper;
  }

  public LocalDateTime readDateValue(Object jsonDate) {
    if (jsonDate==null) {
      return null;
    }
    return new LocalDateTime((Date)jsonDate);
  }

  @Override
  protected String getJsonFieldName(Field field) {
    return mongoJsonMapper.getFieldName(field);
  }

  /**
   * Returns the value of a Mongo JSON ID field, without removing it from the JSON object being parsed.
   */
  @Override
  public <T extends Id> T readId() {
    Object id = jsonObject.get("_id");
    Class<T> idType = (Class<T>) mappings.getFieldType(readableClass, "id");
    return toId(id, idType);
  }
}
