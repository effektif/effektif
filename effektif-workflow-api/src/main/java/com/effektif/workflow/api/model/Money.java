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
package com.effektif.workflow.api.model;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;


/**
 * @author Tom Baeyens
 */
public class Money {
  
  static DecimalFormat formatter = new DecimalFormat("###.##", new DecimalFormatSymbols(Locale.ENGLISH));
  
  protected Double amount;

  public Double getAmount() {
    return this.amount;
  }
  public void setAmount(Double amount) {
    this.amount = amount;
  }
  public Money amount(Double amount) {
    this.amount = amount;
    return this;
  }
  
  protected String currency;

  public String getCurrency() {
    return this.currency;
  }
  public void setCurrency(String currency) {
    this.currency = currency;
  }
  public Money currency(String currency) {
    this.currency = currency;
    return this;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    Money money = (Money) o;

    if (!amount.equals(money.amount))
      return false;
    return currency.equals(money.currency);

  }

  @Override
  public int hashCode() {
    int result = amount.hashCode();
    result = 31 * result + currency.hashCode();
    return result;
  }

  @Override
  public String toString() {
    StringBuilder moneyText = new StringBuilder();
    if (amount!=null) {
      moneyText.append(formatter.format(amount));
    } else {
      moneyText.append("0");
    }
    if (currency!=null) {
      moneyText.append(" ");
      moneyText.append(currency);
    }
    return moneyText.toString();
  }
}
