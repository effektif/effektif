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

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Map;

import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import com.effektif.workflow.api.mapper.JsonReadable;
import com.effektif.workflow.api.model.Id;
import com.fasterxml.jackson.databind.ObjectMapper;


/**
 * @author Tom Baeyens
 */
public class JsonReader extends AbstractReader {
  
  public static DateTimeFormatter DATE_FORMAT = ISODateTimeFormat.dateTimeParser();

  public JsonReader() {
    this(new Mappings());
  }

  public JsonReader(Mappings mappings) {
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
