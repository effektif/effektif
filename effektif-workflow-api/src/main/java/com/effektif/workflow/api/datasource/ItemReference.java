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
package com.effektif.workflow.api.datasource;


/**
 * @author Tom Baeyens
 */
public class ItemReference {

  protected String id;
  protected String dataSourceId;
  protected String label;
  protected String type;

  public String getId() {
    return this.id;
  }
  public void setId(String id) {
    this.id = id;
  }
  public ItemReference id(String id) {
    this.id = id;
    return this;
  }

  public String getDataSourceId() {
    return this.dataSourceId;
  }
  public void setDataSourceId(String dataSourceId) {
    this.dataSourceId = dataSourceId;
  }
  public ItemReference dataSourceId(String dataSourceId) {
    this.dataSourceId = dataSourceId;
    return this;
  }

  public String getType() {
    return this.type;
  }
  public void setType(String type) {
    this.type = type;
  }
  public ItemReference type(String type) {
    this.type = type;
    return this;
  }

  public String getLabel() {
    return this.label;
  }
  public void setLabel(String label) {
    this.label = label;
  }
  public ItemReference label(String label) {
    this.label = label;
    return this;
  }
}
