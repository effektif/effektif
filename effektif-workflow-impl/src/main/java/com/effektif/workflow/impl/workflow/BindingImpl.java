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
package com.effektif.workflow.impl.workflow;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.effektif.workflow.api.Configuration;
import com.effektif.workflow.impl.data.DataType;
import com.effektif.workflow.impl.data.TypedValueImpl;
import com.effektif.workflow.impl.data.types.ObjectTypeImpl;
import com.effektif.workflow.impl.script.ExpressionService;
import com.effektif.workflow.impl.script.ScriptImpl;
import com.effektif.workflow.impl.script.ScriptResult;
import com.effektif.workflow.impl.workflowinstance.ScopeInstanceImpl;
import com.effektif.workflow.impl.workflowinstance.VariableInstanceImpl;


/**
 * @author Tom Baeyens
 */
public class BindingImpl<T> {

  public DataType dataType;

  public T value;
  public String variableId;
  public List<String> fields;
  public ExpressionService expressionService; 
  public ScriptImpl expression;
  public List<BindingImpl<T>> bindings;
  
  public BindingImpl(Configuration configuration) {
    this.expressionService = configuration.get(ExpressionService.class);
  }
  
  public T getValue(ScopeInstanceImpl scopeInstance) {
    // IDEA : you might want to coerse value returned to this.dataType (ensure initialized properly)
    if (this.value!=null) {
      return value;
    
    } else if (this.variableId!=null) {
      TypedValueImpl typedValue = getTypedFieldValue(scopeInstance, variableId, fields);
      return (T) typedValue.getValue();
      
    } else if (this.expression!=null) {
      ScriptResult scriptResult = expression.evaluate(scopeInstance);
      Object value = scriptResult.getResult();
      return (T) value;
      
    } else if (this.bindings!=null) {
      List<Object> values = new ArrayList<>();
      for (BindingImpl binding: this.bindings) {
        Object elementValue = binding.getValue(scopeInstance);
        if (elementValue!=null ) {
          if (Collection.class.isAssignableFrom(elementValue.getClass())) {
            Iterator iterator = ((Collection)elementValue).iterator();
            while (iterator.hasNext()) {
              values.add(iterator.next());
            }
          } else {
            values.add(elementValue);
          }
        }
      }
      return (T) values;
    }
    
    return null;
  }

  public static TypedValueImpl getTypedFieldValue(ScopeInstanceImpl scopeInstance, String variableId, List<String> fields) {
    VariableInstanceImpl variableInstance = scopeInstance.findVariableInstance(variableId);
    if (fields==null) {
      return variableInstance.getTypedValue();
    }
    TypedValueImpl typedValue = new TypedValueImpl(variableInstance.type, variableInstance.getValue());
    for (String field: fields) {
      ObjectTypeImpl<?> objectType = (ObjectTypeImpl< ? >) typedValue.type; 
      objectType.dereference(typedValue, field);
    }
    return typedValue;
  }
}
