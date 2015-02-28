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

import java.util.List;

import com.effektif.workflow.api.Configuration;
import com.effektif.workflow.api.types.ChoiceType;
import com.effektif.workflow.impl.data.AbstractDataType;
import com.effektif.workflow.impl.data.InvalidValueException;


/**
 * @author Tom Baeyens
 */
public class ChoiceTypeImpl extends AbstractDataType<ChoiceType> {
  
  protected List<String> options;

  public ChoiceTypeImpl(Configuration configuration) {
    super(new ChoiceType(), String.class, configuration);
  }

  public ChoiceTypeImpl(ChoiceType choiceType, Configuration configuration) {
    super(choiceType, String.class, configuration);
    this.options = choiceType.getOptions();
  }
  
  @Override
  public Object convertJsonToInternalValue(Object jsonValue) throws InvalidValueException {
    validateInternalValue(jsonValue);
    return jsonValue; 
  }

  @Override
  public void validateInternalValue(Object internalValue) throws InvalidValueException {
    if ( internalValue!=null 
         && !options.contains(internalValue) ) {
      throw new InvalidValueException("Invalid value '"+internalValue+"'.  Expected one of "+options+" (or null)");
    }
  }
}
