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
package com.effektif.workflow.impl.activitytypes;

import java.util.HashMap;
import java.util.Map;

import com.effektif.workflow.api.activities.NoneTask;
import com.effektif.workflow.api.activities.ScriptTask;
import com.effektif.workflow.api.workflow.Activity;
import com.effektif.workflow.api.workflowinstance.ActivityInstance;
import com.effektif.workflow.impl.plugin.ControllableActivityInstance;
import com.effektif.workflow.impl.plugin.Validator;
import com.effektif.workflow.impl.script.Script;
import com.effektif.workflow.impl.script.ScriptResult;
import com.effektif.workflow.impl.script.ScriptService;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeName;


@JsonTypeName("serviceTask")
public class ScriptTask extends NoneTask {

  @JsonIgnore
  protected ScriptService scriptService;
  
  public String script;
  public Map<String, String> scriptToWorkflowMappings;
  public String resultVariableId;
  
  @JsonIgnore
  public Script compiledScript;
  
  public ScriptTask script(String script) {
    this.script = script;
    return this;
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
  
  @Override
  public void validate(Activity activity, Validator validator) {
    if (script!=null) {
      this.scriptService = validator.getServiceRegistry().getService(ScriptService.class);
      this.compiledScript = scriptService.compile(script);
      this.compiledScript.scriptToProcessMappings = scriptToWorkflowMappings;
    }
    // TODO if specified, check if the resultVariableDefinitionId exists
  }

  @Override
  public void start(ControllableActivityInstance activityInstance) {
    if (script!=null) {
      ScriptResult scriptResult = scriptService.evaluateScript(activityInstance, compiledScript);
      scriptResult.getResult();
      /* Object result = 
        if (resultVariableDefinitionId!=null) {
        activityInstance.setVariableValue(resultVariableDefinitionId, result);
      } */
    }
    activityInstance.onwards();
  }
  
  @Override
  public boolean isAsync(ActivityInstance activityInstance) {
    return true;
  }
}
