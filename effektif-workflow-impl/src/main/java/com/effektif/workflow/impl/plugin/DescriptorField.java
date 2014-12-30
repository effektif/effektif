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

import java.lang.reflect.Field;

import com.effektif.workflow.impl.type.DataType;
import com.fasterxml.jackson.annotation.JsonIgnore;


/**
 * @author Walter White
 */
public class DescriptorField {

  public String name;
  public String label;
  public Boolean isRequired;
  public DataType dataType;
  
  @JsonIgnore
  public Field field;
  
  public DescriptorField(Field field, DataType dataType, ConfigurationField configurationField) {
    this.name = field.getName();
    this.field = field;
    this.field.setAccessible(true);
    this.dataType = dataType;
    this.isRequired = configurationField.required() ? true : null;
    Label label = field.getAnnotation(Label.class);
    if (label!=null) {
      this.label = label.value();
    } else {
      this.label = name;
    }
  }
}
