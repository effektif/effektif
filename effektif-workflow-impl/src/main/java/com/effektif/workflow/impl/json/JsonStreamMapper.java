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
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;

import com.effektif.workflow.impl.json.types.LocalDateTimeStreamMapper;
import com.effektif.workflow.impl.json.types.WorkflowIdStreamMapper;
import com.effektif.workflow.impl.json.types.WorkflowInstanceIdStreamMapper;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;


/**
 * A facade for API object serialisation and deserialisation to and from JSON.
 *
 * @author Tom Baeyens
 */
public class JsonStreamMapper {

  ObjectMapper objectMapper;
  Mappings mappings;
  boolean pretty;
  
  public JsonStreamMapper() {
    this.objectMapper = new ObjectMapper();
    
    this.mappings = new Mappings();
    this.mappings.registerTypeMapperFactory(new LocalDateTimeStreamMapper());
    this.mappings.registerTypeMapperFactory(new WorkflowIdStreamMapper());
    this.mappings.registerTypeMapperFactory(new WorkflowInstanceIdStreamMapper());
  }
  
  public JsonStreamMapper pretty() {
    this.pretty = true;
    return this;
  }

  public <T> T readString(String jsonString, Class<?> clazz) {
    try {
      JsonStreamReader jsonStreamReader = new JsonStreamReader(mappings);
      Map<String,Object> beanJsonObject = objectMapper.readValue(new StringReader(jsonString), Map.class);
      return (T) jsonStreamReader.readObject(beanJsonObject, clazz);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
  
  public <T> String write(T o) {
    StringWriter stringWriter = new StringWriter();
    write(o, stringWriter);
    return stringWriter.toString(); 
  }
  
  public <T> void write(T bean, Writer writer) {
    try {
      JsonGenerator jgen = new JsonFactory().createGenerator(writer);
      if (pretty) {
        jgen.setPrettyPrinter(new DefaultPrettyPrinter());
      }
      JsonStreamWriter jsonStreamWriter = new JsonStreamWriter(mappings,jgen);
      jsonStreamWriter.writeObject(bean);
      jgen.flush();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public Mappings getMappings() {
    return mappings;
  }
}
