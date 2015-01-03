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
package com.effektif.workflow.impl.json;

import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;

import org.joda.time.LocalDateTime;

import com.effektif.workflow.impl.plugin.Initializable;
import com.effektif.workflow.impl.plugin.ServiceRegistry;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;


public class JacksonJsonService implements JsonService, Initializable {
  
  public JsonFactory jsonFactory;
  public ObjectMapper objectMapper;

  @Override
  public void initialize(ServiceRegistry serviceRegistry) {
    this.objectMapper = serviceRegistry.getService(ObjectMapper.class);
    this.jsonFactory = serviceRegistry.getService(JsonFactory.class);
    
    objectMapper
      .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
      .setVisibility(PropertyAccessor.ALL, Visibility.NONE)
      .setVisibility(PropertyAccessor.FIELD, Visibility.ANY)
      .setSerializationInclusion(Include.NON_EMPTY);
    
    SimpleModule module = new SimpleModule();
    module.addSerializer(new LocalDateTimeSerializer());
    module.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer());
    objectMapper.registerModule(module);
  }
  
  public void registerSubtype(Class<?> subtype) {
    this.objectMapper.registerSubtypes(subtype);
  }

  public String objectToJsonString(Object object) {
    StringWriter stringWriter = new StringWriter();
    objectToJson(object, stringWriter, objectMapper.writer());
    return stringWriter.toString();
  }

  public String objectToJsonStringPretty(Object object) {
    StringWriter stringWriter = new StringWriter();
    objectToJson(object, stringWriter, objectMapper.writerWithDefaultPrettyPrinter());
    return stringWriter.toString();
  }

  public void objectToJson(Object object, Writer writer) {
    objectToJson(object, writer, objectMapper.writer());
  }
  
  @SuppressWarnings("unchecked")
  public Map<String, Object> objectToJsonMap(Object object) {
    return objectMapper.convertValue(object, Map.class);
  }
  
  protected void objectToJson(Object object, Writer writer, ObjectWriter objectWriter) {
    try {
      objectWriter
        .writeValue(writer, object);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
  
  public <T> T jsonToObject(String json, Class<T> type) {
    try {
      return jsonToObject(jsonFactory.createParser(json), type);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public <T> T jsonToObject(Reader reader, Class<T> type) {
    try {
      return jsonToObject(jsonFactory.createParser(reader), type);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
  
  public <T> T jsonMapToObject(Map<String,Object> jsonMap, Class<T> type) {
    return objectMapper.convertValue(jsonMap, type);
  }

  protected <T> T jsonToObject(JsonParser jsonParser, Class<T> type) throws IOException {
    T object = objectMapper
      .reader(type)
      .readValue(jsonParser);
    return object;
  }

  
  public JsonFactory getJsonFactory() {
    return jsonFactory;
  }

  public void setJsonFactory(JsonFactory jsonFactory) {
    this.jsonFactory = jsonFactory;
  }

  public ObjectMapper getObjectMapper() {
    return objectMapper;
  }

  public void setObjectMapper(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }
}
