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

import com.effektif.workflow.api.mapper.JsonReader;
import com.effektif.workflow.api.mapper.JsonWriter;
import com.effektif.workflow.api.workflow.Binding;


/**
 * @author Tom Baeyens
 */
public abstract class Comparator extends Condition {
  
  protected Binding<?> left;
  protected Binding<?> right;

  @Override
  public void readJson(JsonReader r) {
    left = r.readBinding("left");
    right = r.readBinding("right");
  }

  @Override
  public void writeJson(JsonWriter w) {
    w.writeBinding("left", left);
    w.writeBinding("right", right);
  }
  
  public Binding<?> getLeft() {
    return this.left;
  }
  public void setLeft(Binding<?> left) {
    this.left = left;
  }
  public Comparator left(Binding<?> left) {
    this.left = left;
    return this;
  }
  public Comparator leftExpression(String leftExpression) {
    this.left = new Binding().expression(leftExpression);
    return this;
  }

  public Binding<?> getRight() {
    return this.right;
  }
  public void setRight(Binding<?> right) {
    this.right = right;
  }
  public Comparator right(Binding<?> right) {
    this.right = right;
    return this;
  }
  public Comparator rightValue(Object rightValue) {
    this.right = new Binding().value(rightValue);
    return this;
  }
  public Comparator right(String rightExpression) {
    this.right = new Binding().expression(rightExpression);
    return this;
  }
  
  @Override
  public String toString() {
    return "( "+toString(left)+" "+getName()+" "+toString(right)+" )";
  }
  
  protected abstract String getName();
}
