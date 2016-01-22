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
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Type;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;


/**
 * An API that implements logic for mapping model classes to and from JSON.
 *
 * @author Tom Baeyens
 */
public class JsonStreamMapper {

  ObjectMapper objectMapper;
  Mappings mappings;
  boolean pretty;
  
  public JsonStreamMapper() {
    this.objectMapper = new ObjectMapper();
  }

  public JsonStreamMapper pretty() {
    this.pretty = true;
    return this;
  }

  public <T> T readString(String jsonString, Class<T> clazz) {
    return readString(jsonString, (Type)clazz);
  }

  public <T> T readString(String jsonString, Type type) {
    if (jsonString==null || "".equals(jsonString.trim())) {
      return null;
    }
    Reader reader = new StringReader(jsonString);
    return read(reader, type);
  }

  public <T> T read(Reader reader, Class<T> clazz) {
    return read(reader, (Type)clazz);
  }

  public <T> T read(Reader reader, Type type) {
    try {
      Object beanJsonObject = parseJson(reader);
      if (beanJsonObject==null) {
        return null;
      }
      return readJsonObject(beanJsonObject, type);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public Object parseJson(Reader reader) {
    try {
      return objectMapper.readValue(reader, Object.class);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public <T> T readJsonObject(Object jsonObject, Type type) {
    if (jsonObject==null) {
      return null;
    }
    JsonStreamReader jsonStreamReader = new JsonStreamReader(mappings);
    return (T) jsonStreamReader.readObject(jsonObject, type);
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
  
  public void setMappings(Mappings mappings) {
    this.mappings = mappings;
  }
}
