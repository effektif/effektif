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
 * A form instance (aka runtime form, rendered form) that is used for rendering 
 * a form or submit the values.  The fields in form instance can contain a value,
 * and (in contrast to form fields) no link to the workflow variables. 
 * 
 * Also see <a href="https://github.com/effektif/effektif/wiki/Forms">The Forms wiki page</a>
 * for more documentation on Forms.
 * 
 * @author Tom Baeyens
 */
public class FormInstance extends AbstractForm {

  protected List<FormInstanceField> fields;
  public static final String FORM_INSTANCE_KEY = "formInstance";

  public FormInstance() {
  }
  
  public FormInstance(Form form) {
    super(form);
    if (form.getFields()!=null) {
      fields = new ArrayList<>();
      for (FormField field: form.getFields()) {
        FormInstanceField formInstanceField = new FormInstanceField(field);
        fields.add(formInstanceField);
      }
    }
  }

  public List<FormInstanceField> getFields() {
    return this.fields;
  }
  public void setFields(List<FormInstanceField> fields) {
    this.fields = fields;
  }
  
  public FormInstance value(String fieldId, Object value) {
    if (fields!=null) {
      for (FormInstanceField field: fields) {
        if (fieldId.equals(field.id)) {
          field.setValue(value);
          return this;
        }
      }
    }
    if (fields==null) {
      fields = new ArrayList<>();
    }
    fields.add(new FormInstanceField()
      .id(fieldId)
      .value(value));
    return this;
  }
}
