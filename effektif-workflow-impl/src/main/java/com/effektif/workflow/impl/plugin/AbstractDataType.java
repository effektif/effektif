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
package com.effektif.workflow.impl.plugin;

import com.effektif.workflow.api.type.Type;
import com.effektif.workflow.impl.WorkflowParser;
import com.effektif.workflow.impl.WorkflowSerializer;
import com.effektif.workflow.impl.type.InvalidValueException;


public abstract class AbstractDataType<T extends Type> implements DataType<T> {
  
  protected T apiType;
  protected Class<?> apiClass;
  
  public AbstractDataType(Class< ? > apiClass) {
    this.apiClass = apiClass;
  }
  
  public Class< ? > getApiClass() {
    return apiClass;
  }
  
  public T getApiType() {
    return apiType;
  }
  
  public boolean isSerializeRequired() {
    return false;
  }
  
  @Override
  public void serialize(Object value, WorkflowSerializer serializer) {
  }

  public abstract Object convertJsonToInternalValue(Object jsonValue);

  public void parse(T typeApi, WorkflowParser parser) {
    this.apiType = typeApi;
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
  
  public Class< ? > getValueClass() {
    return null;
  }
}
