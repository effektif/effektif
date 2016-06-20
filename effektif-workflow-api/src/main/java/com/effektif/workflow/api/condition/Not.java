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

import com.effektif.workflow.api.bpmn.BpmnElement;
import com.effektif.workflow.api.bpmn.BpmnReader;
import com.effektif.workflow.api.bpmn.BpmnWriter;
import com.effektif.workflow.api.bpmn.XmlElement;
import com.effektif.workflow.api.json.TypeName;


/**
 * Logical <em>not</em>, used to transform a {@link Condition}.
 *
 * @author Tom Baeyens
 */
@TypeName("not")
@BpmnElement("not")
public class Not extends Condition {

  protected Condition condition;

  @Override
  public boolean isEmpty() {
    return condition == null || condition.isEmpty();
  }

  @Override
  public void readBpmn(BpmnReader r) {
    condition = r.readCondition();
    r.endElement();
  }

  @Override
  public void writeBpmn(BpmnWriter w) {
    if (!isEmpty()) {
      w.startElementEffektif(getClass());
      if (condition != null) {
        condition.writeBpmn(w);
      }
      w.endElement();
    }
  }

  public Condition getCondition() {
    return this.condition;
  }
  public void setCondition(Condition condition) {
    this.condition = condition;
  }
  public Not condition(Condition condition) {
    this.condition = condition;
    return this;
  }

  @Override
  public String toString() {
    return "(not "+toString(condition)+")";
  }

}
