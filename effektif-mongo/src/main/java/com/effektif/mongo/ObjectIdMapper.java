package com.effektif.mongo;/* Copyright (c) 2015, Effektif GmbH.
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

import java.lang.reflect.Type;

import org.bson.types.ObjectId;

import com.effektif.workflow.impl.json.JsonObjectWriter;
import com.effektif.workflow.impl.json.JsonReader;
import com.effektif.workflow.impl.json.JsonTypeMapper;
import com.effektif.workflow.impl.json.JsonTypeMapperFactory;
import com.effektif.workflow.impl.json.JsonWriter;
import com.effektif.workflow.impl.json.Mappings;
import com.effektif.workflow.impl.json.types.AbstractTypeMapper;

/**
 * Maps a {@link ObjectId} to a MongoDB ObjectId.
 *
 * @author Tom Baeyens
 */
public class ObjectIdMapper extends AbstractTypeMapper<ObjectId> implements JsonTypeMapperFactory {

  @Override
  public JsonTypeMapper createTypeMapper(Type type, Class< ? > clazz, Mappings mappings) {
    if (clazz==ObjectId.class) {
      return this;
    }
    return null;
  }

  @Override
  public void write(ObjectId objectValue, JsonWriter jsonWriter) {
    JsonObjectWriter jsonObjectWriter = (JsonObjectWriter) jsonWriter;
    jsonObjectWriter.writeValue(objectValue);
  }

  @Override
  public ObjectId read(Object jsonValue, JsonReader jsonReader) {
    try {
      return (ObjectId) jsonValue;
    } catch (ClassCastException e) {
      return null;
    }
  }
}
