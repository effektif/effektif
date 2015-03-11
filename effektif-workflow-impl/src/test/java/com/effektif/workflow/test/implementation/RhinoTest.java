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
package com.effektif.workflow.test.implementation;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.junit.Test;

import sun.org.mozilla.javascript.internal.Context;
import sun.org.mozilla.javascript.internal.ContextAction;
import sun.org.mozilla.javascript.internal.ContextFactory;
import sun.org.mozilla.javascript.internal.Scriptable;

import com.effektif.workflow.impl.script.RhinoVariableScope;


/**
 * @author Tom Baeyens
 */
public class RhinoTest {

  @SuppressWarnings("restriction")
  @Test 
  public void testRhinoScriptResolving() {

    final String scriptText = "file.owner.name";
    
    ContextFactory contextFactory = ContextFactory.getGlobal();
    
    final Object script = contextFactory.call(new ContextAction() {
      public Object run(Context context) {
        try {
          return context.compileString(scriptText, "script", 1, null);
        } catch (Exception e) {
          e.printStackTrace();
          return null;
        }
      }
    });
    
    
    
    Object result = contextFactory.call(new ContextAction() {
      public Object run(Context context) {
        Scriptable scope = context.initStandardObjects();
        
        StringWriter consoleData = new StringWriter();
        PrintWriter console = new PrintWriter(consoleData);
        RhinoVariableScope rhinoVariableScope = new RhinoVariableScope(null, null, console, scope);
        
        rhinoVariableScope.localObjects.put("file", new MagicScriptableObject("file"));
        
        Object result = null;
        try {
          sun.org.mozilla.javascript.internal.Script rhinoCompiledScript = (sun.org.mozilla.javascript.internal.Script) script;
          result = rhinoCompiledScript.exec(context, rhinoVariableScope);
          
        } catch (Exception e) {
          console.println("Exception while executing script: "+e.toString());
        }
        return result;
      }
    });
  }
  
  @SuppressWarnings("restriction")
  public static class MagicScriptableObject extends sun.org.mozilla.javascript.internal.ScriptableObject {
    String name;
    public MagicScriptableObject(String name) {
      this.name = name;
    }
    @Override
    public String getClassName() {
      return null;
    }
    @Override
    public Object get(String field, Scriptable arg1) {
      System.out.println("resolving "+name+" . "+field);
      return new MagicScriptableObject(field);
    }
  }
}
