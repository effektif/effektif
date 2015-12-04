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
package com.effektif.workflow.api.types;

import com.effektif.workflow.api.bpmn.BpmnReadable;
import com.effektif.workflow.api.bpmn.BpmnReader;
import com.effektif.workflow.api.bpmn.BpmnWritable;
import com.effektif.workflow.api.bpmn.BpmnWriter;


/**
 * @author Tom Baeyens
 */
public class ChoiceOption implements BpmnWritable, BpmnReadable {
  
  protected String id;
  protected String name;

  public String getName() {
    return this.name;
  }
  public void setName(String name) {
    this.name = name;
  }

  @Override
  public void writeBpmn(BpmnWriter w) {
    w.startElementEffektif("option");
    w.writeStringAttributeEffektif("id", id);
    w.writeStringAttributeEffektif("name", name);
    w.endElement();
  }
  
  @Override
  public void readBpmn(BpmnReader r) {
    id = r.readStringAttributeEffektif("id");
    name = r.readStringAttributeEffektif("name");
  }

  public String getId() {
    return this.id;
  }
  public void setId(String id) {
    this.id = id;
  }
  public ChoiceOption id(String id) {
    this.id = id;
    return this;
  }

  public ChoiceOption name(String name) {
    this.name = name;
    return this;
  }

  public String toString() {
    return id;
  }
}
