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
package com.effektif.workflow.impl.script;

import java.io.StringWriter;

import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import com.effektif.workflow.api.workflow.Script;
import com.effektif.workflow.impl.WorkflowParser;
import com.effektif.workflow.impl.configuration.Brewable;
import com.effektif.workflow.impl.configuration.Brewery;
import com.effektif.workflow.impl.workflowinstance.ScopeInstanceImpl;


public class StandardScriptService implements ScriptService, Brewable {

  public static final String JAVASCRIPT = "JavaScript";

  protected ScriptEngineManager scriptEngineManager;
  protected String language;
  
  public StandardScriptService() {
  }

  public StandardScriptService(String language) {
    this.language = language;
  }

  @Override
  public void brew(Brewery brewery) {
    this.scriptEngineManager = brewery.get(ScriptEngineManager.class);
  }

  public ScriptImpl compile(Script script, WorkflowParser parser) {
    ScriptImpl scriptImpl = new ScriptImpl();
    scriptImpl.mappings = script.getMappings();
    
    String language = script.getLanguage();
    if (language==null) {
      language = JAVASCRIPT;
    }
    Compilable compilable = (Compilable) scriptEngineManager.getEngineByName(language);
    scriptImpl.scriptService = new StandardScriptService(language);
    
    try {
      scriptImpl.compiledScript = compilable.compile(script.getScript());
    } catch (ScriptException e) {
      parser.addError("Script doesn't compile: %s", e.getMessage()); 
    }
    return scriptImpl;
  }
  
  @Override
  public ScriptResult evaluate(ScopeInstanceImpl scopeInstance, ScriptImpl script) {
    ScriptResult scriptResult = new ScriptResult();
    try {
      StringWriter logWriter = new StringWriter();
      StandardScriptContext scriptContext = new StandardScriptContext(scopeInstance, script, logWriter);
      CompiledScript compiledScript = (CompiledScript) script.compiledScript;
      Object result = compiledScript.eval(scriptContext);
      scriptResult.setResult(result);
      scriptResult.setLogs(logWriter.toString());
    } catch (ScriptException e) {
      e.printStackTrace();
      scriptResult.setException(e);
    }
    return scriptResult;
  }
}
