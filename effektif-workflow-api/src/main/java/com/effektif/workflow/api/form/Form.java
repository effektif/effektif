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

import java.util.ArrayList;
import java.util.List;


/**
 * A form definition (aka ‘declaration’) that specifies the fields to display for a
 * {@link com.effektif.workflow.api.activities.UserTask} or
 * {@link com.effektif.workflow.api.triggers.FormTrigger}.
 *
 * @see <a href="https://github.com/effektif/effektif/wiki/Forms">Forms</a>
 *
 * @author Tom Baeyens
 */
public class Form extends AbstractForm {

  protected List<FormField> fields;

  public List<FormField> getFields() {
    return this.fields;
  }
  public void setFields(List<FormField> fields) {
    this.fields = fields;
  }
  public Form field(FormField field) {
    if (fields==null) {
      fields = new ArrayList<>();
    }
    fields.add(field);
    return this;
  }
  public Form field(String bindingExpression) {
    field(new FormField().bindingExpression(bindingExpression));
    return this;
  }
  
  @Override
  public Form description(String description) {
    super.description(description);
    return this;
  }
}
