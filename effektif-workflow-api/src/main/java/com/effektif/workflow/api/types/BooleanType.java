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
@TypeName("boolean")
public class BooleanType extends DataType {

  public static final BooleanType INSTANCE = new BooleanType();

  @Override
  public Type getValueType() {
    return Boolean.class;
  }

  @Override
  public Object readBpmnValue(BpmnReader r) {
    String value = r.readStringAttributeEffektif("value");
    return value == null ? null : Boolean.valueOf(value);
  }

  @Override
  public void writeBpmnValue(BpmnWriter w, Object value) {
    if (value != null) {
      w.writeStringAttributeEffektif("value", value.toString());
    }
  }
}
