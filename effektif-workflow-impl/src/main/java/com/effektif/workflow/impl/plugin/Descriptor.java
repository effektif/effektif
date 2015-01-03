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

import java.util.ArrayList;
import java.util.List;

import com.effektif.workflow.api.annotations.Label;
import com.effektif.workflow.impl.type.DataType;


public class Descriptor {

  protected ActivityType activityType;
  protected DataType dataType;

  protected String label;
  protected String description;
// protected byte[] iconBytes;
// protected String iconMimeType;
// datatypes might also need a javascript rendering configuration
  protected List<DescriptorField> configurationFields;
  protected Class<?> configurationClass;

  
  public Descriptor(DataType dataType, String label, String description) {
    this.dataType = dataType;
    this.label = label;
    this.description = description;
  }

  public Descriptor(Class< ? > configurationClass, List<DescriptorField> configurationFields) {
    this.configurationClass = configurationClass;
    this.configurationFields = configurationFields;
    
    if (configurationClass!=null) {
      Label labelAnnotation = configurationClass.getAnnotation(Label.class);
      if (labelAnnotation != null) {
        this.label = labelAnnotation.value();
      }
      Description descriptionAnnotation = configurationClass.getAnnotation(Description.class);
      if (descriptionAnnotation != null) {
        this.description = labelAnnotation.value();
      }
    }
  }
  
  public Descriptor(Class< ? > configurationClass, List<DescriptorField> configurationFields, ActivityType activityType) {
    this(configurationClass, configurationFields);
    this.activityType = activityType;
  }
  
  public Descriptor(Class< ? > configurationClass, List<DescriptorField> configurationFields, DataType dataType) {
    this(configurationClass, configurationFields);
    this.dataType = dataType;
  }
  
  public Descriptor(DataType dataType) {
    this.dataType = dataType;
  }

  public String getLabel() {
    return label;
  }
  
  public void setLabel(String label) {
    this.label = label;
  }
  
  public Descriptor label(String label) {
    this.label = label;
    return this;
  }
  
  public String getDescription() {
    return description;
  }
  
  public void setDescription(String description) {
    this.description = description;
  }
  
  public Descriptor description(String description) {
    this.description = description;
    return this;
  }
  
  public List<DescriptorField> getConfigurationFields() {
    return configurationFields;
  }
  
  public void setConfigurationFields(List<DescriptorField> configurationFields) {
    this.configurationFields = configurationFields;
  }
  
  public Descriptor configurationField(DescriptorField field) {
    if (this.configurationFields==null) {
      this.configurationFields = new ArrayList<>();
    }
    this.configurationFields.add(field);
    return this;
  }

  public ActivityType getActivityType() {
    return this.activityType;
  }
  public void setActivityType(ActivityType activityType) {
    this.activityType = activityType;
  }
  public Descriptor activityType(ActivityType activityType) {
    this.activityType = activityType;
    return this;
  }
  
  public DataType getDataType() {
    return this.dataType;
  }
  public void setDataType(DataType dataType) {
    this.dataType = dataType;
  }
  public Descriptor dataType(DataType dataType) {
    this.dataType = dataType;
    return this;
  }

  public Class<?> getConfigurationClass() {
    return this.configurationClass;
  }
  public void setConfigurationClass(Class<?> configurationClass) {
    this.configurationClass = configurationClass;
  }
  public Descriptor configurationClass(Class<?> configurationClass) {
    this.configurationClass = configurationClass;
    return this;
  }

}
