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
import java.util.EnumSet;

import com.effektif.workflow.impl.json.JsonTypeMapper;
import com.effektif.workflow.impl.json.JsonTypeMapperFactory;
import com.effektif.workflow.impl.json.Mappings;
import com.effektif.workflow.impl.util.Reflection;

/**
 * Based on {@link CollectionMapperFactory}
 *
 * @author Peter Hilton
 */
public class EnumSetMapperFactory implements JsonTypeMapperFactory {

  @Override
  public JsonTypeMapper createTypeMapper(Type type, Class< ? > clazz, Mappings mappings) {
    if (clazz!=null && clazz==EnumSet.class) {
      Type enumerationType = Reflection.getTypeArg(type, 0);
      JsonTypeMapper elementMapper = mappings.getTypeMapper(enumerationType);
      return new EnumSetMapper(elementMapper, (Class<Enum>) enumerationType);
    }
    return null;
  }
}
