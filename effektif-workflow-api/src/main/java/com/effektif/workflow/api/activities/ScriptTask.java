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

  protected String script;
  protected Map<String,String> mappings;
  
  public ScriptTask() {
  }

  public ScriptTask(String id) {
    super(id);
  }

  public ScriptTask script(String script) {
    this.script = script;
    return this;
  }
  
  public ScriptTask variableMapping(String scriptVariableName, String workflowVariableId) {
    if (mappings==null) {
      mappings = new HashMap<>();
    }
    mappings.put(scriptVariableName, workflowVariableId);
    return this;
  }
  
  public String getScript() {
    return script;
  }
  
  public void setScript(String script) {
    this.script = script;
  }
  
  public Map<String, String> getMappings() {
    return mappings;
  }
  
  public void setMappings(Map<String, String> mappings) {
    this.mappings = mappings;
  }
}
