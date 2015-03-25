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
package com.effektif.workflow.api.form;

import com.effektif.workflow.api.workflow.Binding;


/**
 * Represents one input field on a {@link com.effektif.workflow.api.form.Form},
 * also specifying how it’s connected to the workflow variables -
 * see <a href="https://github.com/effektif/effektif/wiki/Forms">Forms</a>.
 *
 * You don’t have to specify a form field’s type when you define a workflow.
 * The type is derived from the binding expression and is passed to the form rendering engines.
 * 
 * @author Tom Baeyens
 */
public class FormField extends AbstractFormField {

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
}
