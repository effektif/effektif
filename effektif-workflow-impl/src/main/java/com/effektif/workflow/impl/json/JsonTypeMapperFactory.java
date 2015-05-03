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


/**
 * @author Tom Baeyens
 */
public interface JsonTypeMapperFactory {

  /** returns a json type mapper only if this factory is applicable for the given clazz/type. 
   * @param clazz TODO
   * @param mappings TODO*/
  JsonTypeMapper createTypeMapper(Type type, Class< ? > clazz, Mappings mappings);
}
