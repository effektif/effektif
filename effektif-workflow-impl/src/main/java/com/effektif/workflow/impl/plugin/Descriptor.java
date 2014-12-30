/* Copyright 2014 Effektif GmbH.
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
package com.effektif.workflow.impl.plugin;

import java.util.List;

import com.effektif.workflow.impl.type.DataType;


/**
 * @author Walter White
 */
public class Descriptor {

  protected ActivityType activityType; 
  protected DataType dataType; 
  
  protected String label;
  protected String description;
// protected byte[] iconBytes;
// protected String iconMimeType;
// datatypes might also need a javascript rendering configuration
  protected List<DescriptorField> configurationFields;

  public Descriptor() {
  }

  public Descriptor(DataType dataType) {
    this.dataType = dataType;
  }

  public ActivityType getActivityType() {
    return activityType;
  }
  
  public void setActivityType(ActivityType activityType) {
    this.activityType = activityType;
  }
  
  public DataType getDataType() {
    return dataType;
  }
  
  public void setDataType(DataType dataType) {
    this.dataType = dataType;
  }
  
  public String getLabel() {
    return label;
  }
  
  public void setLabel(String label) {
    this.label = label;
  }
  
  public String getDescription() {
    return description;
  }
  
  public void setDescription(String description) {
    this.description = description;
  }
  
  public List<DescriptorField> getConfigurationFields() {
    return configurationFields;
  }
  
  public void setConfigurationFields(List<DescriptorField> configurationFields) {
    this.configurationFields = configurationFields;
  }
}
