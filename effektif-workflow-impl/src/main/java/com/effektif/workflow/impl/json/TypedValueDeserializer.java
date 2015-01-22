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
package com.effektif.workflow.impl.json;

import java.io.IOException;

import com.effektif.workflow.api.command.TypedValue;
import com.effektif.workflow.impl.type.DataTypeService;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.ResolvableDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;


public class TypedValueDeserializer extends StdDeserializer<TypedValue> implements ResolvableDeserializer {

  private static final long serialVersionUID = 1L;
  
  protected ObjectMapper objectMapper;
  protected DataTypeService dataTypeService;
  protected JsonDeserializer<?> defaultDeserializer;

  public TypedValueDeserializer(JsonDeserializer<?> defaultDeserializer, DataTypeService dataTypeService, ObjectMapper objectMapper) {
    super(TypedValue.class);
    this.objectMapper = objectMapper;
    this.dataTypeService = dataTypeService;
    this.defaultDeserializer = defaultDeserializer;
  }
  
  @Override
  public TypedValue deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
    TypedValue typedValue = (TypedValue) defaultDeserializer.deserialize(jp, ctxt);
    if (typedValue.getType()!=null && typedValue.getValue()!=null) {
      JavaType javaType = dataTypeService.createJavaType(typedValue.getType());
      if (javaType!=null) {
        Object deserializedValue = objectMapper.convertValue(typedValue.getValue(), javaType);
        typedValue.setValue(deserializedValue);
      }
    }
    return typedValue;
  }

  // for some reason you have to implement ResolvableDeserializer when modifying BeanDeserializer
  // otherwise deserializing throws JsonMappingException??
  @Override
  public void resolve(DeserializationContext ctxt) throws JsonMappingException {
    ((ResolvableDeserializer) defaultDeserializer).resolve(ctxt);
  }
}
