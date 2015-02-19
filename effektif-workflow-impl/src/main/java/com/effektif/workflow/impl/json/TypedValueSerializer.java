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

import com.effektif.workflow.api.model.TypedValue;
import com.effektif.workflow.api.types.Type;
import com.effektif.workflow.impl.data.DataTypeService;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ResolvableSerializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;


/**
 * @author Tom Baeyens
 */
public class TypedValueSerializer extends StdSerializer<TypedValue> implements ResolvableSerializer {

  protected DataTypeService dataTypeService;
  protected JsonSerializer<TypedValue> defaultSerializer;

  public TypedValueSerializer(JsonSerializer<?> defaultSerializer, DataTypeService dataTypeService) {
    super(TypedValue.class);
    this.dataTypeService = dataTypeService;
    this.defaultSerializer = (JsonSerializer<TypedValue>) defaultSerializer;
  }
  
  @Override
  public void resolve(SerializerProvider provider) throws JsonMappingException {
    ((ResolvableSerializer) defaultSerializer).resolve(provider);
  }

  @Override
  public void serialize(TypedValue typedValue, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonGenerationException {
    if (typedValue.getValue()!=null && typedValue.getType()==null) {
      Type type = dataTypeService.getTypeByValue(typedValue.getValue());
      typedValue.setType(type);
    }
    defaultSerializer.serialize(typedValue, jgen, provider);
  }
}
