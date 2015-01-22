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
package com.effektif.workflow.api.ref;


public class VariableReference {

  protected String id;
  protected String label;
  protected String typeLabel;

  public String getId() {
    return this.id;
  }
  public void setId(String id) {
    this.id = id;
  }
  public VariableReference id(String id) {
    this.id = id;
    return this;
  }
  
  public String getLabel() {
    return this.label;
  }
  public void setLabel(String label) {
    this.label = label;
  }
  public VariableReference label(String label) {
    this.label = label;
    return this;
  }

  public String getTypeLabel() {
    return this.typeLabel;
  }
  public void setTypeLabel(String typeLabel) {
    this.typeLabel = typeLabel;
  }
  public VariableReference typeLabel(String typeLabel) {
    this.typeLabel = typeLabel;
    return this;
  }
}
