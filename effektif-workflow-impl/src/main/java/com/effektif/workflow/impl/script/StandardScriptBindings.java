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

import java.io.Writer;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.script.Bindings;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.effektif.workflow.impl.data.DataType;
import com.effektif.workflow.impl.data.TypedValueImpl;
import com.effektif.workflow.impl.workflowinstance.ScopeInstanceImpl;
import com.effektif.workflow.impl.workflowinstance.VariableInstanceImpl;


public class StandardScriptBindings implements Bindings {
  
  public static final Logger log = LoggerFactory.getLogger(StandardScriptBindings.class);
  
  static final Map<String, Set<String>> TRANSIENT_KEYS = new HashMap<>();
  static {
    TRANSIENT_KEYS.put(StandardScriptService.JAVASCRIPT, new HashSet<String>(Arrays.asList("context", "print", "println", "Function")));
  }
  protected boolean isTransient(Object key) {
    Set<String> namesToIgnore = TRANSIENT_KEYS.get(language);
    if (key!=null
        && namesToIgnore!=null
        && namesToIgnore.contains(key)) {
      return true;
    }
    return false;
  }

  protected String language;
  protected ScopeInstanceImpl scopeInstance;
  protected Map<String,String> scriptToProcessMappings;
  protected Console console;
  protected Map<Object,Object> transientValues;

  public StandardScriptBindings(ScriptImpl script, ScopeInstanceImpl scopeInstance, Writer logWriter) {
    this.scriptToProcessMappings = script.mappings;
    this.language = ((StandardScriptService)script.scriptService).language;
    this.scopeInstance = scopeInstance;
    this.console = new Console(logWriter);
    this.transientValues = new HashMap<>();
  }
  
  @Override
  public boolean containsKey(Object key) {
    if (isTransient(key)) {
      // if (log.isDebugEnabled()) log.debug("ScriptBindings.containsKey("+key+") TRANSIENT");
      return transientValues.containsKey(key);
      
    } else {
      if (log.isDebugEnabled()) log.debug("ScriptBindings.containsKey("+key+")");
      if (!(key instanceof String)) {
        return false;
      }
      String name = (String) key;
      if (isTransient(name)) {
        return false;
      }
      if ("console".equals(name)) {
        return true;
      }
      if (scriptToProcessMappings!=null && scriptToProcessMappings.containsKey(name)) {
        return true;
      }
      if (name.length()>0) {
        return scopeInstance.scope.hasVariableRecursive(name);
      }
      return false;
    }
  }

  @Override
  public Object get(Object key) {
    if (isTransient(key)) {
      // if (log.isDebugEnabled()) log.debug("ScriptBindings.get("+key+") TRANSIENT");
      return transientValues.get(key);
    } 
    if (log.isDebugEnabled()) log.debug("ScriptBindings.get("+key+")");
    String scriptVariableName = (String) key;
    if ("console".equals(scriptVariableName)) {
      return console;
    }
    TypedValueImpl typedValue = getTypedValue(scriptVariableName);
    DataType type = typedValue.getType();
    Object value = typedValue.getValue();
    return type.convertInternalToScriptValue(value, language);
  }
  
  protected String getVariableId(String scriptVariableName) {
    if (scriptToProcessMappings!=null) {
      String variableId = scriptToProcessMappings.get(scriptVariableName);
      if (variableId!=null) {
        return variableId;
      }
    }
    return scriptVariableName;
  }

  public TypedValueImpl getTypedValue(String scriptVariableName) {
    String variableId = getVariableId(scriptVariableName);
    return scopeInstance.getTypedValue(variableId);
  }

  @Override
  public Object put(String scriptVariableName, Object scriptValue) {
    if (isTransient(scriptVariableName)) {
      // if (log.isDebugEnabled()) log.debug("ScriptBindings.put("+scriptVariableName+","+scriptValue+") TRANSIENT");
      return transientValues.put(scriptVariableName, scriptValue);
    } 
    if (log.isDebugEnabled()) log.debug("ScriptBindings.put("+scriptVariableName+","+scriptValue+")");
    if (isTransient(scriptVariableName)){
      return null;
    }
    String variableId = getVariableId(scriptVariableName);
    if (variableId!=null) {
      VariableInstanceImpl variableInstance = scopeInstance.findVariableInstance(variableId);
      DataType type = variableInstance.type;
      Object value = type.convertScriptValueToInternal(scriptValue, language);
      variableInstance.setValue(value);
    } else {
      scopeInstance.createVariableInstanceByValue(scriptValue);
    }
    return null;
  }

  // --- dungeons -------------------------------------------------------------------------

  @Override
  public int size() {
    if (log.isDebugEnabled())
      log.debug("ScriptBindings.size()");
    throw new UnsupportedOperationException("Please implement me");
  }

  @Override
  public boolean isEmpty() {
    if (log.isDebugEnabled())
      log.debug("ScriptBindings.isEmpty()");
    throw new UnsupportedOperationException("Please implement me");
  }

  @Override
  public boolean containsValue(Object value) {
    if (log.isDebugEnabled())
      log.debug("ScriptBindings.containsValue("+value+")");
    throw new UnsupportedOperationException("Please implement me");
  }

  @Override
  public void clear() {
    if (log.isDebugEnabled())
      log.debug("ScriptBindings.clear()");
    throw new UnsupportedOperationException("Please implement me");
  }

  @Override
  public Set<String> keySet() {
    if (log.isDebugEnabled())
      log.debug("ScriptBindings.keySet()");
    throw new UnsupportedOperationException("Please implement me");
  }

  @Override
  public Collection<Object> values() {
    if (log.isDebugEnabled())
      log.debug("ScriptBindings.values()");
    throw new UnsupportedOperationException("Please implement me");
  }

  @Override
  public Set<java.util.Map.Entry<String, Object>> entrySet() {
    if (log.isDebugEnabled())
      log.debug("ScriptBindings.entrySet()");
    throw new UnsupportedOperationException("Please implement me");
  }

  @Override
  public void putAll(Map< ? extends String, ? extends Object> toMerge) {
    if (log.isDebugEnabled())
      log.debug("ScriptBindings.putAll("+toMerge+")");
    throw new UnsupportedOperationException("Please implement me");
  }

  @Override
  public Object remove(Object key) {
    if (log.isDebugEnabled())
      log.debug("ScriptBindings.remove("+key+")");
    throw new UnsupportedOperationException("Please implement me");
  }
}
