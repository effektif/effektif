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
package com.effektif.workflow.api.activities;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonTypeName;


@JsonTypeName("scriptTask")
public class ScriptTask extends NoneTask {

  public static final String KEY_SCRIPT = "script";
  public static final String KEY_MAPPINGS = "mapping";
  public static final String KEY_RESULT_VARIABLE_ID = "resultVariableId";
  
  public ScriptTask script(String script) {
    setConfiguration(KEY_SCRIPT, script);
    return this;
  }
  
  public ScriptTask variableMapping(String scriptVariableName, String workflowVariableId) {
    Map<String,String> mappings = (Map<String, String>) getConfiguration(KEY_MAPPINGS);
    if (mappings==null) {
      mappings = new HashMap<>();
      setConfiguration(KEY_MAPPINGS, mappings);
    }
    mappings.put(scriptVariableName, workflowVariableId);
    return this;
  }
  
  public ScriptTask resultVariableId(String resultVariableId) {
    setConfiguration(KEY_RESULT_VARIABLE_ID, resultVariableId);
    return this;
  } 
}
