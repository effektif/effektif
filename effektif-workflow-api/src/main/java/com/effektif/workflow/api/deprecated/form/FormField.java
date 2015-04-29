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
package com.effektif.workflow.api.deprecated.form;

import com.effektif.workflow.api.bpmn.BpmnReadable;
import com.effektif.workflow.api.bpmn.BpmnReader;
import com.effektif.workflow.api.bpmn.BpmnWritable;
import com.effektif.workflow.api.bpmn.BpmnWriter;
import com.effektif.workflow.api.workflow.Binding;


/**
 * Represents one input field on a {@link com.effektif.workflow.api.deprecated.form.Form},
 * also specifying how its connected to the workflow variables.
 *
 * You don’t have to specify a form field’s type when you define a workflow.
 * The type is derived from the binding expression and is passed to the form rendering engines.
 *
 * @see <a href="https://github.com/effektif/effektif/wiki/Forms">Forms</a>
 *
 * @author Tom Baeyens
 */
public class FormField extends AbstractFormField implements BpmnReadable, BpmnWritable {

  protected Binding<?> binding;

  public Binding<?> getBinding() {
    return this.binding;
  }
  public void setBinding(Binding<?> binding) {
    this.binding = binding;
  }
  public FormField binding(Binding<?> binding) {
    this.binding = binding;
    return this;
  }
  /** shortcut to set the binding expression on this field 
   * @see https://github.com/effektif/effektif/wiki/Expressions */
  public FormField bindingExpression(String bindingExpression) {
    this.binding = new Binding().expression(bindingExpression);
    return this;
  }
  
  @Override
  public FormField id(String id) {
    super.id(id);
    return this;
  }
  @Override
  public FormField name(String name) {
    super.name(name);
    return this;
  }
  /** sets readonly to true in a fluent api style */
  @Override
  public FormField readOnly() {
    super.readOnly();
    return this;
  }
  /** sets required to true in a fluent api style */
  @Override
  public FormField required() {
    super.required();
    return this;
  }
  @Override
  public FormField property(String key, Object value) {
    super.property(key, value);
    return this;
  }
  @Override
  public FormField propertyOpt(String key, Object value) {
    super.propertyOpt(key, value);
    return this;
  }

  @Override
  public void readBpmn(BpmnReader r) {
    id = r.readStringAttributeEffektif("id");
    name = r.readStringAttributeEffektif("name");
    readOnly = r.readBooleanAttributeEffektif("readonly");
    required = r.readBooleanAttributeEffektif("required");

    Binding<String> fieldBinding = new Binding<>();
    fieldBinding.setValue(r.readStringAttributeEffektif("value"));
    fieldBinding.setExpression(r.readStringAttributeEffektif("expression"));
    setBinding(fieldBinding);
  }

  @Override
  public void writeBpmn(BpmnWriter w) {
    w.startElementEffektif("field");
    w.writeStringAttributeEffektif("id", id);
    w.writeStringAttributeEffektif("name", name);

    if (readOnly != null) {
      w.writeStringAttributeBpmn("readonly", readOnly);
    }
    if (required != null) {
      w.writeStringAttributeBpmn("required", required);
    }

    if (binding != null) {
      w.writeStringAttributeEffektif("expression", binding.getExpression());
      if (binding.getValue() != null) {
        w.writeStringAttributeEffektif("value", binding.getValue().toString());
      }
    }
    w.endElement();
  }
}
