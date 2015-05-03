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
import java.util.HashSet;
import java.util.Set;

import com.effektif.workflow.impl.json.JsonTypeMapper;
import com.effektif.workflow.impl.json.JsonTypeMapperFactory;
import com.effektif.workflow.impl.json.Mappings;
import com.effektif.workflow.impl.util.Lists;


/**
 * @author Tom Baeyens
 */
public class NumberMapperFactory implements JsonTypeMapperFactory {

  @Override
  public JsonTypeMapper createTypeMapper(Type type, Class< ? > clazz, Mappings mappings) {
    if (isNumberClass(clazz)) {
      return new NumberMapper(type);
    }
    return null;
  }

  private static final Set<String> NUMBERTYPENAMES = new HashSet<>(
          Lists.of("byte", "short", "int", "long", "float", "double"));

  private boolean isNumberClass(Class< ? > clazz) {
    if (clazz==null) {
      return false;
    }
    return Number.class.isAssignableFrom(clazz)
      || NUMBERTYPENAMES.contains(clazz.getName());
  }
}
