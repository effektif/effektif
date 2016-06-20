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

import com.effektif.workflow.api.bpmn.BpmnElement;
import com.effektif.workflow.api.bpmn.BpmnReader;
import com.effektif.workflow.api.bpmn.BpmnWriter;
import com.effektif.workflow.api.bpmn.XmlElement;
import com.effektif.workflow.api.json.TypeName;


/**
 * Logical <em>or</em>, for combining {@link Condition} instances.
 *
 * @author Tom Baeyens
 */
@TypeName("or")
@BpmnElement("or")
public class Or extends Condition {

  protected List<Condition> conditions;

  @Override
  public boolean isEmpty() {
    return conditions == null || conditions.isEmpty();
  }

  @Override
  public void readBpmn(BpmnReader r) {
    conditions = r.readConditions();
  }

  @Override
  public void writeBpmn(BpmnWriter w) {
    if (!isEmpty()) {
      w.startElementEffektif(getClass());
      for (Condition condition : conditions) {
        condition.writeBpmn(w);
      }
      w.endElement();
    }
  }

  public List<Condition> getConditions() {
    return this.conditions;
  }
  public void setConditions(List<Condition> conditions) {
    this.conditions = conditions;
  }
  
  public Or condition(Condition condition) {
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
        separator = " or ";
      } else {
        string.append(separator);
      }
      string.append(condition.toString());
    }
    return string.toString();
  }
}
