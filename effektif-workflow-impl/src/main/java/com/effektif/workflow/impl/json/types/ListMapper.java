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

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.effektif.workflow.impl.json.JsonReader;
import com.effektif.workflow.impl.json.JsonTypeMapper;
import com.effektif.workflow.impl.json.JsonWriter;


/**
 * @author Tom Baeyens
 */
public class ListMapper implements JsonTypeMapper<List>{

  public static final JsonTypeMapper INSTANCE = new ListMapper();

  @Override
  public Class<List> getMappedClass() {
    return List.class;
  }

  @Override
  public List read(Object jsonValue, Type type, JsonReader jsonReader) {
    Type elementType = null;
    if (type instanceof ParameterizedType) {
      elementType = ((ParameterizedType)type).getActualTypeArguments()[0];
    }
    
    List list = new ArrayList();
    Collection jsonCollection = (Collection) jsonValue;
    Iterator jsonIterator = jsonCollection.iterator();
    while (jsonIterator.hasNext()) {
      Object jsonElement = jsonIterator.next();
      Object objectElementValue = jsonReader.readObject(jsonElement, elementType);
      list.add(objectElementValue);
    }
    
    return list;
  }

  @Override
  public void write(List objectValue, JsonWriter jsonWriter) {
    jsonWriter.arrayStart();
    for (Object objectElement: objectValue) {
      jsonWriter.writeObject(objectElement);
    }
    jsonWriter.arrayEnd();
  }
}
