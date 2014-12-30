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
public class ScriptTask extends DefaultTask {

  public String script;
  public Map<String, String> scriptToWorkflowMappings;
  public String resultVariableId;
  
  public ScriptTask script(String script) {
    this.script = script;
    return this;
  }
  
  public String getScript() {
    return script;
  }
  
  public void setScript(String script) {
    this.script = script;
  }
  
  public Map<String, String> getScriptToWorkflowMappings() {
    return scriptToWorkflowMappings;
  }

  public void setScriptToWorkflowMappings(Map<String, String> scriptToWorkflowMappings) {
    this.scriptToWorkflowMappings = scriptToWorkflowMappings;
  }
  
  public String getResultVariableId() {
    return resultVariableId;
  }

  public void setResultVariableId(String resultVariableId) {
    this.resultVariableId = resultVariableId;
  }

  public ScriptTask variableMapping(String scriptVariableName, String workflowVariableId) {
    if (scriptToWorkflowMappings==null) {
      scriptToWorkflowMappings = new HashMap<>();
    }
    scriptToWorkflowMappings.put(scriptVariableName, workflowVariableId);
    return this;
  }
  
  public ScriptTask resultVariableId(String resultVariableId) {
    this.resultVariableId = resultVariableId;
    return this;
  } 
}
