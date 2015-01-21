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
package com.effektif.workflow.impl.types;

import com.effektif.workflow.api.types.BindingType;
import com.effektif.workflow.api.types.Type;
import com.effektif.workflow.api.workflow.Binding;
import com.effektif.workflow.impl.json.JsonService;
import com.effektif.workflow.impl.plugin.AbstractDataType;
import com.effektif.workflow.impl.plugin.DataType;
import com.effektif.workflow.impl.plugin.PluginService;
import com.effektif.workflow.impl.plugin.ServiceRegistry;


public class BindingTypeImpl extends AbstractDataType<BindingType> {
  
  protected DataType targetType;
  protected PluginService pluginService;
  protected JsonService jsonService;

  public BindingTypeImpl() {
    super(BindingType.class, Object.class);
  }

  @Override
  public boolean isSerializeRequired() {
    return true;
  }

  @Override
  public Object serialize(Object o) {
    if (o!=null) {
      Binding binding = (Binding) o;
      Object value = binding.getValue();
      if (value!=null && binding.getType()==null) {
        Type type = pluginService.getTypeByValue(value);
        binding.type(type);
      }
    }
    return o;
  }
  
  @Override
  public Object deserialize(Object o) {
    if (o!=null) {
      Binding binding = (Binding) o;
      Object value = binding.getValue();
      if (value!=null && binding.getType()!=null) {
        binding.value(targetType.deserialize(binding.getValue()));
      }
    }
    return o;
  }

  @Override
  public void initialize(BindingType bindingTypeApi, ServiceRegistry serviceRegistry) {
    super.initialize(bindingTypeApi, serviceRegistry);
    this.pluginService = serviceRegistry.getService(PluginService.class);
    Type targetTypeApi = bindingTypeApi.getTargetType();
    if (targetTypeApi!=null) {
      this.targetType = pluginService.createDataType(targetTypeApi);
      this.valueClass = this.targetType.getValueClass();
    } 
  }

  @Override
  public Object convertJsonToInternalValue(Object apiValue) throws InvalidValueException {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public Object convertInternalToJsonValue(Object internalValue) {
    throw new UnsupportedOperationException("TODO");
  }
}
