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
import org.mongodb.morphia.annotations.Embedded;

import com.effektif.imports.CloningContext;
import com.effektif.model.engine.ExecutionContext;
import com.effektif.rest.json.RestType;


/**
 * @author Tom Baeyens
 */
@RestType("not")
public class NotExpression extends ConditionExpression {

  public static final String DB_expression = "e";
  @Embedded(DB_expression)
  public ConditionExpression expression;

  public NotExpression expression(ConditionExpression expression) {
    this.expression = expression;
    return this;
  }
  
  @Override
  public boolean eval(ExecutionContext executionContext) {
    return !expression.eval(executionContext);
  }
  
  @Override
  public void collectVariableIdsUsed(Set<ObjectId> variableIdsUsed) {
    if (expression!=null) {
      expression.collectVariableIdsUsed(variableIdsUsed);
    }
  }

  @Override
  public String toString(ExecutionContext executionContext) {
    return "( not "+expression+" )";
  }

  public void onClone(CloningContext ctx) {
    if (expression != null) {
      expression.onClone(ctx);
    }
  }
}
