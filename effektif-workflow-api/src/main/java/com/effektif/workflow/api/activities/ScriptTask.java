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
package com.effektif.workflow.api.activities;

import com.effektif.workflow.api.workflow.Script;
import com.fasterxml.jackson.annotation.JsonTypeName;


/**
 * A script task - JavaScript code that will be executed by the process engine.
 *
 * @see <a href="https://github.com/effektif/effektif/wiki/Script-Task">Script Task</a>
 * @author Tom Baeyens
 */
@JsonTypeName("scriptTask")
public class ScriptTask extends NoneTask {

  protected Script script;
  
  @Override
  public ScriptTask id(String id) {
    super.id(id);
    return this;
  }

  public ScriptTask script(Script script) {
    this.script = script;
    return this;
  }
  
  public ScriptTask script(String script) {
    if (this.script == null) {
      this.script = new Script();
    }
    this.script.script(script);
    return this;
  }

  public ScriptTask scriptMapping(String scriptVariableName, String variableId) {
    if (this.script == null) {
      this.script = new Script();
    }
    this.script.mapping(scriptVariableName, variableId);
    return this;
  }
  
  public Script getScript() {
    return script;
  }
  
  public void setScript(Script script) {
    this.script = script;
  }
}
