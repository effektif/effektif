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
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.mozilla.javascript.Callable;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.IdFunctionObject;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.effektif.workflow.impl.data.DataType;
import com.effektif.workflow.impl.data.TypedValueImpl;
import com.effektif.workflow.impl.data.types.ListTypeImpl;
import com.effektif.workflow.impl.workflowinstance.ScopeInstanceImpl;
import com.effektif.workflow.impl.workflowinstance.VariableInstanceImpl;


/**
 * @author Tom Baeyens
 */
public class RhinoVariableScope implements Scriptable {

  private static final Logger log = LoggerFactory.getLogger(RhinoScriptService.class);
  
  protected ScopeInstanceImpl scopeInstance;
  protected Scriptable parentScope;
  
  public Map<String,Object> objects;
  protected Map<String,Callable> functions = null;
  protected Set<String> updated;
  protected Map<String,String> mappings;
  
  public RhinoVariableScope(ScopeInstanceImpl scopeInstance, Map<String,String> scriptToWorkflowMappings, PrintWriter console, Scriptable parentScope) {
    this.scopeInstance = scopeInstance;
    this.parentScope = parentScope;
    this.mappings = scriptToWorkflowMappings;
    this.updated = new HashSet<>();
    initializeObjects(console);
    initializeFunctions();
  }

  protected void initializeObjects(PrintWriter console) {
    this.objects = new HashMap<>();
    this.objects.put("console", new Console(console)); 
    this.objects.put("JSON", new JSON());
  }
  
  protected void initializeFunctions() {
    functions = new HashMap<>();
    functions.put("contains", new Callable() {
      @Override
      public Object call(Context context, Scriptable scope, Scriptable thisObject, Object[] args) {
        if (args==null || args.length!=2 || args[0]==null) {
          return false;
        }
        if (args[0] instanceof String) {
          return ((String)args[0]).contains((CharSequence) args[1]);
        }
        if (args[0] instanceof Collection) {
          return ((Collection)args[0]).contains(args[1]);
        }
        return false;
      }
    });
  }
  
  @Override
  public Object get(String name, Scriptable start) {
    log.debug("get "+name+" | "+start);
    if (objects.containsKey(name)) {
      return objects.get(name);
    }
    if (functions.containsKey(name)) {
      return functions.get(name);
    }
    String variableId = mappings!=null ? mappings.get(name) : null;
    if (variableId==null) {
      variableId = name;
    }
    Object nativeValue = null;
    VariableInstanceImpl variableInstance = scopeInstance.findVariableInstance(variableId);
    if (variableInstance!=null) {
      TypedValueImpl typedValue = variableInstance.getTypedValue();
      log.debug("  lazy loaded variable "+name+" = "+(typedValue!=null ? typedValue.value : "null"));
      nativeValue = convertInternalToNative(typedValue, name);
    }
    objects.put(name, nativeValue);
    return nativeValue;
  }
  
  @Override
  public boolean has(String name, Scriptable start) {
    log.debug("has "+name+" | "+start);
    if (objects.containsKey(name)
        || functions.containsKey(name)) {
      return true;
    }
    String variableId = mappings!=null ? mappings.get(name) : null;
    if (variableId==null) {
      variableId = name;
    }
    return scopeInstance.findVariableInstance(variableId)!=null;
  }

  @Override
  public void put(String name, Scriptable start, Object value) {
    log.debug("put "+name+" | "+start+" | "+value);
    objects.put(name, value);
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
      Object localObject = objects.get(scriptVariableName);
      String variableId = mappings!=null ? mappings.get(scriptVariableName) : null;
      if (variableId==null) {
        variableId = scriptVariableName;
      }
      VariableInstanceImpl variableInstance = scopeInstance.findVariableInstance(variableId);
      if (variableInstance!=null) {
        DataType type = variableInstance.type;
        // NativeObject implements Map
        // NativeArray implements List
        // So the data type conversion from javascript to internal should work
        Object value = null;
        if (localObject instanceof DirtyCheckingNativeObject) {
          DirtyCheckingNativeObject nativeObject = (DirtyCheckingNativeObject) localObject;
          value = nativeObject.value;
        } else if (localObject instanceof DirtyCheckingNativeArray) {
          DirtyCheckingNativeArray nativeArray = (DirtyCheckingNativeArray) localObject;
          value = nativeArray.values;
        } else {
          value = localObject;
        }
        TypedValueImpl typedValue = new TypedValueImpl(type, value);
        updatedValues.put(variableId, typedValue);
      }
    }
    return updatedValues;
  }

  /** the dirty checking native objects will call this method when they are changed */
  protected void updated(String name) {
    log.debug("updated: "+name);
    updated.add(name);
  }

  protected Object convertInternalToNative(TypedValueImpl typedValue, String name) {
    if (typedValue==null) {
      return null;
    }
    return convertInternalToNative(typedValue.type,  typedValue.value, name);
  }

  protected Object convertInternalToNative(DataType type, Object value, String name) {
    if (value==null) {
      return null;
    }
    Class< ? extends Object> valueClass = value.getClass();
    if (String.class.isAssignableFrom(valueClass)
        || Number.class.isAssignableFrom(valueClass)
        || Boolean.class.isAssignableFrom(valueClass)) {
      return value;
    }
    if (type instanceof ListTypeImpl) {
      ListTypeImpl listType = (ListTypeImpl) type;
      return new DirtyCheckingNativeArray(listType.elementType, (List<Object>) value, name);
    }
    return new DirtyCheckingNativeObject(type, value, name);
  }

  public class DirtyCheckingNativeArray extends NativeArray {
    private static final long serialVersionUID = 1L;
    String name;
    DataType elementType;
    List<Object> values;
    public DirtyCheckingNativeArray(DataType elementType, List<Object> values, String name) {
      super(values.size());
      this.name = name;
      this.values = values;
    }
    
    @Override
    public Object get(int index, Scriptable arg1) {
      log.debug("  get index "+index);
      Object elementValue = values.get(index);
      return convertInternalToNative(elementType, elementValue, name);
    }

    @Override
    public Object get(int index) {
      return get(index, null);
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
    private static final long serialVersionUID = 1L;
    String name;
    DataType type;
    Object value;
    public DirtyCheckingNativeObject(DataType type, Object value, String name) {
      this.name = name;
      this.type = type;
      this.value = value;
    }
    
    @Override
    public boolean has(String field, Scriptable arg1) {
      log.debug("  has field "+field);
      return true;
    }

    @Override
    public Object get(String field, Scriptable arg1) {
      log.debug("  get field "+field);
      TypedValueImpl typedFieldValue = type.dereference(value, field);
      return convertInternalToNative(typedFieldValue, field);
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
