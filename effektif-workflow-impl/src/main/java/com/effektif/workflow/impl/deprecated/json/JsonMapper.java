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
package com.effektif.workflow.impl.deprecated.json;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Type;

import com.effektif.workflow.api.deprecated.json.JsonReader;
import com.effektif.workflow.api.deprecated.json.JsonWriter;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * A facade for API object JSON serialisation and deserialisation,
 * using the {@link JsonWriter} and {@link JsonReader} APIs.
 *
 * @author Tom Baeyens
 */
public class JsonMapper extends AbstractMapper {
  
  static ObjectMapper objectMapper = new ObjectMapper();
  
  public String writeToString(Object o) {
    return writeToString(o, false);
  }

  public String writeToStringPretty(Object o) {
    return writeToString(o, true); 
  }

  public String writeToString(Object o, boolean pretty) {
    StringWriter stringWriter = new StringWriter();
    writeToStream(o, stringWriter, pretty);
    return stringWriter.toString(); 
  }

  public void writeToStreamPretty(Object o, Writer writer) {
    writeToStream(o, writer, true);
  }

  public void writeToStream(Object o, Writer writer) {
    writeToStream(o, writer, false);
  }

  public void writeToStream(Object o, Writer writer, boolean pretty) {
    try {
      JsonGenerator jgen = new JsonFactory().createGenerator(writer);
      if (pretty) {
        jgen.setPrettyPrinter(new DefaultPrettyPrinter());
      }
      new JsonWriterImpl(mappings, jgen).writeObject(o, o.getClass());
      jgen.flush();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
  
  public <T> T readFromString(String jsonString, Type type) {
    return readFromReader(new StringReader(jsonString), type);
  }

  public <T> T readFromReader(Reader jsonStream, Type type) {
    try {
      Object jsonObject = objectMapper.readValue(jsonStream, Object.class);
      return readFromJsonObject(jsonObject, type);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public <T> T readFromJsonObject(Object jsonObject, Type type) {
    return (T) new JsonReaderImpl(mappings).toObject(jsonObject, type);
  }
}
