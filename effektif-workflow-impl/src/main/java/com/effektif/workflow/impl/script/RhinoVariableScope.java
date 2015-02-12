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
package com.effektif.workflow.impl.script;

import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sun.org.mozilla.javascript.internal.Callable;
import sun.org.mozilla.javascript.internal.Context;
import sun.org.mozilla.javascript.internal.IdFunctionObject;
import sun.org.mozilla.javascript.internal.NativeArray;
import sun.org.mozilla.javascript.internal.NativeObject;
import sun.org.mozilla.javascript.internal.Scriptable;
import sun.org.mozilla.javascript.internal.ScriptableObject;

import com.effektif.workflow.impl.data.DataType;
import com.effektif.workflow.impl.data.TypedValueImpl;
import com.effektif.workflow.impl.workflowinstance.ScopeInstanceImpl;
import com.effektif.workflow.impl.workflowinstance.VariableInstanceImpl;


public class RhinoVariableScope implements Scriptable {
  
  private static final Logger log = LoggerFactory.getLogger(RhinoScriptService.class);
  
  protected ScopeInstanceImpl scopeInstance;
  protected Scriptable parentScope;
  
  protected Map<String,Object> localObjects;
  protected Set<String> updated;
  protected Map<String,String> scriptToWorkflowMappings;
  
  public RhinoVariableScope(ScopeInstanceImpl scopeInstance, Map<String,String> scriptToWorkflowMappings, PrintWriter console, Scriptable parentScope) {
    this.scopeInstance = scopeInstance;
    this.parentScope = parentScope;
    this.scriptToWorkflowMappings = scriptToWorkflowMappings;
    this.updated = new HashSet<>();
    this.localObjects = new HashMap<>();
    this.localObjects.put("console", new Console(console)); 
    this.localObjects.put("JSON", new JSON()); 
  }

  @Override
  public Object get(String name, Scriptable start) {
    log.debug("get "+name+" | "+start);
    if (localObjects.containsKey(name)) {
      return localObjects.get(name);
    }
    String variableId = scriptToWorkflowMappings.get(name);
    if (variableId==null) {
      variableId = name;
    }
    Object nativeValue = null;
    VariableInstanceImpl variableInstance = scopeInstance.findVariableInstance(variableId);
    if (variableInstance!=null) {
      TypedValueImpl typedValue = variableInstance.getTypedValue();
      Object jsonValue = typedValue.type.convertInternalToJsonValue(typedValue.value);
      log.debug("  lazy loading "+name+" = "+jsonValue);
      nativeValue = convertInternalToNative(jsonValue, name);
    }
    localObjects.put(name, nativeValue);
    return nativeValue;
  }
  
  @Override
  public boolean has(String name, Scriptable start) {
    log.debug("has "+name+" | "+start);
    if ("console".equals(name)) {
      return true;
    }
    if (localObjects.containsKey(name)) {
      return true;
    }
    String variableId = scriptToWorkflowMappings.get(name);
    if (variableId==null) {
      variableId = name;
    }
    return scopeInstance.findVariableInstance(variableId)!=null;
  }

  @Override
  public void put(String name, Scriptable start, Object value) {
    log.debug("put "+name+" | "+start+" | "+value);
    localObjects.put(name, value);
    updated(name);
  }

  @Override
  public Scriptable getParentScope() {
    log.debug("getParentScope");
    return parentScope;
  }

  @Override
  public Object[] getIds() {
    log.debug("getIds");
    throw new RuntimeException("not supported");
  }

  @Override
  public Scriptable getPrototype() {
    log.debug("getPrototype");
    return parentScope.getPrototype();
  }
  
