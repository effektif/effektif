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
package com.effektif.workflow.api.triggers;

import com.effektif.workflow.api.form.Form;
import com.effektif.workflow.api.form.FormField;
import com.effektif.workflow.api.workflow.Trigger;
import com.fasterxml.jackson.annotation.JsonTypeName;


/**
 * @author Tom Baeyens
 */
@JsonTypeName("form")
public class FormTrigger extends Trigger {
  
  protected Form form;

  public Form getForm() {
    return this.form;
  }
  public void setForm(Form form) {
    this.form = form;
  }
  public FormTrigger form(Form form) {
    this.form = form;
    return this;
  }
  /** adds a form field */  
  public FormTrigger field(FormField field) {
    if (form==null) {
      form = new Form();
    }
    form.field(field);
    return this;
  }
  /** shortcut to add a field and set the binding expression */  
  public FormTrigger field(String bindingExpression) {
    field(new FormField().binding(bindingExpression));
    return this;
  }
}
