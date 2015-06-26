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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.effektif.workflow.impl.data.InvalidValueException;
import com.effektif.workflow.impl.json.JsonReader;
import com.effektif.workflow.impl.json.JsonTypeMapper;
import com.effektif.workflow.impl.json.JsonWriter;


/**
 * Maps a {@link List} to a JSON array for serialisation and deserialisation.
 *
 * @author Tom Baeyens
 */
public class ListMapper extends AbstractTypeMapper<List> {

  JsonTypeMapper elementMapper;
  
  public ListMapper(JsonTypeMapper elementMapper) {
    this.elementMapper = elementMapper;
  }

  @Override
  public List read(Object jsonValue, JsonReader jsonReader) {
    if (!Collection.class.isAssignableFrom(jsonValue.getClass())) {
      throw new InvalidValueException(String.format("Invalid Collection value ‘%s’ (%s)", jsonValue, jsonValue.getClass().getName()));
    }
    List list = new ArrayList();
    Collection jsonCollection = (Collection) jsonValue;
    Iterator jsonIterator = jsonCollection.iterator();
    while (jsonIterator.hasNext()) {
      Object jsonElement = jsonIterator.next();
      Object objectElementValue = elementMapper.read(jsonElement, jsonReader);
      list.add(objectElementValue);
    }
    return list;
  }

  @Override
  public void write(List objectValue, JsonWriter jsonWriter) {
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
