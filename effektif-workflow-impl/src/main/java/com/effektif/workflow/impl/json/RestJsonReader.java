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
package com.effektif.workflow.impl.json;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Map;

import com.effektif.workflow.api.json.JsonReadable;
import com.effektif.workflow.api.model.Id;
import com.effektif.workflow.impl.json.deprecated.JsonMappings;
import com.fasterxml.jackson.databind.ObjectMapper;


/**
 * @author Tom Baeyens
 */
public class RestJsonReader extends AbstractJsonReader {
  
  static ObjectMapper objectMapper = new ObjectMapper();
  
  public RestJsonReader() {
    this(new JsonMappings());
  }

  public RestJsonReader(JsonMappings jsonMappings) {
    this.jsonMappings = jsonMappings;
  }

  public <T extends JsonReadable> T toObject(String jsonString, Class<T> type) {
    return toObject(new StringReader(jsonString), type);
  }

  public <T extends JsonReadable> T toObject(Reader jsonStream, Class<T> type) {
    try {
      this.jsonObject = objectMapper.readValue(jsonStream, Map.class);
      return readCurrentObject(type);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public <T extends Id> T readId(Class<T> idType) {
    return readId("id", idType);
  }
}
