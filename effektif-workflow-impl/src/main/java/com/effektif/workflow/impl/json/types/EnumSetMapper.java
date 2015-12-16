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

import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;

import com.effektif.workflow.impl.data.InvalidValueException;
import com.effektif.workflow.impl.json.JsonReader;
import com.effektif.workflow.impl.json.JsonTypeMapper;
import com.effektif.workflow.impl.json.JsonWriter;

/**
 * Maps a {@link Set} to a JSON array for serialisation and deserialisation.
 * Based on {@link CollectionMapper}
 *
 * @author Peter Hilton
 */
public class EnumSetMapper extends AbstractTypeMapper<EnumSet> {

  JsonTypeMapper elementMapper;
  private Class<Enum> clazz;

  public EnumSetMapper(JsonTypeMapper elementMapper, Class<Enum> clazz) {
    this.elementMapper = elementMapper;
    this.clazz = clazz;
  }

  @Override
  public EnumSet read(Object jsonValue, JsonReader jsonReader) {
    if (!Collection.class.isAssignableFrom(jsonValue.getClass())) {
      throw new InvalidValueException(String.format("Invalid Collection value ‘%s’ (%s)", jsonValue, jsonValue.getClass().getName()));
    }
    EnumSet set = EnumSet.noneOf(clazz);
    Collection jsonCollection = (Collection) jsonValue;
    for (Object jsonElement : jsonCollection) {
      Object objectElementValue = elementMapper.read(jsonElement, jsonReader);
      set.add(objectElementValue);
    }
    return set;
  }

  @Override
  public void write(EnumSet objectValue, JsonWriter jsonWriter) {
    jsonWriter.arrayStart();
    for (Object objectElement: objectValue) {
      elementMapper.write(objectElement, jsonWriter);
    }
    jsonWriter.arrayEnd();
  }

  @Override
  public String toString() {
    return "EnumSetMapper<"+elementMapper+">";
  }
  
  
}
