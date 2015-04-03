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
package com.effektif.workflow.impl.activity.types;

import com.effektif.workflow.api.activities.StartEvent;
import com.effektif.workflow.api.mapper.XmlElement;
import com.effektif.workflow.impl.activity.AbstractActivityType;
import com.effektif.workflow.impl.bpmn.BpmnReader;
import com.effektif.workflow.impl.bpmn.BpmnWriter;
import com.effektif.workflow.impl.workflowinstance.ActivityInstanceImpl;


/**
 * @author Tom Baeyens
 */
public class StartEventImpl extends AbstractActivityType<StartEvent> {

  private static final String BPMN_ELEMENT_NAME = "startEvent";

  public StartEventImpl() {
    super(StartEvent.class);
  }
  
  @Override
  public StartEvent readBpmn(XmlElement xml, BpmnReader reader) {
    if (!reader.isLocalPart(xml, BPMN_ELEMENT_NAME)) {
      return null;
    }
    StartEvent event = new StartEvent();
    return event;
  }

  @Override
  public void writeBpmn(StartEvent event, XmlElement xml, BpmnWriter writer) {
    writer.setBpmnName(xml, BPMN_ELEMENT_NAME);
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
