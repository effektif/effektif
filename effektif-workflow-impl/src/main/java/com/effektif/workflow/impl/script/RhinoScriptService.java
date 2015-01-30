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

import java.io.PrintWriter;
import java.io.StringWriter;

import javax.script.ScriptException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sun.org.mozilla.javascript.internal.Context;
import sun.org.mozilla.javascript.internal.ContextAction;
import sun.org.mozilla.javascript.internal.ContextFactory;
import sun.org.mozilla.javascript.internal.Scriptable;

import com.effektif.workflow.impl.configuration.Brewable;
import com.effektif.workflow.impl.configuration.Brewery;
import com.effektif.workflow.impl.workflowinstance.ScopeInstanceImpl;


public class RhinoScriptService implements ScriptService, Brewable {

  private static final Logger log = LoggerFactory.getLogger(RhinoScriptService.class);
  
  protected ContextFactory contextFactory;
  
  @Override
  public void brew(Brewery brewery) {
    this.contextFactory = ContextFactory.getGlobal();
  }

  @Override
  public Script compile(final String scriptText) {
    Script script = new Script();
    script.compiledScript = contextFactory.call(new ContextAction() {
      public Object run(Context context) {
        return context.compileString(scriptText, "script", 1, null);
      }
    });
    return script;
  }

  @Override
  public ScriptResult evaluate(final ScopeInstanceImpl scopeInstance, final Script script) {
    return (ScriptResult) contextFactory.call(new ContextAction() {
      public Object run(Context context) {
        // TODO consider calling context.seal(key); to make things more secure
        Scriptable scope = context.initStandardObjects();
        
        StringWriter console = new StringWriter();
        VariableScope variableScope = new VariableScope(scopeInstance, console, scope);
        
//      NativeObject greeting = new NativeObject();
//      greeting.put("message", greeting, "hello");
//      scope.put("greeting", scope, greeting);
//      scope.put("console", scope, new Console(new StringWriter()));
        
        try {
          sun.org.mozilla.javascript.internal.Script rhinoCompiledScript = (sun.org.mozilla.javascript.internal.Script) script.compiledScript;
          Object result = rhinoCompiledScript.exec(context, variableScope);
          scriptOutput.setLogs(console.toString());
        } catch (ScriptException e) {
          e.printStackTrace();
          scriptOutput.setException(e);
        }
        
        if (script.mappings!=null) {
          // TODO map output variables
        }
        
//      if (result instanceof NativeObject) {
//      NativeObject nObj = (NativeObject) result;
//      for (Object key : nObj.getAllIds()) {
//        System.out.println(key);
//        System.out.println(nObj.get((String) key, nObj));
//      }
        
        return scriptResult;
      }
    });
  }
}
