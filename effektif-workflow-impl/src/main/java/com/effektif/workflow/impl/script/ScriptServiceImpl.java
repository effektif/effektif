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

import com.effektif.workflow.impl.configuration.Brewery;
import com.effektif.workflow.impl.configuration.Brewable;
import com.effektif.workflow.impl.workflowinstance.ScopeInstanceImpl;


public class ScriptServiceImpl implements ScriptService, Brewable {

  public static final String JAVASCRIPT = "JavaScript";

  protected ScriptEngineManager scriptEngineManager;
  
  public ScriptServiceImpl() {
  }

  @Override
  public void brew(Brewery brewery) {
    this.scriptEngineManager = brewery.get(ScriptEngineManager.class);
  }

  public Script compile(String script) {
    return compile(script, null);
  }
  
  public Script compile(String scriptText, String language) {
    if (language==null) {
      language = JAVASCRIPT;
    }
    CompiledScript compiledScript = buildCompiledScript(scriptText, language);
    return new Script()
      .language(language)
      .compiledScript(compiledScript);
  }

  protected CompiledScript buildCompiledScript(String scriptText, String language) {
    try {
      Compilable compilable = (Compilable) scriptEngineManager.getEngineByName(language);
      return compilable.compile(scriptText);
    } catch (ScriptException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public ScriptResult evaluateScript(ScopeInstanceImpl scopeInstance, Script script) {
    ScriptResult scriptOutput = new ScriptResult();
    try {
      StringWriter logWriter = new StringWriter();
      ScriptContextImpl scriptContext = new ScriptContextImpl(scopeInstance, script, logWriter);
      Object result = script.compiledScript.eval(scriptContext);
      scriptOutput.setResult(result);
      scriptOutput.setLogs(logWriter.toString());
    } catch (ScriptException e) {
      e.printStackTrace();
      scriptOutput.setException(e);
    }
    return scriptOutput;
  }
}
