/* Copyright 2014 Effektif GmbH.
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
package com.effektif.workflow.impl.data;

import com.effektif.workflow.api.types.Type;


public abstract class AbstractDataType<T extends Type> implements DataType<T> {
  
  protected T serializable;
  protected Class<? extends Type> apiClass;
  protected Class<?> valueClass;
  
  public AbstractDataType(Class<? extends Type> apiClass) {
    this.apiClass = apiClass;
  }
  
  @Override
  public boolean isStatic() {
    return false;
  }

  public Class<? extends Type> getApiClass() {
    return apiClass;
  }
  
  public T serialize() {
    return serializable;
  }
  
  @Override
  public TypeGenerator getTypeGenerator() {
    return null;
  }

  @Override
  public Object convertJsonToInternalValue(Object jsonValue) throws InvalidValueException {
    return jsonValue;
  }

  public void validateInternalValue(Object internalValue) throws InvalidValueException {
  }

  public Object convertInternalToJsonValue(Object internalValue) {
    return internalValue;
  }

  public Object convertInternalToScriptValue(Object internalValue, String language) {
    return internalValue;
  }

  public Object convertScriptValueToInternal(Object scriptValue, String language) {
    return scriptValue;
  }

  @Override
  public Class< ? > getValueClass() {
    return valueClass;
  }
}
