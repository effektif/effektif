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

import com.effektif.workflow.impl.data.InvalidValueException;
import com.effektif.workflow.impl.json.JsonReader;
import com.effektif.workflow.impl.json.JsonTypeMapper;
import com.effektif.workflow.impl.json.JsonTypeMapperFactory;
import com.effektif.workflow.impl.json.JsonWriter;
import com.effektif.workflow.impl.json.Mappings;


/**
 * Maps a {@link Boolean} to a JSON Boolean field for serialisation and deserialisation.
 *
 * @author Tom Baeyens
 */
public class BooleanMapper extends AbstractTypeMapper<Boolean> implements JsonTypeMapperFactory {

  @Override
  public JsonTypeMapper createTypeMapper(Type type, Class< ? > clazz, Mappings mappings) {
    if ( Boolean.class==type
            || boolean.class==type ) {
      return this;
    }
    return null;
  }

  @Override
  public void write(Boolean objectValue, JsonWriter jsonWriter) {
    jsonWriter.writeBoolean(objectValue);
  }

  @Override
  public Boolean read(Object jsonValue, JsonReader jsonReader) {
    if (!Boolean.class.isAssignableFrom(jsonValue.getClass())) {
      throw new InvalidValueException(String.format("Invalid Boolean value ‘%s’ (%s)", jsonValue, jsonValue.getClass().getName()));
    }
    return (Boolean) jsonValue;
  }
}
