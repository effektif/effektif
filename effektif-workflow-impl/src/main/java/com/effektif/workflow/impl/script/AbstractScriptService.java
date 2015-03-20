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
package com.effektif.workflow.impl.script;

import com.effektif.workflow.api.workflow.Condition;
import com.effektif.workflow.api.workflow.Script;
import com.effektif.workflow.impl.WorkflowParser;
import com.effektif.workflow.impl.workflowinstance.ScopeInstanceImpl;


/**
 * @author Tom Baeyens
 */
public abstract class AbstractScriptService implements ScriptService, ConditionService {
  
  public abstract ScriptResult run(ScopeInstanceImpl scopeInstance, CompiledScript compiledScript);

  public CompiledCondition compile(Condition condition, WorkflowParser parser) {
    Script script = new Script();
    script.setScript(condition.getExpression());
    script.setMappings(condition.getMappings());
    return (CompiledCondition) compile(script, parser);
  }
}
