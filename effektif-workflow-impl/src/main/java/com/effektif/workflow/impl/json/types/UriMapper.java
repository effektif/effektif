/* Copyright (c) 2016, Effektif GmbH.
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
import java.net.URI;
import java.net.URISyntaxException;

import com.effektif.workflow.impl.json.JsonReader;
import com.effektif.workflow.impl.json.JsonTypeMapper;
import com.effektif.workflow.impl.json.JsonTypeMapperFactory;
import com.effektif.workflow.impl.json.JsonWriter;
import com.effektif.workflow.impl.json.Mappings;

/**
 * Maps a {@link URI} to its {@link String} representation.
 *
 * @author Peter Hilton
 */
public class UriMapper extends AbstractTypeMapper<URI> implements JsonTypeMapperFactory {

  @Override
  public JsonTypeMapper createTypeMapper(Type type, Class<?> clazz, Mappings mappings) {
    if (clazz==URI.class) {
      return this;
    }
    return null;
  }

  @Override
  public void write(URI objectValue, JsonWriter jsonWriter) {
    if (objectValue == null) {
      jsonWriter.writeNull();
    }
    else {
      jsonWriter.writeString(objectValue.toString());
    }
  }

  @Override
  public URI read(Object jsonValue, JsonReader jsonReader) {
    try {
      return jsonValue == null ? null : new URI(jsonValue.toString());
    } catch (URISyntaxException | ClassCastException e) {
      return null;
    }
  }
}
