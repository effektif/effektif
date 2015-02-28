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
package com.effektif.workflow.api.workflow;

import java.util.HashMap;
import java.util.Map;


/**
 * @author Tom Baeyens
 */
public class Condition  {
  
  protected String expression;

  public String getExpression() {
    return this.expression;
  }
  public void setExpression(String expression) {
    this.expression = expression;
  }
  public Condition expression(String expression) {
    this.expression = expression;
    return this;
  }

  /** if in the expression you want to use variable names other then the 
   * variable ids, then mappings can be used to map condition expression 
   * variable names to workflow variable ids. */ 
  protected Map<String, String> mappings;

  public Map<String,String> getMappings() {
    return this.mappings;
  }
  public void setMappings(Map<String,String> mappings) {
    this.mappings = mappings;
  }
  public Condition mappings(Map<String,String> mappings) {
    this.mappings = mappings;
    return this;
  }

  public Condition mapping(String scriptVariableName, String variableId) {
    if (mappings==null) {
      mappings = new HashMap<>(); 
    }
    mappings.put(scriptVariableName, variableId);
    return this;
  }
}
