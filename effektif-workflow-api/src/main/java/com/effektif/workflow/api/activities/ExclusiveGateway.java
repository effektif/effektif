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
package com.effektif.workflow.api.activities;

import com.effektif.workflow.api.serialization.bpmn.BpmnElement;
import com.effektif.workflow.api.serialization.bpmn.BpmnReader;
import com.effektif.workflow.api.serialization.bpmn.BpmnWriter;
import com.effektif.workflow.api.serialization.json.TypeName;
import com.effektif.workflow.api.workflow.Activity;


/**
 * An exclusive gateway, used to fork and join sequence flows for conditionally executing tasks.
 *
 * @see <a href="https://github.com/effektif/effektif/wiki/Exclusive-Gateway">Exclusive Gateway</a>
 * @author Tom Baeyens
 */
@TypeName("exclusiveGateway")
@BpmnElement("exclusiveGateway")
public class ExclusiveGateway extends Activity {

  @Override
  public void readBpmn(BpmnReader r) {
    super.readBpmn(r);
    defaultTransitionId = r.readStringAttributeEffektif("defaultTransitionId");
  }

  @Override
  public void writeBpmn(BpmnWriter w) {
    super.writeBpmn(w);
    w.writeStringAttributeEffektif("defaultTransitionId", defaultTransitionId);
  }
}
