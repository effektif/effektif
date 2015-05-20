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
package com.effektif.script.test;

import static org.junit.Assert.*;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.junit.Test;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextAction;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.Scriptable;

import com.effektif.script.RhinoVariableScope;


/**
 * @author Tom Baeyens
 */
public class RhinoTest {

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
        
        rhinoVariableScope.objects.put("file", new MagicScriptableObject("file"));
        
        Object result = null;
        try {
          org.mozilla.javascript.Script rhinoCompiledScript = (org.mozilla.javascript.Script) script;
          result = rhinoCompiledScript.exec(context, rhinoVariableScope);
          
        } catch (Exception e) {
          console.println("Exception while executing script: "+e.toString());
        }
        return result;
      }
    });
    
    assertNotNull(result);
  }
  
  public static class MagicScriptableObject extends org.mozilla.javascript.ScriptableObject {
    private static final long serialVersionUID = 1L;
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