  /** maps variableIds to internal values */
  public Map<String,TypedValueImpl> getUpdatedVariableValues() {
    Map<String,TypedValueImpl> updatedValues = new HashMap<>();
    for (String scriptVariableName: updated) {
      Object nativeObject = localObjects.get(scriptVariableName);
      String variableId = scriptToWorkflowMappings.get(scriptVariableName);
      if (variableId==null) {
        variableId = scriptVariableName;
      }
      VariableInstanceImpl variableInstance = scopeInstance.findVariableInstance(variableId);
      if (variableInstance!=null) {
        DataType type = variableInstance.type;
        // NativeObject implements Map
        // NativeArray implements List
        // So the data type conversion from javascript to internal should work
        Object internalValue = type.convertJsonToInternalValue(nativeObject);
        TypedValueImpl value = new TypedValueImpl(type, internalValue);
        updatedValues.put(scriptVariableName, value);
      }
    }
    return updatedValues;
  }

  /** the dirty checking native objects will call this method when they are changed */
  protected void updated(String name) {
    log.debug("updated: "+name);
    updated.add(name);
  }

  protected Object convertInternalToNative(Object internalObject, String name) {
    if (internalObject==null) {
      return null;
    }
    Class< ? extends Object> valueClass = internalObject.getClass();
    if (String.class.isAssignableFrom(valueClass)
        || Number.class.isAssignableFrom(valueClass)) {
      return internalObject;
    }
    if (Map.class.isAssignableFrom(valueClass)) {
      return new DirtyCheckingNativeObject((Map)internalObject, name);
    }
    if (Collection.class.isAssignableFrom(valueClass)) {
      return new DirtyCheckingNativeArray((Collection)internalObject, name);
    }
    return null;
  }

  public class DirtyCheckingNativeArray extends NativeArray {
    String name;
    public DirtyCheckingNativeArray(Collection collection, String name) {
      super(collection.size());
      this.name = name;
      Iterator iterator = collection.iterator();
      int i=0;
      while (iterator.hasNext()) {
        Object nativeObject = convertInternalToNative(iterator.next(), name);
        this.set(i, nativeObject);
        i++;
      }
    }
    @Override
    public void add(int arg0, Object arg1) {
      updated(name);
      super.add(arg0, arg1);
    }
    @Override
    public boolean add(Object arg0) {
      updated(name);
      return super.add(arg0);
    }
    @Override
    public boolean addAll(Collection arg0) {
      updated(name);
      return super.addAll(arg0);
    }
    @Override
    public boolean addAll(int arg0, Collection arg1) {
      updated(name);
      return super.addAll(arg0, arg1);
    }
    @Override
    public void clear() {
      updated(name);
      super.clear();
    }
    @Override
    public void defineOwnProperty(Context arg0, Object arg1, ScriptableObject arg2) {
      updated(name);
      super.defineOwnProperty(arg0, arg1, arg2);
    }
    @Override
    public void delete(int arg0) {
      updated(name);
      super.delete(arg0);
    }
    @Override
    public Object execIdCall(IdFunctionObject arg0, Context arg1, Scriptable arg2, Scriptable arg3, Object[] arg4) {
      updated(name);
      return super.execIdCall(arg0, arg1, arg2, arg3, arg4);
    }
    @Override
    public void put(int arg0, Scriptable arg1, Object arg2) {
      updated(name);
      super.put(arg0, arg1, arg2);
    }
    @Override
    public void put(String arg0, Scriptable arg1, Object arg2) {
      updated(name);
      super.put(arg0, arg1, arg2);
    }
    @Override
    public Object remove(int arg0) {
      updated(name);
      return super.remove(arg0);
    }
    @Override
    public boolean remove(Object arg0) {
      updated(name);
      return super.remove(arg0);
    }
    @Override
    public boolean removeAll(Collection arg0) {
      updated(name);
      return super.removeAll(arg0);
    }
    @Override
    public boolean retainAll(Collection arg0) {
      updated(name);
      return super.retainAll(arg0);
    }
    @Override
    public Object set(int arg0, Object arg1) {
      updated(name);
      return super.set(arg0, arg1);
    }
    @Override
    protected void setInstanceIdValue(int arg0, Object arg1) {
      updated(name);
      super.setInstanceIdValue(arg0, arg1);
    }
    
  }

  public class DirtyCheckingNativeObject extends NativeObject {
    String name;
    public DirtyCheckingNativeObject(Map<String,Object> map, String name) {
      this.name = name;
      for (Map.Entry<String,Object> entry: map.entrySet()) {
        Object nativeObject = convertInternalToNative(entry.getValue(), name);
        this.put(entry.getKey(), this, nativeObject);
      }
    }

