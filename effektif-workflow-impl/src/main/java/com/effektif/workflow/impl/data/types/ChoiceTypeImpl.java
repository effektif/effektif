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

import com.effektif.workflow.api.types.ChoiceOption;
import com.effektif.workflow.api.types.ChoiceType;
import com.effektif.workflow.impl.data.AbstractDataType;

/**
 * @author Tom Baeyens
 */
public class ChoiceTypeImpl extends AbstractDataType<ChoiceType> {
  
  public ChoiceTypeImpl() {
    super(new ChoiceType());
  }

  public ChoiceTypeImpl(ChoiceType choiceType) {
    super(choiceType);
  }

  @Override
  public boolean isStatic() {
    return false;
  }

  @Override
  public String validateInternalValue(Object internalValue) {
    List<ChoiceOption> options = type.getOptions();
    if (options!=null) {
      for (ChoiceOption option: options) {
        if (internalValue.equals(option.getId())) {
          return null;
        }
      }
    }
    return "Invalid value '"+internalValue+"'. Expected one of "+options+" (or null).";
  }
}
