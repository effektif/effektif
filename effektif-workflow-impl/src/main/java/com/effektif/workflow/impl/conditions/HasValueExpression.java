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
package com.effektif.workflow.impl.conditions;

import java.util.Set;

import org.bson.types.ObjectId;

import com.effektif.imports.CloningContext;
import com.effektif.model.engine.ExecutionContext;
import com.effektif.model.types.binding.Binding;
import com.effektif.rest.json.RestType;


/**
 * @author Tom Baeyens
 */
@RestType("hasValue")
public class HasValueExpression extends SingleBindingExpression {
  
  @Override
  public boolean eval(ExecutionContext executionContext) {
    Binding binding = getBinding();
    TypedValue typedValue = executionContext.activityInstance.getVariableTypedValue(binding);
    if (typedValue==null) {
      return false;
    }
    Object value = typedValue.getValueInExpressionFormat();
    return eval(value);
  }

  protected boolean eval(Object value) {
    return value!=null && !"".equals(value);
  }

  @Override
  public void collectVariableIdsUsed(Set<ObjectId> variableIdsUsed) {
    Binding binding = getBinding();
    addVariableIdUsed(variableIdsUsed, binding);
  }
  
  @Override
  public String toString(ExecutionContext executionContext) {
    Binding binding = getBinding();
    return "("+toString(binding, executionContext)+" has value)";
  }
  
  @Override
  public void onClone(CloningContext ctx) {
    Binding binding = getBinding();
    if (binding!=null) {
      binding.onClone(ctx);
    }
  }
}
