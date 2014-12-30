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
package com.effektif.workflow.api.command;

import java.util.HashMap;
import java.util.Map;


/**
 * @author Tom Baeyens
 */
public abstract class AbstractCommand {

  protected Map<String,Object> variableValues;

  public Map<String,Object> getVariableValues() {
    return this.variableValues;
  }

  public void setVariableValues(Map<String,Object> variableValues) {
    this.variableValues = variableValues;
  }
  
  public AbstractCommand variableValue(String variableId, Object variableValue) {
    if (variableValues==null) {
      variableValues = new HashMap<>();
    }
    variableValues.put(variableId, variableValue);
    return this;
  }
}
