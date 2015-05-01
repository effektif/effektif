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

import java.lang.reflect.Type;

import com.effektif.workflow.impl.json.JsonReader;
import com.effektif.workflow.impl.json.JsonTypeMapper;
import com.effektif.workflow.impl.json.JsonTypeMapperFactory;
import com.effektif.workflow.impl.json.JsonWriter;
import com.effektif.workflow.impl.json.Mappings;


/**
 * Maps an object to the json object representation for JSON serialisation and 
 * just returns the json object for deserialisation.
 *
 * @author Tom Baeyens
 */
public class ValueMapper extends AbstractTypeMapper<Object> implements JsonTypeMapperFactory {

  @Override
  public JsonTypeMapper createTypeMapper(Class< ? > clazz, Type type, Mappings mappings) {
    if (clazz==null || clazz==Object.class) {
      return this;
    }
    return null;
  }

  @Override
  public Object read(Object jsonValue, JsonReader jsonReader) {
    return jsonValue;
  }

  @Override
  public void write(Object objectValue, JsonWriter jsonWriter) {
    jsonWriter.writeObject(objectValue);
  }
}
