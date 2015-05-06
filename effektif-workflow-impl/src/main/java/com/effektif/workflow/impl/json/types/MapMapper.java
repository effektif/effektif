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
package com.effektif.workflow.impl.json.types;

import java.util.LinkedHashMap;
import java.util.Map;

import com.effektif.workflow.impl.json.JsonReader;
import com.effektif.workflow.impl.json.JsonTypeMapper;
import com.effektif.workflow.impl.json.JsonWriter;


/**
 * Maps a {@link Map} to a JSON object for serialisation and deserialisation.
 *
 * @author Tom Baeyens
 */
public class MapMapper extends AbstractTypeMapper<Map> implements JsonTypeMapper<Map> {

  JsonTypeMapper valueMapper;
  
  public MapMapper(JsonTypeMapper valueMapper) {
    this.valueMapper = valueMapper;
  }
  
  @Override
  public void write(Map map, JsonWriter jsonWriter) {
    boolean inline = jsonWriter.getInline();
    if (!inline) {
      jsonWriter.objectStart();
    }
    for (Object key: map.keySet()) {
      if (key!=null) {
        if (!(key instanceof String)) {
          throw new RuntimeException("Only String keys allowed: "+key+" ("+key.getClass().getName()+"): Occurred when writing map "+map);
        }
        jsonWriter.writeFieldName((String)key);
        Object value = map.get(key);
        valueMapper.write(value, jsonWriter);
      }
    }
    if (!inline) {
      jsonWriter.objectEnd();
    }
  }

  @Override
  public Map read(Object jsonValue, JsonReader jsonReader) {
    Map<String,Object> objectMap = new LinkedHashMap<>();
    Map<String,Object> jsonMap = (Map<String, Object>) jsonValue;
    for (String key: jsonMap.keySet()) {
      Object jsonElementValue = jsonMap.get(key);
      Object objectElementValue = valueMapper.read(jsonElementValue, jsonReader);
      objectMap.put(key, objectElementValue);
    }
    return objectMap;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName()+"<"+valueMapper+">";
  }
}
