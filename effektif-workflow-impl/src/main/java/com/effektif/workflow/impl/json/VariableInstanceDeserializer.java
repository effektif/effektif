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

import java.io.IOException;

import com.effektif.workflow.api.workflowinstance.VariableInstance;
import com.effektif.workflow.impl.data.DataTypeService;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.ResolvableDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;


/**
 * @author Tom Baeyens
 */
public class VariableInstanceDeserializer extends StdDeserializer<VariableInstance> implements ResolvableDeserializer {

  private static final long serialVersionUID = 1L;
  
  protected ObjectMapper objectMapper;
  protected DataTypeService dataTypeService;
  protected JsonDeserializer<?> defaultDeserializer;

  public VariableInstanceDeserializer(JsonDeserializer<?> defaultDeserializer, DataTypeService dataTypeService, ObjectMapper objectMapper) {
    super(VariableInstance.class);
    this.objectMapper = objectMapper;
    this.dataTypeService = dataTypeService;
    this.defaultDeserializer = defaultDeserializer;
  }
  
  @Override
  public VariableInstance deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
    VariableInstance variableInstance = (VariableInstance) defaultDeserializer.deserialize(jp, ctxt);
    if (variableInstance.getType()!=null && variableInstance.getValue()!=null) {
      JavaType javaType = dataTypeService.createJavaType(variableInstance.getType());
      if (javaType!=null) {
        Object deserializedValue = objectMapper.convertValue(variableInstance.getValue(), javaType);
        variableInstance.setValue(deserializedValue);
      }
    }
    return variableInstance;
  }

  // for some reason you have to implement ResolvableDeserializer when modifying BeanDeserializer
  // otherwise deserializing throws JsonMappingException??
  @Override
  public void resolve(DeserializationContext ctxt) throws JsonMappingException {
    ((ResolvableDeserializer) defaultDeserializer).resolve(ctxt);
  }
}
