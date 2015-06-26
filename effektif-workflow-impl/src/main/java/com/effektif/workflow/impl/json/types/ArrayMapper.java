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

import java.lang.reflect.Array;
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
public class ArrayMapper extends AbstractTypeMapper<Object[]> implements JsonTypeMapper<Object[]>{

  JsonTypeMapper elementMapper;
  Class arrayClass;    // eg String[]
  Class componentType; // the class without the array eg String
  
  public ArrayMapper(JsonTypeMapper elementMapper, Class arrayClass) {
    this.elementMapper = elementMapper;
    this.arrayClass = arrayClass;
    this.componentType = arrayClass.getComponentType();
  }

  @Override
  public Object[] read(Object jsonValue, JsonReader jsonReader) {
    if (!Collection.class.isAssignableFrom(jsonValue.getClass())) {
      throw new InvalidValueException(String.format("Invalid Collection value ‘%s’ (%s)", jsonValue, jsonValue.getClass().getName()));
    }
    Collection jsonCollection = (Collection) jsonValue;
    Object[] array = (Object[]) Array.newInstance(componentType, jsonCollection.size());
    Iterator jsonIterator = jsonCollection.iterator();
    int index = 0;
    while (jsonIterator.hasNext()) {
      Object jsonElement = jsonIterator.next();
      Object objectElementValue = elementMapper.read(jsonElement, jsonReader);
      array[index] = objectElementValue;
      index++;
    }
    return array;
  }

  @Override
  public void write(Object[] objectValue, JsonWriter jsonWriter) {
    jsonWriter.arrayStart();
    for (Object objectElement: objectValue) {
      elementMapper.write(objectElement, jsonWriter);
    }
    jsonWriter.arrayEnd();
  }

  @Override
  public String toString() {
    return "ArrayMapper<"+elementMapper+">";
  }
}
