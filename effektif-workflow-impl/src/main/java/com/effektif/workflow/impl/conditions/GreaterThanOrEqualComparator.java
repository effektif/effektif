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

import com.effektif.model.engine.ExecutionContext;
import com.effektif.model.types.Types;
import com.effektif.rest.json.RestType;


/**
 * @author Tom Baeyens
 */
@RestType("greaterThanOrEqual")
public class GreaterThanOrEqualComparator extends Comparator {

  @Override
  public boolean compare(TypedValue leftTypedValue, TypedValue rightTypedValue, ExecutionContext executionContext) {
    Object leftValue = leftTypedValue!=null ? leftTypedValue.value : null;
    Object rightValue = rightTypedValue!=null ? rightTypedValue.value : null;
    if (leftValue==null && rightValue==null) return true;
    if (leftValue!=null && rightValue==null) return false;
    if (leftValue==null && rightValue!=null) return false;

    if (leftTypedValue.type.getClass()==Types.NUMBER.getClass()
        && rightTypedValue.type.getClass()==Types.NUMBER.getClass()) {
      return ((Double)leftValue) >= ((Double)rightValue);
    }
    return false;
  }
  
  @Override
  public String toString(ExecutionContext executionContext) {
    return "( "+left.toString(executionContext)+" greater than or equal to "+right.toString(executionContext)+" )";
  }

}
