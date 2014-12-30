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

import java.util.HashMap;
import java.util.Map;

import javax.script.CompiledScript;


/**
 * @author Walter White
 */
public class Script {

  public String language;
  public CompiledScript compiledScript;

  /** maps script variable names to process variable definition names */ 
  public Map<String, String> scriptToProcessMappings;

  public Script language(String language) {
    this.language = language;
    return this;
  }

  public Script compiledScript(CompiledScript compiledScript) {
    this.compiledScript = compiledScript;
    return this;
  }
  
  public Script scriptToProcessMapping(String scriptVariableName, String variableDefinitionId) {
    if (scriptToProcessMappings==null) {
      scriptToProcessMappings = new HashMap<>();
    }
    scriptToProcessMappings.put(scriptVariableName, variableDefinitionId);
    return this;
  }
}
