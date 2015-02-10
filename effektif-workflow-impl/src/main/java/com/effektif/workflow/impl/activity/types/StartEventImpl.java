/* Copyright 2014 Effektif GmbH.
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
package com.effektif.workflow.impl.activity.types;

import com.effektif.workflow.api.activities.StartEvent;
import com.effektif.workflow.api.xml.XmlElement;
import com.effektif.workflow.impl.activity.AbstractActivityType;
import com.effektif.workflow.impl.bpmn.BpmnReader;
import com.effektif.workflow.impl.bpmn.BpmnWriter;
import com.effektif.workflow.impl.workflowinstance.ActivityInstanceImpl;


public class StartEventImpl extends AbstractActivityType<StartEvent> {

  public StartEventImpl() {
    super(StartEvent.class);
  }
  
  @Override
  public StartEvent readBpmn(XmlElement activityXml, BpmnReader bpmnReader) {
    if (!bpmnReader.isLocalPart(activityXml, "startEvent")) {
      return null;
    }
    StartEvent startEvent = new StartEvent();
    startEvent.id(bpmnReader.readBpmnAttribute(activityXml, "id"));
    return startEvent;
  }

  @Override
  public void writeBpmn(StartEvent startEvent, XmlElement startEventXml, BpmnWriter bpmnWriter) {
    bpmnWriter.setBpmnName(startEventXml, "startEvent");
    bpmnWriter.writeBpmnAttribute(startEventXml, "id", startEvent.getId());
  }

  @Override
  public void execute(ActivityInstanceImpl activityInstance) {
    activityInstance.onwards();
  }
  
  @Override
  public boolean isFlushSkippable() {
    return true;
  }

}
