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
package com.effektif.workflow.api.condition;

import com.effektif.workflow.api.bpmn.BpmnReader;
import com.effektif.workflow.api.bpmn.BpmnWriter;
import com.effektif.workflow.api.workflow.Binding;


/**
 * A {@link Condition} based on a unary operator.
 *
 * @author Tom Baeyens
 */
public abstract class SingleBindingCondition extends Condition {
  
  protected Binding<?> left;

  @Override
  public boolean isEmpty() {
    return left == null || left.isEmpty();
  }

  @Override
  public void readBpmn(BpmnReader r) {
    left = r.readBinding(getClass(), Object.class);
  }

  @Override
  public void writeBpmn(BpmnWriter w) {
    if (!isEmpty()) {
      w.writeBinding(getClass(), getLeft());
    }
  }

  public Binding<?> getLeft() {
    return this.left;
  }
  public void setLeft(Binding<?> left) {
    this.left = left;
  }
  public SingleBindingCondition left(Binding<?> left) {
    this.left = left;
    return this;
  }

  public SingleBindingCondition leftExpression(String leftExpression) {
    this.left = new Binding().expression(leftExpression);
    return this;
  }
}
