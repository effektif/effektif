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

import com.effektif.workflow.impl.data.InvalidValueException;
import com.effektif.workflow.impl.json.JsonReader;
import com.effektif.workflow.impl.json.JsonTypeMapper;
import com.effektif.workflow.impl.json.JsonWriter;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;


/**
 * Maps a {@link List} to a JSON array for serialisation and deserialisation.
 *
 * @author Tom Baeyens
 */
public class CollectionMapper extends AbstractTypeMapper<Collection> {

  JsonTypeMapper elementMapper;
  Class<? extends Collection> collectionType;
  
  public CollectionMapper(JsonTypeMapper elementMapper, Class<? extends Collection> collectionType) {
    this.elementMapper = elementMapper;
    this.collectionType = collectionType;
  }

  @Override
  public Collection read(Object jsonValue, JsonReader jsonReader) {
    if (!Collection.class.isAssignableFrom(jsonValue.getClass())) {
      throw new InvalidValueException(String.format("Invalid Collection value ‘%s’ (%s)", jsonValue, jsonValue.getClass().getName()));
    }
    Collection list = null;
    try {
      list = collectionType.newInstance();
    } catch (Exception e) {
      throw new RuntimeException("Couldn't instantiate collection: "+e.getMessage(), e);
    }
    Collection jsonCollection = (Collection) jsonValue;
    Iterator jsonIterator = jsonCollection.iterator();
    while (jsonIterator.hasNext()) {
      Object jsonElement = jsonIterator.next();
      Object objectElementValue = jsonElement!=null ? elementMapper.read(jsonElement, jsonReader) : null;
      list.add(objectElementValue);
    }
    return list;
  }

  @Override
  public void write(Collection objectValue, JsonWriter jsonWriter) {
    jsonWriter.arrayStart();
    for (Object objectElement: objectValue) {
      elementMapper.write(objectElement, jsonWriter);
    }
    jsonWriter.arrayEnd();
  }

  @Override
  public String toString() {
    return "ListMapper<"+elementMapper+">";
  }
  
  
}
