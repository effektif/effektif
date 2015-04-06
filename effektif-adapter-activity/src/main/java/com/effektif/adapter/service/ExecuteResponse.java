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
package com.effektif.adapter.service;

import java.util.HashMap;
import java.util.Map;


/**
 * @author Tom Baeyens
 */
public class ExecuteResponse {

  protected boolean onwards;
  protected Map<String,Object> outputParameterValues;

  
  public Map<String, Object> getOutputParameterValues() {
    return outputParameterValues;
  }
  
  public void setOutputParameterValues(Map<String, Object> outputParameterValues) {
    this.outputParameterValues = outputParameterValues;
  }

  public void setOutputParameterValue(String outputParameterKey, Object value) {
    if (outputParameterValues==null) {
      outputParameterValues = new HashMap<>(); 
    }
    outputParameterValues.put(outputParameterKey, value);
  }

  public boolean isOnwards() {
    return onwards;
  }
  
  public void setOnwards(boolean onwards) {
    this.onwards = onwards;
  }
}
