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

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonTypeName;


/**
 * @author Walter White
 */
@JsonTypeName("choice")
public class ChoiceType extends AbstractDataType {
  
  public String label;

  /** maps option ids to option labels */
  protected Map<String, String> options;
  
  public ChoiceType option(String id, String label) {
    if (options==null) {
      options = new HashMap<>();
    }
    options.put(id, label);
    return this;
  }
  
  public ChoiceType label(String label) {
    this.label = label;
    return this;
  }
  
  public Map<String,String> getOptions() {
    return options;
  }
  
  public String getLabel() {
    return label;
  }
  
  public void setLabel(String label) {
    this.label = label;
  }

  @Override
  public Object convertJsonToInternalValue(Object jsonValue) throws InvalidValueException {
    validateInternalValue(jsonValue);
    return jsonValue; 
  }

  @Override
  public void validateInternalValue(Object internalValue) throws InvalidValueException {
    if ( internalValue!=null 
         && !options.containsKey(internalValue) ) {
      throw new InvalidValueException("Invalid value '"+internalValue+"'.  Expected one of "+options.keySet()+" (or null)");
    }
  }
}
