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
import com.effektif.rest.json.RestType;


/**
 * @author Tom Baeyens
 */
@RestType("containsIgnoreCase")
public class ContainsIgnoreCaseComparator extends Comparator {

  @Override
  public boolean compare(TypedValue leftTypedValue, TypedValue rightTypedValue, ExecutionContext executionContext) {
    Object leftValue = leftTypedValue!=null ? leftTypedValue.value : null;
    Object rightValue = rightTypedValue!=null ? rightTypedValue.value : null;
    if (leftValue==null && rightValue==null) return true;
    if (leftValue!=null && rightValue==null) return true;
    if (leftValue==null && rightValue!=null) return false;
    String leftString = (String) leftValue;
    String rightString = (String) rightValue;
    return leftString.toLowerCase().contains(rightString.toLowerCase());
  }
  
  @Override
  public String toString(ExecutionContext executionContext) {
    return "( "+left.toString(executionContext)+" contains ignore case "+right.toString(executionContext)+" )";
  }

}
