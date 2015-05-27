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

import java.util.ArrayList;
import java.util.List;

import com.effektif.workflow.api.bpmn.BpmnReadable;
import com.effektif.workflow.api.bpmn.BpmnReader;
import com.effektif.workflow.api.bpmn.BpmnWritable;
import com.effektif.workflow.api.bpmn.BpmnWriter;
import com.effektif.workflow.api.bpmn.XmlElement;
import com.effektif.workflow.api.types.DataType;


/**
 * Models multiple instances of a workflow activity, where each instance has a specified workflow variable set to one
 * of the values in a collection.
 *
 * Note that BPMN supports an <code>isSequential</code> flag to indicate whether the multiple instances are executed
 * sequentially or in parallel.
 *
 * @see <a href="https://github.com/effektif/effektif/wiki/Multi-instance-tasks">Multi-instance tasks</a>
 * @author Tom Baeyens
 */
public class MultiInstance implements BpmnReadable, BpmnWritable {

  protected Variable variable;
  protected List<Binding<Object>> values;

  /**
   * Reads the multi-instance model from extension elements, ignoring the BPMN multiInstanceLoopCharacteristics element.
   */
  @Override
  public void readBpmn(BpmnReader r) {
    for (XmlElement element : r.readElementsEffektif("variable")) {
      r.startElement(element);
      variable = new Variable();
      variable.setId(r.readStringAttributeBpmn("id"));
      variable.setType(r.readTypeAttributeEffektif());
      r.endElement();
    }
    values = r.readBindings("value");
  }

  @Override
  public void writeBpmn(BpmnWriter w) {
    w.startElementEffektif("multiInstance");
    w.startElementEffektif("variable");
    w.writeStringAttributeEffektif("id", variable.getId());
    w.writeTypeAttribute(variable.getType());
    w.endElement();
    w.writeBindings("value", values);
    w.endElement();
  }

  //  @Override
//  public void readJson(JsonReader r) {
//    variable = r.readObject("variable");
//    values = r.readList("values");
//  }
//
//  @Override
//  public void writeJson(JsonWriter w) {
//    w.writeWritable("variable", variable);
//    w.writeList("values", values);
//  }

  public Variable getVariable() {
    return this.variable;
  }
  public void setVariable(Variable variable) {
    this.variable = variable;
  }
  public MultiInstance variable(Variable variable) {
    this.variable = variable;
    return this;
  }
  public MultiInstance variable(String id, DataType type) {
    this.variable = new Variable()
      .id(id)
      .type(type);
    return this;
  }
  public List<Binding<Object>> getValues() {
    return this.values;
  }
  public void setValues(List<Binding<Object>> values) {
    this.values = values;
  }
  public MultiInstance valuesExpression(String expression) {
    addValueBinding(new Binding().expression(expression));
    return this;
  }
  protected MultiInstance addValueBinding(Binding valueBinding) {
    if (values==null) {
      values = new ArrayList<>();
    }
    values.add(valueBinding);
    return this;
  }

}
