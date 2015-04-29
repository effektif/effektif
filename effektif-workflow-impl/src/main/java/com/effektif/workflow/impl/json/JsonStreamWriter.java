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
import java.math.BigDecimal;
import java.math.BigInteger;

import com.fasterxml.jackson.core.JsonGenerator;

/**
 * Serialises to JSON using a stream-based JSON representation.
 */
public class JsonStreamWriter extends JsonWriter {

  JsonGenerator jgen;
  
  public JsonStreamWriter(Mappings mappings, JsonGenerator jgen) {
    super(mappings);
    this.jgen = jgen;
  }

  public void flush() {
    try {
      jgen.flush();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
  
  @Override
  public void objectStart() {
    try {
      jgen.writeStartObject();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void writeFieldName(String fieldName) {
    try {
      jgen.writeFieldName(fieldName);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void objectEnd() {
    try {
      jgen.writeEndObject();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void arrayEnd() {
    try {
      jgen.writeEndArray();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void arrayStart() {
    try {
      jgen.writeStartArray();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void writeNull() {
    try {
      jgen.writeNull();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }


  @Override
  public void writeString(String s) {
    try {
      jgen.writeString(s);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void writeBoolean(Boolean b) {
    try {
      jgen.writeBoolean(b);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
  
  @Override
  public void writeNumber(Number n) {
    try {
      if (n instanceof Long) {
        jgen.writeNumber((Long)n);
      } else if (n instanceof Integer) {
        jgen.writeNumber((Integer)n);
      } else if (n instanceof Double) {
        jgen.writeNumber((Double)n);
      } else if (n instanceof Float) {
        jgen.writeNumber((Float)n);
      } else if (n instanceof Short) {
        jgen.writeNumber((Short)n);
      } else if (n instanceof BigDecimal) {
        jgen.writeNumber((BigDecimal)n);
      } else if (n instanceof BigInteger) {
        jgen.writeNumber((BigInteger)n);
      } else {
        throw new RuntimeException("Couldn't write number of type "+n.getClass());
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}