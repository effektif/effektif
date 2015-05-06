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

import java.util.Map;

import com.effektif.workflow.api.workflow.Trigger;

/**
 * A mapping from a ‘base class’, e.g. {@link Trigger}, to its subclasses - actual trigger implementations.
 *
 * @author Tom Baeyens
 */
public class BeanMapping {

  TypeMapping typeMapping;
  
  protected BeanMapping() {
  }
  
  public BeanMapping(TypeMapping typeMapping) {
    this.typeMapping = typeMapping;
  }
  
  public TypeMapping getTypeMapping(Map<String, Object> jsonObject) {
    return typeMapping;
  }

  public TypeMapping getTypeMapping(Class<?> beanClass) {
    return typeMapping;
  }
}
