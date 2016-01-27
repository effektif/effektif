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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.effektif.workflow.api.bpmn.BpmnReader;
import com.effektif.workflow.api.bpmn.BpmnWriter;
import com.effektif.workflow.api.bpmn.XmlElement;
import com.effektif.workflow.api.json.GenericType;
import com.effektif.workflow.api.json.TypeName;

/**
 * @author Tom Baeyens
 */
@TypeName("list")
public class ListType extends DataType {

  protected DataType elementType;

  public ListType() {
  }
  public ListType(DataType elementType) {
    this.elementType = elementType;
  }

  public DataType getElementType() {
    return this.elementType;
  }
  public void setElementType(DataType elementType) {
    this.elementType = elementType;
  }
  public ListType elementType(DataType elementType) {
    this.elementType = elementType;
    return this;
  }

  @Override
  public void readBpmn(BpmnReader r) {
    XmlElement element = r.readElementEffektif("element");
    if (element!=null) {
      r.startElement(element);
      elementType = r.readTypeAttributeEffektif();
      r.endElement();
    }
  }

  @Override
  public void writeBpmn(BpmnWriter w) {
    super.writeBpmn(w);
    if (elementType!=null) {
      w.startElementEffektif("element");
      w.writeTypeAttribute(elementType);
      elementType.writeBpmn(w);
      w.endElement();
    }
  }

  @Override
  public Object readBpmnValue(BpmnReader r) {
    List defaultValues = new ArrayList();
    for (XmlElement nestedElement: r.readElementsEffektif("value")) {
      r.startElement(nestedElement);
      defaultValues.add(elementType.readBpmnValue(r));
      r.endElement();
    }
    return defaultValues;
  }

  @Override
  public void writeBpmnValue(BpmnWriter w, Object list) {
    if (list != null && Collection.class.isAssignableFrom(list.getClass())) {
      for (Object value : (Collection) list) {
        w.startElementEffektif("value");
        elementType.writeBpmnValue(w, value);
        w.endElement();
      }
    }
  }

  @Override
  public Type getValueType() {
    Type elementValueType = elementType!=null ? elementType.getValueType() : null;
    return new GenericType(List.class, elementValueType);
  }


}
