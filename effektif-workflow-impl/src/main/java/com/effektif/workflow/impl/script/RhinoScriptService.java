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
package com.effektif.workflow.impl.script;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextAction;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.Scriptable;

import com.effektif.workflow.api.workflow.Script;
import com.effektif.workflow.impl.WorkflowParser;
import com.effektif.workflow.impl.configuration.Brewable;
import com.effektif.workflow.impl.configuration.Brewery;
import com.effektif.workflow.impl.workflowinstance.ScopeInstanceImpl;


/**
 * @author Tom Baeyens
 */
@SuppressWarnings("restriction")
public class RhinoScriptService extends AbstractScriptService implements ScriptService, ConditionService, Brewable {

  private static final Logger log = LoggerFactory.getLogger(RhinoScriptService.class);
  
  protected ContextFactory contextFactory;
  
  @Override
  public void brew(Brewery brewery) {
    this.contextFactory = ContextFactory.getGlobal();
  }

  @Override
  public ScriptImpl compile(final Script script, final WorkflowParser parser) {
    ScriptImpl scriptImpl = new ScriptImpl();
    scriptImpl.scriptService = this;
    scriptImpl.mappings = script.getMappings();
    scriptImpl.compiledScript = contextFactory.call(new ContextAction() {
      public Object run(Context context) {
        try {
          return context.compileString(script.getScript(), "script", 1, null);
        } catch (Exception e) {
          parser.addError("Script doesn't compile: %s", e.getMessage());
          return null;
        }
      }
    });
    return scriptImpl;
  }

  @Override
  public ScriptResult evaluate(final ScopeInstanceImpl scopeInstance, final CompiledScript compiledScript) {
    final ScriptImpl script = (ScriptImpl) compiledScript;
    return (ScriptResult) contextFactory.call(new ContextAction() {
      public Object run(Context context) {
        Scriptable scope = context.initStandardObjects();
        
        StringWriter consoleData = new StringWriter();
        PrintWriter console = new PrintWriter(consoleData);
        RhinoVariableScope rhinoVariableScope = new RhinoVariableScope(scopeInstance, script.mappings, console, scope);
        
        ScriptResult scriptResult = new ScriptResult();
        try {
          org.mozilla.javascript.Script rhinoCompiledScript = (org.mozilla.javascript.Script) script.compiledScript;
          Object result = rhinoCompiledScript.exec(context, rhinoVariableScope);
          
          if (script.expectedResultType!=null && result!=null) {
            result = script.expectedResultType.convertJsonToInternalValue(result);
          }
          scriptResult.setResult(result);
          scriptResult.setUpdates(rhinoVariableScope.getUpdatedVariableValues());
          
        } catch (Exception e) {
          log.debug("Exception in JavaScript: "+e.getMessage(), e);
          console.println("Exception while executing script: "+e.toString());
          scriptResult.setException(e);
        }

        scriptResult.setLogs(consoleData.toString());
        return scriptResult;
      }
    });
  }
}
