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
package com.effektif.workflow.test.jsonspike.json;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;

import com.effektif.workflow.test.jsonspike.json.typemappers.LocalDateTimeStreamMapper;
import com.effektif.workflow.test.jsonspike.json.typemappers.WorkflowIdStreamMapper;
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

  Mappings mappings;
  boolean pretty;
  ObjectMapper objectMapper;
  
  public JsonStreamMapper() {
    this.mappings = new Mappings();
    this.objectMapper = new ObjectMapper();
    
    this.mappings.registerTypeMapper(new LocalDateTimeStreamMapper());
    this.mappings.registerTypeMapper(new WorkflowIdStreamMapper());
  }
  
  public JsonStreamMapper pretty() {
    this.pretty = true;
    return this;
  }

  public <T> T readString(String jsonString, Class<?> clazz) {
    try {
      JsonStreamFieldReader jsonStreamFieldReader = new JsonStreamFieldReader(mappings);
      Map<String,Object> beanJsonObject = objectMapper.readValue(new StringReader(jsonString), Map.class);
      return (T) jsonStreamFieldReader.readBean(beanJsonObject, clazz);
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
      JsonStreamFieldWriter jsonStreamFieldWriter = new JsonStreamFieldWriter(mappings,jgen);
      jsonStreamFieldWriter.writeBean(bean);
      jgen.flush();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
