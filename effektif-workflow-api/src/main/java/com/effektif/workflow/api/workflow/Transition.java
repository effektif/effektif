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

import com.effektif.workflow.api.bpmn.BpmnReader;
import com.effektif.workflow.api.bpmn.BpmnWriter;
import com.effektif.workflow.api.bpmn.XmlElement;
import com.effektif.workflow.api.condition.Condition;

/**
 * A sequence flow that connects two activities in a workflow.
 *
 * BPMN XML: {@code <sequenceFlow id="flow1" sourceRef="theStart" targetRef="approveRequest"/>}
 *
 * @author Tom Baeyens
 */
public class Transition extends Element {

  protected String id;

  /** The {@link com.effektif.workflow.api.workflow.Activity#id} for the activity this transition leaves from. */
  protected String from;

  /** The {@link com.effektif.workflow.api.workflow.Activity#id} for the activity this transition goes to. */
  protected String to;

  protected Condition condition;
  protected Boolean isToNext;

//  @Override
//  public void readJson(JsonReader r) {
//    id = r.readString("id");
//    from = r.readString("from");
//    to = r.readString("to");
//    condition = r.readObject("condition");
//    isToNext = r.readBoolean("isToNext");
//    super.readJson(r);
//  }
//
//  @Override
//  public void writeJson(JsonWriter w) {
//    w.writeString("id", id);
//    w.writeString("from", id);
//    w.writeString("to", id);
//    w.writeWritable("condition", condition);
//    w.writeBoolean("isToNext", isToNext);
//    super.writeJson(w);
//  }

  @Override
  public void readBpmn(BpmnReader r) {
    super.readBpmn(r);
    id = r.readStringAttributeBpmn("id");
    from = r.readStringAttributeBpmn("sourceRef");
    to = r.readStringAttributeBpmn("targetRef");

    r.startExtensionElements();
    for (XmlElement conditionElement : r.readElementsEffektif("condition")) {
      r.startElement(conditionElement);
      condition = r.readCondition();
      r.endElement();
    }

    r.endExtensionElements();
  }
  
  @Override
  public void writeBpmn(BpmnWriter w) {
    super.writeBpmn(w);
    w.writeStringAttributeBpmn("id", id);
    w.writeStringAttributeBpmn("sourceRef", from);
    w.writeStringAttributeBpmn("targetRef", to);

    if (condition != null) {
      w.startExtensionElements();
      w.startElementEffektif("condition");
      condition.writeBpmn(w);
      w.endElement();
      w.endExtensionElements();
    }
  }

  public String getId() {
    return this.id;
  }
  public void setId(String id) {
    this.id = id;
  }
  public Transition id(String id) {
    this.id = id;
    return this;
  }

  public Transition() {
  }
  
  public String getFrom() {
    return this.from;
  }
  public void setFrom(String from) {
    this.from = from;
  }
  public Transition from(String from) {
    this.from = from;
    return this;
  }
  
  public String getTo() {
    return this.to;
  }
  public void setTo(String to) {
    this.to = to;
  }
  public Transition to(String to) {
    this.to = to;
    return this;
  }
  
  public Transition toNext() {
    this.isToNext = true;
    return this;
  }
  public boolean isToNext() {
    return Boolean.TRUE.equals(isToNext);
  }
  public void setToNext(Boolean toNext) {
    this.isToNext = toNext;
  }
  
  public Condition getCondition() {
    return this.condition;
  }
  public void setCondition(Condition condition) {
    this.condition = condition;
  }
  public Transition condition(Condition condition) {
    this.condition = condition;
    return this;
  }

  @Override
  public Transition name(String name) {
    super.name(name);
    return this;
  }

  @Override
  public Transition description(String description) {
    super.description(description);
    return this;
  }

  @Override
  public Transition property(String key, Object value) {
    super.property(key, value);
    return this;
  }

  @Override
  public Transition propertyOpt(String key, Object value) {
    super.propertyOpt(key, value);
    return this;
  }

  @Override
  public String toString() {
    return "("+(from!=null?from:" ")+")--"+(id!=null?id+"--":"")+">("+(to!=null?to:" ")+")";
  }
}
