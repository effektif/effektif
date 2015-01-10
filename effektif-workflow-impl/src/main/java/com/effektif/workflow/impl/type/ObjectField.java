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
package com.effektif.workflow.impl.type;


public class ObjectField {

  protected String name;
  protected String label;
  protected DataType dataType;
  protected Boolean required;
  
  public ObjectField() {
  }
  
  public ObjectField(String name) {
    this.name = name;
  }

  public String getName() {
    return this.name;
  }
  public void setName(String name) {
    this.name = name;
  }
  public ObjectField name(String name) {
    this.name = name;
    return this;
  }
  
  public String getLabel() {
    return this.label;
  }
  public void setLabel(String label) {
    this.label = label;
  }
  public ObjectField label(String label) {
    this.label = label;
    return this;
  }
  
  public Boolean getRequired() {
    return this.required;
  }
  public void setRequired(Boolean required) {
    this.required = required;
  }
  public ObjectField required(Boolean required) {
    this.required = required;
    return this;
  }
  
  public DataType getDataType() {
    return this.dataType;
  }
  public void setDataType(DataType dataType) {
    this.dataType = dataType;
  }
  public ObjectField dataType(DataType dataType) {
    this.dataType = dataType;
    return this;
  }
}
