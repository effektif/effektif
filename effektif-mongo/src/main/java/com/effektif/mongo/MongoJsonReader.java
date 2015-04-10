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

import java.util.Date;

import org.joda.time.LocalDateTime;

import com.effektif.workflow.api.mapper.JsonReadable;
import com.effektif.workflow.api.model.Id;
import com.effektif.workflow.impl.mapper.AbstractReader;
import com.effektif.workflow.impl.mapper.Mappings;
import com.mongodb.BasicDBObject;


/**
 * @author Tom Baeyens
 */
public class MongoJsonReader extends AbstractReader {

  public MongoJsonReader() {
    super();
  }

  public MongoJsonReader(Mappings mappings) {
    super(mappings);
  }

  public <T extends JsonReadable> T toObject(BasicDBObject dbObject, Class<T> type) {
    return readReadable(dbObject, type);
  }

  @Override
  public <T extends Id> T readId() {
    return readId("_id");
  }

  public LocalDateTime readDateValue(Object jsonDate) {
    return new LocalDateTime((Date)jsonDate);
  }
}
