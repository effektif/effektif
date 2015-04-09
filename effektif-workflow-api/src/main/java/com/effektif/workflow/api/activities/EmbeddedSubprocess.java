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

import com.effektif.workflow.api.mapper.BpmnElement;
import com.effektif.workflow.api.workflow.Activity;
import com.fasterxml.jackson.annotation.JsonTypeName;


/**
 * Invokes a workflow that is embedded in another workflow.
 *
 * @see <a href="https://github.com/effektif/effektif/wiki/Embedded-Subprocess">Embedded Subprocess</a>
 * @author Tom Baeyens
 */
@JsonTypeName("embeddedSubprocess")
@BpmnElement("subProcess")
public class EmbeddedSubprocess extends Activity {

//  @Override
//  public EmbeddedSubprocess readBpmn(XmlElement xml, BpmnReader reader) {
//    if (!reader.isLocalPart(xml, BPMN_ELEMENT_NAME)) {
//      return null;
//    }
//    EmbeddedSubprocess activity = new EmbeddedSubprocess();
//    return activity;
//  }
//
//  @Override
//  public void writeBpmn(EmbeddedSubprocess activity, XmlElement xml, BpmnWriter writer) {
//    writer.setBpmnName(xml, BPMN_ELEMENT_NAME);
//    writer.writeBpmnAttribute(xml, "id", activity.getId());
//  }


}