    @Override
    public void clear() {
      updated(name);
      super.clear();
    }

    @Override
    public Object put(Object arg0, Object arg1) {
      updated(name);
      return super.put(arg0, arg1);
    }

    @Override
    public void putAll(Map arg0) {
      updated(name);
      super.putAll(arg0);
    }

    @Override
    public Object remove(Object arg0) {
      updated(name);
      return super.remove(arg0);
    }

    @Override
    public void defineOwnProperty(Context arg0, Object arg1, ScriptableObject arg2) {
      updated(name);
      super.defineOwnProperty(arg0, arg1, arg2);
    }

    @Override
    public void put(String arg0, Scriptable arg1, Object arg2) {
      updated(name);
      super.put(arg0, arg1, arg2);
    }

    @Override
    public void setAttributes(String arg0, int arg1) {
      updated(name);
      super.setAttributes(arg0, arg1);
    }

    @Override
    public void defineConst(String arg0, Scriptable arg1) {
      updated(name);
      super.defineConst(arg0, arg1);
    }

    @Override
    public void defineFunctionProperties(String[] arg0, Class< ? > arg1, int arg2) {
      updated(name);
      super.defineFunctionProperties(arg0, arg1, arg2);
    }

    @Override
    public void defineOwnProperties(Context arg0, ScriptableObject arg1) {
      updated(name);
      super.defineOwnProperties(arg0, arg1);
    }

    @Override
    public void defineProperty(String arg0, Class< ? > arg1, int arg2) {
      updated(name);
      super.defineProperty(arg0, arg1, arg2);
    }

    @Override
    public void defineProperty(String arg0, Object arg1, int arg2) {
      updated(name);
      super.defineProperty(arg0, arg1, arg2);
    }

    @Override
    public void defineProperty(String arg0, Object arg1, Method arg2, Method arg3, int arg4) {
      updated(name);
      super.defineProperty(arg0, arg1, arg2, arg3, arg4);
    }

    @Override
    public void delete(int arg0) {
      updated(name);
      super.delete(arg0);
    }

    @Override
    public void put(int arg0, Scriptable arg1, Object arg2) {
      updated(name);
      super.put(arg0, arg1, arg2);
    }

    @Override
    public void putConst(String arg0, Scriptable arg1, Object arg2) {
      updated(name);
      super.putConst(arg0, arg1, arg2);
    }

    @Override
    public void setAttributes(int arg0, int arg1) {
      updated(name);
      super.setAttributes(arg0, arg1);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void setAttributes(int arg0, Scriptable arg1, int arg2) {
      updated(name);
      super.setAttributes(arg0, arg1, arg2);
    }

    @Override
    public void setGetterOrSetter(String arg0, int arg1, Callable arg2, boolean arg3) {
      updated(name);
      super.setGetterOrSetter(arg0, arg1, arg2, arg3);
    }
  }

  
  
  @Override
  public Object get(int index, Scriptable start) {
    throw new RuntimeException("not supported");
  }

  @Override
  public void put(int index, Scriptable start, Object value) {
    throw new RuntimeException("not supported");
  }

  @Override
  public boolean has(int index, Scriptable start) {
    throw new RuntimeException("not supported");
  }

  @Override
  public void delete(String name) {
    throw new RuntimeException("not supported");
  }

  @Override
  public void delete(int index) {
    throw new RuntimeException("not supported");
  }

  @Override
  public String getClassName() {
    throw new RuntimeException("not supported");
  }

  @Override
  public Object getDefaultValue(Class< ? > hint) {
    throw new RuntimeException("not supported");
  }

  @Override
  public boolean hasInstance(Scriptable instance) {
    throw new RuntimeException("not supported");
  }

  @Override
  public void setParentScope(Scriptable arg0) {
    throw new RuntimeException("not supported");
  }

  @Override
  public void setPrototype(Scriptable arg0) {
    throw new RuntimeException("not supported");
  }
}
