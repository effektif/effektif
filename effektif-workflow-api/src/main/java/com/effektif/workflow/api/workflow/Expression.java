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

import java.util.Map;

import com.effektif.workflow.api.types.Type;


/**
 * @author Tom Baeyens
 */
public class Expression extends Script {

  protected Type type;

  public Type getType() {
    return this.type;
  }
  public void setType(Type type) {
    this.type = type;
  }
  public Expression type(Type type) {
    this.type = type;
    return this;
  }
  
  
  @Override
  public Expression language(String language) {
    super.language(language);
    return this;
  }
  @Override
  public Expression script(String script) {
    super.script(script);
    return this;
  }
  @Override
  public Expression mappings(Map<String, String> mappings) {
    super.mappings(mappings);
    return this;
  }
  @Override
  public Expression mapping(String scriptVariableName, String variableId) {
    super.mapping(scriptVariableName, variableId);
    return this;
  }
}
