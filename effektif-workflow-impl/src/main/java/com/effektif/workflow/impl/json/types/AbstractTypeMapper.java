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

import com.effektif.workflow.impl.json.JsonTypeMapper;
import com.effektif.workflow.impl.json.Mappings;


/**
 * Maps values of some type to a JSON string field for serialisation and deserialisation.
 *
 * @author Tom Baeyens
 */
public abstract class AbstractTypeMapper<T> implements JsonTypeMapper<T> {

  protected Mappings mappings;

  public Mappings getMappings() {
    return mappings;
  }
  
  public void setMappings(Mappings mappings) {
    this.mappings = mappings;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }
  
  
}
