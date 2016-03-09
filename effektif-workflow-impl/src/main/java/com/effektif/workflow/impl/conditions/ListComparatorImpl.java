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

import com.effektif.workflow.api.condition.ListComparator;
import com.effektif.workflow.impl.WorkflowParser;
import com.effektif.workflow.impl.data.DataTypeImpl;
import com.effektif.workflow.impl.data.TypedValueImpl;
import com.effektif.workflow.impl.data.types.ListTypeImpl;
import com.effektif.workflow.impl.util.StringUtil;
import com.effektif.workflow.impl.workflow.BindingImpl;
import com.effektif.workflow.impl.workflowinstance.ScopeInstanceImpl;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Markus Vogt
 */
public abstract class ListComparatorImpl implements ConditionImpl<ListComparator> {

  protected BindingImpl<?> left;
  protected List<BindingImpl<?>> right;
  private ArrayList<Object> aggregatedList;

  public BindingImpl<?> getLeft() {
    return this.left;
  }

  public void setLeft(BindingImpl<?> left) {
    this.left = left;
  }

  public ListComparatorImpl left(BindingImpl<?> left) {
    this.left = left;
    return this;
  }

  public List<BindingImpl<?>> getRight() {
    return this.right;
  }

  public void setRight(List<BindingImpl<?>> right) {
    this.right = right;
  }

  public ListComparatorImpl right(List<BindingImpl<?>> right) {
    this.right = right;
    return this;
  }

  @Override
  public boolean eval(ScopeInstanceImpl scopeInstance) {
    TypedValueImpl leftTypedValue = scopeInstance.getTypedValue(left);

    List<TypedValueImpl> rightTypedValues = new ArrayList<>();
    for (BindingImpl<?> binding : right) {
      rightTypedValues.add(scopeInstance.getTypedValue(binding));
    }

    return compare(leftTypedValue, rightTypedValues, scopeInstance);
  }

  protected abstract boolean compare(TypedValueImpl leftTypedValue, List<TypedValueImpl> typedValueList,
                                  ScopeInstanceImpl scopeInstance);

  public static boolean isNotNull(TypedValueImpl typedValue) {
    return !isNull(typedValue);
  }

  public static boolean isNull(TypedValueImpl typedValue) {
    return typedValue == null || typedValue.value == null;
  }

  public static boolean isNotNull(List<TypedValueImpl> typedValues) {
    return !isNull(typedValues);
  }

  public static boolean isNull(List<TypedValueImpl> typedValues) {
    return typedValues == null || typedValues.isEmpty();
  }

  @Override
  public void parse(ListComparator comparator, ConditionService conditionService, WorkflowParser parser) {
    this.left = parser.parseBinding(comparator.getLeft(), "left");
    this.right = parser.parseRawBindings(comparator.getRights(), "rights");
  }

  public String toString() {
    return StringUtil.toString(left) + getComparatorSymbol() + StringUtil.toString(right);
  }

  public abstract String getComparatorSymbol();

  protected List getAggregatedFlatList(List<TypedValueImpl> valueList) {
    aggregatedList = new ArrayList<>();
    for (TypedValueImpl typedValue : valueList) {
      aggregateValuesToFlatList(typedValue);
    }
    return aggregatedList;
  }

  @SuppressWarnings("unchecked")
  private void aggregateValuesToFlatList(TypedValueImpl typedValue) {
    DataTypeImpl type = typedValue.type;
    if (type instanceof ListTypeImpl) {
      handleListType(typedValue, (ListTypeImpl) type);
    } else {
      aggregatedList.add(typedValue.value);
    }
  }

  @SuppressWarnings("unchecked")
  private void handleListType(TypedValueImpl typedValue, ListTypeImpl type) {
    List valueList = (List) typedValue.value;
    if (type.elementType instanceof ListTypeImpl) {
      aggregateValuesToFlatList((TypedValueImpl) typedValue.value);
    } else {
      aggregatedList.addAll(valueList);
    }
  }
}
