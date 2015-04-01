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
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;
import java.util.Map;

import com.effektif.workflow.api.json.JsonWritable;
import com.effektif.workflow.api.json.JsonWriter;
import com.effektif.workflow.api.model.Id;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;


/**
 * @author Tom Baeyens
 */
public class RestJsonWriter implements JsonWriter {
  
  JsonGenerator jgen;
  SubclassMappings subclassMappings;

  public RestJsonWriter() {
    this(new SubclassMappings());
  }

  public RestJsonWriter(SubclassMappings subclassMappings) {
    this.subclassMappings = subclassMappings;
  }

  public void toStream(JsonWritable o, Writer writer) {
    try {
      jgen = new JsonFactory().createGenerator(writer);
      writeObject(o);
      jgen.flush();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public String toString(JsonWritable o) {
    StringWriter stringWriter = new StringWriter();
    toStream(o, stringWriter);
    return stringWriter.toString(); 
  }

  public RestJsonWriter writeObject(JsonWritable o) {
    try {
      if (o!=null) {
        jgen.writeStartObject();
        subclassMappings.writeTypeField(this, o);
        o.writeFields(this);
        jgen.writeEndObject();
      } else {
        jgen.writeNull();
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return this;
  }

  @Override
  public void writeString(String fieldName, String stringValue) {
    if (stringValue!=null) {
      try {
        jgen.writeStringField(fieldName, stringValue);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  @Override
  public void writeId(Id id) {
    if (id!=null) {
      try {
        jgen.writeStringField("id", id.getInternal());
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  @Override
  public void writeObject(String fieldName, JsonWritable o) {
    if (o!=null) {
      try {
        jgen.writeFieldName(fieldName);
        writeObject(o);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  @Override
  public void writeList(String fieldName, List< ? extends JsonWritable> elements) {
    if (elements!=null) {
      try {
        jgen.writeFieldName(fieldName);
        jgen.writeStartArray();
        for (JsonWritable element: elements) {
          writeObject(element);
        }
        jgen.writeEndArray();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  @Override
  public void writeFields(Map<String, Object> fieldValues) {
  }
}
