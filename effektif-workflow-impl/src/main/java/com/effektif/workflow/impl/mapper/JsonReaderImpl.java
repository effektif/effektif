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
package com.effektif.workflow.impl.mapper;

import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import com.effektif.workflow.api.model.Id;


/**
 * Standard JSON deserialisation implementation, characterised by plain string IDs and ISO format dates.
 *
 * TODO Rename to StandardJsonReader, to make the difference with MongoJsonReader clearer.
 *
 * @author Tom Baeyens
 */
public class JsonReaderImpl extends AbstractJsonReader {
  
  public static DateTimeFormatter DATE_FORMAT = ISODateTimeFormat.dateTimeParser();

  public JsonReaderImpl() {
    this(new Mappings());
  }

  public JsonReaderImpl(Mappings mappings) {
    this.mappings = mappings;
  }

  @Override
  public <T extends Id> T readId() {
    return readId("id");
  }

  public LocalDateTime readDateValue(Object jsonDate) {
    if (jsonDate==null) {
      return null;
    }
    return DATE_FORMAT.parseLocalDateTime((String)jsonDate);
  }
}
