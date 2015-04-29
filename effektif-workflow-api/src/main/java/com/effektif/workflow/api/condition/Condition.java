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

import com.effektif.workflow.api.bpmn.BpmnReadable;
import com.effektif.workflow.api.bpmn.BpmnWritable;

/**
 * The process engine uses a <code>Condition</code> subclass to determine which
 * {@link com.effektif.workflow.api.workflow.Transition} to take after an
 * {@link com.effektif.workflow.api.activities.ExclusiveGateway}.
 *
 * @author Tom Baeyens
 */
public abstract class Condition implements BpmnReadable, BpmnWritable {

  /***
   * Returns true if and only if this instance defines a condition.
   */
  public abstract boolean isEmpty();

  protected String toString(Object o) {
    if (o==null) {
      return "null";
    }
    return o.toString();
  }
}
