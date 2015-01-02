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

import java.util.Map;

import com.effektif.workflow.api.activities.ScriptTask;
import com.effektif.workflow.impl.definition.ActivityImpl;
import com.effektif.workflow.impl.definition.WorkflowValidator;
import com.effektif.workflow.impl.instance.ActivityInstanceImpl;
import com.effektif.workflow.impl.plugin.AbstractActivityType;
import com.effektif.workflow.impl.script.Script;
import com.effektif.workflow.impl.script.ScriptResult;
import com.effektif.workflow.impl.script.ScriptService;


public class ScriptTaskImpl extends AbstractActivityType<ScriptTask> {

  protected ScriptService scriptService;
  public String script;
  public Map<String, String> scriptToWorkflowMappings;
  public String resultVariableId;
  public Script compiledScript;
  
  public ScriptTaskImpl() {
    super(ScriptTask.class);
  }

  @Override
  public void validate(ActivityImpl activity, ScriptTask scriptTask, WorkflowValidator validator) {
    if (script!=null) {
      this.scriptService = validator.getServiceRegistry().getService(ScriptService.class);
      this.compiledScript = scriptService.compile(script);
      this.compiledScript.scriptToProcessMappings = scriptToWorkflowMappings;
    }
    // TODO if specified, check if the resultVariableDefinitionId exists
  }

  @Override
  public void execute(ActivityInstanceImpl activityInstance) {
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
  public boolean isAsync(ActivityInstanceImpl activityInstance) {
    return true;
  }
}
