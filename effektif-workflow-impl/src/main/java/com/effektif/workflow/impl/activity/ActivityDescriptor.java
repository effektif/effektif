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
package com.effektif.workflow.impl.activity;

import com.effektif.workflow.api.types.DataType;
import com.effektif.workflow.api.types.TextType;
import com.effektif.workflow.api.workflow.Extensible;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


/**
 * @author Tom Baeyens
 */
public class ActivityDescriptor extends Extensible {

  protected String key;
  protected String name;
  protected String description;
  protected String icon;
  /**
   * References another dynamic descriptor.
   */
  protected String descriptorId;

  protected List<InputDescriptor> inputDescriptors;
  protected List<OutputDescriptor> outputDescriptors;

  public ActivityDescriptor inputParameterString(String inputParameterKey, String inputParameterLabel) {
    inputDescriptor(inputParameterKey, inputParameterLabel, new TextType());
    return this;
  }

  public ActivityDescriptor inputDescriptor(String inputParameterKey, String inputParameterLabel, DataType type) {
    inputDescriptor(new InputDescriptor()
      .key(inputParameterKey)
      .name(inputParameterLabel)
      .type(type)
    );
    return this;
  }

  public ActivityDescriptor inputDescriptors(Collection<InputDescriptor> inputDescriptors) {
    if (inputDescriptors!=null) {
      for (InputDescriptor inputDescriptor: inputDescriptors) {
        inputDescriptor(inputDescriptor);
      }
    }
    return this;
  }

  public ActivityDescriptor inputDescriptor(InputDescriptor inputDescriptor) {
    if (inputDescriptors==null) {
      inputDescriptors = new ArrayList<>();
    }
    inputDescriptors.add(inputDescriptor);
    return this;
  }

  public ActivityDescriptor outputDescriptorString(String outputParameterKey, String outputParameterLabel) {
    outputDescriptor(outputParameterKey, outputParameterLabel, new TextType());
    return this;
  }

  public ActivityDescriptor outputDescriptors(Collection<OutputDescriptor> outputDescriptors) {
    if (outputDescriptors!=null) {
      for (OutputDescriptor outputDescriptor: outputDescriptors) {
        outputDescriptor(outputDescriptor);
      }
    }
    return this;
  }

  public ActivityDescriptor outputDescriptor(String outputParameterKey, String outputParameterLabel, DataType type) {
    outputDescriptor(new OutputDescriptor()
      .key(outputParameterKey)
      .name(outputParameterLabel)
      .type(type)
    );
    return this;
  }
  
  public ActivityDescriptor outputDescriptor(OutputDescriptor outputParameter) {
    if (outputDescriptors==null) {
      outputDescriptors = new ArrayList<>();
    }
    outputDescriptors.add(outputParameter);
    return this;
  }

  public String getKey() {
    return this.key;
  }
  public void setKey(String key) {
    this.key = key;
  }
  public ActivityDescriptor key(String key) {
    this.key = key;
    return this;
  }

  public String getName() {
    return this.name;
  }
  public void setName(String name) {
    this.name = name;
  }
  public ActivityDescriptor name(String name) {
    this.name = name;
    return this;
  }

  public String getDescription() {
    return this.description;
  }
  public void setDescription(String description) {
    this.description = description;
  }
  public ActivityDescriptor description(String description) {
    this.description = description;
    return this;
  }

  public String getDescriptorId() {
    return this.descriptorId;
  }
  public void setDescriptorId(String descriptorId) {
    this.descriptorId = descriptorId;
  }
  public ActivityDescriptor descriptorId(String descriptorId) {
    this.descriptorId = descriptorId;
    return this;
  }

  public String getIcon() {
    return this.icon;
  }
  public void setIcon(String icon) {
    this.icon = icon;
  }
  public ActivityDescriptor icon(String icon) {
    this.icon = icon;
    return this;
  }

  public List<InputDescriptor> getInputDescriptors() {
    return inputDescriptors;
  }

  public void setInputDescriptors(List<InputDescriptor> inputDescriptors) {
    this.inputDescriptors = inputDescriptors;
  }

  public List<OutputDescriptor> getOutputDescriptors() {
    return outputDescriptors;
  }

  public void setOutputDescriptors(List<OutputDescriptor> outputParameters) {
    this.outputDescriptors = outputParameters;
  }

  @Override
  public ActivityDescriptor property(String key, Object value) {
    super.property(key, value);
    return this;
  }

  @Override
  public ActivityDescriptor propertyOpt(String key, Object value) {
    super.propertyOpt(key, value);
    return this;
  }
  
  public InputDescriptor getInputDescriptor(String inputParameterKey) {
    if (inputParameterKey!=null && inputDescriptors!=null) {
      for (InputDescriptor inputDescriptor: inputDescriptors) {
        if (inputParameterKey.equals(inputDescriptor.getKey())) {
          return inputDescriptor;
        }
      }
    }
    return null;
  }
  
  public OutputDescriptor getOutputDescriptor(String outputParameterKey) {
    if (outputParameterKey!=null && outputDescriptors!=null) {
      for (OutputDescriptor outputDescriptor: outputDescriptors) {
        if (outputParameterKey.equals(outputDescriptor.getKey())) {
          return outputDescriptor;
        }
      }
    }
    return null;
  }
}
