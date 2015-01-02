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
package com.effektif.workflow.impl.type;

import com.effektif.workflow.impl.definition.VariableImpl;
import com.effektif.workflow.impl.definition.WorkflowValidator;


public abstract class AbstractDataType<T> implements DataType<T> {
  
  protected Class<T> configurationClass;
  
  public AbstractDataType(Class<T> configurationClass) {
    this.configurationClass = configurationClass;
  }
  
  @Override
  public Class<T> getConfigurationClass() {
    return configurationClass;
  }

  @Override
  public void validate(VariableImpl variable, T apiVariable, WorkflowValidator validator) {
  }

  @Override
  public void validateInternalValue(Object internalValue) throws InvalidValueException {
  }

  @Override
  public Object convertInternalToJsonValue(Object internalValue) {
    return internalValue;
  }

  @Override
  public Object convertInternalToScriptValue(Object internalValue, String language) {
    return internalValue;
  }

  @Override
  public Object convertScriptValueToInternal(Object scriptValue, String language) {
    return scriptValue;
  }
  
  @Override
  public Class< ? > getValueType() {
    return null;
  }
}
