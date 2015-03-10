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
import com.effektif.workflow.api.types.DecisionOption;
import com.effektif.workflow.api.types.DecisionType;
import com.effektif.workflow.impl.data.AbstractDataType;
import com.effektif.workflow.impl.data.InvalidValueException;


/**
 * @author Tom Baeyens
 */
public class DecisionTypeImpl extends AbstractDataType<DecisionType> {
  
  protected List<DecisionOption> options;

  public DecisionTypeImpl(Configuration configuration) {
    super(new DecisionType(), String.class, configuration);
  }

  public DecisionTypeImpl(DecisionType decisionType, Configuration configuration) {
    super(decisionType, String.class, configuration);
    this.options = decisionType.getOptions();
  }
  
  @Override
  public Object convertJsonToInternalValue(Object jsonValue) throws InvalidValueException {
    validateInternalValue(jsonValue);
    return jsonValue; 
  }

  @Override
  public void validateInternalValue(Object internalValue) throws InvalidValueException {
    if (internalValue==null) {
      return;
    }
    if (options!=null) {
      for (DecisionOption option: options) {
        if (internalValue.equals(option.getName())) {
          return;
        }
      }
    }
    throw new InvalidValueException("Invalid value '"+internalValue+"'.  Expected one of "+options+" (or null)");
  }
}
