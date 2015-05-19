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
package com.effektif.workflow.api.workflow;

import java.util.HashMap;
import java.util.Map;


/**
 * @author Tom Baeyens
 */
public class Script {

  protected String language;
  protected String script;
  /** Maps script variable names to workflow variable IDs. */
  protected Map<String, String> mappings;

  public String getLanguage() {
    return this.language;
  }
  public void setLanguage(String language) {
    this.language = language;
  }
  public Script language(String language) {
    this.language = language;
    return this;
  }
  
  public String getScript() {
    return this.script;
  }
  public void setScript(String script) {
    this.script = script;
  }
  public Script script(String script) {
    this.script = script;
    return this;
  }

  public Map<String,String> getMappings() {
    return this.mappings;
  }
  public void setMappings(Map<String,String> mappings) {
    this.mappings = mappings;
  }
  public Script mappings(Map<String,String> mappings) {
    this.mappings = mappings;
    return this;
  }

  public Script mapping(String scriptVariableName, String variableId) {
    if (mappings==null) {
      mappings = new HashMap<>(); 
    }
    mappings.put(scriptVariableName, variableId);
    return this;
  }
}
