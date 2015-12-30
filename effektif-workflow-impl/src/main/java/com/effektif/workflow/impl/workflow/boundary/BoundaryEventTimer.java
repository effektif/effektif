/* Copyright (c) 2014, Effektif GmbH.
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
package com.effektif.workflow.impl.workflow.boundary;

import com.effektif.workflow.api.bpmn.BpmnElement;
import com.effektif.workflow.api.bpmn.BpmnReader;
import com.effektif.workflow.api.bpmn.BpmnTypeAttribute;
import com.effektif.workflow.api.json.TypeName;
import com.effektif.workflow.api.workflow.Activity;
import com.effektif.workflow.api.workflow.BoundaryEvent;
import com.effektif.workflow.api.workflow.Timer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * @author Tom Baeyens
 */

@TypeName("boundaryEventTimer")
@BpmnElement("timerEventDefinition")
//@BpmnTypeAttribute(attribute="type", value="adapter")
public class BoundaryEventTimer extends Timer {

  public BoundaryEvent boundaryEvent;
  public Activity activity;

//  /** The {@link Activity#id} for the activity this event is defined on. */
//  public String fromId;
//
//  /** The {@link Activity#id} for the activity this event goes to. */
//  public List<String> toTransitionIds = new ArrayList<>();

  @Override
  public void readBpmn(BpmnReader r) {
    super.readBpmn(r);

    BoundaryEvent boundaryEvent = new BoundaryEvent();
    boundaryEvent.readBpmn(r);

  }



//  public Set<String> getTransitions() {
//    return new HashSet<>(toTransitionIds);
//  }
//
//  public List<String> getToTransitionIds() {
//    return this.toTransitionIds;
//  }
//
//  public void setToTransitionIds(List<String> toTransitionIds) {
//    this.toTransitionIds = toTransitionIds;
//  }
//
//  public BoundaryEventTimer toActivityId(String toActivityId) {
//    this.toTransitionIds.add(toActivityId);
//    return this;
//  }
//
//  public String getFromId() {
//    return fromId;
//  }
//
//  public void setFromId(String fromId) {
//    this.fromId = fromId;
//  }
}

