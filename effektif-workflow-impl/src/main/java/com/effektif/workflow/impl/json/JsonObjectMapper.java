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
package com.effektif.workflow.impl.json;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


/**
 * A facade for API object serialisation and deserialisation to and from JSON.
 *
 * @author Tom Baeyens
 */
public abstract class JsonObjectMapper {

  protected Mappings mappings;

  public <T> T read(Map<String,Object> beanJsonMap, Class<?> clazz) {
    return read(beanJsonMap, (Type)clazz);
  }

  public <T> T read(Object jsonObject, Type type) {
    if (jsonObject==null) {
      return null;
    }
    JsonObjectReader jsonObjectReader = new JsonObjectReader(mappings);
    return (T) jsonObjectReader.readObject(jsonObject, type);
  }

  public <T,R> R write(T bean) {
    JsonObjectWriter jsonObjectWriter = new JsonObjectWriter(mappings, this);
    jsonObjectWriter.writeObject(bean);
    return (R) jsonObjectWriter.result;
  }
  
  protected Map<String, Object> newObjectMap() {
    return new LinkedHashMap<>();
  }

  public List<Object> newArray() {
    return new ArrayList<>();
  }

  public Mappings getMappings() {
    return mappings;
  }
 
  public void setMappings(Mappings mappings) {
    this.mappings = mappings;
  }
}
