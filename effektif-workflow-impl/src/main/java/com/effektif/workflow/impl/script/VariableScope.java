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

import sun.org.mozilla.javascript.internal.Scriptable;
import sun.util.logging.resources.logging;

import com.effektif.workflow.impl.workflowinstance.ScopeInstanceImpl;


public class VariableScope implements Scriptable {
  
  private static final Logger log = LoggerFactory.getLogger(VariableScope.class);
  
  ScopeInstanceImpl workflowScope;
  Scriptable parentScope;
  
  public VariableScope(ScopeInstanceImpl workflowScope, Scriptable parentScope) {
    this.workflowScope = workflowScope;
    this.parentScope = parentScope;
  }

  @Override
  public void delete(String name) {
    
  }

  @Override
  public void delete(int index) {
  }

  @Override
  public Object get(String name, Scriptable start) {
    return null;
  }

  @Override
  public Object get(int index, Scriptable start) {
    return null;
  }

  @Override
  public String getClassName() {
    return null;
  }

  @Override
  public Object getDefaultValue(Class< ? > hint) {
    return null;
  }

  @Override
  public Object[] getIds() {
    return null;
  }

  @Override
  public Scriptable getParentScope() {
    return null;
  }

  @Override
  public Scriptable getPrototype() {
    return null;
  }

  @Override
  public boolean has(String name, Scriptable start) {
    return false;
  }

  @Override
  public boolean has(int index, Scriptable start) {
    return false;
  }

  @Override
  public boolean hasInstance(Scriptable instance) {
    return false;
  }

  @Override
  public void put(String name, Scriptable start, Object value) {
  }

  @Override
  public void put(int index, Scriptable start, Object value) {
  }

  @Override
  public void setParentScope(Scriptable arg0) {
  }

  @Override
  public void setPrototype(Scriptable arg0) {
  }

}
