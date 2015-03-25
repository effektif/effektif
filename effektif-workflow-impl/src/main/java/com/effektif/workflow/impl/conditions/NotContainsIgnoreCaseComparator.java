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
@RestType("notContainsIgnoreCase")
public class NotContainsIgnoreCaseComparator extends ContainsIgnoreCaseComparator {

  @Override
  public boolean compare(TypedValue leftValue, TypedValue rightValue, ExecutionContext executionContext) {
    return !super.compare(leftValue, rightValue, executionContext);
  }

  @Override
  public String toString(ExecutionContext executionContext) {
    return "( "+left.toString(executionContext)+" does not contain ignore case "+right.toString(executionContext)+" )";
  }

}
