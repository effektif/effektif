/*
 * Copyright 2014 Effektif GmbH.
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
 * limitations under the License.
 */
package com.effektif.workflow.impl.data.types;

import com.effektif.workflow.api.types.BooleanType;
import com.effektif.workflow.api.types.TextType;
import com.effektif.workflow.impl.data.AbstractDataType;
import com.effektif.workflow.impl.data.InvalidValueException;


public class BooleanTypeImpl extends AbstractDataType<TextType> {

  public BooleanTypeImpl() {
    super(BooleanType.class);
    this.valueClass = Boolean.class;
  }
  
  protected BooleanTypeImpl(Class apiClass) {
    super(apiClass);
    this.valueClass = Boolean.class;
  }
  
  @Override
  public boolean isStatic() {
    return true;
  }

  @Override
  public Object convertJsonToInternalValue(Object valueApi) throws InvalidValueException {
    if (valueApi==null || (valueApi instanceof Boolean)) {
      return valueApi;
    }
    throw new InvalidValueException("Expected boolean, but was "+valueApi.getClass().getSimpleName());
  }
}
