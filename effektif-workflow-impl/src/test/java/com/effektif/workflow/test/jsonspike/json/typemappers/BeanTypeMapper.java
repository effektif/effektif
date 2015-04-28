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
package com.effektif.workflow.test.jsonspike.json.typemappers;

import java.lang.reflect.Type;
import java.util.Map;

import com.effektif.workflow.test.jsonspike.json.JsonFieldReader;
import com.effektif.workflow.test.jsonspike.json.JsonFieldWriter;
import com.effektif.workflow.test.jsonspike.json.TypeMapper;


/**
 * Maps a JavaBean to a {@link Map} field for JSON serialisation and deserialisation.
 *
 * @author Tom Baeyens
 */
public class BeanTypeMapper implements TypeMapper<Object> {

  public static final TypeMapper INSTANCE = new BeanTypeMapper();

  @Override
  public Class<Object> getMappedClass() {
    return null;
  }

  @Override
  public Object read(Object jsonValue, Type type, JsonFieldReader jsonFieldReader) {
    return jsonFieldReader.readBean((Map<String, Object>) jsonValue, (Class<?>) type);
  }

  @Override
  public void write(Object objectValue, JsonFieldWriter jsonFieldWriter) {
    jsonFieldWriter.writeBean(objectValue);
  }

}
