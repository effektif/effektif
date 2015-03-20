/* Copyright (c) 2014, Effektif GmbH.
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
 * limitations under the License. */
package com.effektif.workflow.api.model;


/**
 * @author Tom Baeyens
 */
public class Money {

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
}
