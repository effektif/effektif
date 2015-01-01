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

import com.fasterxml.jackson.annotation.JsonTypeName;





@JsonTypeName("text")
public class TextType extends AbstractDataType {

  public static final String TYPE = "text";
  
  public static final TextType INSTANCE = new TextType();

  @Override
  public Object convertJsonToInternalValue(Object apiValue) throws InvalidValueException {
    if (apiValue==null || (apiValue instanceof String)) {
      return apiValue;
    }
    throw new InvalidValueException("Expected string, but was "+apiValue.getClass().getSimpleName());
  }

  @Override
  public Class< ? > getValueType() {
    return String.class;
  }
}
