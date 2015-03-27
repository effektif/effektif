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

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonTypeName;


/**
 * @author Tom Baeyens
 */
@JsonTypeName("and")
public class And extends Condition {

  protected List<Condition> conditions;

  public List<Condition> getConditions() {
    return this.conditions;
  }
  public void setConditions(List<Condition> conditions) {
    this.conditions = conditions;
  }
  
  public And condition(Condition condition) {
    if (conditions==null) {
      conditions = new ArrayList<>();
    }
    conditions.add(condition);
    return this;
  }

  @Override
  public String toString() {
    if (conditions==null) {
      return "true";
    }
    StringBuilder string = new StringBuilder();
    String separator = null;
    for (Condition condition: conditions) {
      if (separator==null) {
        separator = " and ";
      } else {
        string.append(separator);
      }
      string.append(toString(condition));
    }
    return string.toString();
  }
}
