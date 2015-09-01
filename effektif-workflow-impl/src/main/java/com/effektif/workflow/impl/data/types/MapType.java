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
package com.effektif.workflow.impl.data.types;

import java.lang.reflect.Type;
import java.util.Map;

import com.effektif.workflow.api.bpmn.BpmnReader;
import com.effektif.workflow.api.bpmn.BpmnWriter;
import com.effektif.workflow.api.bpmn.XmlElement;
import com.effektif.workflow.api.json.GenericType;
import com.effektif.workflow.api.json.TypeName;
import com.effektif.workflow.api.types.DataType;


/** represents an object map where all the values have the same type.
 * Used when generating the map type from a value dynamically.
 * 
 * @author Tom Baeyens
 */
@TypeName("map")
public class MapType extends DataType {

  public static final MapType INSTANCE = new MapType();

  protected DataType elementType;

  public MapType() {
  }
  public MapType(DataType elementType) {
    this.elementType = elementType;
  }

  public DataType getElementType() {
    return this.elementType;
  }
  public void setElementType(DataType elementType) {
    this.elementType = elementType;
  }
  public MapType elementType(DataType elementType) {
    this.elementType = elementType;
    return this;
  }

  @Override
  public void readBpmn(BpmnReader r) {
    super.readBpmn(r);
    XmlElement element = r.readElementEffektif("object");
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
      w.startElementEffektif("object");
      w.writeTypeAttribute(elementType);
      elementType.writeBpmn(w);
      w.endElement();
    }
  }

  @Override
  public Type getValueType() {
    return elementType!=null ? new GenericType(Map.class, elementType.getValueType()) : null;
  }
}
