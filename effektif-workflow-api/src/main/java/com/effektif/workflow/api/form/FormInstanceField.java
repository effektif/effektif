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
package com.effektif.workflow.api.form;


/** One field in a form instance and it's value.
 * 
 * @author Tom Baeyens
 */
public class FormInstanceField extends AbstractFormField {

  protected Object value;

  public Object getValue() {
    return this.value;
  }
  public void setValue(Object value) {
    this.value = value;
  }
  public FormInstanceField value(Object value) {
    this.value = value;
    return this;
  }
  
  @Override
  public FormInstanceField id(String id) {
    super.id(id);
    return this;
  }
  @Override
  public FormInstanceField name(String name) {
    super.name(name);
    return this;
  }
  @Override
  public FormInstanceField readOnly() {
    super.readOnly();
    return this;
  }
  @Override
  public FormInstanceField required() {
    super.required();
    return this;
  }
}
