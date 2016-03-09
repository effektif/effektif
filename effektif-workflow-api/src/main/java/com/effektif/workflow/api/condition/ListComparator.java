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
import com.effektif.workflow.api.bpmn.XmlElement;
import com.effektif.workflow.api.types.ListType;
import com.effektif.workflow.api.workflow.Binding;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A {@link Condition} based on a binary operator.
 *
 * @author Tom Baeyens
 */
public abstract class ListComparator extends Condition {

  protected Binding<?> left;
  protected List<Binding> rights;

  @Override
  public boolean isEmpty() {
    return left == null && rights == null;
  }

  public Binding<?> getLeft() {
    return this.left;
  }

  public void setLeft(Binding<?> left) {
    this.left = left;
  }

  public ListComparator left(Binding<?> left) {
    this.left = left;
    return this;
  }

  public ListComparator leftExpression(String leftExpression) {
    this.left = new Binding().expression(leftExpression);
    return this;
  }

  public List<Binding> getRights() {
    return this.rights;
  }

  public void setRights(List<Binding> rights) {
    this.rights = rights;
  }

  public ListComparator right(List<Binding> right) {
    this.rights = right;
    return this;
  }

  public ListComparator rightValue(List<Object> rightValues) {
    this.rights = new ArrayList<>();
    rights.addAll(rightValues.stream().map(rightValue -> new Binding().value(rightValue)).collect(Collectors.toList()));
    return this;
  }

  public ListComparator rightExpression(List<String> rightExpressions) {
    this.rights = new ArrayList<>();
    rights.addAll(rightExpressions.stream().map(rightExpression -> new Binding().value(rightExpression)).collect(Collectors.toList()));
    return this;
  }

  @Override
  public String toString() {
    return "( " + toString(left) + " " + getName() + " " + toString(rights) + " )";
  }

  protected abstract String getName();

  @Override
  public void readBpmn(BpmnReader r) {
    for (XmlElement containsElement : r.readElementsEffektif(getClass())) {
      r.startElement(containsElement);
      left = r.readBinding("left", String.class);
      rights = r.readRawBindings("rights");
      r.endElement();
    }
  }

  @Override
  public void writeBpmn(BpmnWriter w) {
    w.startElementEffektif(getClass());
    w.writeBinding("left", getLeft());
    w.writeRawBindings("rights", getRights());
    w.endElement();
  }
}
