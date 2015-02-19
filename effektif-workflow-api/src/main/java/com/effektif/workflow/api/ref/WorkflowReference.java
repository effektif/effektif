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
package com.effektif.workflow.api.ref;


/**
 * @author Tom Baeyens
 */
public class WorkflowReference {
  
  protected String id;
  protected String name;
  protected String label;

  public String getId() {
    return this.id;
  }
  public void setId(String id) {
    this.id = id;
  }
  public WorkflowReference id(String id) {
    this.id = id;
    return this;
  }

  public String getName() {
    return this.name;
  }
  public void setName(String name) {
    this.name = name;
  }
  public WorkflowReference name(String name) {
    this.name = name;
    return this;
  }
  
  public String getLabel() {
    return this.label;
  }
  public void setLabel(String label) {
    this.label = label;
  }
  public WorkflowReference label(String label) {
    this.label = label;
    return this;
  }
}
