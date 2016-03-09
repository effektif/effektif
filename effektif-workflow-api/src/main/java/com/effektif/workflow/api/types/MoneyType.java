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
package com.effektif.workflow.api.types;

import java.lang.reflect.Type;

import com.effektif.workflow.api.bpmn.BpmnReader;
import com.effektif.workflow.api.bpmn.BpmnWriter;
import com.effektif.workflow.api.json.TypeName;
import com.effektif.workflow.api.model.Money;


/**
 * @author Tom Baeyens
 */
@TypeName("money")
public class MoneyType extends DataType {

  public static final MoneyType INSTANCE = new MoneyType();
  
  @Override
  public Type getValueType() {
    return Money.class;
  }

  @Override
  public Object readBpmnValue(BpmnReader r) {
    String currency = r.readStringAttributeEffektif("currency");
    String amountString = r.readStringAttributeEffektif("amount");
    try {
      Double amount = amountString == null ? null : Double.valueOf(amountString);
      return new Money().currency(currency).amount(amount);
    }
    catch (NumberFormatException e) {
      return null;
    }
  }

  @Override
  public void writeBpmnValue(BpmnWriter w, Object value) {
    if (value != null && value instanceof Money) {
      Money moneyValue = (Money) value;
      w.writeStringAttributeEffektif("currency", moneyValue.getCurrency());
      w.writeStringAttributeEffektif("amount", moneyValue.getAmount());
    }
  }

}
