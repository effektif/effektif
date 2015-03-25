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

import org.mongodb.morphia.annotations.Embedded;

import com.effektif.model.types.binding.Binding;


/**
 * @author Tom Baeyens
 */
public abstract class SingleBindingExpression extends ConditionExpression {
  
  public static final String DB_left = "l";
  @Embedded(DB_left)
  public ValueExpression left;
  public SingleBindingExpression left(ValueExpression left) {
    this.left = left;
    return this;
  }
  
  protected Binding getBinding() {
    return (left!=null ? left.binding : null);
  }
}
