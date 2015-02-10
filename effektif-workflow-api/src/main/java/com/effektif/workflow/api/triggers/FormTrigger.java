/* Copyright (c) 2014, Effektif GmbH.
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
 * limitations under the License. */
package com.effektif.workflow.api.triggers;

import com.effektif.workflow.api.form.Form;
import com.effektif.workflow.api.workflow.Trigger;


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
}
