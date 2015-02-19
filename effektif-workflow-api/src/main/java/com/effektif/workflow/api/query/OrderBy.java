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
package com.effektif.workflow.api.query;


/**
 * @author Tom Baeyens
 */
public class OrderBy {
  
  protected String field;
  protected OrderDirection direction;
  
  public String getField() {
    return field;
  }
  
  public void setField(String field) {
    this.field = field;
  }
  
  public OrderBy field(String field) {
    this.field = field;
    return this;
  }
  
  public OrderDirection getDirection() {
    return direction;
  }
  
  public void setDirection(OrderDirection direction) {
    this.direction = direction;
  }

  public OrderBy direction(OrderDirection direction) {
    this.direction = direction;
    return this;
  }
}
