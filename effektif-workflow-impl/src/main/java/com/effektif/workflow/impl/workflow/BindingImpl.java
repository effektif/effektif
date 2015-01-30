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
package com.effektif.workflow.impl.workflow;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.effektif.workflow.api.Configuration;
import com.effektif.workflow.impl.data.DataType;
import com.effektif.workflow.impl.data.TypedValueImpl;
import com.effektif.workflow.impl.data.types.ListTypeImpl;
import com.effektif.workflow.impl.data.types.ObjectTypeImpl;
import com.effektif.workflow.impl.script.ExpressionService;
import com.effektif.workflow.impl.script.ScriptImpl;
import com.effektif.workflow.impl.script.ScriptResult;
import com.effektif.workflow.impl.workflowinstance.ScopeInstanceImpl;


public class BindingImpl<T> {

  public TypedValueImpl typedValue;
  public String variableId;
  public List<String> fields;
  public ExpressionService expressionService; 
  public ScriptImpl expression;
  public DataType expressionExpectedType;
  public List<BindingImpl> bindings;
  public DataType bindingsElementType;
  
  public BindingImpl(Configuration configuration) {
    this.expressionService = configuration.get(ExpressionService.class);
  }
  
  public TypedValueImpl getTypedValue(ScopeInstanceImpl scopeInstance) {
    TypedValueImpl typedValue = null;
    if (this.typedValue!=null) {
      typedValue = this.typedValue;
    
    } else if (this.variableId!=null) {
      typedValue = scopeInstance.getTypedValue(this.variableId);
      if (this.fields!=null) {
        for (String field: this.fields) {
          ObjectTypeImpl<?> objectType = (ObjectTypeImpl< ? >) typedValue.type; 
          objectType.dereference(typedValue, field);
        }
      }
      
    } else if (this.expression!=null) {
      ScriptResult scriptResult = expression.evaluate(scopeInstance);
      Object result = scriptResult.getResult();
      if (expressionExpectedType!=null) {
        // TODO result = expressionExpectedType.shoehorn(result);
        typedValue = new TypedValueImpl(expressionExpectedType, result);
      }
      
    } else if (this.bindings!=null) {
      List<Object> values = new ArrayList<>();
      for (BindingImpl binding: this.bindings) {
        TypedValueImpl elementValue = binding.getTypedValue(scopeInstance);
        if (elementValue!=null && elementValue.value!=null) {
          if (Collection.class.isAssignableFrom(elementValue.value.getClass())) {
            Iterator iterator = ((Collection)elementValue.value).iterator();
            while (iterator.hasNext()) {
              values.add(iterator.next());
            }
          } else {
            values.add(elementValue.value);
          }
        }
      }
      ListTypeImpl type = new ListTypeImpl(bindingsElementType);
      return new TypedValueImpl(type, values);
    }
    return typedValue;
  }
}
