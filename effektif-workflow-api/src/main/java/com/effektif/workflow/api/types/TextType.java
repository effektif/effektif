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

import java.lang.reflect.Type;

import com.effektif.workflow.api.bpmn.BpmnReader;
import com.effektif.workflow.api.bpmn.BpmnWriter;
import com.effektif.workflow.api.json.TypeName;


/**
 * @author Tom Baeyens
 */
@TypeName("text")
public class TextType extends DataType {

  public static final TextType INSTANCE = new TextType();
  private Boolean multiLine;

  @Override
  public Type getValueType() {
    return String.class;
  }

  public boolean isMultiLine() {
    return Boolean.TRUE.equals(multiLine);
  }

  public TextType multiLine() {
    multiLine = Boolean.TRUE;
    return this;
  }

  @Override
  public void readBpmn(BpmnReader r) {
    multiLine = Boolean.valueOf(r.readStringAttributeEffektif("multiLine"));
  }

  @Override
  public void writeBpmn(BpmnWriter w) {
    super.writeBpmn(w);
    w.writeStringAttributeEffektif("multiLine", multiLine);
  }
}
