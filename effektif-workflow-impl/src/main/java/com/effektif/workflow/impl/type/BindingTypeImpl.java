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
package com.effektif.workflow.impl.type;

import com.effektif.workflow.api.type.BindingType;
import com.effektif.workflow.api.type.Type;
import com.effektif.workflow.api.workflow.Binding;
import com.effektif.workflow.impl.WorkflowParser;
import com.effektif.workflow.impl.plugin.AbstractDataType;
import com.effektif.workflow.impl.plugin.DataType;
import com.effektif.workflow.impl.plugin.PluginService;


public class BindingTypeImpl extends AbstractDataType<BindingType> {
  
  protected DataType targetType;
  protected PluginService pluginService;

  public BindingTypeImpl() {
    super(BindingType.class, Object.class);
  }

  @Override
  public boolean isSerializeRequired() {
    return true;
  }

  @Override
  public void serialize(Object o) {
    Binding binding = (Binding) o;
    Object value = binding.getValue();
    if (value!=null && binding.getType()==null) {
      DataType dataType = pluginService.getDataTypeByValueClass(value.getClass());
      if (dataType==null) {
        throw new RuntimeException("No data type found for value "+value+" ("+value.getClass().getName()+")");
      }
      binding.type(dataType.getTypeApi());
    }
  }
  
  @Override
  public void parse(BindingType bindingTypeApi, WorkflowParser parser) {
    super.parse(bindingTypeApi, parser);
    this.pluginService = parser.getServiceRegistry().getService(PluginService.class);
    Type targetTypeApi = bindingTypeApi.getTargetType();
    if (targetTypeApi!=null) {
      this.targetType = parser.parseType(targetTypeApi);
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
