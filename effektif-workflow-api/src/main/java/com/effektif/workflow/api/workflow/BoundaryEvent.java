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
package com.effektif.workflow.api.workflow;

import com.effektif.workflow.api.bpmn.*;

import java.util.ArrayList;
import java.util.List;

/**
 * A boundary event.
 *
 * BPMN XML: {@code <sequenceFlow id="flow1" sourceRef="theStart" targetRef="approveRequest"/>}
 //          <bpmn:boundaryEvent id="BoundaryEvent_1ymyt09" attachedToRef="Task_02wgtff">
 //            <bpmn:outgoing>SequenceFlow_0se37xg</bpmn:outgoing>
 //            <bpmn:timerEventDefinition>
 //              <bpmn:timeDuration>PT5M</bpmn:timeDuration>
 //            </bpmn:timerEventDefinition>
 //          </bpmn:boundaryEvent>
 //
 //          <bpmn:sequenceFlow id="SequenceFlow_0se37xg" sourceRef="BoundaryEvent_1ymyt09" targetRef="Task_13koiv2" />
 */
public class BoundaryEvent implements BpmnReadable, BpmnWritable {

  protected String boundaryId;

  /** The {@link Activity#id} for the activity this event is defined on. */
  protected String fromId;
  List<String> toTransitionIds = new ArrayList<>();

  @Override
  public void readBpmn(BpmnReader r) {

    boundaryId = r.readStringAttributeBpmn("id");
    fromId = r.readStringAttributeBpmn("attachedToRef");

    for (XmlElement outgoingElement : r.readElementsBpmn("outgoing")) {
      r.startElement(outgoingElement);
      toTransitionIds.add(outgoingElement.getText());
      r.endElement();
    }

    r.readElementsBpmn("boundaryEvent");
  }

  @Override
  public void writeBpmn(BpmnWriter w) {
//    super.writeBpmn(w);
    // TODO
  }


  public List<String> getToTransitionIds() {
    return toTransitionIds;
  }

  public void setToTransitionIds(List<String> toTransitionIds) {
    this.toTransitionIds = toTransitionIds;
  }

  public String getBoundaryId() {
    return this.boundaryId;
  }
  public void setBoundaryId(String boundaryId) {
    this.boundaryId = boundaryId;
  }
  public BoundaryEvent boundaryId(String boundaryId) {
    this.boundaryId = boundaryId;
    return this;
  }

  public BoundaryEvent() {
  }
  
  public String getFromId() {
    return this.fromId;
  }
  public void setFromId(String fromId) {
    this.fromId = fromId;
  }
  public BoundaryEvent fromId(String fromId) {
    this.fromId = fromId;
    return this;
  }

  @Override
  public String toString() {
    return "("+(fromId!=null?fromId:" ")+")--"+(boundaryId!=null?boundaryId+"--":"")+">(";//+(toTransitionIds!=null?toTransitionIds:" ")+")";
  }
}
