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
package com.effektif.workflow.impl.json;

import org.joda.time.LocalDateTime;

import com.effektif.workflow.api.model.TypedValue;
import com.effektif.workflow.api.workflowinstance.VariableInstance;
import com.effektif.workflow.impl.configuration.Brewery;
import com.effektif.workflow.impl.configuration.Supplier;
import com.effektif.workflow.impl.data.DataTypeService;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;


/**
 * @author Tom Baeyens
 */
public class DefaultObjectMapperSupplier implements Supplier {

  @Override
  public Object supply(Brewery brewery) {
    final ObjectMapper objectMapper = new ObjectMapper();
    brewery.brew(objectMapper);
    
    final DataTypeService dataTypeService = brewery.get(DataTypeService.class);
    
    objectMapper
      .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
      .setVisibility(PropertyAccessor.ALL, Visibility.NONE)
      .setVisibility(PropertyAccessor.FIELD, Visibility.ANY)
      .setSerializationInclusion(Include.NON_EMPTY);
  
    SimpleModule module = new SimpleModule();
    module.addSerializer(new LocalDateTimeSerializer());
    module.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer());
    module.setDeserializerModifier(new BeanDeserializerModifier() {
      @Override
      public JsonDeserializer< ? > modifyDeserializer(DeserializationConfig config, BeanDescription beanDesc, JsonDeserializer< ? > deserializer) {
        if (beanDesc.getBeanClass() == TypedValue.class) {
          return new TypedValueDeserializer(deserializer, dataTypeService, objectMapper);
        } else if (beanDesc.getBeanClass() == VariableInstance.class) {
          return new VariableInstanceDeserializer(deserializer, dataTypeService, objectMapper);
        }
        return deserializer;
      }
    });
    module.setSerializerModifier(new BeanSerializerModifier() {
      @Override
      public JsonSerializer< ? > modifySerializer(SerializationConfig config, BeanDescription beanDesc, JsonSerializer< ? > serializer) {
        if (beanDesc.getBeanClass() == TypedValue.class) {
          return new TypedValueSerializer(serializer, dataTypeService);
        }
        return serializer;
      }
    });
    objectMapper.registerModule(module);
    
    return objectMapper;
  }
}
